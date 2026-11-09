package com.softwaremill.jox.simtime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class IsolationTest {
    static final long MS = Duration.ofMillis(1).toNanos();

    /** Measures how long a 50ms sleep takes, in a thread started using the given builder. */
    private static long measureSleep(Thread.Builder builder) throws InterruptedException {
        long[] elapsed = new long[1];
        builder.start(
                        () -> {
                            long t0 = TestClock.elapsed();
                            JdkPrimitivesTest.sleep(50 * MS);
                            elapsed[0] = TestClock.elapsed() - t0;
                        })
                .join();
        return elapsed[0];
    }

    private static void assertRealSleep(long elapsed) {
        assertTrue(elapsed >= 50 * MS && elapsed < 10_000 * MS, "elapsed: " + elapsed);
    }

    @Test
    void platformThreadsStartedInASimulationUseRealTime() throws Exception {
        SimulatedTime.run(
                () -> {
                    assertRealSleep(measureSleep(Thread.ofPlatform()));
                    assertEquals(0, TestClock.elapsed());
                    return null;
                });
    }

    @Test
    void virtualThreadsStartedFromPlatformThreadsUseRealTime() throws Exception {
        SimulatedTime.run(
                () -> {
                    long[] elapsed = new long[1];
                    Thread.ofPlatform()
                            .start(
                                    () -> {
                                        try {
                                            elapsed[0] = measureSleep(Thread.ofVirtual());
                                        } catch (InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                    })
                            .join();
                    assertRealSleep(elapsed[0]);
                    assertEquals(0, TestClock.elapsed());
                    return null;
                });
    }

    @Test
    void clocksOutsideTheSimulationUseRealTime() throws Exception {
        Instant before = Instant.now();
        SimulatedTime.run(
                () -> {
                    Thread.sleep(Duration.ofDays(300));
                    return null;
                });
        Duration elapsed = Duration.between(before, Instant.now());
        assertTrue(elapsed.compareTo(Duration.ofMinutes(1)) < 0, "elapsed: " + elapsed);
    }

    @Test
    void parallelSimulationsHaveIndependentClocks() throws Exception {
        int n = 2000;
        CyclicBarrier barrier = new CyclicBarrier(2);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<long[]> seconds = executor.submit(() -> ticks(barrier, n, 1));
            Future<long[]> sevenSeconds = executor.submit(() -> ticks(barrier, n, 7));
            long[] expected1 = new long[n];
            long[] expected7 = new long[n];
            for (int i = 0; i < n; i++) {
                expected1[i] = (i + 1) * TimeUnit.SECONDS.toNanos(1);
                expected7[i] = (i + 1) * TimeUnit.SECONDS.toNanos(7);
            }
            assertArrayEquals(expected1, seconds.get());
            assertArrayEquals(expected7, sevenSeconds.get());
        }
    }

    private static long[] ticks(CyclicBarrier barrier, int n, long stepSeconds) throws Exception {
        barrier.await();
        return SimulatedTime.run(
                () -> {
                    long[] at = new long[n];
                    for (int i = 0; i < n; i++) {
                        Thread.sleep(Duration.ofSeconds(stepSeconds));
                        at[i] = TestClock.elapsed();
                    }
                    return at;
                });
    }

    /** Sends a byte over a loopback connection, using virtual threads. */
    private static int sendOverSocket(int b) throws Exception {
        try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            Thread client =
                    Thread.ofVirtual()
                            .start(
                                    () -> {
                                        try (Socket s =
                                                new Socket(
                                                        server.getInetAddress(),
                                                        server.getLocalPort())) {
                                            s.getOutputStream().write(b);
                                        } catch (IOException e) {
                                            throw new UncheckedIOException(e);
                                        }
                                    });
            try (Socket s = server.accept()) {
                int received = s.getInputStream().read();
                // untimed: in a simulation, a timed join could time out while I/O is in flight
                client.join();
                return received;
            }
        }
    }

    @Test
    void socketIoWorksInASimulation() throws Exception {
        assertEquals(1, SimulatedTime.run(() -> sendOverSocket(1)));
    }

    /** The JDK's I/O pollers must not become part of a simulation, in which socket I/O happens. */
    @Test
    void socketIoWorksOutsideAfterASimulationUsedIt() throws Exception {
        SimulatedTime.run(() -> sendOverSocket(1));
        assertEquals(2, sendOverSocket(2));
    }
}
