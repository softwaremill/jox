package com.softwaremill.jox.simtime;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import com.softwaremill.jox.simtime.boot.Hooks;

/** Checks that simulations can run in this JVM. */
final class Environment {
    private Environment() {}

    private static final long DAY_NANOS = TimeUnit.DAYS.toNanos(1);

    private static volatile boolean supported;

    static void ensureSupported() {
        if (!supported) {
            synchronized (Environment.class) {
                if (!supported) {
                    ensureAgentLoaded(Hooks.CLASS_NAME);
                    initPollers();
                    selfTest();
                    supported = true;
                }
            }
        }
        // JDK classes might be rewritten after startup, when they are first loaded
        ensureNoAgentErrors(Hooks.agentErrors());
    }

    static void ensureNoAgentErrors(List<String> errors) {
        if (!errors.isEmpty()) throw unsupported(String.join("; ", errors), null);
    }

    static void ensureAgentLoaded(String hooksClass) {
        Class<?> hooks;
        try {
            hooks = Class.forName(hooksClass, false, null);
        } catch (ClassNotFoundException e) {
            hooks = null;
        }
        if (hooks == null || Hooks.class != hooks) {
            throw new IllegalStateException(
                    "The jox simulated time agent isn't loaded: add"
                        + " -javaagent:path/to/simulated-time.jar to the JVM options, e.g. in the"
                        + " surefire plugin's argLine");
        }
    }

    /**
     * The JDK's I/O poller threads are virtual threads (on Linux), inheriting the scheduler of the
     * thread that first uses socket I/O. If that happened in a simulation, they would only run as
     * part of it.
     */
    private static void initPollers() {
        try {
            Class.forName("sun.nio.ch.Poller", true, null);
        } catch (ClassNotFoundException e) {
            // pollers are implemented differently in this JDK
        }
    }

    /**
     * Fails if the simulated time mechanisms don't work as expected, e.g. due to changes in JDK
     * internals. Complements the agent's startup check of rewritten JDK methods, by also covering
     * the scheduler's use of JDK internals.
     */
    private static void selfTest() {
        // on a new platform thread, as the caller might be a virtual thread
        FutureTask<String> test =
                new FutureTask<>(
                        () -> new SimulatedScheduler(SimulationOptions.DEFAULT).run(SELF_TEST));
        Thread.ofPlatform().start(test);
        String failure;
        try {
            failure = test.get();
        } catch (ExecutionException e) {
            throw unsupported(e.getCause().toString(), e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
        if (failure != null) throw unsupported(failure + " doesn't use virtual time", null);
    }

    /** Returns the name of the first mechanism that doesn't use virtual time, or {@code null}. */
    private static final Callable<String> SELF_TEST =
            () -> {
                Thread.sleep(Duration.ofDays(1));
                if (elapsed() != DAY_NANOS) return "Thread.sleep";

                Thread child = Thread.ofVirtual().start(() -> LockSupport.parkNanos(DAY_NANOS));
                child.join();
                if (elapsed() != 2 * DAY_NANOS) return "LockSupport.parkNanos";

                ReentrantLock lock = new ReentrantLock();
                Condition condition = lock.newCondition();
                lock.lock();
                try {
                    condition.awaitNanos(DAY_NANOS);
                } finally {
                    lock.unlock();
                }
                if (elapsed() != 3 * DAY_NANOS) return "Condition.awaitNanos";

                Instant expected = SimulatedTime.START.plus(Duration.ofDays(3));
                if (!expected.equals(Instant.now())) return "Instant.now";
                return null;
            };

    private static long elapsed() {
        return Hooks.nanoTime() - SimulatedTime.NANO_TIME_ORIGIN;
    }

    private static UnsupportedOperationException unsupported(String reason, Throwable cause) {
        return new UnsupportedOperationException(
                "jox simulated time doesn't work on Java " + Runtime.version() + ": " + reason,
                cause);
    }
}
