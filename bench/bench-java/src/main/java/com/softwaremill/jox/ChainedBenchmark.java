package com.softwaremill.jox;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Chained send-receive test for {@link Channel} - a series of threads proxying values to subsequent channels.
 */
@Warmup(iterations = 3, time = 5000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 5000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Group)
public class ChainedBenchmark {
    @Param({"0", "100"})
    public int capacity;

    @Param({"100", "1000", "10000"})
    public int channelCount;

    private final static int OPERATIONS_PER_INVOCATION = 10_000_000;

    @Benchmark
    @OperationsPerInvocation(OPERATIONS_PER_INVOCATION)
    @Group("chain_iterative")
    public void sendReceiveInChain() throws InterruptedException {
        // we want to measure the amount of time a send-receive pair takes
        int elements = OPERATIONS_PER_INVOCATION / channelCount;
        Channel<Integer>[] channels = new Channel[channelCount];
        for (int i = 0; i < channelCount; i++) {
            channels[i] = new Channel<>(capacity);
        }

        Thread[] threads = new Thread[channelCount + 1];
        threads[0] = Thread.startVirtualThread(() -> {
            var ch = channels[0];
            for (int i = 0; i < elements; i++) {
                try {
                    ch.send(63);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        for (int t = 1; t < channelCount; t++) {
            int finalT = t;
            threads[t] = Thread.startVirtualThread(() -> {
                var ch1 = channels[finalT - 1];
                var ch2 = channels[finalT];
                for (int i = 0; i < elements; i++) {
                    try {
                        ch2.send(ch1.receive());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        threads[channelCount] = Thread.startVirtualThread(() -> {
            var ch = channels[channelCount - 1];
            for (int i = 0; i < elements; i++) {
                try {
                    ch.receive();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        for (Thread thread : threads) {
            thread.join();
        }
    }
}
