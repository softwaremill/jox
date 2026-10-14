package com.softwaremill.jox.simtime.boot;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Entry points called from code rewritten by the simulated time agent, including {@code java.base}
 * classes. Loaded by the bootstrap class loader, and shared between the agent and the simulation
 * runtime.
 *
 * <p>Outside a simulation (when no {@link Handler} is installed, or the current thread isn't
 * managed by a simulation), every hook behaves like the call it replaces.
 *
 * <p>Internal API.
 */
public final class Hooks {
    private Hooks() {}

    /** Usable without loading this class, e.g. to check if the agent is present. */
    public static final String CLASS_NAME = "com.softwaremill.jox.simtime.boot.Hooks";

    /** Returned by {@link Handler} methods when the current thread isn't part of a simulation. */
    public static final long NOT_SIMULATED = Long.MIN_VALUE;

    /**
     * Implemented by the simulation runtime. Methods return {@link #NOT_SIMULATED} / {@code null}
     * when the current thread isn't part of a simulation.
     */
    public interface Handler {
        /** Whether the current thread is part of a simulation. */
        boolean isSimulated();

        long nanoTime();

        long currentTimeMillis();

        Instant currentInstant();

        ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit);

        /** Throws if the current thread's simulation has been aborted. */
        void checkAborted();
    }

    private static volatile Handler handler;

    /** Installs the simulation runtime's handler; called once, when the runtime is initialized. */
    public static void install(Handler h) {
        handler = h;
    }

    private static final AtomicInteger unblocksInFlight = new AtomicInteger();

    private static final List<String> agentErrors = new CopyOnWriteArrayList<>();

    /**
     * The number of virtual threads, which the JDK's unblocker thread has taken off the
     * monitor-waiting list, but which haven't yet been made runnable.
     */
    public static int unblocksInFlight() {
        return unblocksInFlight.get();
    }

    /** Errors which prevent simulations from working correctly, reported by the agent. */
    public static List<String> agentErrors() {
        return agentErrors;
    }

    public static void reportAgentError(String error) {
        agentErrors.add(error);
    }

    /** Replaces {@link System#nanoTime()} in allowlisted JDK classes. */
    public static long nanoTime() {
        Handler h = handler;
        if (h != null) {
            long t = h.nanoTime();
            if (t != NOT_SIMULATED) return t;
        }
        return System.nanoTime();
    }

    /** Replaces {@link System#currentTimeMillis()} in allowlisted JDK classes. */
    public static long currentTimeMillis() {
        Handler h = handler;
        if (h != null) {
            long t = h.currentTimeMillis();
            if (t != NOT_SIMULATED) return t;
        }
        return System.currentTimeMillis();
    }

    /**
     * Replaces {@link System#nanoTime()} in application classes. Fails if the simulation has been
     * aborted, to break loops which poll the clock.
     */
    public static long appNanoTime() {
        Handler h = handler;
        if (h != null) h.checkAborted();
        return nanoTime();
    }

    /**
     * Replaces {@link System#currentTimeMillis()} in application classes, see {@link
     * #appNanoTime()}.
     */
    public static long appCurrentTimeMillis() {
        Handler h = handler;
        if (h != null) h.checkAborted();
        return currentTimeMillis();
    }

    /**
     * Replaces {@link Thread#onSpinWait()} in application classes. Simulations run on a single
     * carrier thread, so spinning can't make progress: the spinning thread yields instead.
     */
    public static void onSpinWait() {
        Handler h = handler;
        if (h != null && h.isSimulated()) Thread.yield();
        else Thread.onSpinWait();
    }

    /**
     * Called at the start of {@code java.time.Clock.currentInstant()}; {@code null} means that the
     * original method should continue.
     */
    public static Instant currentInstant() {
        Handler h = handler;
        return h == null ? null : h.currentInstant();
    }

    /**
     * Replaces {@link ForkJoinPool#schedule(Runnable, long, TimeUnit)} in allowlisted JDK classes.
     */
    public static ScheduledFuture<?> schedule(
            ForkJoinPool pool, Runnable task, long delay, TimeUnit unit) {
        Handler h = handler;
        if (h != null) {
            ScheduledFuture<?> f = h.schedule(task, delay, unit);
            if (f != null) return f;
        }
        return pool.schedule(task, delay, unit);
    }

    /** Called at the start of {@code VirtualThread.compareAndSetOnWaitingList}. */
    public static void beforeOnWaitingListCas(boolean newValue) {
        if (!newValue) unblocksInFlight.incrementAndGet();
    }

    /** Called at the end of {@code VirtualThread.unblock()}. */
    public static void afterUnblock() {
        unblocksInFlight.decrementAndGet();
    }
}
