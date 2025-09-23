package com.softwaremill.jox.structured;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.softwaremill.jox.ChannelDone;
import com.softwaremill.jox.ChannelError;

/**
 * Capability granted by an {@link Scopes#supervised(Scoped)} concurrency scope.
 *
 * <p>Represents a capability to fork supervised or unsupervised, asynchronously running
 * computations in a concurrency scope. Such forks can be created using {@link Scope#fork}, {@link
 * Scope#forkUser}, {@link Scope#forkCancellable} or {@link Scope#forkUnsupervised}.
 */
public class Scope {
    private final StructuredTaskScope<Object, Void> rawScope;
    private final Supervisor supervisor;
    private final AtomicBoolean scopeDone;
    private final Lock externalSchedulerLock = new ReentrantLock();
    private volatile ActorRef<ExternalScheduler> externalSchedulerActor;

    Scope() {
        this.scopeDone = new AtomicBoolean(false);
        this.rawScope = StructuredTaskScope.open(new CancelWhenDoneJoiner(scopeDone));
        this.supervisor = new Supervisor();
    }

    Supervisor getSupervisor() {
        return supervisor;
    }

    <T> T run(Scoped<T> f) throws InterruptedException {
        try {
            try {
                try {
                    var mainBodyFork = forkUser(() -> f.run(this));

                    var loop = true;
                    while (loop) {
                        switch (supervisor.getCommands().receiveOrClosed()) {
                            case RunFork<?> r -> rawScope.fork(r.f());
                            case ChannelDone _, ChannelError _ -> loop = false;
                            default -> throw new IllegalStateException();
                        }
                    }

                    // might throw if any supervised fork threw
                    supervisor.join();
                    // if no exceptions, the main f-fork must be done by now
                    return mainBodyFork.join();
                } finally {
                    cancelAndJoinRawScope();
                }
                // join might have been interrupted
            } finally {
                rawScope.close();
            }

            // all forks are guaranteed to have finished: some might have ended up throwing
            // exceptions (InterruptedException or others), but only the first one is propagated
            // below. That's why we add all the other exceptions as suppressed.
        } catch (ExecutionException e) {
            // unwrapping execution exception from CompletableFutures to custom exception
            JoxScopeExecutionException joxScopeExecutionException =
                    new JoxScopeExecutionException(e.getCause());
            supervisor.addSuppressedErrors(joxScopeExecutionException);
            throw joxScopeExecutionException;
        } catch (Throwable e) {
            supervisor.addSuppressedErrors(e);
            throw e;
        }
    }

    void cancelAndJoinRawScope() throws InterruptedException {
        // the scope is done, now we have to let the structured concurrency API scope let know
        // that it should cleanup as well. Due to its design, this can only be done by a
        // work-around: setting a scope-done flag, and forking an empty computation; our joiner
        // implementation will get notified of this, read the flag, and decide to cancel the "raw"
        // scope.
        scopeDone.set(true);
        rawScope.fork(() -> {});
        rawScope.join();
    }

    /**
     * Starts a fork (logical thread of execution), which is guaranteed to complete before the
     * enclosing {@link Scopes#supervised(Scoped)} block completes.
     *
     * <p>The fork behaves as a daemon thread. That is, if the body of the scope completes
     * successfully, and all other user forks (created using {@link #forkUser(Callable)}) complete
     * successfully, the scope will end, cancelling all running forks (including this one, if it's
     * still running). That is, successful completion of this fork isn't required to end the scope.
     *
     * <p>An exception thrown while evaluating {@code f} will cause the fork to fail and the
     * enclosing scope to end (cancelling all other running forks).
     */
    public <T> Fork<T> fork(Callable<T> f) throws InterruptedException {
        var result = new CompletableFuture<T>();
        supervisor
                .getCommands()
                .send(
                        new RunFork<T>(
                                () -> {
                                    try {
                                        result.complete(f.call());
                                    } catch (Throwable e) {
                                        // we notify the supervisor first, so that if this is the
                                        // first failing fork in the scope, the supervisor will
                                        // get first notified of the exception by the "original"
                                        // (this) fork if the supervisor doesn't end the scope, the
                                        // exception will be thrown when joining the result;
                                        // otherwise, not completing the result; any joins will
                                        // end up being interrupted
                                        if (!supervisor.forkException(e)) {
                                            result.completeExceptionally(e);
                                        }
                                    }
                                    return null;
                                }));
        return new ForkUsingResult<>(result);
    }

    /**
     * Starts a fork (logical thread of execution), which is guaranteed to complete before the
     * enclosing {@link Scopes#supervised(Scoped)} block completes.
     *
     * <p>The fork behaves as a user-level thread. That is, the scope won't end until the body of
     * the scope, and all other user forks (including this one) complete successfully. That is,
     * successful completion of this fork is required to end the scope.
     *
     * <p>An exception thrown while evaluating {@code f} will cause the enclosing scope to end
     * (cancelling all other running forks).
     */
    public <T> Fork<T> forkUser(Callable<T> f) throws InterruptedException {
        var result = new CompletableFuture<T>();
        supervisor.forkStarts();
        supervisor
                .getCommands()
                .send(
                        new RunFork<T>(
                                () -> {
                                    try {
                                        result.complete(f.call());
                                        supervisor.forkSuccess();
                                    } catch (Throwable e) {
                                        if (!supervisor.forkException(e)) {
                                            result.completeExceptionally(e);
                                        }
                                    }
                                    return null;
                                }));
        return new ForkUsingResult<>(result);
    }

    /**
     * Starts a fork (logical thread of execution), which is guaranteed to complete before the
     * enclosing {@link Scopes#supervised(Scoped)} block completes.
     *
     * <p>In case an exception is thrown while evaluating {@code f}, it will be thrown when calling
     * the returned {@link Fork}'s <code>.join()</code> method.
     *
     * <p>Success or failure isn't signalled to the enclosing scope, and doesn't influence the
     * scope's lifecycle.
     *
     * <p>For alternate behaviors, see {@link #fork}, {@link #forkUser}, {@link #forkCancellable}.
     */
    public <T> Fork<T> forkUnsupervised(Callable<T> f) throws InterruptedException {
        var result = new CompletableFuture<T>();
        supervisor
                .getCommands()
                .send(
                        new RunFork<T>(
                                () -> {
                                    try {
                                        result.complete(f.call());
                                    } catch (Throwable e) {
                                        result.completeExceptionally(e);
                                    }
                                    return null;
                                }));
        return new ForkUsingResult<>(result);
    }

    /**
     * Starts a fork (logical thread of execution), which is guaranteed to complete before the
     * enclosing {@link Scopes#supervised(Scoped)} block completes, and which can be cancelled
     * on-demand.
     *
     * <p>In case an exception is thrown while evaluating {@code f}, it will be thrown when calling
     * the returned {@link CancellableFork}'s {@code .join()} method.
     *
     * <p>The fork is unsupervised (similarly to {@link #forkUnsupervised(Callable)}), hence success
     * or failure isn't signalled to the enclosing scope and doesn't influence the scope's
     * lifecycle.
     *
     * <p>For alternate behaviors, see {@link #fork}, {@link #forkUser} and {@link
     * #forkUnsupervised}.
     *
     * <p>Implementation note: a cancellable fork is created by starting a nested scope in a fork,
     * and then starting a fork there. Hence, it is more expensive than {@link Scope#fork}, as two
     * virtual threads are started.
     */
    public <T> CancellableFork<T> forkCancellable(Callable<T> f) throws InterruptedException {
        var result = new CompletableFuture<T>();
        // forks can be never run, if they are cancelled immediately - we need to detect this, not
        // to await on result.get()
        var started = new AtomicBoolean(false);
        // interrupt signal
        var done = new Semaphore(0);
        supervisor
                .getCommands()
                .send(
                        new RunFork<T>(
                                () -> {
                                    new Scope()
                                            .run(
                                                    nestedScope ->
                                                            forkCancellableNestedScope(
                                                                    nestedScope,
                                                                    started,
                                                                    done,
                                                                    result,
                                                                    f));
                                    return null;
                                }));
        return new CancellableForkUsingResult<>(result, done, started);
    }

    private static <T> Void forkCancellableNestedScope(
            Scope nestedScope,
            AtomicBoolean started,
            Semaphore done,
            CompletableFuture<T> result,
            Callable<T> f)
            throws InterruptedException {
        nestedScope
                .getSupervisor()
                .getCommands()
                .send(
                        new RunFork<T>(
                                () -> {
                                    // "else" means that the fork is already cancelled, so doing
                                    // nothing in that case
                                    if (!started.getAndSet(true)) {
                                        try {
                                            result.complete(f.call());
                                        } catch (Exception e) {
                                            result.completeExceptionally(e);
                                        }
                                    }

                                    // the nested scope can now finish
                                    done.release();
                                    return null;
                                }));
        done.acquire();
        return null;
    }

    /**
     * Returns a concurrency-scope-specific runner, which allows scheduling of functions to be run
     * within the current concurrency scope, from the context of arbitrary threads (not necessarily
     * threads that are part of the current concurrency scope).
     *
     * <p>Usage: obtain a runner from within a concurrency scope, while on a fork/thread that is
     * managed by the concurrency scope. Then, pass that runner to the external library. It can then
     * schedule functions (e.g. create forks) to be run within the concurrency scope from arbitrary
     * threads, as long as the concurrency scope isn't complete.
     *
     * <p>Execution is scheduled through an {@link ActorRef}, which is lazily created, and bound to
     * an {@link Scope} instances.
     *
     * <p>This method should **only** be used when integrating `Jox` with libraries that manage
     * concurrency on their own, and which run callbacks on a managed thread pool. The logic
     * executed by the third-party library should be entirely contained within the lifetime of this
     * concurrency scope. The sole purpose of this method is to enable running scope-aware logic
     * from threads **other** than Jox-managed.
     *
     * <p>Use with care!
     *
     * @see ExternalRunner#runAsync(ThrowingConsumer) for running functions within the scope
     */
    public ExternalRunner externalRunner() throws InterruptedException {
        if (externalSchedulerActor == null) {
            externalSchedulerLock.lock();
            try {
                if (externalSchedulerActor == null) {
                    externalSchedulerActor = ActorRef.create(this, r -> r.accept(Scope.this));
                }
            } finally {
                externalSchedulerLock.unlock();
            }
        }
        return new ExternalRunner(externalSchedulerActor);
    }

    private record CancelWhenDoneJoiner(AtomicBoolean scopeDone)
            implements StructuredTaskScope.Joiner<Object, Void> {
        @Override
        public Void result() {
            return null;
        }

        @Override
        public boolean onFork(StructuredTaskScope.Subtask<?> subtask) {
            return scopeDone.get();
        }
    }
}

interface ExternalScheduler {
    void run(ThrowingConsumer<Scope> r) throws Exception;
}
