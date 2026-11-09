package com.softwaremill.jox.simtime;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import com.softwaremill.jox.simtime.SimulatedTimeException.DeadlockException;
import com.softwaremill.jox.simtime.SimulatedTimeException.RunawayTimeException;
import com.softwaremill.jox.simtime.SimulatedTimeException.WatchdogException;
import com.softwaremill.jox.simtime.boot.Hooks;

/**
 * A virtual thread scheduler, which runs the threads of a simulation one at a time on a single
 * carrier thread: the platform thread calling {@link #run}. When all threads are blocked, virtual
 * time advances to the next timer.
 *
 * <p>Extends {@link ForkJoinPool} only so that the JDK uses {@link #schedule} for the timers of
 * timed waits; the pool never starts any workers.
 *
 * <p>Invariants: {@link #execute} and {@link #submit} never block or throw (they are called by the
 * JDK while a virtual thread is pinned); there's only one carrier thread, so threads of a
 * simulation never run in parallel (spin-waits in jox channels rely on this).
 */
final class SimulatedScheduler extends ForkJoinPool {
    private static final long DEADLOCK_GRACE_NANOS = TimeUnit.MILLISECONDS.toNanos(500);
    private static final long IDLE_POLL_NANOS = TimeUnit.MILLISECONDS.toNanos(1);
    private static final long UNBLOCK_POLL_NANOS = TimeUnit.MICROSECONDS.toNanos(10);

    static {
        Hooks.install(new HooksHandler());
    }

    private final SimulationOptions options;
    private final VirtualClock clock = new VirtualClock();
    private final ConcurrentLinkedQueue<Runnable> runQueue = new ConcurrentLinkedQueue<>();
    private final Set<Thread> managedThreads = ConcurrentHashMap.newKeySet();
    private final AtomicReference<Thread> carrier = new AtomicReference<>();

    /**
     * Real time ({@link System#nanoTime()}) when virtual time last advanced, or the scheduler was
     * idle. Updated at least every {@link #IDLE_POLL_NANOS} while idle, so that the watchdog
     * doesn't fire then.
     */
    private volatile long lastProgress;

    /** Once set, the simulation can't run anymore. */
    private volatile WatchdogException watchdogFailure;

    SimulatedScheduler(SimulationOptions options) {
        super(1);
        this.options = options;
    }

    VirtualClock clock() {
        return clock;
    }

    /**
     * The scheduler running the current thread, or {@code null} if it's not part of a simulation.
     */
    static SimulatedScheduler current() {
        Thread t = Thread.currentThread();
        if (!t.isVirtual()) return null;
        return VirtualThreads.schedulerOf(t) instanceof SimulatedScheduler s ? s : null;
    }

    @Override
    public void execute(Runnable task) {
        enqueue(task, task);
    }

    /**
     * Since Java 26, the JDK submits continuations to {@link ForkJoinPool} schedulers using this
     * method.
     */
    @Override
    public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task) {
        enqueue(task::quietlyInvoke, task);
        return task;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return clock.schedule(task, delay, unit);
    }

    private void enqueue(Runnable task, Object submitted) {
        Thread thread = VirtualThreads.threadOf(submitted);
        if (thread != null) managedThreads.add(thread);
        runQueue.add(task);
        if (!isRunningOnCarrier()) {
            Thread c = carrier.get();
            if (c != null) LockSupport.unpark(c);
        }
    }

    /** Whether the current thread is this scheduler's carrier, or a thread mounted on it. */
    private boolean isRunningOnCarrier() {
        Thread t = Thread.currentThread();
        return t.isVirtual() ? VirtualThreads.schedulerOf(t) == this : t == carrier.get();
    }

    /**
     * Runs {@code body} in a new virtual thread of this scheduler, using the calling platform
     * thread as the carrier, until that thread completes. Can be called multiple times, but not
     * concurrently: virtual time, pending timers and threads that are still alive carry over
     * between calls, but only run during a call.
     */
    <T> T run(Callable<T> body) throws Exception {
        Thread caller = Thread.currentThread();
        if (caller.isVirtual()) {
            throw new IllegalStateException("Simulations must be run from a platform thread");
        }
        if (!carrier.compareAndSet(null, caller)) {
            throw new IllegalStateException("The simulation is already running");
        }
        Outcome<T> outcome = new Outcome<>(body);
        Thread root = VirtualThreads.builder(this).name("simulation-root").unstarted(outcome);
        Thread watchdog =
                Thread.ofPlatform()
                        .daemon()
                        .name("simulation-watchdog")
                        .unstarted(() -> watch(caller));
        try {
            throwIfAborted();
            lastProgress = System.nanoTime();
            watchdog.start();
            root.start();
            runUntilTerminated(root);
            root.join();
        } finally {
            watchdog.interrupt();
            carrier.set(null);
        }
        return outcome.get();
    }

    private void runUntilTerminated(Thread root) {
        long idleSince = -1;
        while (true) {
            throwIfAborted();
            Runnable task = runQueue.poll();
            if (task != null) {
                idleSince = -1;
                task.run();
                continue;
            }
            if (!root.isAlive()) return;
            if (unblockPending()) {
                LockSupport.parkNanos(UNBLOCK_POLL_NANOS);
                continue;
            }
            // a task might have been submitted by the unblocker after the queue was checked
            if (!runQueue.isEmpty()) continue;
            if (fireNextTimers()) {
                idleSince = -1;
                continue;
            }

            // all threads are blocked: only a thread from outside the simulation can wake them up
            long now = System.nanoTime();
            if (idleSince < 0) idleSince = now;
            else if (now - idleSince > DEADLOCK_GRACE_NANOS) {
                throw new DeadlockException(
                        dump("Deadlock: all threads are blocked, and there are no pending timers"));
            }
            LockSupport.parkNanos(IDLE_POLL_NANOS);
            lastProgress = System.nanoTime();
        }
    }

    /**
     * Whether the JDK's unblocker thread is about to make a thread, blocked on a monitor, runnable.
     * Time must not advance until that happens.
     */
    private boolean unblockPending() {
        // the order matters: the unblocker increments the in-flight counter before clearing the
        // waiting-list flag
        for (Thread t : managedThreads) {
            if (!t.isAlive()) {
                managedThreads.remove(t);
            } else if (VirtualThreads.isOnWaitingList(t)) {
                return true;
            }
        }
        return Hooks.unblocksInFlight() > 0;
    }

    private boolean fireNextTimers() {
        long deadline = clock.nextDeadline();
        if (deadline < 0) return false;
        if (deadline > options.maxVirtualTime().toNanos()) {
            throw new RunawayTimeException(
                    dump(
                            "Virtual time would exceed the maximum of "
                                    + options.maxVirtualTime()
                                    + "; next timer at "
                                    + formatNanos(deadline)));
        }
        List<Runnable> due = clock.advanceToNextDeadline();
        lastProgress = System.nanoTime();
        for (Runnable r : due) r.run();
        return true;
    }

    private void watch(Thread carrierThread) {
        long timeout = options.watchdogTimeout().toNanos();
        Duration checkEvery = Duration.ofNanos(Math.max(timeout / 10, IDLE_POLL_NANOS));
        while (true) {
            try {
                Thread.sleep(checkEvery);
            } catch (InterruptedException e) {
                return;
            }
            if (System.nanoTime() - lastProgress < timeout) continue;

            StringBuilder carrierStack =
                    new StringBuilder("  carrier ").append(carrierThread).append('\n');
            appendStackTrace(carrierStack, carrierThread.getStackTrace());
            watchdogFailure =
                    new WatchdogException(
                            dump(
                                            "Virtual time didn't advance for "
                                                    + options.watchdogTimeout()
                                                    + " of real time, while a thread was running"
                                                    + " (busy loop, or a blocked carrier thread?)")
                                    + carrierStack);
            // unblocks threads which block the carrier
            for (Thread t : managedThreads) t.interrupt();
            LockSupport.unpark(carrierThread);
            reportIfNotStopped(carrierThread, checkEvery);
            return;
        }
    }

    /**
     * A thread running a loop, which doesn't read the clock or yield, can't be stopped. Reporting
     * it is the best that can be done.
     */
    private void reportIfNotStopped(Thread carrierThread, Duration checkEvery) {
        long deadline = System.nanoTime() + options.watchdogTimeout().toNanos();
        while (System.nanoTime() < deadline) {
            try {
                Thread.sleep(checkEvery);
            } catch (InterruptedException e) {
                return; // run() completed
            }
        }
        if (carrier.get() == carrierThread) {
            System.err.println(
                    "[jox simulated time] The simulation's carrier thread can't be stopped (a loop"
                            + " which doesn't read the clock?). "
                            + watchdogFailure.getMessage());
        }
    }

    private void throwIfAborted() {
        WatchdogException e = watchdogFailure;
        if (e != null) throw e;
    }

    private String dump(String reason) {
        StringBuilder sb =
                new StringBuilder(reason)
                        .append("; virtual time: ")
                        .append(formatNanos(clock.nanoTime()))
                        .append('\n');
        for (Thread t : managedThreads) {
            if (!t.isAlive()) continue;
            sb.append("  ").append(t).append(' ').append(t.getState()).append('\n');
            appendStackTrace(sb, t.getStackTrace());
        }
        return sb.toString();
    }

    private static void appendStackTrace(StringBuilder sb, StackTraceElement[] stackTrace) {
        for (StackTraceElement e : stackTrace) sb.append("      at ").append(e).append('\n');
    }

    private static String formatNanos(long nanos) {
        return Duration.ofNanos(nanos).toString();
    }

    private static final class Outcome<T> implements Runnable {
        private final Callable<T> body;
        private T result;
        private Throwable error;

        Outcome(Callable<T> body) {
            this.body = body;
        }

        @Override
        public void run() {
            try {
                result = body.call();
            } catch (Throwable e) {
                error = e;
            }
        }

        T get() throws Exception {
            if (error instanceof Exception e) throw e;
            if (error instanceof Error e) throw e;
            if (error != null) throw new IllegalStateException(error);
            return result;
        }
    }

    /** Provides virtual time to threads which are part of a simulation. */
    private static final class HooksHandler implements Hooks.Handler {
        private static final long START_MILLIS = SimulatedTime.START.toEpochMilli();

        @Override
        public boolean isSimulated() {
            return current() != null;
        }

        @Override
        public long nanoTime() {
            SimulatedScheduler s = current();
            return s == null
                    ? Hooks.NOT_SIMULATED
                    : SimulatedTime.NANO_TIME_ORIGIN + s.clock.nanoTime();
        }

        @Override
        public long currentTimeMillis() {
            SimulatedScheduler s = current();
            return s == null
                    ? Hooks.NOT_SIMULATED
                    : START_MILLIS + TimeUnit.NANOSECONDS.toMillis(s.clock.nanoTime());
        }

        @Override
        public Instant currentInstant() {
            SimulatedScheduler s = current();
            return s == null ? null : SimulatedTime.START.plusNanos(s.clock.nanoTime());
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
            SimulatedScheduler s = current();
            return s == null ? null : s.clock.schedule(task, delay, unit);
        }

        @Override
        public void checkAborted() {
            SimulatedScheduler s = current();
            if (s != null && s.watchdogFailure != null) {
                throw new IllegalStateException("The simulation has been aborted by the watchdog");
            }
        }
    }
}
