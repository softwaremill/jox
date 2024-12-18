package com.softwaremill.jox.flows;


import static com.softwaremill.jox.flows.Flows.usingEmit;
import static com.softwaremill.jox.structured.Scopes.unsupervised;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.softwaremill.jox.Channel;
import com.softwaremill.jox.Source;
import com.softwaremill.jox.structured.UnsupervisedScope;

/**
 * Describes an asynchronous transformation pipeline. When run, emits elements of type `T`.
 * <p>
 * A flow is lazy - evaluation happens only when it's run.
 * <p>
 * Flows can be created using the {@link Flows#fromValues}, {@link Flows#fromSource}} and other `Flow.from*` methods, {@link Flows#tick} etc.
 * <p>
 * Transformation stages can be added using the available combinators, such as {@link Flow#map}, {@link Flow#buffer}, {@link Flow#grouped}, etc.
 * Each such method returns a new immutable {@link Flow} instance.
 * <p>
 * Running a flow is possible using one of the `run*` methods, such as {@link Flow#runToList}, {@link Flow#runToChannel} or {@link Flow#runFold}.
 */
public class Flow<T> {
    protected final FlowStage<T> last;

    public Flow(FlowStage<T> last) {
        this.last = last;
    }

    // region Run operations

    /** Invokes the given function for each emitted element. Blocks until the flow completes. */
    public void runForeach(Consumer<T> sink) throws Exception {
        last.run(sink::accept);
    }

    /** Invokes the provided {@link FlowEmit} for each emitted element. Blocks until the flow completes. */
    public void runToEmit(FlowEmit<T> emit) throws Exception {
        last.run(emit);
    }

    /** Accumulates all elements emitted by this flow into a list. Blocks until the flow completes. */
    public List<T> runToList() throws Exception {
        List<T> result = new ArrayList<>();
        runForeach(result::add);
        return result;
    }

    /** The flow is run in the background, and each emitted element is sent to a newly created channel, which is then returned as the result
     * of this method.
     * <p>
     * Buffer capacity can be set via scoped value {@link Channel#BUFFER_SIZE}. If not specified in scope, {@link Channel#DEFAULT_BUFFER_SIZE} is used.
     * <p>
     * Method does not block until the flow completes.
     *
     * @param scope
     *  Required for creating async forks responsible for writing to channel
     */
    public Source<T> runToChannel(UnsupervisedScope scope) {
        if (last instanceof SourceBackedFlowStage<T>(Source<T> source)) {
            return source;
        } else {
            Channel<T> channel = Channel.withScopedBufferSize();
            runLastToChannelAsync(scope, channel);
            return channel;
        }
    }

    /**
     * Uses `zero` as the current value and applies function `f` on it and a value emitted by this flow. The returned value is used as the
     * next current value and `f` is applied again with the next value emitted by the flow. The operation is repeated until the flow emits
     * all elements.
     *
     * @param zero
     *   An initial value to be used as the first argument to function `f` call.
     * @param f
     *   A {@link BiFunction} that is applied to the current value and value emitted by the flow.
     * @return
     *   Combined value retrieved from running function `f` on all flow elements in a cumulative manner where result of the previous call is
     *   used as an input value to the next.
     */
    public <U> U runFold(U zero, BiFunction<U, T, U> f) throws Exception {
        AtomicReference<U> current = new AtomicReference<>(zero);
        last.run(t -> current.set(f.apply(current.get(), t)));
        return current.get();
    }

    /**
     * Ignores all elements emitted by the flow. Blocks until the flow completes.
     */
    public void runDrain() throws Exception {
        runForeach(t -> {});
    }

    // endregion

    // region Flow operations

    /** When run, the current pipeline is run asynchronously in the background, emitting elements to a buffer.
     *  The elements of the buffer are then emitted by the returned flow.
     *
     * @param bufferCapacity determines size of a buffer.
     *
     * Any exceptions are propagated by the returned flow.
     */
    public Flow<T> buffer(int bufferCapacity) {
        return usingEmit(emit -> {
            Channel<T> ch = new Channel<>(bufferCapacity);
            try {
                unsupervised(scope -> {
                    runLastToChannelAsync(scope, ch);
                    FlowEmit.channelToEmit(ch, emit);
                    return null;
                });
            } catch (ExecutionException e) {
                throw (Exception) e.getCause();
            }
        });
    }

    /**
     * Applies the given `mappingFunction` to each element emitted by this flow. The returned flow then emits the results.
     */
    public <U> Flow<U> map(Function<T, U> mappingFunction) {
        return usingEmit(emit -> {
            last.run(t -> emit.apply(mappingFunction.apply(t)));
        });
    }

    /**
     * Emits only those elements emitted by this flow, for which `filteringPredicate` returns `true`.
     */
    public Flow<T> filter(Predicate<T> filteringPredicate) {
        return usingEmit(emit -> {
            last.run(t -> {
                if (filteringPredicate.test(t)) {
                    emit.apply(t);
                }
            });
        });
    }

    /**
     * Applies the given `mappingFunction` to each element emitted by this flow, in sequence.
     * The given {@link Consumer<FlowEmit>} can be used to emit an arbitrary number of elements.
     * <p>
     * The {@link FlowEmit} instance provided to the `mappingFunction` callback should only be used on the calling thread.
     * That is, {@link FlowEmit} is thread-unsafe. Moreover, the instance should not be stored or captured in closures, which outlive the invocation of `mappingFunction`.
     */
    public <U> Flow<U> mapUsingEmit(Function<T, Consumer<FlowEmit<U>>> mappingFunction) {
        return usingEmit(emit -> last.run(t -> mappingFunction.apply(t).accept(emit)));
    }

    /**
     * Applies the given effectful function `f` to each element emitted by this flow. The returned flow emits the elements unchanged.
     * If `f` throws an exceptions, the flow fails and propagates the exception.
     */
    public Flow<T> tap(Consumer<T> f) {
        return map(t -> {
            f.accept(t);
            return t;
        });
    }

    /**
     * Applies the given `mappingFunction` to each element emitted by this flow, obtaining a nested flow to run.
     * The elements emitted by the nested flow are then emitted by the returned flow.
     * <p>
     * The nested flows are run in sequence, that is, the next nested flow is started only after the previous one completes.
     */
    public <U> Flow<U> flatMap(Function<T, Flow<U>> mappingFunction) {
        return usingEmit(emit -> last.run(t -> mappingFunction.apply(t).runToEmit(emit)));
    }

    /**
     * Takes the first `n` elements from this flow and emits them. If the flow completes before emitting `n` elements, the returned flow
     * completes as well.
     */
    public Flow<T> take(int n) {
        return Flows.usingEmit(emit -> {
            AtomicInteger taken = new AtomicInteger(0);
            try {
                last.run(t -> {
                    if (taken.getAndIncrement() < n) {
                        emit.apply(t);
                    } else {
                        throw new BreakException();
                    }
                });
            } catch (ExecutionException e) {
                if (!(e.getCause() instanceof BreakException)) {
                    throw e;
                }
                // ignore
            } catch (BreakException e) {
                // ignore
            }
        });
    }

    private static class BreakException extends RuntimeException {
    }

    /**
     * Chunks up the elements into groups of the specified size. The last group may be smaller due to the flow being complete.
     *
     * @param n The number of elements in a group.
     */
    public Flow<List<T>> grouped(int n) {
        return groupedWeighted(n, t -> 1L);
    }

    /**
     * Chunks up the elements into groups that have a cumulative weight greater or equal to the `minWeight`. The last group may be smaller
     * due to the flow being complete.
     *
     * @param minWeight The minimum cumulative weight of elements in a group.
     * @param costFn The function that calculates the weight of an element.
     */
    public Flow<List<T>> groupedWeighted(long minWeight, Function<T, Long> costFn) {
        if (minWeight <= 0) {
            throw new IllegalArgumentException("minWeight must be > 0");
        }

        return Flows.usingEmit(emit -> {
            List<T> buffer = new ArrayList<>();
            AtomicLong accumulatedCost = new AtomicLong(0L);
            last.run(t -> {
                buffer.add(t);
                accumulatedCost.addAndGet(costFn.apply(t));

                if (accumulatedCost.get() >= minWeight) {
                    emit.apply(new ArrayList<>(buffer));
                    buffer.clear();
                    accumulatedCost.set(0);
                }
            });
            if (!buffer.isEmpty()) {
                emit.apply(buffer);
            }
        });
    }

    /**
     * Discard all elements emitted by this flow. The returned flow completes only when this flow completes (successfully or with an error).
     */
    public Flow<Void> drain() {
        return Flows.usingEmit(emit -> {
            last.run(t -> {});
        });
    }

    /**
     * Always runs `f` after the flow completes, whether it's because all elements are emitted, or when there's an error.
     */
    public Flow<T> onComplete(Runnable f) {
        return Flows.usingEmit(emit -> {
            try {
                last.run(emit);
            } finally {
                f.run();
            }
        });
    }

    /**
     * Runs `f` after the flow completes successfully, that is when all elements are emitted.
     */
    public Flow<T> onDone(Runnable f) {
        return Flows.usingEmit(emit -> {
            last.run(emit);
            f.run();
        });
    }

    /**
     * Runs `f` after the flow completes with an error. The error can't be recovered.
     */
    public Flow<T> onError(Consumer<Throwable> f) {
        return Flows.usingEmit(emit -> {
            try {
                last.run(emit);
            } catch (Throwable e) {
                f.accept(e);
                throw e;
            }
        });
    }

    // endregion

    private void runLastToChannelAsync(Channel<T> channel) throws ExecutionException, InterruptedException {
        unsupervised(scope -> {
            runLastToChannelAsync(scope, channel);
            return null;
        });
    }

    private void runLastToChannelAsync(UnsupervisedScope scope, Channel<T> channel) {
        scope.forkUnsupervised(() -> {
            try {
                last.run(channel::send);
                channel.done();
            } catch (Throwable e) {
                channel.error(e);
            }
            return null;
        });
    }
}




