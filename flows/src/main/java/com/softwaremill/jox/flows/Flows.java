package com.softwaremill.jox.flows;

import static com.softwaremill.jox.structured.Scopes.unsupervised;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import com.softwaremill.jox.Channel;
import com.softwaremill.jox.ChannelClosed;
import com.softwaremill.jox.ChannelDone;
import com.softwaremill.jox.ChannelError;
import com.softwaremill.jox.Source;
import com.softwaremill.jox.structured.Fork;
import com.softwaremill.jox.structured.Scopes;

public final class Flows {

    private Flows() {}

    /**
     * Creates a flow, which when run, provides a {@link FlowEmit} instance to the given `withEmit` function. Elements can be emitted to be
     * processed by downstream stages by calling {@link FlowEmit#apply}.
     * <p>
     * The {@link FlowEmit} instance provided to the {@param withEmit} callback should only be used on the calling thread.
     * That is, {@link FlowEmit} is thread-unsafe. Moreover, the instance should not be stored or captured in closures, which outlive the invocation of {@param withEmit}.
     */
    public static <T> Flow<T> usingEmit(ThrowingConsumer<FlowEmit<T>> withEmit) {
        return new Flow<>(withEmit::accept);
    }

    /**
     * Creates a flow using the given {@param source}. An element is emitted for each value received from the source.
     * If the source is completed with an error, is it propagated by throwing.
     */
    public static <T> Flow<T> fromSource(Source<T> source) {
        return new Flow<>(new SourceBackedFlowStage<>(source));
    }

    /**
     * Creates a flow from the given `iterable`. Each element of the iterable is emitted in order.
     */
    public static <T> Flow<T> fromIterable(Iterable<T> iterable) {
        return fromIterator(iterable.iterator());
    }

    /**
     * Creates a flow from the given values. Each value is emitted in order.
     */
    @SafeVarargs
    public static <T> Flow<T> fromValues(T... ts) {
        return fromIterator(Arrays.asList(ts).iterator());
    }

    /**
     * Creates a flow from the given (lazily evaluated) `iterator`. Each element of the iterator is emitted in order.
     */
    public static <T> Flow<T> fromIterator(Iterator<T> it) {
        return usingEmit(emit -> {
            while (it.hasNext()) {
                emit.apply(it.next());
            }
        });
    }

    /**
     * Creates a flow from the given fork. The flow will emit up to one element, or complete by throwing an exception if the fork fails.
     */
    public static <T> Flow<T> fromFork(Fork<T> f) {
        return usingEmit(emit -> emit.apply(f.join()));
    }

    /**
     * Creates a flow which emits elements starting with `zero`, and then applying `mappingFunction` to the previous element to get the next one.
     */
    public static <T> Flow<T> iterate(T zero, Function<T, T> mappingFunction) {
        return usingEmit(emit -> {
            T t = zero;
            //noinspection InfiniteLoopStatement
            while (true) {
                emit.apply(t);
                t = mappingFunction.apply(t);
            }
        });
    }

    /**
     * Creates a flow which emits a range of numbers, from `from`, to `to` (inclusive), stepped by `step`.
     */
    public static Flow<Integer> range(int from, int to, int step) {
        // do nothing
        return usingEmit(emit -> {
            for (int i = from; i <= to; i += step) {
                emit.apply(i);
            }
        });
    }

    /**
     * Creates a flow which emits the given `value` repeatedly, at least {@param interval} apart between each two elements.
     * The first value is emitted immediately.
     * <p>
     * The interval is measured between subsequent emissions. Hence, if the following transformation pipeline is slow, the next emission can
     * occur immediately after the previous one is fully processed (if processing took more than the inter-emission interval duration).
     * However, ticks do not accumulate; for example, if processing is slow enough that multiple intervals pass between send invocations,
     * only one tick will be sent.
     *
     * @param interval
     *   The temporal spacing between subsequent ticks.
     * @param value
     *   The element to emitted on every tick.
     */
    public static <T> Flow<T> tick(Duration interval, T value) {
        return usingEmit(emit -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                long start = System.nanoTime();
                emit.apply(value);
                long end = System.nanoTime();
                long sleep = interval.toNanos() - (end - start);
                if (sleep > 0) {
                    //noinspection BusyWait
                    Thread.sleep(TimeUnit.NANOSECONDS.toMillis(sleep), (int) (sleep % 1_000_000));
                }
            }
        });
    }

    /** Creates a flow, which emits the given `element` repeatedly. */
    public static <T> Flow<T> repeat(T element) {
        return repeatEval(() -> element);
    }

    /** Creates a flow, which emits the result of evaluating `supplierFunction` repeatedly. As the parameter is passed by-name, the evaluation is deferred
     * until the element is emitted, and happens multiple times.
     *
     * @param supplierFunction
     *   The code block, computing the element to emit.
     */
    public static <T> Flow<T> repeatEval(Supplier<T> supplierFunction) {
        return usingEmit(emit -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                emit.apply(supplierFunction.get());
            }
        });
    }

    /** Creates a flow, which emits the value contained in the result of evaluating `supplierFunction` repeatedly.
     *  When the evaluation of `supplierFunction` returns a {@link Optional#empty()}, the flow is completed as "done", and no more values are evaluated or emitted.
     * <p>
     * As the `supplierFunction` parameter is passed by-name, the evaluation is deferred until the element is emitted, and happens multiple times.
     *
     * @param supplierFunction
     *   The code block, computing the optional element to emit.
     */
    public static <T> Flow<T> repeatEvalWhileDefined(Supplier<Optional<T>> supplierFunction) {
        // do nothing
        return usingEmit(emit -> {
            boolean shouldRun = true;
            while (shouldRun) {
                Optional<T> result = supplierFunction.get();
                if (result.isPresent()) {
                    emit.apply(result.get());
                } else {
                    shouldRun = false;
                }
            }
        });
    }

    /** Create a flow which sleeps for the given `timeout` and then completes as done. */
    public static <T> Flow<T> timeout(Duration timeout) {
        return usingEmit(_ -> Thread.sleep(timeout.toMillis()));
    }

    /**
     * Creates a flow which concatenates the given `flows` in order. First elements from the first flow are emitted, then from the second etc.
     * If any of the flows completes with an error, it is propagated.
     */
    @SafeVarargs
    public static <T> Flow<T> concat(Flow<T>... flows) {
        return usingEmit(emit -> {
            for (Flow<T> currentFlow : flows) {
                currentFlow.runToEmit(emit);
            }
        });
    }

    /** Creates an empty flow, which emits no elements and completes immediately. */
    public static <T> Flow<T> empty() {
        return usingEmit(_ -> {});
    }

    /** Creates a flow that emits a single element when `from` completes, or throws an exception when `from` fails. */
    public static <T> Flow<T> fromCompletableFuture(CompletableFuture<T> from) {
        return usingEmit(emit -> {
            emit.apply(from.get());
        });
    }

    /**
     * Creates a flow that emits all elements from the given future {@link Source} when `from` completes.
     *
     * @param from the future source to emit elements from
     */
    public static <T> Flow<T> fromFutureSource(CompletableFuture<Source<T>> from) {
        return fromSource(from.join());
    }

    /**
     * Creates a flow that fails immediately with the given {@link java.lang.Exception}
     *
     * @param t
     *   The {@link java.lang.Exception} to fail with
     */
    public static <T> Flow<T> failed(Exception t) {
        return usingEmit(_ -> {
            throw t;
        });
    }

    /**
     * Creates a flow which emits the first element ({@link Map.Entry#getKey()}) of entries returned by repeated applications of `f`.
     * The `initial` state is used for the first application, and then the state is updated with the second element of the entry. Emission stops when `f` returns {@link Optional#empty()},
     * otherwise it continues indefinitely.
     */
    public static <S, T>  Flow<T> unfold(S initial, Function<S, Optional<Map.Entry<T, S>>> f) {
        return usingEmit(emit -> {
            S s = initial;
            while (true) {
                var result = f.apply(s);
                if (result.isEmpty()) {
                    break;
                }
                Map.Entry<T, S> entry = result.get();
                emit.apply(entry.getKey());
                s = entry.getValue();
            }
        });
    }

    /** Creates a Flow from a Publisher, that is, which emits the elements received by subscribing to the publisher. A new
     * subscription is created every time this flow is run.
     * <p>
     * The data is passed from a subscription to the flow using a Channel, with a capacity given by the {@link Channel#BUFFER_SIZE} in
     * scope or {@link Channel#DEFAULT_BUFFER_SIZE} is used. That's also how many elements will be at most requested from the publisher at a time.
     * <p>
     * The publisher parameter should implement the JDK 9+ Flow.Publisher API
     */
    public static <T> Flow<T> fromPublisher(Publisher<T> p) {
        return usingEmit(emit -> {
            // using an unsafe scope for efficiency
            Scopes.unsupervised(scope -> {
                Channel<T> channel = Channel.withScopedBufferSize();
                int capacity = Channel.BUFFER_SIZE.orElse(Channel.DEFAULT_BUFFER_SIZE);
                int demandThreshold = (int) Math.ceil(capacity / 2.0);

                // used to "extract" the subscription that is set in the subscription running in a fork
                AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();
                Subscription subscription = null;

                int toDemand = 0;

                try {
                    // unsafe, but we are sure that this won't throw any exceptions (unless there's a bug in the publisher)
                    scope.forkUnsupervised(() -> {
                        p.subscribe(new Subscriber<T>() {
                            @Override
                            public void onSubscribe(Subscription s) {
                                subscriptionRef.set(s);
                                s.request(capacity);
                            }

                            @Override
                            public void onNext(T t) {
                                try {
                                    channel.send(t);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                channel.error(t);
                            }

                            @Override
                            public void onComplete() {
                                channel.done();
                            }
                        });
                        return null;
                    });

                    while (true) {
                        Object t = channel.receiveOrClosed();
                        if (t instanceof ChannelDone) {
                            break;
                        } else if (t instanceof ChannelError error) {
                            throw error.toException();
                        } else {
                            //noinspection unchecked
                            emit.apply((T) t);

                            // if we have an element, onSubscribe must have already happened; we can read the subscription and cache it for later
                            if (subscription == null) {
                                subscription = subscriptionRef.get();
                            }

                            // now that we've received an element from the channel, we can request more
                            toDemand += 1;
                            // we request in batches, to avoid too many requests
                            if (toDemand >= demandThreshold) {
                                subscription.request(toDemand);
                                toDemand = 0;
                            }
                        }
                    }
                    // exceptions might be propagated from the channel, but they might also originate from an interruption
                    return null;
                } catch (Exception e) {
                    Subscription s = subscriptionRef.get();
                    if (s != null) {
                        s.cancel();
                    }
                    throw e;
                }
            });
        });
    }

    /**
     * Sends a given number of elements (determined by `segmentSize`) from each flow in `flows` to the returned flow and repeats. The order
     * of elements in all flows is preserved.
     * <p>
     * If any of the flows is done before the others, the behavior depends on the `eagerComplete` flag. When set to `true`, the returned flow
     * is completed immediately, otherwise the interleaving continues with the remaining non-completed flows. Once all but one flows are
     * complete, the elements of the remaining non-complete flow are emitted by the returned flow.
     * <p>
     * The provided flows are run concurrently and asynchronously. The size of used buffer is determined by the {@link Channel#BUFFER_SIZE} that is in scope, or default {@link Channel#DEFAULT_BUFFER_SIZE} is used.
     *
     * @param flows
     *   The flows whose elements will be interleaved.
     * @param segmentSize
     *   The number of elements sent from each flow before switching to the next one.
     * @param eagerComplete
     *   If `true`, the returned flow is completed as soon as any of the flows completes. If `false`, the interleaving continues with the
     *   remaining non-completed flows.
     */
    public static <T> Flow<T> interleaveAll(List<Flow<T>> flows, int segmentSize, boolean eagerComplete) {
        return interleaveAll(flows, segmentSize, eagerComplete, Channel.BUFFER_SIZE.orElse(Channel.DEFAULT_BUFFER_SIZE));
    }

    /**
     * Sends a given number of elements (determined by `segmentSize`) from each flow in `flows` to the returned flow and repeats. The order
     * of elements in all flows is preserved.
     * <p>
     * If any of the flows is done before the others, the behavior depends on the `eagerComplete` flag. When set to `true`, the returned flow
     * is completed immediately, otherwise the interleaving continues with the remaining non-completed flows. Once all but one flows are
     * complete, the elements of the remaining non-complete flow are emitted by the returned flow.
     * <p>
     * The provided flows are run concurrently and asynchronously.
     *
     * @param flows
     *   The flows whose elements will be interleaved.
     * @param segmentSize
     *   The number of elements sent from each flow before switching to the next one.
     * @param eagerComplete
     *   If `true`, the returned flow is completed as soon as any of the flows completes. If `false`, the interleaving continues with the
     *   remaining non-completed flows.
     */
    public static <T> Flow<T> interleaveAll(List<Flow<T>> flows, int segmentSize, boolean eagerComplete, int bufferCapacity) {
        if (flows.isEmpty()) {
            return Flows.empty();
        } else if (flows.size() == 1) {
            return flows.getFirst();
        } else {
            return usingEmit(emit -> {
                Channel<T> results = new Channel<>(bufferCapacity);
                unsupervised(scope -> {
                    scope.forkUnsupervised(() -> {
                        List<Source<T>> availableSources = new ArrayList<>(flows.stream()
                                .map(flow -> flow.runToChannel(scope))
                                .toList());
                        int currentSourceIndex = 0;
                        int elementsRead = 0;

                        while (true) {
                            var received = availableSources.get(currentSourceIndex).receiveOrClosed();
                            if (received instanceof ChannelDone) {
                                ///  channel is done, remove it from the list of available sources
                                availableSources.remove(currentSourceIndex);
                                currentSourceIndex = currentSourceIndex == 0 ? availableSources.size() - 1 : currentSourceIndex - 1;

                                // if all sources are done, or eagerComplete break the loop
                                if (eagerComplete || availableSources.isEmpty()) {
                                    results.doneOrClosed();
                                    break;
                                } else {
                                    // switch to the next source
                                    currentSourceIndex = (currentSourceIndex + 1) % availableSources.size();
                                    elementsRead = 0;
                                }
                            } else if (received instanceof ChannelError(Throwable cause)) {
                                // if any source fails, propagate the error
                                results.errorOrClosed(cause);
                                break;
                            } else {
                                elementsRead++;

                                // switch to the next source when segmentSize is reached and there are more sources available
                                if (elementsRead == segmentSize && availableSources.size() > 1) {
                                    currentSourceIndex = (currentSourceIndex + 1) % availableSources.size();
                                    elementsRead = 0;
                                }
                                //noinspection unchecked
                                Object result = results.sendOrClosed((T) received);
                                if (result instanceof ChannelClosed) {
                                    break;
                                }
                            }
                        }
                        return null;
                    });
                    FlowEmit.channelToEmit(results, emit);
                    return null;
                });
            });
        }
    }

    /**
     * Converts a {@link java.io.InputStream} into a `Flow<byte[]>`.
     *
     * @param is
     *   an `InputStream` to read bytes from.
     * @param chunkSize
     *   maximum number of bytes to read from the underlying `InputStream` before emitting a new chunk.
     */
    public static Flow<byte[]> fromInputStream(InputStream is, int chunkSize) {
        return usingEmit(emit -> {
            try (is) {
                while (true) {
                    byte[] buf = new byte[chunkSize];
                    int readBytes = is.read(buf);
                    if (readBytes == -1) {
                        break;
                    } else {
                        if (readBytes > 0) {
                            emit.apply(readBytes == chunkSize ? buf : Arrays.copyOf(buf, readBytes));
                        }
                    }
                }
            }
        });
    }

    /**
     * Creates a flow that emits byte chunks read from a file.
     *
     * @param path
     *   path the file to read from.
     * @param chunkSize
     *   maximum number of bytes to read from the file before emitting a new chunk.
     */
    public static Flow<byte[]> fromFile(Path path, int chunkSize) {
        return usingEmit(emit -> {
            if (Files.isDirectory(path)) {
                throw new IOException("Path %s is a directory".formatted(path));
            }
            SeekableByteChannel fileChannel;
            try {
                fileChannel = FileChannel.open(path, StandardOpenOption.READ);
            } catch (UnsupportedOperationException e) {
                // Some file systems don't support file channels
                fileChannel = Files.newByteChannel(path, StandardOpenOption.READ);
            }

            try {
                while (true) {
                    ByteBuffer buf = ByteBuffer.allocate(chunkSize);
                    int readBytes = fileChannel.read(buf);
                    if (readBytes < 0) {
                        break;
                    } else {
                        if (readBytes > 0) {
                            byte[] byteArray = new byte[readBytes];
                            buf.flip();
                            buf.get(byteArray, 0, readBytes);
                            emit.apply(byteArray);
                        }
                    }
                }
            } finally {
                fileChannel.close();
            }
        });
    }
}
