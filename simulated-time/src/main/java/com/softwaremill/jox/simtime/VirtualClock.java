package com.softwaremill.jox.simtime;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Virtual time elapsed since the start of a simulation, together with the timers that are due in
 * the future. Time only moves when {@link #advanceToNextDeadline()} is called. Timers with the same
 * deadline fire in the order in which they've been scheduled.
 */
final class VirtualClock {
    /** Includes cancelled timers, which are removed lazily. */
    private final PriorityQueue<Timer> timers = new PriorityQueue<>();

    private int cancelledTimers;
    private long nextSeq;
    private volatile long nanoTime;

    long nanoTime() {
        return nanoTime;
    }

    synchronized ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        long deadline = nanoTime + Math.max(0, unit.toNanos(delay));
        if (deadline < nanoTime) deadline = Long.MAX_VALUE; // overflow
        Timer t = new Timer(deadline, nextSeq++, task);
        timers.add(t);
        return t;
    }

    synchronized int pendingTimers() {
        return timers.size() - cancelledTimers;
    }

    /** The deadline of the earliest pending timer, or {@code -1} if there are none. */
    synchronized long nextDeadline() {
        Timer t = firstPending();
        return t == null ? -1 : t.deadline;
    }

    /**
     * Moves time forward to the earliest deadline, and removes the timers which are then due. There
     * must be a pending timer.
     *
     * @return The tasks of the due timers, to be run by the caller.
     */
    synchronized List<Runnable> advanceToNextDeadline() {
        // deadlines are never in the past: time only advances to the earliest one
        nanoTime = firstPending().deadline;
        List<Runnable> due = new ArrayList<>();
        for (Timer t = firstPending(); t != null && t.deadline == nanoTime; t = firstPending()) {
            timers.poll();
            t.done = true;
            due.add(t.task);
        }
        return due;
    }

    /** Removes cancelled timers from the head of the queue, and returns the first pending one. */
    private Timer firstPending() {
        while (!timers.isEmpty() && timers.peek().cancelled) {
            timers.poll();
            cancelledTimers--;
        }
        return timers.peek();
    }

    private synchronized boolean cancel(Timer t) {
        if (t.done) return false;
        t.done = true;
        t.cancelled = true;
        cancelledTimers++;
        // bounds the memory used by cancelled timers
        if (cancelledTimers > timers.size() / 2) {
            timers.removeIf(timer -> timer.cancelled);
            cancelledTimers = 0;
        }
        return true;
    }

    private final class Timer implements ScheduledFuture<Void> {
        private final long deadline;
        private final long seq;
        private final Runnable task;
        // guarded by VirtualClock.this
        private boolean done;
        private boolean cancelled;

        Timer(long deadline, long seq, Runnable task) {
            this.deadline = deadline;
            this.seq = seq;
            this.task = task;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return VirtualClock.this.cancel(this);
        }

        @Override
        public boolean isCancelled() {
            synchronized (VirtualClock.this) {
                return cancelled;
            }
        }

        @Override
        public boolean isDone() {
            synchronized (VirtualClock.this) {
                return done;
            }
        }

        @Override
        public Void get() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Void get(long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(deadline - nanoTime, TimeUnit.NANOSECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            Timer other = (Timer) o;
            int c = Long.compare(deadline, other.deadline);
            return c != 0 ? c : Long.compare(seq, other.seq);
        }
    }
}
