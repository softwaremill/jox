package com.softwaremill.jox.simtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Phaser;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class JdkPrimitivesTest {
    static final long S = Duration.ofSeconds(1).toNanos();
    static final long M = Duration.ofMinutes(1).toNanos();
    static final long H = Duration.ofHours(1).toNanos();

    static int pendingTimers() {
        return SimulatedScheduler.current().clock().pendingTimers();
    }

    static void sleep(long nanos) {
        try {
            Thread.sleep(Duration.ofNanos(nanos));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void clocksStartAtFixedValuesAndFollowVirtualTime() throws Exception {
        SimulatedTime.run(
                () -> {
                    assertEquals(0, TestClock.elapsed());
                    assertEquals(SimulatedTime.START, Instant.now());

                    Thread.sleep(Duration.ofDays(2));
                    Instant expected = SimulatedTime.START.plus(Duration.ofDays(2));
                    assertEquals(Duration.ofDays(2).toNanos(), TestClock.elapsed());
                    assertEquals(expected.toEpochMilli(), System.currentTimeMillis());
                    assertEquals(expected, Instant.now());
                    assertEquals(expected, Clock.systemUTC().instant());
                    assertEquals(expected.toEpochMilli(), Clock.systemUTC().millis());
                    assertEquals(expected, Clock.systemDefaultZone().instant());
                    assertEquals(expected.toEpochMilli(), Clock.systemDefaultZone().millis());
                    assertEquals(
                            LocalDateTime.ofInstant(expected, ZoneOffset.UTC),
                            LocalDateTime.now(ZoneOffset.UTC));
                    return null;
                });
    }

    private record Wakeup(long sleptFor, long wokenAt) {}

    @Test
    void sleepersAreWokenUpInDeadlineOrder() throws Exception {
        List<Wakeup> wakeups =
                SimulatedTime.run(
                        () -> {
                            Random random = new Random(42);
                            List<Wakeup> result = Collections.synchronizedList(new ArrayList<>());
                            List<Thread> threads = new ArrayList<>();
                            for (int i = 0; i < 1000; i++) {
                                long d = random.nextLong(1, 1000) * S;
                                threads.add(
                                        Thread.ofVirtual()
                                                .start(
                                                        () -> {
                                                            sleep(d);
                                                            result.add(
                                                                    new Wakeup(
                                                                            d,
                                                                            TestClock.elapsed()));
                                                        }));
                            }
                            for (Thread t : threads) t.join();
                            return result;
                        });
        assertEquals(1000, wakeups.size());
        for (int i = 0; i < wakeups.size(); i++) {
            Wakeup w = wakeups.get(i);
            assertEquals(w.sleptFor(), w.wokenAt());
            if (i > 0) assertTrue(wakeups.get(i - 1).wokenAt() <= w.wokenAt());
        }
    }

    @Test
    void sameDeadlineTimersFireInSchedulingOrder() throws Exception {
        SimulatedTime.run(
                () -> {
                    List<Integer> woken = Collections.synchronizedList(new ArrayList<>());
                    List<Thread> threads = new ArrayList<>();
                    for (int i = 0; i < 100; i++) {
                        int id = i;
                        Thread t =
                                Thread.ofVirtual()
                                        .start(
                                                () -> {
                                                    sleep(H);
                                                    woken.add(id);
                                                });
                        threads.add(t);
                        // ensures that the thread has started sleeping before the next one starts
                        Thread.yield();
                    }
                    for (Thread t : threads) t.join();
                    List<Integer> expected = new ArrayList<>();
                    for (int i = 0; i < 100; i++) expected.add(i);
                    assertEquals(expected, woken);
                    assertEquals(H, TestClock.elapsed());
                    return null;
                });
    }

    @Test
    void conditionSignalledEarlyLeavesNoTimer() throws Exception {
        SimulatedTime.run(
                () -> {
                    ReentrantLock lock = new ReentrantLock();
                    Condition c = lock.newCondition();
                    Thread.ofVirtual()
                            .start(
                                    () -> {
                                        sleep(S);
                                        lock.lock();
                                        try {
                                            c.signal();
                                        } finally {
                                            lock.unlock();
                                        }
                                    });
                    lock.lock();
                    try {
                        assertTrue(c.awaitNanos(H) > 0);
                    } finally {
                        lock.unlock();
                    }
                    assertEquals(S, TestClock.elapsed());
                    assertEquals(0, pendingTimers());
                    return null;
                });
    }

    @Test
    void completableFutureGetReturnsWhenCompletedBeforeTimeout() throws Exception {
        SimulatedTime.run(
                () -> {
                    var cf = new CompletableFuture<String>();
                    Thread.ofVirtual()
                            .start(
                                    () -> {
                                        sleep(S);
                                        cf.complete("ok");
                                    });
                    assertEquals("ok", cf.get(1, TimeUnit.HOURS));
                    assertEquals(S, TestClock.elapsed());
                    assertEquals(0, pendingTimers());
                    return null;
                });
    }

    /** These timers run on a JDK-internal platform thread, using real time. */
    @Test
    void completableFutureTimeoutsUseRealTime() throws Exception {
        SimulatedTime.run(
                () -> {
                    var timedOut =
                            new CompletableFuture<String>().orTimeout(50, TimeUnit.MILLISECONDS);
                    var e = assertThrows(ExecutionException.class, timedOut::get);
                    assertInstanceOf(TimeoutException.class, e.getCause());

                    var delayed =
                            CompletableFuture.supplyAsync(
                                    () -> "delayed",
                                    CompletableFuture.delayedExecutor(50, TimeUnit.MILLISECONDS));
                    assertEquals("delayed", delayed.get());

                    assertEquals(0, TestClock.elapsed());
                    return null;
                });
    }

    interface TimedWait {
        void await() throws Exception;
    }

    static Stream<Named<TimedWait>> timedWaits() {
        return Stream.of(
                Named.of("LockSupport.parkNanos", () -> LockSupport.parkNanos(M)),
                Named.of(
                        "Condition.awaitNanos",
                        () -> {
                            ReentrantLock lock = new ReentrantLock();
                            Condition c = lock.newCondition();
                            lock.lock();
                            try {
                                assertTrue(c.awaitNanos(M) <= 0);
                            } finally {
                                lock.unlock();
                            }
                        }),
                Named.of(
                        "Object.wait",
                        () -> {
                            Object monitor = new Object();
                            synchronized (monitor) {
                                monitor.wait(Duration.ofMinutes(1).toMillis());
                            }
                        }),
                Named.of(
                        "CountDownLatch.await",
                        () -> assertFalse(new CountDownLatch(1).await(1, TimeUnit.MINUTES))),
                Named.of(
                        "ArrayBlockingQueue.poll",
                        () ->
                                assertNull(
                                        new ArrayBlockingQueue<String>(1)
                                                .poll(1, TimeUnit.MINUTES))),
                Named.of(
                        "CompletableFuture.get",
                        () ->
                                assertThrows(
                                        TimeoutException.class,
                                        () ->
                                                new CompletableFuture<String>()
                                                        .get(1, TimeUnit.MINUTES))),
                Named.of(
                        "Thread.join",
                        () -> {
                            Thread t = Thread.ofVirtual().start(() -> sleep(H));
                            assertFalse(t.join(Duration.ofMinutes(1)));
                        }),
                Named.of(
                        "StampedLock.tryWriteLock",
                        () -> {
                            StampedLock lock = new StampedLock();
                            lock.readLock();
                            assertEquals(0, lock.tryWriteLock(1, TimeUnit.MINUTES));
                        }),
                Named.of(
                        "Phaser.awaitAdvanceInterruptibly",
                        () -> {
                            Phaser phaser = new Phaser(2);
                            int phase = phaser.arrive();
                            assertThrows(
                                    TimeoutException.class,
                                    () ->
                                            phaser.awaitAdvanceInterruptibly(
                                                    phase, 1, TimeUnit.MINUTES));
                        }),
                Named.of(
                        "Exchanger.exchange",
                        () ->
                                assertThrows(
                                        TimeoutException.class,
                                        () ->
                                                new Exchanger<String>()
                                                        .exchange("x", 1, TimeUnit.MINUTES))),
                Named.of(
                        "SynchronousQueue.poll",
                        () -> assertNull(new SynchronousQueue<String>().poll(1, TimeUnit.MINUTES))),
                Named.of(
                        "FutureTask.get",
                        () ->
                                assertThrows(
                                        TimeoutException.class,
                                        () ->
                                                new FutureTask<>(() -> "x")
                                                        .get(1, TimeUnit.MINUTES))));
    }

    @ParameterizedTest
    @MethodSource("timedWaits")
    void timedWaitTimesOut(TimedWait timedWait) throws Exception {
        SimulatedTime.run(
                () -> {
                    timedWait.await();
                    assertEquals(M, TestClock.elapsed());
                    return null;
                });
    }

    @Test
    void objectWaitReturnsWhenNotified() throws Exception {
        SimulatedTime.run(
                () -> {
                    Object monitor = new Object();
                    boolean[] notified = {false};
                    Thread.ofVirtual()
                            .start(
                                    () -> {
                                        sleep(S);
                                        synchronized (monitor) {
                                            notified[0] = true;
                                            monitor.notifyAll();
                                        }
                                    });
                    synchronized (monitor) {
                        while (!notified[0]) monitor.wait(Duration.ofHours(1).toMillis());
                    }
                    assertEquals(S, TestClock.elapsed());
                    assertEquals(0, pendingTimers());
                    return null;
                });
    }

    @Test
    void structuredTaskScopeTimesOut() throws Exception {
        SimulatedTime.run(
                () -> {
                    assertThrows(
                            StructuredTaskScope.TimeoutException.class,
                            () -> {
                                try (var scope =
                                        StructuredTaskScope.open(
                                                StructuredTaskScope.Joiner.awaitAll(),
                                                cf -> cf.withTimeout(Duration.ofMinutes(30)))) {
                                    scope.fork(
                                            () -> {
                                                Thread.sleep(Duration.ofHours(1));
                                                return null;
                                            });
                                    scope.join();
                                }
                            });
                    assertEquals(30 * M, TestClock.elapsed());
                    assertEquals(0, pendingTimers());
                    return null;
                });
    }

    @Test
    void structuredTaskScopeForksArePartOfTheSimulation() throws Exception {
        SimulatedTime.run(
                () -> {
                    SimulatedScheduler scheduler = SimulatedScheduler.current();
                    try (var scope = StructuredTaskScope.open()) {
                        var a =
                                scope.fork(
                                        () -> {
                                            Thread.sleep(Duration.ofHours(2));
                                            return SimulatedScheduler.current();
                                        });
                        var b =
                                scope.fork(
                                        () -> {
                                            Thread.sleep(Duration.ofHours(1));
                                            return TestClock.elapsed();
                                        });
                        scope.join();
                        assertSame(scheduler, a.get());
                        assertEquals(H, b.get());
                    }
                    assertEquals(2 * H, TestClock.elapsed());
                    return null;
                });
    }
}
