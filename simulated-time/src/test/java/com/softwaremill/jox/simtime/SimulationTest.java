package com.softwaremill.jox.simtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.softwaremill.jox.simtime.SimulatedTimeException.WatchdogException;

class SimulationTest {
    static final long H = Duration.ofHours(1).toNanos();

    @Test
    void returnsTheResult() throws Exception {
        assertEquals(42, SimulatedTime.run(() -> 42));
    }

    @Test
    void propagatesExceptions() {
        IOException checked = new IOException("checked");
        assertSame(
                checked,
                assertThrows(
                        IOException.class,
                        () ->
                                SimulatedTime.run(
                                        () -> {
                                            throw checked;
                                        })));

        AssertionError error = new AssertionError("error");
        assertSame(
                error,
                assertThrows(
                        AssertionError.class,
                        () ->
                                SimulatedTime.run(
                                        () -> {
                                            throw error;
                                        })));
    }

    @Test
    void mustBeRunFromPlatformThread() throws Exception {
        AtomicReference<Throwable> error = new AtomicReference<>();
        Thread.ofVirtual()
                .start(
                        () -> {
                            try {
                                SimulatedTime.run(() -> null);
                            } catch (Throwable e) {
                                error.set(e);
                            }
                        })
                .join();
        assertTrue(error.get() instanceof IllegalStateException, String.valueOf(error.get()));
    }

    @Test
    void timeTimersAndThreadsCarryOverBetweenRuns() throws Exception {
        Simulation simulation = new Simulation();
        AtomicBoolean childDone = new AtomicBoolean();
        Thread child =
                simulation.run(
                        () -> {
                            Thread t =
                                    Thread.ofVirtual()
                                            .start(
                                                    () -> {
                                                        JdkPrimitivesTest.sleep(2 * H);
                                                        childDone.set(true);
                                                    });
                            Thread.sleep(Duration.ofHours(1));
                            return t;
                        });
        assertFalse(childDone.get());

        simulation.run(
                () -> {
                    assertEquals(H, TestClock.elapsed());
                    child.join();
                    assertEquals(2 * H, TestClock.elapsed());
                    return null;
                });
        assertTrue(childDone.get());
    }

    @Test
    void concurrentRunsOfTheSameSimulationAreRejected() throws Exception {
        Simulation simulation = new Simulation();
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicReference<Exception> firstRunFailure = new AtomicReference<>();
        Thread first =
                Thread.ofPlatform()
                        .start(
                                () -> {
                                    try {
                                        simulation.run(
                                                () -> {
                                                    started.countDown();
                                                    // released from outside the simulation
                                                    release.await();
                                                    return null;
                                                });
                                    } catch (Exception e) {
                                        firstRunFailure.set(e);
                                    }
                                });
        started.await();
        try {
            assertThrows(IllegalStateException.class, () -> simulation.run(() -> null));
        } finally {
            release.countDown();
        }
        first.join();
        assertEquals(null, firstRunFailure.get());
    }

    static class SleepsInStaticInitializer {
        static {
            JdkPrimitivesTest.sleep(H);
        }

        static void touch() {}
    }

    @Test
    void runsFailAfterTheWatchdogFired() {
        Simulation simulation = new Simulation(FailureModesTest.SHORT_WATCHDOG);
        assertThrows(
                WatchdogException.class,
                () ->
                        simulation.run(
                                () -> {
                                    SleepsInStaticInitializer.touch();
                                    return null;
                                }));
        assertThrows(WatchdogException.class, () -> simulation.run(() -> 42));
    }
}
