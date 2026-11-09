package com.softwaremill.jox.simtime;

import static com.softwaremill.jox.structured.Scopes.supervised;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import com.softwaremill.jox.Channel;
import com.softwaremill.jox.Select;
import com.softwaremill.jox.flows.Flows;
import com.softwaremill.jox.structured.JoxScopeExecutionException;
import com.softwaremill.jox.structured.Race;

class JoxTest {
    static final long M = Duration.ofMinutes(1).toNanos();
    static final long H = Duration.ofHours(1).toNanos();

    static int pendingTimers() {
        return JdkPrimitivesTest.pendingTimers();
    }

    @Test
    void tickEmitsAtFixedIntervals() throws Exception {
        var ticks =
                SimulatedTime.run(
                        () ->
                                Flows.tick(Duration.ofHours(1), "x")
                                        .map(_ -> TestClock.elapsed())
                                        .take(3)
                                        .runToList());
        assertEquals(List.of(0L, H, 2 * H), ticks);
    }

    @Test
    void tickStopsWhenMergedTimeoutCompletes() throws Exception {
        SimulatedTime.run(
                () -> {
                    var ticks =
                            Flows.tick(Duration.ofMinutes(25), 0L)
                                    .map(_ -> TestClock.elapsed())
                                    .merge(Flows.timeout(Duration.ofHours(1)), false, true)
                                    .runToList();
                    assertEquals(List.of(0L, 25 * M, 50 * M), ticks);
                    assertEquals(H, TestClock.elapsed());
                    return null;
                });
    }

    @Test
    void throttleSpacesOutElements() throws Exception {
        var at =
                SimulatedTime.run(
                        () ->
                                Flows.fromValues(1, 2, 3)
                                        .throttle(1, Duration.ofHours(1))
                                        .map(_ -> TestClock.elapsed())
                                        .runToList());
        assertEquals(List.of(H, 2 * H, 3 * H), at);
    }

    @Test
    void groupedWithinEmitsGroupsWhenTheTimeWindowEnds() throws Exception {
        var groups =
                SimulatedTime.run(
                        () ->
                                Flows.<Integer>usingEmit(
                                                emit -> {
                                                    for (int i = 1; i <= 8; i++) {
                                                        Thread.sleep(Duration.ofMinutes(25));
                                                        emit.apply(i);
                                                    }
                                                })
                                        .groupedWithin(10, Duration.ofHours(1))
                                        .map(g -> TestClock.elapsed() / M + "m=" + g)
                                        .runToList());
        assertEquals(List.of("60m=[1, 2]", "120m=[3, 4]", "180m=[5, 6, 7]", "200m=[8]"), groups);
    }

    @Test
    void raceTimeoutFailsWhenComputationTakesTooLong() throws Exception {
        SimulatedTime.run(
                () -> {
                    assertThrows(
                            TimeoutException.class,
                            () ->
                                    Race.timeout(
                                            Duration.ofHours(1).toMillis(),
                                            () -> {
                                                Thread.sleep(Duration.ofHours(2));
                                                return 1;
                                            }));
                    assertEquals(H, TestClock.elapsed());
                    assertEquals(0, pendingTimers());
                    return null;
                });
    }

    @Test
    void raceTimeoutReturnsResultOfFastComputation() throws Exception {
        SimulatedTime.run(
                () -> {
                    int r =
                            Race.timeout(
                                    Duration.ofHours(1).toMillis(),
                                    () -> {
                                        Thread.sleep(Duration.ofMinutes(30));
                                        return 42;
                                    });
                    assertEquals(42, r);
                    assertEquals(30 * M, TestClock.elapsed());
                    assertEquals(0, pendingTimers());
                    return null;
                });
    }

    @Test
    void selectWithinTimesOutWhenNoClauseIsReady() throws Exception {
        SimulatedTime.run(
                () -> {
                    Channel<Integer> c1 = Channel.newRendezvousChannel();
                    Channel<Integer> c2 = Channel.newBufferedChannel(3);
                    assertThrows(
                            TimeoutException.class,
                            () ->
                                    Select.selectWithin(
                                            Duration.ofHours(1),
                                            c1.receiveClause(),
                                            c2.receiveClause()));
                    assertEquals(H, TestClock.elapsed());
                    return null;
                });
    }

    @Test
    void selectWithinReceivesValueSentBeforeTimeout() throws Exception {
        SimulatedTime.run(
                () -> {
                    Channel<Integer> c1 = Channel.newRendezvousChannel();
                    Channel<Integer> c2 = Channel.newRendezvousChannel();
                    int r =
                            supervised(
                                    scope -> {
                                        scope.fork(
                                                () -> {
                                                    Thread.sleep(Duration.ofMinutes(30));
                                                    c2.send(7);
                                                    return null;
                                                });
                                        return Select.selectWithin(
                                                Duration.ofHours(1),
                                                c1.receiveClause(),
                                                c2.receiveClause());
                                    });
                    assertEquals(7, r);
                    assertEquals(30 * M, TestClock.elapsed());
                    assertEquals(0, pendingTimers());
                    return null;
                });
    }

    @Test
    void supervisedForksCompleteInOrder() throws Exception {
        SimulatedTime.run(
                () -> {
                    List<String> done = Collections.synchronizedList(new ArrayList<>());
                    supervised(
                            scope -> {
                                for (int h : new int[] {3, 1, 2}) {
                                    scope.forkUser(
                                            () -> {
                                                Thread.sleep(Duration.ofHours(h));
                                                done.add(h + "@" + TestClock.elapsed() / H);
                                                return null;
                                            });
                                }
                                return null;
                            });
                    assertEquals(List.of("1@1", "2@2", "3@3"), done);
                    assertEquals(3 * H, TestClock.elapsed());
                    return null;
                });
    }

    @Test
    void supervisedForkErrorInterruptsOtherForks() throws Exception {
        SimulatedTime.run(
                () -> {
                    AtomicLong interruptedAt = new AtomicLong(-1);
                    var e =
                            assertThrows(
                                    JoxScopeExecutionException.class,
                                    () ->
                                            supervised(
                                                    scope -> {
                                                        scope.fork(
                                                                () -> {
                                                                    try {
                                                                        Thread.sleep(
                                                                                Duration.ofHours(
                                                                                        5));
                                                                    } catch (
                                                                            InterruptedException
                                                                                    ie) {
                                                                        interruptedAt.set(
                                                                                TestClock
                                                                                        .elapsed());
                                                                        throw ie;
                                                                    }
                                                                    return null;
                                                                });
                                                        scope.fork(
                                                                () -> {
                                                                    Thread.sleep(
                                                                            Duration.ofHours(1));
                                                                    throw new RuntimeException(
                                                                            "boom");
                                                                });
                                                        Thread.sleep(Duration.ofHours(10));
                                                        return null;
                                                    }));
                    assertEquals("boom", e.getCause().getMessage());
                    assertEquals(H, interruptedAt.get());
                    assertEquals(H, TestClock.elapsed());
                    assertEquals(0, pendingTimers());
                    return null;
                });
    }

    @Test
    void cancellingForksInterruptsThem() throws Exception {
        SimulatedTime.run(
                () -> {
                    AtomicLong daemonInterruptedAt = new AtomicLong(-1);
                    supervised(
                            scope -> {
                                scope.fork(
                                        () -> {
                                            try {
                                                Thread.sleep(Duration.ofHours(10));
                                            } catch (InterruptedException e) {
                                                daemonInterruptedAt.set(TestClock.elapsed());
                                                throw e;
                                            }
                                            return null;
                                        });
                                var cancellable =
                                        scope.forkCancellable(
                                                () -> {
                                                    Thread.sleep(Duration.ofHours(10));
                                                    return null;
                                                });
                                Thread.sleep(Duration.ofMinutes(30));
                                var e = assertThrows(Exception.class, cancellable::cancel);
                                assertInstanceOf(InterruptedException.class, e.getCause());
                                assertEquals(30 * M, TestClock.elapsed());
                                Thread.sleep(Duration.ofMinutes(30));
                                return null;
                            });
                    assertEquals(H, daemonInterruptedAt.get());
                    assertEquals(0, pendingTimers());
                    return null;
                });
    }

    /** A smoke test of jox channels, which spin-wait, in a simulation. */
    @Test
    void rendezvousTransfersComplete() throws Exception {
        long n = 100_000;
        long sum =
                SimulatedTime.run(
                        () -> {
                            Channel<Long> ch = Channel.newRendezvousChannel();
                            return supervised(
                                    scope -> {
                                        scope.fork(
                                                () -> {
                                                    for (long i = 0; i < n; i++) ch.send(i);
                                                    return null;
                                                });
                                        long s = 0;
                                        for (long i = 0; i < n; i++) s += ch.receive();
                                        return s;
                                    });
                        });
        assertEquals(n * (n - 1) / 2, sum);
    }

    @Test
    void rendezvousTransfersInterleavedWithSleeps() throws Exception {
        SimulatedTime.run(
                () -> {
                    Channel<Long> ch = Channel.newRendezvousChannel();
                    supervised(
                            scope -> {
                                scope.fork(
                                        () -> {
                                            for (long i = 0; i < 1000; i++) {
                                                Thread.sleep(Duration.ofSeconds(1));
                                                ch.send(i);
                                            }
                                            return null;
                                        });
                                for (long i = 0; i < 1000; i++) {
                                    ch.receive();
                                    Thread.sleep(Duration.ofMillis(500));
                                }
                                return null;
                            });
                    // the consumer's last 500ms sleep starts after the producer's last 1s sleep
                    assertEquals(Duration.ofMillis(1000_500).toNanos(), TestClock.elapsed());
                    return null;
                });
    }
}
