package com.softwaremill.jox;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Send-receive test for {@link Channel} and {@link BlockingQueue} - a number of (send, receive) thread pairs,
 * sending/receiving on a dedicated channel.
 */
@Warmup(iterations = 3, time = 4000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 4000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class ParallelBenchmark {
    @Param({"0", "100"})
    public int capacity;

    @Param({"10000"})
    public int parallelism;

    private final static int OPERATIONS_PER_INVOCATION = 10_000_000;

    @Benchmark
    @OperationsPerInvocation(OPERATIONS_PER_INVOCATION)
    public void parallelChannels() throws InterruptedException {
        // we want to measure the amount of time a send-receive pair takes
        int elements = OPERATIONS_PER_INVOCATION / parallelism;
        Channel<Integer>[] channels = new Channel[parallelism];
        for (int i = 0; i < parallelism; i++) {
            channels[i] = new Channel<>(capacity);
        }

        Thread[] threads = new Thread[parallelism * 2];

        // senders
        for (int t = 0; t < parallelism; t++) {
            int finalT = t;
            threads[t] = Thread.startVirtualThread(() -> {
                var ch = channels[finalT];
                for (int i = 0; i < elements; i++) {
                    try {
                        ch.send(91);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        // receivers
        for (int t = 0; t < parallelism; t++) {
            int finalT = t;
            threads[t + parallelism] = Thread.startVirtualThread(() -> {
                var ch = channels[finalT];
                for (int i = 0; i < elements; i++) {
                    try {
                        ch.receive();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        for (Thread thread : threads) {
            thread.join();
        }
    }

    @Benchmark
    @OperationsPerInvocation(OPERATIONS_PER_INVOCATION)
    public void parallelQueues() throws InterruptedException {
        // we want to measure the amount of time a send-receive pair takes
        int elements = OPERATIONS_PER_INVOCATION / parallelism;
        BlockingQueue<Integer>[] queues = new BlockingQueue[parallelism];
        if (capacity == 0) {
            for (int i = 0; i < parallelism; i++) {
                queues[i] = new SynchronousQueue<>();
            }
        } else {
            for (int i = 0; i < parallelism; i++) {
                queues[i] = new ArrayBlockingQueue<>(capacity);
            }
        }

        Thread[] threads = new Thread[parallelism * 2];

        // senders
        for (int t = 0; t < parallelism; t++) {
            int finalT = t;
            threads[t] = Thread.startVirtualThread(() -> {
                var q = queues[finalT];
                for (int i = 0; i < elements; i++) {
                    try {
                        q.put(91);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        // receivers
        for (int t = 0; t < parallelism; t++) {
            int finalT = t;
            threads[t + parallelism] = Thread.startVirtualThread(() -> {
                var q = queues[finalT];
                for (int i = 0; i < elements; i++) {
                    try {
                        q.take();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        for (Thread thread : threads) {
            thread.join();
        }
    }
}
