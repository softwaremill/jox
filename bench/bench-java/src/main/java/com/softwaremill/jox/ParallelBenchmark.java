package com.softwaremill.jox;

import java.util.concurrent.*;

import org.openjdk.jmh.annotations.*;

/**
 * Send-receive test for {@link Channel} and {@link BlockingQueue} - a number of (send, receive)
 * thread pairs, sending/receiving on a dedicated channel.
 */
@Warmup(iterations = 3, time = 4000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 4000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class ParallelBenchmark {
    @Param({"0", "16", "100"})
    public int capacity;

    @Param({"10000"})
    public int parallelism;

    private static final int OPERATIONS_PER_INVOCATION = 10_000_000;

    @Benchmark
    @OperationsPerInvocation(OPERATIONS_PER_INVOCATION)
    public void parallelChannels() throws InterruptedException {
        // we want to measure the amount of time a send-receive pair takes
        int elementsPerChannel = OPERATIONS_PER_INVOCATION / parallelism;

        var latch = new CountDownLatch(parallelism);

        for (int t = 0; t < parallelism; t++) {
            var ch =
                    (capacity == 0)
                            ? Channel.<Integer>newRendezvousChannel()
                            : Channel.<Integer>newBufferedChannel(capacity);
            // sender
            Thread.startVirtualThread(
                    () -> {
                        for (int i = 0; i < elementsPerChannel; i++) {
                            try {
                                ch.send(91);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

            // receiver
            Thread.startVirtualThread(
                    () -> {
                        for (int i = 0; i < elementsPerChannel; i++) {
                            try {
                                ch.receive();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        latch.countDown();
                    });
        }

        latch.await();
    }

    @Benchmark
    @OperationsPerInvocation(OPERATIONS_PER_INVOCATION)
    public void parallelQueues() throws InterruptedException {
        // we want to measure the amount of time a send-receive pair takes
        int elementsPerChannel = OPERATIONS_PER_INVOCATION / parallelism;

        var latch = new CountDownLatch(parallelism);

        for (int t = 0; t < parallelism; t++) {
            BlockingQueue<Integer> q;
            if (capacity == 0) {
                q = new SynchronousQueue<>();
            } else {
                q = new ArrayBlockingQueue<>(capacity);
            }

            // sender
            Thread.startVirtualThread(
                    () -> {
                        for (int i = 0; i < elementsPerChannel; i++) {
                            try {
                                q.put(91);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

            // receiver
            Thread.startVirtualThread(
                    () -> {
                        for (int i = 0; i < elementsPerChannel; i++) {
                            try {
                                q.take();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        latch.countDown();
                    });
        }

        latch.await();
    }
}
