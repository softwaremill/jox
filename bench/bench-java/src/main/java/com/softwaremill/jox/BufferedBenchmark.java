package com.softwaremill.jox;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

/** Buffered tests for {@link ArrayBlockingQueue} and {@link Channel}. */
@Warmup(iterations = 3, time = 3000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 3000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class BufferedBenchmark {
    @Param({"16", "100"})
    public int capacity;

    // going against jmh's best practises, the benchmarks are "iterative" (not using groups), for
    // two reasons:
    // (1) direct comparison w/ Kotlin, as we can't write a group-based benchmark there, due to
    // suspended functions
    // (2) the more complex benchmarks (which use higher numbers of threads) need to be enclosed in
    // a single method anyway

    private static final int OPERATIONS_PER_INVOCATION = 1_000_000;

    @Benchmark
    @OperationsPerInvocation(OPERATIONS_PER_INVOCATION)
    public void arrayBlockingQueue() throws InterruptedException {
        var queue = new ArrayBlockingQueue<>(capacity);
        var t1 =
                Thread.startVirtualThread(
                        () -> {
                            for (int i = 0; i < OPERATIONS_PER_INVOCATION; i++) {
                                try {
                                    queue.put(63);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });

        var t2 =
                Thread.startVirtualThread(
                        () -> {
                            for (int i = 0; i < OPERATIONS_PER_INVOCATION; i++) {
                                try {
                                    queue.take();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });

        t1.join();
        t2.join();
    }

    @Benchmark
    @OperationsPerInvocation(OPERATIONS_PER_INVOCATION)
    public void channel() throws InterruptedException {
        var ch = Channel.newBufferedChannel(capacity);
        var t1 =
                Thread.startVirtualThread(
                        () -> {
                            for (int i = 0; i < OPERATIONS_PER_INVOCATION; i++) {
                                try {
                                    ch.send(63);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });

        var t2 =
                Thread.startVirtualThread(
                        () -> {
                            for (int i = 0; i < OPERATIONS_PER_INVOCATION; i++) {
                                try {
                                    ch.receive();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });

        t1.join();
        t2.join();
    }
}
