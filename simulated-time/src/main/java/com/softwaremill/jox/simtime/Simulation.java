package com.softwaremill.jox.simtime;

import java.util.concurrent.Callable;

/**
 * A simulation, which can run multiple bodies of code in sequence, e.g. a test's setup, the test
 * itself and its teardown. Virtual time, pending timers and threads started by previous bodies
 * carry over between runs. In the common case of running a single body, use {@link
 * SimulatedTime#run(Callable)}.
 *
 * <p>The threads of a simulation only run while some {@link #run} call is in progress.
 */
public final class Simulation {
    private final SimulatedScheduler scheduler;

    /**
     * @throws IllegalStateException When the simulated time agent isn't added to the JVM.
     * @throws UnsupportedOperationException When simulated time doesn't work on the current JVM.
     */
    public Simulation(SimulationOptions options) {
        Environment.ensureSupported();
        this.scheduler = new SimulatedScheduler(options);
    }

    public Simulation() {
        this(SimulationOptions.DEFAULT);
    }

    /**
     * Runs {@code body} in a new virtual thread, which is part of the simulation. Blocks until that
     * thread completes.
     *
     * <p>Within the simulation (that is, in the body and in virtual threads started by it,
     * including forks of structured concurrency scopes), timed waits and sleeps complete without
     * waiting in real time, once all threads of the simulation are blocked. Clock reads ({@link
     * System#nanoTime()}, {@link System#currentTimeMillis()}, {@link java.time.Instant#now()},
     * {@link java.time.Clock#systemUTC()} etc.) return virtual time.
     *
     * <p>Must be called from a platform thread, which is used to run the simulation. Can't be
     * called concurrently on the same instance; separate simulations can run in parallel.
     *
     * @return The result of {@code body}.
     * @throws Exception The exception thrown by {@code body}.
     * @throws SimulatedTimeException When the simulation can't continue: on a deadlock, when the
     *     watchdog fires, or when virtual time exceeds the maximum.
     */
    public <T> T run(Callable<T> body) throws Exception {
        return scheduler.run(body);
    }
}
