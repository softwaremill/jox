package com.softwaremill.jox.flows;


import com.softwaremill.jox.*;
import com.softwaremill.jox.structured.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;

import static com.softwaremill.jox.Select.defaultClause;
import static com.softwaremill.jox.Select.selectOrClosed;
import static com.softwaremill.jox.flows.Flows.usingEmit;
import static com.softwaremill.jox.structured.Scopes.supervised;
import static com.softwaremill.jox.structured.Scopes.unsupervised;
import static java.lang.Thread.sleep;

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
 * <p>
 * Some operations require async processing e.g. {@link Flow#buffer}, {@link Flow#groupBy}.
 * In such case exceptions thrown by the upstream or parameter functions will be wrapped in {@link ChannelErrorException} and in {@link JoxScopeExecutionException} (unless specified differently by the docs for specific method)
 */
public class Flow<T> {
    final FlowStage<T> last;

    public Flow(FlowStage<T> last) {
        this.last = last;
    }

    // region Run operations

    /**
     * Invokes the given function for each emitted element. Blocks until the flow completes.
     */
    public void runForeach(ThrowingConsumer<T> sink) throws Exception {
        last.run(sink::accept);
    }

    /**
     * Invokes the provided {@link FlowEmit} for each emitted element. Blocks until the flow completes.
     */
    public void runToEmit(FlowEmit<T> emit) throws Exception {
        last.run(emit);
    }

    /**
     * Accumulates all elements emitted by this flow into a list. Blocks until the flow completes.
     */
    public List<T> runToList() throws Exception {
        List<T> result = new ArrayList<>();
        runForeach(result::add);
        return result;
    }

    /**
     * The flow is run in the background, and each emitted element is sent to a newly created channel, which is then returned as the result
     * of this method.
     * <p>
     * Buffer capacity can be set via scoped value {@link Channel#BUFFER_SIZE}. If not specified in scope, {@link Channel#DEFAULT_BUFFER_SIZE} is used.
     * <p>
     * Method does not block until the flow completes.
     * <p>
     * Any exceptions thrown by the flow are propagated via the channel.
     *
     * @param scope Required for creating async forks responsible for writing to channel
     */
    public Source<T> runToChannel(UnsupervisedScope scope) {
        return runToChannelInternal(scope, Channel::withScopedBufferSize);
    }

    /**
     * The flow is run in the background, and each emitted element is sent to a newly created channel, which is then returned as the result
     * of this method.
     * <p>
     * Method does not block until the flow completes.
     * <p>
     * Any exceptions thrown by the flow are propagated via the channel.
     *
     * @param scope          Required for creating async forks responsible for writing to channel
     * @param bufferCapacity Specifies buffer capacity of created channel
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
     * @param zero An initial value to be used as the first argument to function `f` call.
     * @param f    A {@link BiFunction} that is applied to the current value and value emitted by the flow.
     * @return Combined value retrieved from running function `f` on all flow elements in a cumulative manner where result of the previous call is
     * used as an input value to the next.
     */
    public <U> U runFold(U zero, ThrowingBiFunction<U, T, U> f) throws Exception {
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

    /**
     * Returns the last element emitted by this flow, wrapped in {@link Optional#of}, or {@link Optional#empty()} when this source is empty.
     */
    public Optional<T> runLastOptional() throws Exception {
        AtomicReference<Optional<T>> value = new AtomicReference<>(Optional.empty());
        last.run(t -> value.set(Optional.of(t)));
        return value.get();
    }

    /**
     * Returns the last element emitted by this flow, or throws {@link NoSuchElementException} when the flow emits no elements (is empty).
     *
     * @throws NoSuchElementException When this flow is empty.
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
     * @param f A binary function (a function that takes two arguments) that is applied to the current and next values emitted by this flow.
     * @return Combined value retrieved from running function `f` on all flow elements in a cumulative manner where result of the previous call is
     * used as an input value to the next.
     * @throws NoSuchElementException When this flow is empty.
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
     * @param n Number of elements to be taken from the end of this flow. It is expected that `n >= 0`.
     * @return A list of up to `n` last elements from this flow.
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

    /**
     * When run, the current pipeline is run asynchronously in the background, emitting elements to a buffer.
     * The elements of the buffer are then emitted by the returned flow.
     *
     * @param bufferCapacity determines size of a buffer.
     *                       <p>
     *                       Any exceptions are propagated by the returned flow.
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
     * When run, the current pipeline is run asynchronously in the background, emitting elements to a buffer.
     * The elements of the buffer are then emitted by the returned flow.
     * <p>
     * Buffer capacity is determined by the {@link Channel#BUFFER_SIZE} that is in scope, or default {@link Channel#DEFAULT_BUFFER_SIZE} is used.
     * <p>
     * Any exceptions are propagated by the returned flow.
     */
    public Flow<T> buffer() {
        return buffer(Channel.BUFFER_SIZE.orElse(Channel.DEFAULT_BUFFER_SIZE));
    }

    /**
     * Applies the given `mappingFunction` to each element emitted by this flow. The returned flow then emits the results.
     */
    public <U> Flow<U> map(ThrowingFunction<T, U> mappingFunction) {
        return usingEmit(emit -> {
            last.run(t -> emit.apply(mappingFunction.apply(t)));
        });
    }


    /**
     * Functional interface used for {@link Flow#mapStateful} and {@link Flow#mapStatefulConcat}.
     *
     * @param <T> type of input flow elements
     * @param <S> type of state
     * @param <U> type of output flow
     */
    @FunctionalInterface
    public interface StatefulMapper<T, S, U> {
        /**
         * @param state   current state
         * @param element current input flow element
         * @return pair of new state and `element` mapped to new type `U`
         */
        Map.Entry<S, U> apply(S state, T element);
    }

    /**
     * Functional interface used for {@link Flow#mapStateful(Supplier, StatefulMapper, OnComplete)} and {@link Flow#mapStatefulConcat(Supplier, StatefulMapper, OnComplete)}
     *
     * @param <S> State Type
     * @param <U> Output Flow Type
     */
    @FunctionalInterface
    public interface OnComplete<S, U> {
        /**
         * @param state at the end of the flow
         * @return {@link Optional#empty()} if value should be skipped, or value wrapped in {@link Optional}
         */
        Optional<U> apply(S state);
    }

    /**
     * Applies the given mapping function `f`, using additional state, to each element emitted by this flow. The results are emitted by the
     * returned flow. Optionally the returned flow emits an additional element, possibly based on the final state, once this flow is done.
     * <p>
     * The `initializeState` function is called once when `statefulMap` is called.
     * <p>
     * The `onComplete` function is called once when this flow is done. If it returns a non-empty {@link Optional}, the value will be emitted by the
     * flow, while an empty value will be ignored.
     *
     * @param initializeState A function that initializes the state.
     * @param f               A function that transforms the element from this flow and the state into a pair of the next state and the result which is emitted by
     *                        the returned flow.
     * @param onComplete      A function that transforms the final state into an optional element emitted by the returned flow.
     */
    public <S, U> Flow<U> mapStateful(Supplier<S> initializeState, StatefulMapper<T, S, U> f, OnComplete<S, U> onComplete) {
        StatefulMapper<T, S, Iterable<U>> resultToSome = (state, element) -> {
            var result = f.apply(state, element);
            return Map.entry(result.getKey(), List.of(result.getValue()));
        };

        return mapStatefulConcat(initializeState, resultToSome, onComplete);
    }

    /**
     * Applies the given mapping function `f`, using additional state, to each element emitted by this flow. The results are emitted by the
     * returned flow.
     * <p>
     * The `initializeState` function is called once when `statefulMap` is called.
     * <p>
     * If you want to send additional element after the flow is done, use {@link Flow#mapStateful(Supplier, StatefulMapper, OnComplete)}
     *
     * @param initializeState A function that initializes the state.
     * @param f               A function that transforms the element from this flow and the state into a pair of the next state and the result which is emitted by
     *                        the returned flow.
     */
    public <S, U> Flow<U> mapStateful(Supplier<S> initializeState, StatefulMapper<T, S, U> f) {
        return mapStateful(initializeState, f, _ -> Optional.empty());
    }

    /**
     * Applies the given mapping function `f`, using additional state, to each element emitted by this flow. The returned flow emits the
     * results one by one. Optionally the returned flow emits an additional element, possibly based on the final state, once this flow is
     * done.
     * <p>
     * The `initializeState` function is called once when `statefulMap` is called.
     * <p>
     * The `onComplete` function is called once when this flow is done. If it returns a non-empty value, the value will be emitted by the
     * returned flow, while an empty value will be ignored.
     *
     * @param initializeState A function that initializes the state.
     * @param f               A function that transforms the element from this flow and the state into a pair of the next state and a
     *                        {@code Iterable} of results which are emitted one by one by the returned flow. If the result of `f` is empty,
     *                        nothing is emitted by the returned flow.
     * @param onComplete      A function that transforms the final state into an optional element emitted by the returned flow.
     */
    public <S, U> Flow<U> mapStatefulConcat(Supplier<S> initializeState, StatefulMapper<T, S, Iterable<U>> f, OnComplete<S, U> onComplete) {
        AtomicReference<S> state = new AtomicReference<>(initializeState.get());
        return usingEmit(emit -> {
            last.run(t -> {
                Map.Entry<S, Iterable<U>> result = f.apply(state.get(), t);
                for (U u : result.getValue()) {
                    emit.apply(u);
                }
                state.set(result.getKey());
            });

            Optional<U> onCompleteResult = onComplete.apply(state.get());
            if (onCompleteResult.isPresent()) {
                emit.apply(onCompleteResult.get());
            }
        });
    }

    /**
     * Applies the given mapping function `f`, using additional state, to each element emitted by this flow. The returned flow emits the
     * results one by one.
     * <p>
     * The `initializeState` function is called once when `statefulMap` is called.
     * <p>
     * If you want to send additional element after the flow is done, use {@link Flow#mapStatefulConcat(Supplier, StatefulMapper, OnComplete)}.
     *
     * @param initializeState A function that initializes the state.
     * @param f               A function that transforms the element from this flow and the state into a pair of the next state and a
     *                        {@code Iterable} of results which are emitted one by one by the returned flow. If the result of `f` is empty,
     *                        nothing is emitted by the returned flow.
     */
    public <S, U> Flow<U> mapStatefulConcat(Supplier<S> initializeState, StatefulMapper<T, S, Iterable<U>> f) {
        return mapStatefulConcat(initializeState, f, _ -> Optional.empty());
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
     * Emits only every nth element emitted by this flow.
     *
     * @param n The interval between two emitted elements.
     */
    public Flow<T> sample(int n) {
        return Flows.usingEmit(emit -> {
            AtomicInteger sampleCounter = new AtomicInteger(0);
            last.run(t -> {
                int counter = sampleCounter.incrementAndGet();
                if (n != 0 && counter % n == 0) {
                    emit.apply(t);
                }
            });
        });
    }

    /**
     * Remove subsequent, repeating elements
     */
    public Flow<T> debounce() {
        return debounceBy(t -> t);
    }

    /**
     * Remove subsequent, repeating elements matching 'f'
     *
     * @param f The function used to compare the previous and current elements
     */
    public <U> Flow<T> debounceBy(ThrowingFunction<T, U> f) {
        return Flows.usingEmit(emit -> {
            AtomicReference<Optional<U>> previousElement = new AtomicReference<>(Optional.empty());
            last.run(t -> {
                U currentElement = f.apply(t);
                if (!previousElement.get().equals(Optional.ofNullable(currentElement))) {
                    emit.apply(t);
                }
                previousElement.set(Optional.ofNullable(currentElement));
            });
        });
    }

    /**
     * Applies the given mapping function `f` to each element emitted by this flow, for which the function returns a non-empty Optional, and emits the result.
     * If `f` returns an empty Optional at an element, the element will be skipped.
     *
     * @param f The mapping function.
     */
    public <U> Flow<U> collect(ThrowingFunction<T, Optional<U>> f) {
        return Flows.usingEmit(emit ->
                last.run(t -> {
                    Optional<U> result = f.apply(t);
                    if (result.isPresent()) {
                        emit.apply(result.get());
                    }
                })
        );
    }

    /**
     * Transforms the elements of the flow by applying an accumulation function to each element, producing a new value at each step. The
     * resulting flow contains the accumulated values at each point in the original flow.
     *
     * @param initial The initial value to start the accumulation.
     * @param f       The accumulation function that is applied to each element of the flow.
     * @return A new Flow containing the accumulated values.
     */
    public <V> Flow<V> scan(V initial, ThrowingBiFunction<V, T, V> f) {
        return Flows.usingEmit(emit -> {
            emit.apply(initial);
            AtomicReference<V> accumulator = new AtomicReference<>(initial);
            last.run(t -> {
                V newValue = f.apply(accumulator.get(), t);
                accumulator.set(newValue);
                emit.apply(newValue);
            });
        });
    }

    /**
     * Combines elements from this and the other flow into Map.Entry. Completion of either flow completes the returned flow as well. The flows
     * are run concurrently.
     * <p>
     * Method uses channels to emit elements. The size of channel buffer is determined by the scoped value {@link Channel#BUFFER_SIZE} or {@link Channel#DEFAULT_BUFFER_SIZE} is used.
     *
     * @see Flow#zipAll
     */
    public <U> Flow<Map.Entry<T, U>> zip(Flow<U> other) {
        return Flows.usingEmit(emit -> {
            Scopes.unsupervised(scope -> {
                Source<T> s1 = this.runToChannel(scope);
                Source<U> s2 = other.runToChannel(scope);

                while (true) {
                    Object result1 = s1.receiveOrClosed();
                    if (result1 instanceof ChannelDone) {
                        return null;
                    } else if (result1 instanceof ChannelError error) {
                        throw error.toException();
                    } else {
                        // noinspection unchecked
                        T t = (T) result1;
                        Object result2 = s2.receiveOrClosed();
                        if (result2 instanceof ChannelDone) {
                            return null;
                        } else if (result2 instanceof ChannelError error) {
                            throw error.toException();
                        } else {
                            // noinspection unchecked
                            U u = (U) result2;
                            emit.apply(Map.entry(t, u));
                        }
                    }
                }
            });
        });
    }

    /**
     * Combines elements from this and the other flow into tuples, handling early completion of either flow with defaults. The flows are run
     * concurrently.
     * <p>
     * Method uses channels to emit elements. The size of channel buffer is determined by the scoped value {@link Channel#BUFFER_SIZE} or {@link Channel#DEFAULT_BUFFER_SIZE} is used.
     *
     * @param other        A flow of elements to be combined with.
     * @param thisDefault  A default element to be used in the result tuple when the other flow is longer.
     * @param otherDefault A default element to be used in the result tuple when the current flow is longer.
     */
    public <U> Flow<Map.Entry<T, U>> zipAll(Flow<U> other, T thisDefault, U otherDefault) {
        return Flows.usingEmit(emit -> {
            Scopes.unsupervised(scope -> {
                Source<T> s1 = this.runToChannel(scope);
                Source<U> s2 = other.runToChannel(scope);

                boolean continueLoop = true;
                while (continueLoop) {
                    Object received1 = s1.receiveOrClosed();
                    if (received1 instanceof ChannelDone) {
                        Object received2 = s2.receiveOrClosed();
                        if (received2 instanceof ChannelDone) {
                            continueLoop = false;
                        } else if (received2 instanceof ChannelError e) {
                            throw e.toException();
                        } else {
                            //noinspection unchecked
                            emit.apply(Map.entry(thisDefault, (U) received2));
                        }
                    } else if (received1 instanceof ChannelError e) {
                        throw e.toException();
                    } else {
                        Object received2 = s2.receiveOrClosed();
                        if (received2 instanceof ChannelDone) {
                            //noinspection unchecked
                            emit.apply(Map.entry((T) received1, otherDefault));
                        } else if (received2 instanceof ChannelError e) {
                            throw e.toException();
                        } else {
                            //noinspection unchecked
                            emit.apply(Map.entry((T) received1, (U) received2));
                        }
                    }
                }
                return null;
            });
        });
    }

    /**
     * Combines each element from this and the index of the element (starting at 0).
     */
    public Flow<Map.Entry<T, Long>> zipWithIndex() {
        return Flows.usingEmit(emit -> {
            AtomicLong index = new AtomicLong(0L);
            last.run(t -> {
                Map.Entry<T, Long> zipped = Map.entry(t, index.getAndIncrement());
                emit.apply(zipped);
            });
        });
    }

    /**
     * Given that this flow emits other flows, flattens the nested flows into a single flow. The resulting flow emits elements from the
     * nested flows in the order they are emitted.
     * <p>
     * The nested flows are run in sequence, that is, the next nested flow is started only after the previous one completes.
     *
     * @param args This param should *NOT* be passed. It's only used to verify that this flow contains other flows.
     * @throws IllegalArgumentException when flow does not contain nested flows, or when `args` are not empty
     */
    @SafeVarargs
    public final T flatten(T... args) {
        if (!Flow.class.equals(getTClass(args))) {
            throw new IllegalArgumentException("requirement failed: flatten can be called on Flow containing Flows");
        }
        //noinspection unchecked,rawtypes
        return (T) this.flatMap(t -> (Flow) t);
    }

    /**
     * Pipes the elements of child flows into the returned flow.
     * <p>
     * If this flow or any of the child flows emit an error, the pulling stops and the output flow propagates the error.
     * <p>
     * Up to `parallelism` child flows are run concurrently in the background. When the limit is reached, until a child flow completes, no
     * more child flows are run.
     * <p>
     * The size of the buffers for the elements emitted by the child flows is determined by the {@link Channel#BUFFER_SIZE} that is in scope, or default {@link Channel#DEFAULT_BUFFER_SIZE} is used.
     *
     * @param parallelism An upper bound on the number of child flows that run in parallel.
     * @param args        This param should *NOT* be passed. It's only used to verify that this flow contains other flows.
     * @throws IllegalArgumentException when flow does not contain nested flows, or when `args` are not empty
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public final <U> T flattenPar(int parallelism, T... args) {
        if (!Flow.class.equals(getTClass(args))) {
            throw new IllegalArgumentException("requirement failed: flattenPar can be called on Flow containing Flows");
        }
        return (T) Flows.usingEmit(emit -> {
            class Nested {
                final Flow<U> child;

                Nested(Flow<U> child) {
                    this.child = child;
                }
            }
            final class ChildDone {}

            unsupervised(scope -> {
                Channel<U> childOutputChannel = Channel.withScopedBufferSize();
                Channel<ChildDone> childDoneChannel = Channel.withScopedBufferSize();

                // When an error occurs in the parent, propagating it also to `childOutputChannel`, from which we always
                // `select` in the main loop. That way, even if max parallelism is reached, errors in the parent will
                // be discovered without delay.
                //noinspection unchecked
                Source<Nested> parentChannel = map(t -> new Nested((Flow<U>) t))
                        .onError(childOutputChannel::error)
                        .runToChannel(scope);

                int runningChannelCount = 1; // parent is running
                boolean parentDone = false;

                while (runningChannelCount > 0) {
                    assert runningChannelCount <= parallelism + 1;

                    Object result;
                    if (runningChannelCount == parallelism + 1 || parentDone) {
                        result = selectOrClosed(childOutputChannel.receiveClause(), childDoneChannel.receiveClause());
                    } else {
                        result = selectOrClosed(childOutputChannel.receiveClause(), childDoneChannel.receiveClause(), parentChannel.receiveClause());
                    }

                    // Only `parentChannel` might be done, child completion is signalled via `childDoneChannel`.
                    if (result instanceof ChannelDone) {
                        parentDone = parentChannel.isClosedForReceive();
                        assert parentDone;
                        runningChannelCount--;
                    } else if (result instanceof ChannelError e) {
                        throw e.toException();
                    } else if (ChildDone.class.isInstance(result)) {
                        runningChannelCount--;
                    } else if (Nested.class.isInstance(result)) {
                        //noinspection unchecked
                        Nested t = (Nested) result;
                        scope.forkUnsupervised(() -> {
                            t.child.onDone(() -> childDoneChannel.send(new ChildDone()))
                                   .runPipeToSink(childOutputChannel, false);
                            return null;
                        });
                        runningChannelCount++;
                    } else if (result != null) {
                        emit.apply(result);
                    }
                }
                return null;
            });
        });
    }

    /**
     * Applies the given `mappingFunction` to each element emitted by this flow, in sequence.
     * The given {@link ThrowingConsumer<FlowEmit>} can be used to emit an arbitrary number of elements.
     * <p>
     * The {@link FlowEmit} instance provided to the `mappingFunction` callback should only be used on the calling thread.
     * That is, {@link FlowEmit} is thread-unsafe. Moreover, the instance should not be stored or captured in closures, which outlive the invocation of `mappingFunction`.
     */
    public <U> Flow<U> mapUsingEmit(ThrowingFunction<T, ThrowingConsumer<FlowEmit<U>>> mappingFunction) {
        return usingEmit(emit -> last.run(t -> mappingFunction.apply(t).accept(emit)));
    }

    /**
     * Applies the given effectful function `f` to each element emitted by this flow. The returned flow emits the elements unchanged.
     * If `f` throws an exceptions, the flow fails and propagates the exception.
     */
    public Flow<T> tap(ThrowingConsumer<T> f) {
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
    public <U> Flow<U> flatMap(ThrowingFunction<T, Flow<U>> mappingFunction) {
        return usingEmit(emit -> last.run(t -> mappingFunction.apply(t).runToEmit(emit)));
    }

    /**
     * Takes the first `n` elements from this flow and emits them. If the flow completes before emitting `n` elements, the returned flow
     * completes as well.
     */
    public Flow<T> take(int n) {
        return usingEmit(emit -> {
            AtomicInteger taken = new AtomicInteger(0);
            try {
                last.run(t -> {
                    if (taken.getAndIncrement() < n) {
                        emit.apply(t);
                    } else {
                        throw new BreakException();
                    }
                });
            } catch (JoxScopeExecutionException e) {
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
     * Groups elements emitted by this flow into child flows. Elements for which `groupingFunction` returns the same value (of type `V`) end up in
     * the same child flow. `childFlowTransform` is applied to each created child flow, and the resulting flow is run in the background.
     * Finally, the child flows are merged back, that is any elements that they emit are emitted by the returned flow.
     * <p>
     * Up to `parallelism` child flows are run concurrently in the background. When the limit is reached, the child flow which didn't
     * receive a new element the longest is completed as done.
     * <p>
     * Child flows for `V` values might be created multiple times (if, after completing a child flow because of parallelism limit, new
     * elements arrive, mapped to a given `V` value). However, it is guaranteed that for a given `V` value, there will be at most one child
     * flow running at any time.
     * <p>
     * Child flows should only complete as done when the flow of received `T` elements completes. Otherwise, the entire stream will fail with
     * an error.
     * <p>
     * Errors that occur in this flow, or in any child flows, become errors of the returned flow (exceptions are wrapped in
     * {@link ChannelClosedException}.
     * <p>
     * The size of the buffers for the elements emitted by this flow (which is also run in the background) and the child flows are determined
     * by the {@link Channel#BUFFER_SIZE} that is in scope, or default {@link Channel#DEFAULT_BUFFER_SIZE} is used.
     * <p>
     * Wraps exceptions from `groupingFunction`, `childFlowTransform` and upstream in {@link ChannelErrorException} and {@link JoxScopeExecutionException} when flow is run.
     * <p>
     *
     * @param parallelism        An upper bound on the number of child flows that run in parallel at any time.
     * @param groupingFunction   Function used to determine the group for an element of type `T`. Each group is represented by a value of type `V`.
     * @param childFlowTransform The function that is used to create a child flow, which is later run in the background. The arguments are the group value, for which the
     *                           flow is created, and a flow of `T` elements in that group (each such element has the same group value `V` returned by `predicated`).
     * @throws JoxScopeExecutionException with cause {@link IllegalStateException}
     *                                    When `childFlowTransform` terminates the flow, before upstream passes all elements.
     */
    public <V, U> Flow<U> groupBy(int parallelism, ThrowingFunction<T, V> groupingFunction, ChildFlowTransformer<T, V, U> childFlowTransform) {
        return new GroupByImpl<>(this, parallelism, groupingFunction, childFlowTransform)
                .run();
    }

    /**
     * Functional interface used in {@link Flow#groupBy} for transforming the child flows.
     */
    @FunctionalInterface
    public interface ChildFlowTransformer<T, V, U> {
        ThrowingFunction<Flow<T>, Flow<U>> apply(V group);
    }

    /**
     * Chunks up the emitted elements into groups, within a time window, or limited by the specified number of elements, whatever happens
     * first. The timeout is reset after a group is emitted. If timeout expires and the buffer is empty, nothing is emitted. As soon as a new
     * element is emitted, the flow will emit it as a single-element group and reset the timer.
     * <p>
     * The size of buffers used by this method is determined by {@link Channel#BUFFER_SIZE} that is in scope, or default {@link Channel#DEFAULT_BUFFER_SIZE} is used.
     * <p>
     * Wraps exceptions from upstream in {@link ChannelErrorException} and {@link JoxScopeExecutionException} when flow is run.
     * <p>
     *
     * @param n        The maximum number of elements in a group.
     * @param duration The time window in which the elements are grouped.
     */
    public Flow<List<T>> groupedWithin(int n, Duration duration) {
        return groupedWeightedWithin(n, duration, _ -> 1L);
    }

    /**
     * Chunks up the emitted elements into groups, within a time window, or limited by the cumulative weight being greater or equal to the
     * `minWeight`, whatever happens first. The timeout is reset after a group is emitted. If timeout expires and the buffer is empty,
     * nothing is emitted. As soon as a new element is received, the flow will emit it as a single-element group and reset the timer.
     * <p>
     * The size of buffer used by this method is determined by {@link Channel#BUFFER_SIZE} that is in scope, or default {@link Channel#DEFAULT_BUFFER_SIZE} is used.
     *
     * @param minWeight The minimum cumulative weight of elements in a group if no timeout happens.
     * @param duration  The time window in which the elements are grouped.
     * @param costFn    The function that calculates the weight of an element.
     */
    @SuppressWarnings("unchecked")
    public Flow<List<T>> groupedWeightedWithin(long minWeight, Duration duration, Function<T, Long> costFn) {
        if (minWeight <= 0) throw new IllegalArgumentException("requirement failed: minWeight must be > 0");
        if (duration.toMillis() <= 0) throw new IllegalArgumentException("requirement failed: duration must be > 0");

        return usingEmit(emit -> {
            unsupervised(scope -> {
                Source<T> flowSource = runToChannel(scope);
                Channel<List<T>> outputChannel = Channel.withScopedBufferSize();
                Channel<GroupingTimeout> timerChannel = Channel.withScopedBufferSize();

                forkPropagate(scope, outputChannel, () -> {
                    List<T> buffer = new ArrayList<>();
                    final AtomicLong accumulatedCost = new AtomicLong(0);

                    CancellableFork<GroupingTimeout> timeoutFork = forkTimeout(scope, timerChannel, duration);

                    Callable<CancellableFork<Void>> sendBufferAndCleanupCost = () -> {
                        outputChannel.send(new ArrayList<>(buffer));
                        buffer.clear();
                        accumulatedCost.set(0);
                        return null;
                    };

                    boolean shouldRun = true;
                    while (shouldRun) {
                        shouldRun = switch (selectOrClosed(flowSource.receiveClause(), timerChannel.receiveClause())) {
                            case ChannelDone _:
                                // source is done, emit the buffer and finish
                                if (timeoutFork != null) timeoutFork.cancelNow();
                                if (!buffer.isEmpty()) outputChannel.send(buffer);
                                outputChannel.done();
                                yield false;
                            case ChannelError(Throwable cause, Channel<?> _):
                                // source returned error, propagate it and finish
                                if (timeoutFork != null) timeoutFork.cancelNow();
                                outputChannel.error(cause);
                                yield false;
                            case GroupingTimeout _:
                                timeoutFork = null; // enter 'timed out state', may stay in this state if buffer is empty
                                if (!buffer.isEmpty()) {
                                    sendBufferAndCleanupCost.call();
                                    // cancel existing timeout and start a new one
                                    timeoutFork = forkTimeout(scope, timerChannel, duration);
                                }
                                yield true;
                            case Object t:
                                buffer.add((T) t);
                                try {
                                    long cost = accumulatedCost.updateAndGet(v -> v + costFn.apply((T) t));
                                    if (timeoutFork == null || cost >= minWeight) {
                                        // timeout passed when buffer was empty or buffer full
                                        sendBufferAndCleanupCost.call();
                                        // cancel existing timeout and start a new one
                                        if (timeoutFork != null) timeoutFork.cancelNow();
                                        timeoutFork = forkTimeout(scope, timerChannel, duration);
                                    }
                                    yield true;
                                } catch (Exception e) {
                                    if (timeoutFork != null) timeoutFork.cancelNow();
                                    throw e;
                                }
                        };
                    }
                    return null;
                });
                FlowEmit.channelToEmit(outputChannel, emit);
                return null;
            });
        });
    }

    private CancellableFork<GroupingTimeout> forkTimeout(UnsupervisedScope scope, Channel<GroupingTimeout> timerChannel, Duration duration) {
        return scope.forkCancellable(() -> {
            sleep(duration);
            timerChannel.sendOrClosed(GroupingTimeout.INSTANCE);
            return null;
        });
    }

    private enum GroupingTimeout {
        INSTANCE
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
     * @param costFn    The function that calculates the weight of an element.
     */
    public Flow<List<T>> groupedWeighted(long minWeight, ThrowingFunction<T, Long> costFn) {
        if (minWeight <= 0) {
            throw new IllegalArgumentException("minWeight must be > 0");
        }

        return usingEmit(emit -> {
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
        return usingEmit(_ -> last.run(_ -> {}));
    }

    /**
     * Always runs `f` after the flow completes, whether it's because all elements are emitted, or when there's an error.
     */
    public Flow<T> onComplete(Runnable f) {
        return usingEmit(emit -> {
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
    public Flow<T> onDone(ThrowingRunnable f) {
        return usingEmit(emit -> {
            last.run(emit);
            f.run();
        });
    }

    /**
     * Runs `f` after the flow completes with an error. The error can't be recovered.
     */
    public Flow<T> onError(ThrowingConsumer<Throwable> f) {
        return usingEmit(emit -> {
            try {
                last.run(emit);
            } catch (Throwable e) {
                f.accept(e);
                throw e;
            }
        });
    }

    /**
     * Intersperses elements emitted by this flow with `inject` elements. The `inject` element is emitted between each pair of elements.
     */
    public Flow<T> intersperse(T inject) {
        return intersperse(Optional.empty(), inject, Optional.empty());
    }

    /**
     * Intersperses elements emitted by this flow with `inject` elements. The `start` element is emitted at the beginning; `end` is emitted
     * after the current flow emits the last element.
     *
     * @param start  An element to be emitted at the beginning.
     * @param inject An element to be injected between the flow elements.
     * @param end    An element to be emitted at the end.
     */
    public Flow<T> intersperse(T start, T inject, T end) {
        return intersperse(Optional.of(start), inject, Optional.of(end));
    }

    private Flow<T> intersperse(Optional<T> start, T inject, Optional<T> end) {
        return usingEmit(emit -> {
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
     * @param elements Number of elements to be emitted. Must be greater than 0.
     * @param per      Per time unit. Must be greater or equal to 1 ms.
     * @return A flow that emits at most `elements` `per` time unit.
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
     * @param f                   A predicate function called on incoming elements.
     * @param includeFirstFailing Whether the flow should also emit the first element that failed the predicate (`false` by default).
     */
    public Flow<T> takeWhile(Predicate<T> f, boolean includeFirstFailing) {
        return usingEmit(emit -> {
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
     * @param other The flow to be appended to this flow.
     */
    public Flow<T> concat(Flow<T> other) {
        return Flows.concat(this, other);
    }

    /**
     * Drops `n` elements from this flow and emits subsequent elements.
     *
     * @param n Number of elements to be dropped.
     */
    public Flow<T> drop(int n) {
        return usingEmit(emit -> {
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
     * <p>
     * Wraps exceptions from upstream and `other` in {@link ChannelErrorException} and {@link JoxScopeExecutionException} when flow is run.
     * <p>
     *
     * @param other              The flow to be merged with this flow.
     * @param propagateDoneLeft  Should the resulting flow complete when the left flow (`this`) completes, before the `other` flow.
     * @param propagateDoneRight Should the resulting flow complete when the right flow (`outer`) completes, before `this` flow.
     */
    public Flow<T> merge(Flow<T> other, boolean propagateDoneLeft, boolean propagateDoneRight) {
        return usingEmit(emit -> {
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

    /**
     * Prepends `other` flow to this source. The resulting flow will emit elements from `other` flow first, and then from this flow.
     *
     * @param other The flow to be prepended to this flow.
     */
    public Flow<T> prepend(Flow<T> other) {
        return other.concat(this);
    }

    /**
     * If this flow has no elements then elements from an `alternative` flow are emitted by the returned flow. If this flow is failed then
     * the returned flow is failed as well.
     *
     * @param alternative An alternative flow to be used when this flow is empty.
     */
    public Flow<T> orElse(Flow<T> alternative) {
        return usingEmit(emit -> {
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
     * @param other         The flow whose elements will be interleaved with the elements of this flow.
     * @param segmentSize   The number of elements sent from each flow before switching to the other one.
     * @param eagerComplete If `true`, the returned flow is completed as soon as either of the flow completes. If `false`, the remaining elements of the
     *                      non-completed flow are sent downstream.
     */
    public <U> Flow<U> interleave(Flow<U> other, int segmentSize, boolean eagerComplete, int bufferCapacity) {
        //noinspection unchecked
        return Flows.interleaveAll(Arrays.asList((Flow<U>) this, other), segmentSize, eagerComplete, bufferCapacity);
    }

    /**
     * Emits a given number of elements (determined by `segmentSize`) from this flow to the returned flow, then emits the same number of
     * elements from the `other` flow and repeats. The order of elements in both flows is preserved.
     * <p>
     * If one of the flows is done before the other, the behavior depends on the `eagerComplete` flag. When set to `true`, the returned flow is
     * completed immediately, otherwise the remaining elements from the other flow are emitted by the returned flow.
     * <p>
     * Both flows are run concurrently and asynchronously. The size of used buffer is determined by the {@link Channel#BUFFER_SIZE} that is in scope, or default {@link Channel#DEFAULT_BUFFER_SIZE} is used.
     *
     * @param other         The flow whose elements will be interleaved with the elements of this flow.
     * @param segmentSize   The number of elements sent from each flow before switching to the other one.
     * @param eagerComplete If `true`, the returned flow is completed as soon as either of the flow completes. If `false`, the remaining elements of the
     *                      non-completed flow are sent downstream.
     */
    public <U> Flow<U> interleave(Flow<U> other, int segmentSize, boolean eagerComplete) {
        //noinspection unchecked
        return Flows.interleaveAll(Arrays.asList((Flow<U>) this, other), segmentSize, eagerComplete);
    }

    /**
     * Applies the given mapping function `f`, to each element emitted by this source, transforming it into an `Iterable` of results,
     * then the returned flow emits the results one by one. Can be used to unfold incoming sequences of elements into single elements.
     *
     * @param f A function that transforms the element from this flow into an `Iterable` of results which are emitted one by one by the
     *          returned flow. If the result of `f` is empty, nothing is emitted by the returned channel.
     */
    public <U> Flow<U> mapConcat(ThrowingFunction<T, Iterable<U>> f) {
        return usingEmit(emit -> {
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
     * <p>
     * Wraps exceptions from `f` and upstream in {@link ChannelErrorException} and {@link JoxScopeExecutionException} when flow is run.
     * <p>
     *
     * @param parallelism An upper bound on the number of forks that run in parallel. Each fork runs the function `f` on a single element from the flow.
     * @param f           The mapping function.
     */
    public <U> Flow<U> mapPar(int parallelism, ThrowingFunction<T, U> f) {
        return usingEmit(emit -> {
            Semaphore semaphore = new Semaphore(parallelism);
            Channel<Fork<Optional<U>>> inProgress = new Channel<>(parallelism);
            Channel<U> results = Channel.withScopedBufferSize();

            // creating a nested scope, so that in case of errors, we can clean up any mapping forks in a "local" fashion,
            // that is without closing the main scope; any error management must be done in the forks, as the scope is
            // unsupervised
            unsupervised(scope -> {
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
                            case ChannelError(Throwable e, Channel<?> _) ->
                                    throw new IllegalStateException("inProgress should never be closed with an error", e);
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
     * <p>
     * Wraps exceptions from `f` and upstream in {@link ChannelErrorException} and {@link JoxScopeExecutionException} when flow is run.
     * <p>
     *
     * @param parallelism An upper bound on the number of forks that run in parallel. Each fork runs the function `f` on a single element from the flow.
     * @param f           The mapping function.
     */
    public <U> Flow<U> mapParUnordered(int parallelism, ThrowingFunction<T, U> f) {
        return usingEmit(emit -> {
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
     * @param n    The number of elements in a window.
     * @param step The number of elements the window slides by.
     */
    public Flow<List<T>> sliding(int n, int step) {
        if (n <= 0) throw new IllegalArgumentException("n must be > 0");
        if (step <= 0) throw new IllegalArgumentException("step must be > 0");

        return usingEmit(emit -> {
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
     * @param other The sink to which elements from this flow will be sent.
     * @see #alsoToTap for a version that drops elements when the `other` sink is not available for receive.
     */
    public Flow<T> alsoTo(Sink<T> other) {
        return usingEmit(emit -> {
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
     * @param other The sink to which elements from this source will be sent.
     * @see #alsoTo for a version that ensures that elements are emitted both by the returned flow and sent to the `other` sink.
     */
    public Flow<T> alsoToTap(Sink<T> other) {
        return usingEmit(emit -> {
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

    /**
     * Converts this {@link Flow} into a {@link Publisher}. The flow is run every time the publisher is subscribed to.
     * <p>
     * Must be run within a concurrency scope, as upon subscribing, a fork is created to run the publishing process. Hence, the scope should
     * remain active as long as the publisher is used.
     * <p>
     * Elements emitted by the flow are buffered, using a buffer of capacity given by the {@link Channel#BUFFER_SIZE} in scope or default value {@link Channel#DEFAULT_BUFFER_SIZE} is used.
     * <p>
     * The returned publisher implements the JDK 9+ {@code Flow.Publisher} API.
     */
    public Publisher<T> toPublisher(Scope scope) {
        return new FromFlowPublisher<>(scope, last);
    }

    // endregion

    // region ByteFlow
    public interface ByteChunkMapper<T> extends ThrowingFunction<T, ByteChunk> {}

    public interface ByteArrayMapper<T> extends ThrowingFunction<T, byte[]> {}

    /**
     * Converts a flow of `byte[]` or {@link ByteChunk} into a dedicated Flow type {@link ByteFlow}.
     *
     * @param args This param should *NOT* be passed. It's only used to verify that this flow contains byte[] or {@link ByteChunk}.
     * @throws IllegalArgumentException if the flow does not contain `byte[]` or {@link ByteChunk} elements.
     */
    @SafeVarargs
    public final ByteFlow toByteFlow(T... args) {
        return new ByteFlow(last, getTClass(args));
    }

    /**
     * Allows to convert given flow into a {@link ByteFlow} by providing a mapping function that converts elements of the flow into {@link ByteChunk}.
     */
    public ByteFlow toByteFlow(ByteChunkMapper<T> f) {
        return new ByteFlow(map(f).last, ByteChunk.class);
    }

    /**
     * Allows to convert given flow into a {@link ByteFlow} by providing a mapping function that converts elements of the flow into `byte[]`.
     */
    public ByteFlow toByteFlow(ByteArrayMapper<T> f) {
        return new ByteFlow(map(f).last, byte[].class);
    }

    /**
     * Encodes a flow of `String` into a flow of bytes using UTF-8.
     */
    public ByteFlow encodeUtf8() {
        Flow<ByteChunk> flow = map(s -> {
            if (s instanceof String string) {
                return ByteChunk.fromArray(string.getBytes(StandardCharsets.UTF_8));
            }
            throw new IllegalArgumentException("requirement failed: method can be called only on flow containing String");
        });
        return new ByteFlow(flow.last, ByteChunk.class);
    }

    /**
     * Subclass dedicated for flow containing ByteChunk
     */
    public static class ByteFlow extends Flow<ByteChunk> {

        private <T> ByteFlow(FlowStage<T> last, Class<T> clazz) {
            super(getLast(last, clazz));
        }

        private static <T> FlowStage<ByteChunk> getLast(FlowStage<T> last, Class<T> clazz) {
            if (ByteChunk.class.equals(clazz)) {
                //noinspection unchecked
                return (FlowStage<ByteChunk>) last;
            } else if (byte[].class.equals(clazz)) {
                return new Flow<>(last).map(t -> ByteChunk.fromArray((byte[]) t)).last;
            } else {
                throw new IllegalArgumentException("requirement failed: ByteFlow can only be created from ByteChunk or byte[]");
            }
        }

        /**
         * Transforms a ByteFlow such that each emitted `String` is a text line from the input.
         *
         * @param charset the charset to use for decoding the bytes into text.
         * @return a flow emitting lines read from the input byte arrays, assuming they represent text.
         */
        public Flow<String> lines(Charset charset) {
            return LinesImpl.lines(charset, this);
        }

        /**
         * Transforms a ByteFlow such that each emitted `String` is a text line from the input decoded using UTF-8 charset.
         *
         * @return a flow emitting lines read from the input byte chunks, assuming they represent text.
         */
        public Flow<String> linesUtf8() {
            return lines(StandardCharsets.UTF_8);
        }


        /**
         * Decodes a stream of chunks of bytes into UTF-8 Strings. This function is able to handle UTF-8 characters encoded on multiple bytes
         * that are split across chunks.
         *
         * @return a flow of Strings decoded from incoming bytes.
         */
        public Flow<String> decodeStringUtf8() {
            return ChunksUtf8Decoder.decodeStringUtf8(last);
        }

        /**
         * Runs the flow into a {@link java.io.InputStream}.
         * <p>
         * Must be run within a concurrency scope, as under the hood the flow is run in the background.
         * <p>
         * Buffer capacity can be set via scoped value {@link Channel#BUFFER_SIZE}. If not specified in scope, {@link Channel#DEFAULT_BUFFER_SIZE} is used.
         */
        public InputStream runToInputStream(UnsupervisedScope scope) {
            Source<ByteChunk> ch = this
                    .runToChannel(scope);

            return new InputStream() {
                private ByteArrayIterator currentChunk = ByteArrayIterator.empty();

                @Override
                public int read() {
                    try {
                        if (!currentChunk.hasNext()) {
                            Object result = ch.receiveOrClosed();
                            if (result instanceof ChannelDone) {
                                return -1;
                            } else if (result instanceof ChannelError error) {
                                throw error.toException();
                            } else {
                                var chunk = (ByteChunk) result;
                                currentChunk = new ByteArrayIterator(chunk.toArray());
                            }
                        }
                        return currentChunk.next() & 0xff; // Convert to unsigned
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public int available() {
                    return currentChunk.available();
                }
            };
        }


        /**
         * Writes content of this flow to an {@link java.io.OutputStream}.
         *
         * @param outputStream Target `OutputStream` to write to. Will be closed after finishing the process or on error.
         */
        public void runToOutputStream(OutputStream outputStream) throws Exception {
            try {
                last.run(t -> outputStream.write(t.toArray()));
                close(outputStream, null);
            } catch (Exception e) {
                close(outputStream, e);
                throw e;
            }
        }

        /**
         * Writes content of this flow to a file.
         *
         * @param path Path to the target file. If not exists, it will be created.s
         */
        public void runToFile(Path path) throws Exception {
            if (Files.isDirectory(path)) {
                throw new IOException("Path %s is a directory".formatted(path));
            }
            final SeekableByteChannel channel = getFileChannel(path);
            try {
                runForeach(chunk -> {
                    try {
                        channel.write(ByteBuffer.wrap(chunk.toArray()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                close(channel, null);
            } catch (Exception t) {
                close(channel, t);
                throw t;
            }
        }

        private SeekableByteChannel getFileChannel(Path path) throws IOException {
            try {
                return FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            } catch (UnsupportedOperationException e) {
                // Some file systems don't support file channels
                return Files.newByteChannel(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            }
        }

        private void close(AutoCloseable closeable, Exception cause) throws Exception {
            try {
                closeable.close();
            } catch (IOException e) {
                if (cause != null) {
                    cause.addSuppressed(e);
                }
                throw cause != null ? cause : e;
            }
        }
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

    private <U> Fork<Optional<U>> forkMapping(UnsupervisedScope scope, ThrowingFunction<T, U> f, Semaphore s, T value, Sink<U> results) {
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

    @SuppressWarnings("unchecked")
    private static <T> Class<T> getTClass(T[] args) {
        if (args.length > 0) {
            throw new IllegalArgumentException("Please do not pass any arguments for this method. Java will detect the type automatically.");
        }
        return (Class<T>) args.getClass().getComponentType();
    }

    private static class BreakException extends RuntimeException {
    }
}
