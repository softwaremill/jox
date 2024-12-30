package com.softwaremill.jox.flows;


import static com.softwaremill.jox.Select.defaultClause;
import static com.softwaremill.jox.Select.selectOrClosed;
import static com.softwaremill.jox.flows.Flows.usingEmit;
import static com.softwaremill.jox.structured.Scopes.supervised;
import static com.softwaremill.jox.structured.Scopes.unsupervised;
import static java.lang.Thread.sleep;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.softwaremill.jox.Channel;
import com.softwaremill.jox.ChannelDone;
import com.softwaremill.jox.ChannelError;
import com.softwaremill.jox.Sink;
import com.softwaremill.jox.Source;
import com.softwaremill.jox.structured.Fork;
import com.softwaremill.jox.structured.Scopes;
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
        return runToChannelInternal(scope, Channel::withScopedBufferSize);
    }

    /** The flow is run in the background, and each emitted element is sent to a newly created channel, which is then returned as the result
     * of this method.
     * <p>
     * Method does not block until the flow completes.
     *
     * @param scope
     *  Required for creating async forks responsible for writing to channel
     * @param bufferCapacity
     *  Specifies buffer capacity of created channel
     */
    public Source<T> runToChannel(UnsupervisedScope scope, int bufferCapacity) {
        return runToChannelInternal(scope, () -> new Channel<>(bufferCapacity));
    }

    private Source<T> runToChannelInternal(UnsupervisedScope scope, Supplier<Channel<T>> channelProvider) {
        if (last instanceof SourceBackedFlowStage<T>(Source<T> source)) {
            return source;
        } else {
            Channel<T> channel = channelProvider.get();
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
        runForeach(_ -> {});
    }

    /**
     * Passes each element emitted by this flow to the given sink. Blocks until the flow completes.
     * <p>
     * Errors are always propagated to the provided sink. Successful flow completion is propagated when `propagateDone` is set to `true`.
     * <p>
     * Fatal errors are rethrown.
     */
    public void runPipeToSink(Sink<T> sink, boolean propagateDone) {
        try {
            last.run(sink::send);
            if (propagateDone) {
                sink.doneOrClosed();
            }
        } catch (Exception e) {
            sink.error(e);
        } catch (Throwable t) {
            sink.error(t);
            throw t;
        }
    }

    /** Returns the last element emitted by this flow, wrapped in {@link Optional#of}, or {@link Optional#empty()} when this source is empty. */
    public Optional<T> runLastOptional() throws Exception {
        AtomicReference<Optional<T>> value = new AtomicReference<>(Optional.empty());
        last.run(t -> value.set(Optional.of(t)));
        return value.get();
    }

    /** Returns the last element emitted by this flow, or throws {@link NoSuchElementException} when the flow emits no elements (is empty).
     *
     * @throws NoSuchElementException
     *   When this flow is empty.
     */
    public T runLast() throws Exception {
        return runLastOptional()
                .orElseThrow(() -> new NoSuchElementException("cannot obtain last element from an empty source"));
    }

    /**
     * Applies function `f` on the first and the following (if available) elements emitted by this flow. The returned value is used as the
     * next current value and `f` is applied again with the next value emitted by this source. The operation is repeated until this flow
     * emits all elements. This is similar operation to {@link Flow#runFold} but it uses the first emitted element as `zero`.
     *
     * @param f
     *   A binary function (a function that takes two arguments) that is applied to the current and next values emitted by this flow.
     * @return
     *   Combined value retrieved from running function `f` on all flow elements in a cumulative manner where result of the previous call is
     *   used as an input value to the next.
     * @throws NoSuchElementException
     *   When this flow is empty.
     */
    public T runReduce(BinaryOperator<T> f) throws Exception {
        AtomicReference<Optional<T>> current = new AtomicReference<>(Optional.empty());
        last.run(t -> {
            current.updateAndGet(currentValue -> currentValue
                    .map(u -> f.apply(u, t))
                    .or(() -> Optional.of(t)));
        });

        return current.get().orElseThrow(() -> new NoSuchElementException("cannot reduce an empty flow"));
    }

    /**
     * Returns the list of up to `n` last elements emitted by this flow. Less than `n` elements is returned when this flow emits less
     * elements than requested. Empty list is returned when called on an empty flow.
     *
     * @param n
     *   Number of elements to be taken from the end of this flow. It is expected that `n >= 0`.
     * @return
     *   A list of up to `n` last elements from this flow.
     */
    public List<T> runTakeLast(int n) throws Exception {
        if (n < 0) {
            throw new IllegalArgumentException("requirement failed: n must be >= 0");
        }
        if (n == 0) {
            runDrain();
            return Collections.emptyList();
        } else if (n == 1) {
            return runLastOptional()
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        } else {
            List<T> buffer = new LinkedList<>();

            last.run(t -> {
                if (buffer.size() == n) {
                    buffer.removeFirst();
                }
                buffer.add(t);
            });

            return new ArrayList<>(buffer);
        }
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
            unsupervised(scope -> {
                runLastToChannelAsync(scope, ch);
                FlowEmit.channelToEmit(ch, emit);
                return null;
            });
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

    /**
     * Chunks up the elements into groups of the specified size. The last group may be smaller due to the flow being complete.
     *
     * @param n The number of elements in a group.
     */
    public Flow<List<T>> grouped(int n) {
        return groupedWeighted(n, _ -> 1L);
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
        return Flows.usingEmit(_ -> last.run(_ -> {}));
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

    /** Intersperses elements emitted by this flow with `inject` elements. The `inject` element is emitted between each pair of elements. */
    public Flow<T> intersperse(T inject) {
        return intersperse(Optional.empty(), inject, Optional.empty());
    }

    /** Intersperses elements emitted by this flow with `inject` elements. The `start` element is emitted at the beginning; `end` is emitted
     * after the current flow emits the last element.
     *
     * @param start
     *   An element to be emitted at the beginning.
     * @param inject
     *   An element to be injected between the flow elements.
     * @param end
     *   An element to be emitted at the end.
     */
    public Flow<T> intersperse(T start, T inject, T end) {
        return intersperse(Optional.of(start), inject, Optional.of(end));
    }

    private Flow<T> intersperse(Optional<T> start, T inject, Optional<T> end) {
        return Flows.usingEmit(emit -> {
            if (start.isPresent()) {
                emit.apply(start.get());
            }
            AtomicBoolean firstEmitted = new AtomicBoolean(false);
            last.run(t -> {
                if (firstEmitted.get()) emit.apply(inject);
                emit.apply(t);
                firstEmitted.set(true);
            });
            if (end.isPresent()) {
                emit.apply(end.get());
            }
        });
    }

    /**
     * Emits elements limiting the throughput to a specific number of elements (evenly spaced) per time unit. Note that the element's
     * emission-time is included in the resulting throughput. For instance, having `throttle(1, Duration.ofSeconds(1))` and emission of the next
     * element taking `Xms` means that the resulting flow will emit elements every `1s + Xms` time. Throttling is not applied to the empty
     * source.
     *
     * @param elements
     *   Number of elements to be emitted. Must be greater than 0.
     * @param per
     *   Per time unit. Must be greater or equal to 1 ms.
     * @return
     *   A flow that emits at most `elements` `per` time unit.
     */
    public Flow<T> throttle(int elements, Duration per) {
        if (elements <= 0) {
            throw new IllegalArgumentException("requirement failed: elements must be > 0");
        }
        if (per.toMillis() <= 0) {
            throw new IllegalArgumentException("requirement failed: per time must be >= 1 ms");
        }
        long emitEveryMillis = per.toMillis() / elements;
        return tap(_ -> {
            try {
                sleep(Duration.ofMillis(emitEveryMillis));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Transform the flow so that it emits elements as long as predicate `f` is satisfied (returns `true`). If `includeFirstFailing` is
     * `true`, the flow will additionally emit the first element that failed the predicate. After that, the flow will complete as done.
     *
     * @param f
     *   A predicate function called on incoming elements.
     * @param includeFirstFailing
     *   Whether the flow should also emit the first element that failed the predicate (`false` by default).
     */
    public Flow<T> takeWhile(Predicate<T> f, boolean includeFirstFailing) {
        return Flows.usingEmit(emit -> {
            try {
                last.run(t -> {
                    if (f.test(t)) {
                        emit.apply(t);
                    } else {
                        if (includeFirstFailing) {
                            emit.apply(t);
                        }
                        throw new BreakException();
                    }
                });
            } catch (BreakException e) {
                // done
            }
        });
    }

    /**
     * Concatenates this flow with the `other` flow. The resulting flow will emit elements from this flow first, and then from the `other`
     * flow.
     *
     * @param other
     *   The flow to be appended to this flow.
     */
    public Flow<T> concat(Flow<T> other) {
        return Flows.concat(this, other);
    }

    /**
     * Drops `n` elements from this flow and emits subsequent elements.
     *
     * @param n
     *   Number of elements to be dropped.
     */
    public Flow<T> drop(int n) {
        return Flows.usingEmit(emit -> {
            AtomicInteger dropped = new AtomicInteger(0);
            last.run(t -> {
                if (dropped.get() < n) {
                    dropped.getAndIncrement();
                } else {
                    emit.apply(t);
                }
            });
        });
    }

    /**
     * Merges two flows into a single flow. The resulting flow emits elements from both flows in the order they are emitted. If one of the
     * flows completes before the other, the remaining elements from the other flow are emitted by the returned flow. This can be changed
     * with the `propagateDoneLeft` and `propagateDoneRight` flags.
     * <p>
     * Both flows are run concurrently in the background.
     * The size of the buffers is determined by the {@link Channel#BUFFER_SIZE} that is in scope, or default value {@link Channel#DEFAULT_BUFFER_SIZE} is chosen if not specified.
     *
     * @param other
     *   The flow to be merged with this flow.
     * @param propagateDoneLeft
     *   Should the resulting flow complete when the left flow (`this`) completes, before the `other` flow.
     * @param propagateDoneRight
     *   Should the resulting flow complete when the right flow (`outer`) completes, before `this` flow.
     */
    public Flow<T> merge(Flow<T> other, boolean propagateDoneLeft, boolean propagateDoneRight) {
        return Flows.usingEmit(emit -> {
            unsupervised(scope -> {
                Source<T> c1 = this.runToChannel(scope);
                Source<T> c2 = other.runToChannel(scope);

                boolean continueLoop = true;
                while (continueLoop) {
                    switch (selectOrClosed(c1.receiveClause(), c2.receiveClause())) {
                        case ChannelDone _ -> {
                            if (c1.isClosedForReceive()) {
                                if (!propagateDoneLeft) FlowEmit.channelToEmit(c2, emit);
                            } else if (!propagateDoneRight) FlowEmit.channelToEmit(c1, emit);
                            continueLoop = false;
                        }
                        case ChannelError error -> throw error.toException();
                        case Object r -> //noinspection unchecked
                                emit.apply((T) r);
                    }
                }
                return null;
            });
        });
    }

    /** Prepends `other` flow to this source. The resulting flow will emit elements from `other` flow first, and then from this flow.
     *
     * @param other
     *   The flow to be prepended to this flow.
     */
    public Flow<T> prepend(Flow<T> other) {
        return other.concat(this);
    }

    /**
     * If this flow has no elements then elements from an `alternative` flow are emitted by the returned flow. If this flow is failed then
     * the returned flow is failed as well.
     *
     * @param alternative
     *   An alternative flow to be used when this flow is empty.
     */
    public Flow<T> orElse(Flow<T> alternative) {
        return Flows.usingEmit(emit -> {
            AtomicBoolean receivedAtLeastOneElement = new AtomicBoolean(false);
            last.run(t -> {
                emit.apply(t);
                receivedAtLeastOneElement.set(true);
            });
            if (!receivedAtLeastOneElement.get()) {
                alternative.runToEmit(emit);
            }
        });
    }

    /**
     * Emits a given number of elements (determined by `segmentSize`) from this flow to the returned flow, then emits the same number of
     * elements from the `other` flow and repeats. The order of elements in both flows is preserved.
     * <p>
     * If one of the flows is done before the other, the behavior depends on the `eagerComplete` flag. When set to `true`, the returned flow is
     * completed immediately, otherwise the remaining elements from the other flow are emitted by the returned flow.
     * <p>
     * Both flows are run concurrently and asynchronously.
     *
     * @param other
     *   The flow whose elements will be interleaved with the elements of this flow.
     * @param segmentSize
     *   The number of elements sent from each flow before switching to the other one.
     * @param eagerComplete
     *   If `true`, the returned flow is completed as soon as either of the flow completes. If `false`, the remaining elements of the
     *   non-completed flow are sent downstream.
     */
    public <U> Flow<U> interleave(Flow<U> other, int segmentSize, boolean eagerComplete, int bufferCapacity) {
        //noinspection unchecked
        return Flows.interleaveAll(Arrays.asList((Flow<U>) this, other), segmentSize, eagerComplete, bufferCapacity);
    }

    /**
     * Applies the given mapping function `f`, to each element emitted by this source, transforming it into an `Iterable` of results,
     * then the returned flow emits the results one by one. Can be used to unfold incoming sequences of elements into single elements.
     *
     * @param f
     *   A function that transforms the element from this flow into an `Iterable` of results which are emitted one by one by the
     *   returned flow. If the result of `f` is empty, nothing is emitted by the returned channel.
     */
    public <U> Flow<U> mapConcat(Function<T, Iterable<U>> f) {
        return Flows.usingEmit(emit -> {
            last.run(t -> {
                for (U u : f.apply(t)) {
                    emit.apply(u);
                }
            });
        });
    }

    /**
     * Applies the given mapping function `f` to each element emitted by this flow. At most `parallelism` invocations of `f` are run in
     * parallel.
     * <p>
     * The mapped results are emitted in the same order, in which inputs are received. In other words, ordering is preserved.
     * <p>
     * The size of the output buffer is determined by the {@link Channel#BUFFER_SIZE} that is in scope, or default value {@link Channel#DEFAULT_BUFFER_SIZE} is chosen if not specified.
     *
     * @param parallelism
     *   An upper bound on the number of forks that run in parallel. Each fork runs the function `f` on a single element from the flow.
     * @param f
     *   The mapping function.
     */
    public <U> Flow<U> mapPar(int parallelism, Function<T, U> f) {
        return Flows.usingEmit(emit -> {
            Semaphore semaphore = new Semaphore(parallelism);
            Channel<Fork<Optional<U>>> inProgress = new Channel<>(parallelism);
            Channel<U> results = Channel.withScopedBufferSize();

            // creating a nested scope, so that in case of errors, we can clean up any mapping forks in a "local" fashion,
            // that is without closing the main scope; any error management must be done in the forks, as the scope is
            // unsupervised
            Scopes.unsupervised(scope -> {
                // a fork which runs the `last` pipeline, and for each emitted element creates a fork
                // notifying only the `results` channels, as it will cause the scope to end, and any other forks to be
                // interrupted, including the inProgress-fork, which might be waiting on a join()
                forkPropagate(scope, results, () -> {
                    last.run(value -> {
                        semaphore.acquire();
                        inProgress.sendOrClosed(forkMapping(scope, f, semaphore, value, results));
                    });
                    inProgress.doneOrClosed();
                    return null;
                });

                // a fork in which we wait for the created forks to finish (in sequence), and forward the mapped values to `results`
                // this extra step is needed so that if there's an error in any of the mapping forks, it's discovered as quickly as
                // possible in the main body
                scope.forkUnsupervised((Callable<T>) () -> {
                    while (true) {
                        switch (inProgress.receiveOrClosed()) {
                            // in the fork's result is a `None`, the error is already propagated to the `results` channel
                            case ChannelDone ignored -> {
                                results.done();
                                return null;
                            }
                            case ChannelError(Throwable e) -> throw new IllegalStateException("inProgress should never be closed with an error", e);
                            case Object fork -> {
                                //noinspection unchecked
                                Optional<U> result = ((Fork<Optional<U>>) fork).join();
                                if (result.isPresent()) {
                                    results.sendOrClosed(result.get());
                                } else {
                                    return null;
                                }
                            }
                        }
                    }
                });

                // in the main body, we call the `emit` methods using the (sequentially received) results; when an error occurs,
                // the scope ends, interrupting any forks that are still running
                FlowEmit.channelToEmit(results, emit);
                return null;
            });
        });
    }

    /**
     * Applies the given mapping function `f` to each element emitted by this flow. At most `parallelism` invocations of `f` are run in
     * parallel.
     * <p>
     * The mapped results **might** be emitted out-of-order, depending on the order in which the mapping function completes.
     * <p>
     * The size of the output buffer is determined by the {@link Channel#BUFFER_SIZE} that is in scope, or default value {@link Channel#DEFAULT_BUFFER_SIZE} is chosen if not specified.
     *
     * @param parallelism
     *   An upper bound on the number of forks that run in parallel. Each fork runs the function `f` on a single element from the flow.
     * @param f
     *   The mapping function.
     */
    public <U> Flow<U> mapParUnordered(int parallelism, Function<T, U> f) {
        return Flows.usingEmit(emit -> {
            Channel<U> results = Channel.withScopedBufferSize();
            Semaphore s = new Semaphore(parallelism);
            unsupervised(unsupervisedScope -> { // the outer scope, used for the fork which runs the `last` pipeline
                forkPropagate(unsupervisedScope, results, () -> {
                    supervised(scope -> { // the inner scope, in which user forks are created, and which is used to wait for all to complete when done
                        try {
                            last.run(t -> {
                                s.acquire();
                                scope.forkUser(() -> {
                                    try {
                                        results.sendOrClosed(f.apply(t));
                                        s.release();
                                    } catch (Throwable cause) {
                                        results.errorOrClosed(cause);
                                    }
                                    return null;
                                });
                            });
                        } catch (Exception e) {
                            results.errorOrClosed(e);
                        }
                        return null;
                    });
                    results.doneOrClosed();
                    return null;
                });
                FlowEmit.channelToEmit(results, emit);
                return null;
            });
        });
    }

    /**
     * Creates sliding windows of elements from this flow. The window slides by `step` elements. The last window may be smaller due to flow
     * being completed.
     *
     * @param n
     *   The number of elements in a window.
     * @param step
     *   The number of elements the window slides by.
     */
    public Flow<List<T>> sliding(int n, int step) {
        if (n <= 0) throw new IllegalArgumentException("n must be > 0");
        if (step <= 0) throw new IllegalArgumentException("step must be > 0");

        return Flows.usingEmit(emit -> {
            final AtomicReference<List<T>> buf = new AtomicReference<>(new ArrayList<>());
            last.run(t -> {
                var buffer = buf.get();
                buffer.add(t);
                if (buffer.size() < n) {
                    // do nothing
                } else if (buffer.size() == n) {
                    emit.apply(new ArrayList<>(buf.get()));
                } else if (step <= n) {
                    // if step is <= n we simply drop `step` elements and continue appending until buffer size is n
                    buffer = buf.updateAndGet(b -> b.subList(step, b.size()));
                    // in special case when step == 1, we have to send the buffer immediately
                    if (buffer.size() == n) emit.apply(new ArrayList<>(buffer));
                } else {
                    // step > n - we drop `step` elements and continue appending until buffer size is n
                    if (buf.get().size() == step) buf.updateAndGet(b -> b.subList(step, buf.get().size()));
                }
            });
            // send the remaining elements, only if these elements were not yet sent
            List<T> buffer = buf.get();
            if (!buffer.isEmpty() && buffer.size() < n) emit.apply(new ArrayList<>(buffer));
        });
    }

    /**
     * Attaches the given {@link Sink} to this flow, meaning elements that pass through will also be sent to the sink. If emitting an
     * element, or sending to the `other` sink blocks, no elements will be processed until both are done. The elements are first emitted by
     * the flow and then, only if that was successful, to the `other` sink.
     * <p>
     * If this flow fails, then failure is passed to the `other` sink as well. If the `other` sink is failed or complete, this becomes a
     * failure of the returned flow (contrary to {@link #alsoToTap} where it's ignored).
     *
     * @param other
     *   The sink to which elements from this flow will be sent.
     * @see #alsoToTap for a version that drops elements when the `other` sink is not available for receive.
     */
    public Flow<T> alsoTo(Sink<T> other) {
        return Flows.usingEmit(emit -> {
            try {
                last.run(t -> {
                    try {
                        emit.apply(t);
                    } catch (Exception e) {
                        other.errorOrClosed(e);
                        throw e;
                    }
                    other.send(t);
                });
                other.done();
            } catch (Exception e) {
                other.errorOrClosed(e);
                throw e;
            }
        });
    }

    /**
     * Attaches the given {@link Sink} to this flow, meaning elements that pass through will also be sent to the sink. If the `other`
     * sink is not available for receive, the elements are still emitted by the returned flow, but not sent to the `other` sink.
     * <p>
     * If this flow fails, then failure is passed to the `other` sink as well. If the `other` sink fails or closes, then failure or closure
     * is ignored and it doesn't affect the resulting flow (contrary to {@link #alsoTo} where it's propagated).
     *
     * @param other
     *   The sink to which elements from this source will be sent.
     * @see #alsoTo for a version that ensures that elements are emitted both by the returned flow and sent to the `other` sink.
     */
    public Flow<T> alsoToTap(Sink<T> other) {
        return Flows.usingEmit(emit -> {
            try {
                last.run(t -> {
                    try {
                        emit.apply(t);
                    } catch (Exception e) {
                        other.errorOrClosed(e);
                    }
                    selectOrClosed(other.sendClause(t), defaultClause((Object) null));
                });
                other.doneOrClosed();
            } catch (Exception e) {
                other.errorOrClosed(e);
            }
        });
    }

    // endregion

    private void forkPropagate(UnsupervisedScope unsupervisedScope, Sink<?> propagateExceptionsTo, Callable<Void> runnable) {
        unsupervisedScope.forkUnsupervised(() -> {
            try {
                runnable.call();
            } catch (Exception e) {
                propagateExceptionsTo.errorOrClosed(e);
            }
            return null;
        });
    }

    private <U> Fork<Optional<U>> forkMapping(UnsupervisedScope scope, Function<T, U> f, Semaphore s, T value, Sink<U> results) {
        return scope.forkUnsupervised(() -> {
            try {
                U u = f.apply(value);
                s.release(); // not in finally, as in case of an exception, no point in starting subsequent forks
                return Optional.of(u);
            } catch (Throwable throwable) { // same as in `forkPropagate`, catching all exceptions
                results.errorOrClosed(throwable);
                return Optional.empty();
            }
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

    private static class BreakException extends RuntimeException {
    }
}
