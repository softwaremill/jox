package com.softwaremill.jox.simtime;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.softwaremill.jox.simtime.SimulatedTimeException.DeadlockException;
import com.softwaremill.jox.simtime.SimulatedTimeException.RunawayTimeException;
import com.softwaremill.jox.simtime.SimulatedTimeException.WatchdogException;

class FailureModesTest {
    static final SimulationOptions SHORT_WATCHDOG =
            new SimulationOptions(Duration.ofMillis(500), Duration.ofDays(365));

    @Test
    void reportsDeadlockWhenAwaitingForever() {
        var e =
                assertThrows(
                        DeadlockException.class,
                        () ->
                                SimulatedTime.run(
                                        () -> {
                                            new CountDownLatch(1).await();
                                            return null;
                                        }));
        assertTrue(e.getMessage().contains("CountDownLatch.await"), e.getMessage());
    }

    @Test
    void reportsDeadlockOfMonitors() {
        var e =
                assertThrows(
                        DeadlockException.class,
                        () ->
                                SimulatedTime.run(
                                        () -> {
                                            Object x = new Object();
                                            Object y = new Object();
                                            CountDownLatch both = new CountDownLatch(2);
                                            Thread t1 =
                                                    Thread.ofVirtual()
                                                            .name("deadlocked-1")
                                                            .start(() -> lockBoth(x, y, both));
                                            Thread t2 =
                                                    Thread.ofVirtual()
                                                            .name("deadlocked-2")
                                                            .start(() -> lockBoth(y, x, both));
                                            t1.join();
                                            t2.join();
                                            return null;
                                        }));
        assertTrue(e.getMessage().contains("deadlocked-1"), e.getMessage());
        assertTrue(e.getMessage().contains("deadlocked-2"), e.getMessage());
    }

    private static void lockBoth(Object first, Object second, CountDownLatch both) {
        synchronized (first) {
            both.countDown();
            try {
                both.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            synchronized (second) {
                assertTrue(false, "unreachable");
            }
        }
    }

    static class SleepsInStaticInitializer {
        static {
            JdkPrimitivesTest.sleep(Duration.ofHours(1).toNanos());
        }

        static void touch() {}
    }

    /** Class initialization pins the carrier thread: the sleep would block it in real time. */
    @Test
    void watchdogFiresWhenCarrierIsBlocked() {
        var e =
                assertThrows(
                        WatchdogException.class,
                        () ->
                                SimulatedTime.run(
                                        SHORT_WATCHDOG,
                                        () -> {
                                            SleepsInStaticInitializer.touch();
                                            return null;
                                        }));
        assertTrue(e.getMessage().contains("SleepsInStaticInitializer"), e.getMessage());
    }

    @Test
    void watchdogFiresOnBusyLoopPollingTheClock() {
        assertThrows(
                WatchdogException.class,
                () ->
                        SimulatedTime.run(
                                SHORT_WATCHDOG,
                                () -> {
                                    long deadline =
                                            System.nanoTime() + Duration.ofHours(1).toNanos();
                                    while (System.nanoTime() < deadline) Thread.onSpinWait();
                                    return null;
                                }));
    }

    static volatile boolean stopBusyLoop;

    @Test
    void watchdogReportsLoopWhichCantBeStopped() throws Exception {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(err, true));
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread runner =
                Thread.ofPlatform()
                        .start(
                                () -> {
                                    try {
                                        SimulatedTime.run(
                                                SHORT_WATCHDOG,
                                                () -> {
                                                    while (!stopBusyLoop) {}
                                                    return null;
                                                });
                                    } catch (Throwable e) {
                                        failure.set(e);
                                    }
                                });
        try {
            await().atMost(Duration.ofSeconds(30))
                    .until(() -> err.toString().contains("can't be stopped"));
        } finally {
            stopBusyLoop = true;
            System.setErr(originalErr);
        }
        runner.join();
        assertInstanceOf(WatchdogException.class, failure.get());
    }

    @Test
    void runawayGuardFiresWhenVirtualTimeExceedsMaximum() {
        var options = new SimulationOptions(Duration.ofSeconds(30), Duration.ofDays(10));
        var e =
                assertThrows(
                        RunawayTimeException.class,
                        () ->
                                SimulatedTime.run(
                                        options,
                                        () -> {
                                            while (true) Thread.sleep(Duration.ofDays(1));
                                        }));
        assertTrue(e.getMessage().contains("PT240H"), e.getMessage());
    }

    @Test
    void runawayGuardFiresOnInfiniteSleep() {
        assertThrows(
                RunawayTimeException.class,
                () ->
                        SimulatedTime.run(
                                () -> {
                                    Thread.sleep(Long.MAX_VALUE);
                                    return null;
                                }));
    }

    @Test
    void failsWithInstructionsWhenAgentIsMissing() {
        var e =
                assertThrows(
                        IllegalStateException.class,
                        () -> Environment.ensureAgentLoaded("com.example.Missing"));
        assertTrue(e.getMessage().contains("-javaagent"), e.getMessage());
    }

    @Test
    void failsWhenAgentReportedErrors() {
        var e =
                assertThrows(
                        UnsupportedOperationException.class,
                        () -> Environment.ensureNoAgentErrors(List.of("cannot rewrite X")));
        assertTrue(e.getMessage().contains("cannot rewrite X"), e.getMessage());
    }

    @Test
    void validatesOptions() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationOptions(Duration.ofMillis(99), Duration.ofDays(1)));
        assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationOptions(Duration.ofSeconds(1), Duration.ofDays(-1)));
        var tooLong = Duration.ofSeconds(Long.MAX_VALUE);
        assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationOptions(tooLong, Duration.ofDays(1)));
        assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationOptions(Duration.ofSeconds(1), tooLong));
    }
}
