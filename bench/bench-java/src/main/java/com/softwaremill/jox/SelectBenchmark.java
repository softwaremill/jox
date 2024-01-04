package com.softwaremill.jox;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.Exchanger;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import static com.softwaremill.jox.Select.select;

/**
 * Tests for {@link Select#select(SelectClause[])}.
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
public class SelectBenchmark {
    private Channel<Integer> channel1 = new Channel<>();
    private Channel<Integer> channel2 = new Channel<>();

    @Benchmark
    @Group("channel")
    @GroupThreads(1)
    public void sendToChannel() throws InterruptedException {
        channel1.send(63);
    }

    @Benchmark
    @Group("channel")
    @GroupThreads(1)
    public void receiveFromChannelUsingSelect() throws InterruptedException {
        select(channel1.receiveClause());
    }

    //

    // including an iterative benchmark, for direct comparison w/ Kotlin, as we can't write a group-based benchmark
    // there, due to suspended functions

    private final static int OPERATIONS_PER_INVOCATION = 1_000_000;

    @Benchmark
    @OperationsPerInvocation(OPERATIONS_PER_INVOCATION)
    @Group("single_channel_iterative")
    public void sendReceiveUsingSelectSingleChannel() throws InterruptedException {
        var t1 = Thread.startVirtualThread(() -> {
            for (int i = 0; i < OPERATIONS_PER_INVOCATION; i++) {
                try {
                    channel2.send(63);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        var t2 = Thread.startVirtualThread(() -> {
            for (int i = 0; i < OPERATIONS_PER_INVOCATION; i++) {
                try {
                    select(channel2.receiveClause());
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
    @Group("two_channels_iterative")
    public void sendReceiveUsingSelectTwoChannels() throws InterruptedException {
        var t1 = Thread.startVirtualThread(() -> {
            for (int i = 0; i < OPERATIONS_PER_INVOCATION / 2; i++) {
                try {
                    channel1.send(63);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        var t2 = Thread.startVirtualThread(() -> {
            for (int i = 0; i < OPERATIONS_PER_INVOCATION / 2; i++) {
                try {
                    channel2.send(63);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        var t3 = Thread.startVirtualThread(() -> {
            for (int i = 0; i < OPERATIONS_PER_INVOCATION; i++) {
                try {
                    select(channel1.receiveClause(), channel2.receiveClause());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        t1.join();
        t2.join();
        t3.join();
    }
}
