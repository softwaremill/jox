package com.softwaremill.jox;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Chained send-receive test for {@link Channel} and {@link BlockingQueue} - a series of threads proxying values to subsequent channels/queues.
 */
@Warmup(iterations = 3, time = 4000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 4000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class ChainedBenchmark {
    @Param({"0", "100"})
    public int capacity;

    @Param({"100", "1000", "10000"})
    public int chainLength;

    private final static int OPERATIONS_PER_INVOCATION = 10_000_000;

    @Benchmark
    @OperationsPerInvocation(OPERATIONS_PER_INVOCATION)
    public void channelChain() throws InterruptedException {
        // we want to measure the amount of time a send-receive pair takes
        int elements = OPERATIONS_PER_INVOCATION / chainLength;
        Channel<Integer>[] channels = new Channel[chainLength];
        for (int i = 0; i < chainLength; i++) {
            channels[i] = new Channel<>(capacity);
        }

        Thread[] threads = new Thread[chainLength + 1];
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

        for (int t = 1; t < chainLength; t++) {
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

        threads[chainLength] = Thread.startVirtualThread(() -> {
            var ch = channels[chainLength - 1];
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

    @Benchmark
    @OperationsPerInvocation(OPERATIONS_PER_INVOCATION)
    public void queueChain() throws InterruptedException {
        // we want to measure the amount of time a send-receive pair takes
        int elements = OPERATIONS_PER_INVOCATION / chainLength;
        BlockingQueue<Integer>[] queues = new BlockingQueue[chainLength];
        if (capacity == 0) {
            for (int i = 0; i < chainLength; i++) {
                queues[i] = new SynchronousQueue<>();
            }
        } else {
            for (int i = 0; i < chainLength; i++) {
                queues[i] = new ArrayBlockingQueue<>(capacity);
            }
        }

        Thread[] threads = new Thread[chainLength + 1];
        threads[0] = Thread.startVirtualThread(() -> {
            var q = queues[0];
            for (int i = 0; i < elements; i++) {
                try {
                    q.put(63);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        for (int t = 1; t < chainLength; t++) {
            int finalT = t;
            threads[t] = Thread.startVirtualThread(() -> {
                var q1 = queues[finalT - 1];
                var q2 = queues[finalT];
                for (int i = 0; i < elements; i++) {
                    try {
                        q2.put(q1.take());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        threads[chainLength] = Thread.startVirtualThread(() -> {
            var q = queues[chainLength - 1];
            for (int i = 0; i < elements; i++) {
                try {
                    q.take();
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
