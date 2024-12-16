package com.softwaremill.jox.flows;

import com.softwaremill.jox.Source;
import com.softwaremill.jox.structured.Fork;

import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

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
        return usingEmit(emit -> Thread.sleep(timeout.toMillis()));
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
        return usingEmit(emit -> {});
    }

    /** Creates a flow that emits a single element when `from` completes, or throws an exception when `from` fails. */
    public static <T> Flow<T> fromCompletableFuture(CompletableFuture<T> from) {
        return usingEmit(emit -> {
            try {
                emit.apply(from.get());
            } catch (ExecutionException e) {
                throw (Exception) e.getCause();
            }
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
        return usingEmit(emit -> {
            throw t;
        });
    }
}