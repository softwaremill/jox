package com.softwaremill.jox.flows;

import static com.softwaremill.jox.Select.selectOrClosed;
import static com.softwaremill.jox.structured.Scopes.unsupervised;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import com.softwaremill.jox.Channel;
import com.softwaremill.jox.ChannelDone;
import com.softwaremill.jox.ChannelError;
import com.softwaremill.jox.Sink;
import com.softwaremill.jox.structured.ExternalRunner;
import com.softwaremill.jox.structured.Scope;
import com.softwaremill.jox.structured.UnsupervisedScope;

class FromFlowPublisher<T> implements Flow.Publisher<T> {

    private final ExternalRunner externalRunner;
    private final FlowStage<T> last;

    FromFlowPublisher(Scope scope, FlowStage<T> last) {
        this.externalRunner = scope.externalRunner();
        this.last = last;
    }

    // 1.10: subscribe can be called multiple times; each time, the flow is started from scratch
    // 1.11: subscriptions are unicast
    @Override
    public void subscribe(Flow.Subscriber<? super T> subscriber) {
        if (subscriber == null) throw new NullPointerException("1.9: subscriber is null");
        // 3.13: the reference to the subscriber is held only as long as the main loop below runs
        // 3.14: not in this implementation

        // `runToSubscriber` blocks as long as data is produced by the flow or until the
        // subscription is cancelled
        // we cannot block `subscribe` (see
        // https://github.com/reactive-streams/reactive-streams-jvm/issues/393),
        // hence running in a fork; however, the reactive library might run .subscribe on a
        // different thread, that's
        // why we need to use the external runner functionality
        externalRunner.runAsync(
                scope ->
                        scope.fork(
                                () -> {
                                    runToSubscriber(subscriber);
                                    return null;
                                }));
    }

    private void runToSubscriber(Flow.Subscriber<? super T> subscriber)
            throws ExecutionException, InterruptedException {
        // starting a new scope so that cancelling (== completing the main body) cleans up
        // (interrupts) any background forks
        // using an unsafe scope for efficiency, we only ever start a single fork where all errors
        // are propagated
        unsupervised(
                scope -> {
                    // processing state: cancelled flag, error sent flag, demand
                    final AtomicBoolean cancelled = new AtomicBoolean(false);
                    final AtomicBoolean errorSent = new AtomicBoolean(false);
                    final AtomicLong demand = new AtomicLong(0L);

                    try {
                        Channel<Signal> signals = Channel.newUnlimitedChannel();
                        // 1.9: onSubscribe must be called first
                        subscriber.onSubscribe(new FlowSubscription(signals));

                        // we need separate error & data channels so that we can select from error &
                        // signals only, without receiving data
                        // 1.4 any errors from running the flow end up here
                        Channel<DummyError> errors = Channel.newUnlimitedChannel();
                        Channel<T> data =
                                com.softwaremill.jox.flows.Flow.newChannelWithBufferSizeFromScope();

                        // running the flow in the background; all errors end up as an error of the
                        // `errors` channel
                        forkPropagate(
                                scope,
                                errors,
                                () -> {
                                    last.run(data::send);
                                    data.done();
                                    return null;
                                });

                        Runnable cancel = () -> cancelled.set(true);
                        Consumer<Throwable> signalErrorAndCancel =
                                e -> {
                                    if (!cancelled.get()) {
                                        cancel.run();
                                        errorSent.set(true);
                                        subscriber.onError(e);
                                    }
                                };

                        Consumer<Long> increaseDemand =
                                d -> {
                                    if (d <= 0)
                                        signalErrorAndCancel.accept(
                                                new IllegalArgumentException(
                                                        "3.9: demand must be positive"));
                                    else {
                                        demand.addAndGet(d);
                                        // 3.17: when demand overflows `Long.MaxValue`, this is
                                        // treated as the signalled demand to be "effectively
                                        // unbounded"
                                        if (demand.get() < 0) demand.set(Long.MAX_VALUE);
                                    }
                                };

                        // main processing loop: running as long as flow is not completed or error
                        // was received
                        while (!cancelled.get()) { // 1.7, 3.12 - ending the main loop after
                            // onComplete/onError
                            if (demand.get() == 0) {
                                switch (selectOrClosed(
                                        errors.receiveClause(), signals.receiveClause())) {
                                    case Request r -> increaseDemand.accept(r.n());
                                    case Cancel _ -> cancel.run();
                                    case DummyError _,
                                            ChannelDone
                                                    _ -> {} // impossible as channel done should be
                                    // received only from `data`, and error
                                    // from `errors` is handled in the next
                                    // branch
                                    case ChannelError
                                                    e -> { // only `errors` can be closed due to an
                                        // error
                                        cancel.run();
                                        errorSent.set(true);
                                        subscriber.onError(e.toException());
                                    }
                                    default ->
                                            throw new IllegalStateException(
                                                    "unexpected clause result");
                                }
                            } else {
                                switch (selectOrClosed(
                                        errors.receiveClause(),
                                        data.receiveClause(),
                                        signals.receiveClause())) {
                                    case Request r -> increaseDemand.accept(r.n());
                                    case Cancel _ -> cancel.run();
                                    case DummyError _ -> {} // impossible
                                    case ChannelDone _ -> { // only `data` can be done
                                        cancel.run(); // 1.6: when signalling onComplete/onError,
                                        // the subscription is considered cancelled
                                        subscriber.onComplete(); // 1.5
                                    }
                                    case ChannelError
                                                    e -> { // only `errors` can be closed due to an
                                        // error
                                        cancel.run();
                                        errorSent.set(true);
                                        subscriber.onError(e.toException());
                                    }
                                    case Object o -> {
                                        //noinspection unchecked
                                        subscriber.onNext((T) o);
                                        demand.decrementAndGet();
                                    }
                                }
                            }
                        }
                    } catch (Throwable e) {
                        // e might be an interrupted exception (the scope ends), or a bug; either
                        // way, letting downstream know
                        if (!errorSent.get()) subscriber.onError(e);
                    }
                    return null;
                });
    }

    private void forkPropagate(
            UnsupervisedScope unsupervisedScope,
            Sink<?> propagateExceptionsTo,
            Callable<Void> runnable) {
        unsupervisedScope.forkUnsupervised(
                () -> {
                    try {
                        runnable.call();
                    } catch (Exception e) {
                        propagateExceptionsTo.errorOrClosed(e);
                    }
                    return null;
                });
    }

    private interface DummyError {}

    /** Signals sent from a {@link FlowSubscription} to a running {@link Flow.Publisher}. */
    private interface Signal {}

    private record Request(long n) implements Signal {}

    private record Cancel() implements Signal {}

    private record FlowSubscription(Sink<Signal> signals) implements Flow.Subscription {

        // 3.2, 3.4: request/cancel can be called anytime, in a thread-safe way
        // 3.3: there's no recursion between request & onNext
        // 3.6: after a cancel, more requests can be sent to the channel, but they won't be
        // processed (the cancel will be processed first)
        // 3.15: the signals channel is never closed
        @Override
        public void request(long n) {
            try {
                signals.send(new Request(n));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // 3.5: as above for 3.2
        // 3.7: as above for 3.6
        // 3.16: as above for 3.15
        @Override
        public void cancel() {
            try {
                signals.send(new Cancel());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // 3.10, 3.11: no synchronous calls in this implementation
    }
}
