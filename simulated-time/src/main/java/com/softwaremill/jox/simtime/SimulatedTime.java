package com.softwaremill.jox.simtime;

import java.time.Instant;
import java.util.concurrent.Callable;

/**
 * Runs code using simulated time: sleeps and timeouts complete immediately in real time, while the
 * clocks observed by the code advance exactly by the requested durations.
 *
 * <p>Requires the simulated time Java agent to be added to the JVM ({@code
 * -javaagent:path/to/simulated-time.jar}).
 */
public final class SimulatedTime {
    private SimulatedTime() {}

    /** The value of {@link Instant#now()} when a simulation starts. */
    public static final Instant START = Instant.parse("2026-01-01T00:00:00Z");

    /**
     * The value of {@link System#nanoTime()} when a simulation starts. Arbitrary (as is the origin
     * of {@code nanoTime} in general): measure elapsed time as differences of {@code nanoTime}
     * values.
     */
    public static final long NANO_TIME_ORIGIN = 1_000_000_000_000L;

    /**
     * Runs {@code body} in a new simulation, using the default options. Blocks until {@code body}
     * completes.
     *
     * @return The result of {@code body}.
     * @throws Exception The exception thrown by {@code body}.
     * @throws SimulatedTimeException When the simulation can't continue: on a deadlock, when the
     *     watchdog fires, or when virtual time exceeds the maximum.
     * @see Simulation#run(Callable)
     */
    public static <T> T run(Callable<T> body) throws Exception {
        return run(SimulationOptions.DEFAULT, body);
    }

    /**
     * Runs {@code body} in a new simulation. Blocks until {@code body} completes.
     *
     * @see #run(Callable)
     */
    public static <T> T run(SimulationOptions options, Callable<T> body) throws Exception {
        return new Simulation(options).run(body);
    }
}
