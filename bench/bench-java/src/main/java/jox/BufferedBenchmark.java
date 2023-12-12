package jox;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Buffered tests for {@link ArrayBlockingQueue} and {@link Channel}.
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
public class BufferedBenchmark {
    @Param({"1", "10", "100"})
    public int capacity;

    private ArrayBlockingQueue<Integer> queue;
    private Channel<Integer> channel;

    @Setup
    public void create() {
        queue = new ArrayBlockingQueue<>(capacity);
        channel = new Channel<>(capacity);
    }

    //

    @Benchmark
    @Group("array_blocking_queue")
    @GroupThreads(1)
    public void putToArrayBlockingQueue() throws InterruptedException {
        queue.put(63);
    }

    @Benchmark
    @Group("array_blocking_queue")
    @GroupThreads(1)
    public void takeFromArrayBlockingQueue() throws InterruptedException {
        queue.take();
    }

    //

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
