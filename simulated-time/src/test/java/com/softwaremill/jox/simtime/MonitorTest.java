package com.softwaremill.jox.simtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;

class MonitorTest {
    static final long H = Duration.ofHours(1).toNanos();

    /**
     * When a monitor is released, the JDK's unblocker thread makes the blocked virtual thread
     * runnable. Before that happens, the simulation might appear idle: time must not advance then.
     *
     * <p>A regression net, not a proof: the race window is small, hence the repetitions (without
     * the fix, this failed in the first iteration in local runs).
     */
    @Test
    void threadBlockedOnMonitorAcquiresItWhenReleasedWithoutTimeAdvancing() throws Exception {
        for (int i = 0; i < 200; i++) {
            long acquiredAt =
                    SimulatedTime.run(
                            () -> {
                                Object monitor = new Object();
                                long[] bAcquiredAt = new long[1];
                                CountDownLatch aHoldsMonitor = new CountDownLatch(1);
                                Thread a =
                                        Thread.ofVirtual()
                                                .start(
                                                        () -> {
                                                            synchronized (monitor) {
                                                                aHoldsMonitor.countDown();
                                                                JdkPrimitivesTest.sleep(H);
                                                            }
                                                        });
                                aHoldsMonitor.await();
                                Thread b =
                                        Thread.ofVirtual()
                                                .start(
                                                        () -> {
                                                            synchronized (monitor) {
                                                                bAcquiredAt[0] =
                                                                        TestClock.elapsed();
                                                            }
                                                        });
                                // a timer that would fire if time advanced while b is being
                                // unblocked
                                Thread c =
                                        Thread.ofVirtual()
                                                .start(() -> JdkPrimitivesTest.sleep(2 * H));
                                a.join();
                                b.join();
                                c.join();
                                return bAcquiredAt[0];
                            });
            assertEquals(H, acquiredAt, "iteration " + i);
        }
    }
}
