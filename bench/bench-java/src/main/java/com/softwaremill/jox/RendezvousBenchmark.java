package com.softwaremill.jox;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.Exchanger;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Rendezvous tests for {@link SynchronousQueue}, {@link Exchanger} and {@link Channel}.
 */
@Warmup(iterations = 3, time = 5000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 5000, timeUnit = TimeUnit.MILLISECONDS)
// after the measurement time, we want to interrupt any pending methods (which might block, waiting for a partner)
// this needs to be slightly larger than the test time to avoid warnings
@Timeout(time = 5100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Group)
public class RendezvousBenchmark {
    private SynchronousQueue<Integer> queue = new SynchronousQueue<>();

    @Benchmark
    @Group("synchronous_queue")
    @GroupThreads(1)
    public void putToSynchronousQueue() throws InterruptedException {
        queue.put(63);
    }

    @Benchmark
    @Group("synchronous_queue")
    @GroupThreads(1)
    public void takeFromSynchronousQueue() throws InterruptedException {
        queue.take();
    }

    //

    private Exchanger<Integer> exchanger = new Exchanger<>();

    @Benchmark
    @Group("exchanger")
    @GroupThreads(1)
    public void exchange1() throws InterruptedException {
        exchanger.exchange(63);
    }

    @Benchmark
    @Group("exchanger")
    @GroupThreads(1)
    public void exchange2() throws InterruptedException {
        exchanger.exchange(64);
    }

    //

    private Channel<Integer> channel = new Channel<>();

    @Benchmark
    @Group("channel")
    @GroupThreads(1)
    public void sendToChannel() throws InterruptedException {
        channel.send(63);
    }

    @Benchmark
    @Group("channel")
    @GroupThreads(1)
    public void receiveFromChannel() throws InterruptedException {
        channel.receive();
    }

    //

    // including an iterative benchmark, for direct comparison w/ Kotlin, as we can't write a group-based benchmark
    // there, due to suspended functions

    private final static int OPERATIONS_PER_INVOCATION = 1_000_000;

    @Benchmark
    @OperationsPerInvocation(OPERATIONS_PER_INVOCATION)
    @Group("channel_iterative")
    public void sendReceive() throws InterruptedException {
        var t1 = Thread.startVirtualThread(() -> {
            for (int i = 0; i < OPERATIONS_PER_INVOCATION; i++) {
                try {
                    channel.send(63);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        var t2 = Thread.startVirtualThread(() -> {
            for (int i = 0; i < OPERATIONS_PER_INVOCATION; i++) {
                try {
                    channel.receive();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        t1.join();
        t2.join();
    }
}
