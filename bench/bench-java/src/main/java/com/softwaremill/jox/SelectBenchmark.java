package com.softwaremill.jox;

import static com.softwaremill.jox.Select.select;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

/** Tests for {@link Select#select(SelectClause[])}. */
@Warmup(iterations = 3, time = 4000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 4000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class SelectBenchmark {
    private static final int OPERATIONS_PER_INVOCATION = 1_000_000;

    @Benchmark
    @OperationsPerInvocation(OPERATIONS_PER_INVOCATION)
    public void selectWithSingleClause() throws InterruptedException {
        var ch = Channel.newRendezvousChannel();
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
                                    select(ch.receiveClause());
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
    public void selectWithTwoClauses() throws InterruptedException {
        var ch1 = Channel.newRendezvousChannel();
        var ch2 = Channel.newRendezvousChannel();
        var t1 =
                Thread.startVirtualThread(
                        () -> {
                            for (int i = 0; i < OPERATIONS_PER_INVOCATION / 2; i++) {
                                try {
                                    ch1.send(63);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });

        var t2 =
                Thread.startVirtualThread(
                        () -> {
                            for (int i = 0; i < OPERATIONS_PER_INVOCATION / 2; i++) {
                                try {
                                    ch2.send(63);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });

        var t3 =
                Thread.startVirtualThread(
                        () -> {
                            for (int i = 0; i < OPERATIONS_PER_INVOCATION; i++) {
                                try {
                                    select(ch1.receiveClause(), ch2.receiveClause());
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
