package com.softwaremill.jox;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.Exchanger;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Rendezvous tests for {@link SynchronousQueue}, {@link Exchanger} and {@link Channel}.
 */
@Warmup(iterations = 3, time = 4000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 4000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class RendezvousBenchmark {
    private final static int OPERATIONS_PER_INVOCATION = 1_000_000;

    @Benchmark
    @OperationsPerInvocation(OPERATIONS_PER_INVOCATION)
    public void synchronousQueue() throws InterruptedException {
        var queue = new SynchronousQueue<>();
        var t1 = Thread.startVirtualThread(() -> {
            for (int i = 0; i < OPERATIONS_PER_INVOCATION; i++) {
                try {
                    queue.put(63);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        var t2 = Thread.startVirtualThread(() -> {
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
    public void exchanger() throws InterruptedException {
        var exchanger = new Exchanger<>();
        var t1 = Thread.startVirtualThread(() -> {
            for (int i = 0; i < OPERATIONS_PER_INVOCATION; i++) {
                try {
                    exchanger.exchange(63);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        var t2 = Thread.startVirtualThread(() -> {
            for (int i = 0; i < OPERATIONS_PER_INVOCATION; i++) {
                try {
                    exchanger.exchange(64);
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
        var ch = Channel.newRendezvousChannel();
        var t1 = Thread.startVirtualThread(() -> {
            for (int i = 0; i < OPERATIONS_PER_INVOCATION; i++) {
                try {
                    ch.send(63);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        var t2 = Thread.startVirtualThread(() -> {
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
