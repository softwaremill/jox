package com.softwaremill.jox.simtime.junit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import com.softwaremill.jox.simtime.TestClock;

@WithSimulatedTime
class SimulatedTimeExtensionTest {
    static final long M = Duration.ofMinutes(1).toNanos();
    static final long H = Duration.ofHours(1).toNanos();

    final AtomicBoolean backgroundDone = new AtomicBoolean();
    long testEnd;

    @BeforeEach
    void setUp() throws InterruptedException {
        assertTrue(Thread.currentThread().isVirtual());
        Thread.sleep(Duration.ofMinutes(10));
        Thread.ofVirtual()
                .start(
                        () -> {
                            try {
                                Thread.sleep(Duration.ofMinutes(30));
                                backgroundDone.set(true);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        });
    }

    @AfterEach
    void tearDown() {
        assertEquals(testEnd, TestClock.elapsed());
    }

    @Test
    void lifecycleMethodsShareTheSimulation() throws InterruptedException {
        assertEquals(10 * M, TestClock.elapsed());
        Thread.sleep(Duration.ofHours(1));
        assertTrue(backgroundDone.get());
        testEnd = TestClock.elapsed();
        assertEquals(70 * M, testEnd);
    }

    @RepeatedTest(3)
    void eachRepetitionRunsInANewSimulation() throws InterruptedException {
        assertEquals(10 * M, TestClock.elapsed());
        Thread.sleep(Duration.ofDays(1));
        testEnd = TestClock.elapsed();
    }

    @Test
    void awaitilityPollingInTheSameThreadUsesVirtualTime() {
        AtomicBoolean flag = new AtomicBoolean();
        Thread.ofVirtual()
                .start(
                        () -> {
                            try {
                                Thread.sleep(Duration.ofHours(1));
                                flag.set(true);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        });
        await().pollInSameThread()
                .pollInterval(Duration.ofMinutes(1))
                .atMost(Duration.ofHours(2))
                .untilTrue(flag);
        testEnd = TestClock.elapsed();
        // the condition is checked once a minute
        assertTrue(testEnd >= 70 * M && testEnd < 71 * M, "condition met at " + testEnd);
    }

    @Nested
    class NestedTests {
        @Test
        void runsInTheSimulationOfTheEnclosingClass() {
            assertEquals(10 * M, TestClock.elapsed());
            testEnd = TestClock.elapsed();
        }
    }
}
