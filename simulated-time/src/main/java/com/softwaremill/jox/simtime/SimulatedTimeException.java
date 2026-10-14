package com.softwaremill.jox.simtime;

/**
 * Thrown when a simulation can't continue. The message includes the stack traces of the threads
 * that are part of the simulation.
 */
public abstract sealed class SimulatedTimeException extends RuntimeException
        permits SimulatedTimeException.DeadlockException,
                SimulatedTimeException.WatchdogException,
                SimulatedTimeException.RunawayTimeException {

    private SimulatedTimeException(String message) {
        super(message);
    }

    /**
     * All threads of the simulation are blocked, there are no pending timers, and no thread has
     * been woken up from outside the simulation for a while.
     */
    public static final class DeadlockException extends SimulatedTimeException {
        DeadlockException(String message) {
            super(message);
        }
    }

    /**
     * Virtual time didn't advance for the configured amount of real time, while some thread of the
     * simulation was running: e.g. a thread is busy-looping, or blocks the carrier thread (such as
     * a sleep within a static initializer).
     */
    public static final class WatchdogException extends SimulatedTimeException {
        WatchdogException(String message) {
            super(message);
        }
    }

    /**
     * Virtual time would advance beyond the configured maximum: e.g. the simulation never
     * completes, or a wait in some code is computed using real time.
     */
    public static final class RunawayTimeException extends SimulatedTimeException {
        RunawayTimeException(String message) {
            super(message);
        }
    }
}
