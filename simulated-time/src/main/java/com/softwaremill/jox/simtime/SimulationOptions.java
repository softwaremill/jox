package com.softwaremill.jox.simtime;

import java.time.Duration;

/**
 * Configuration of a simulation.
 *
 * @param watchdogTimeout The amount of real time, during which virtual time doesn't advance while
 *     some thread of the simulation is running, after which the simulation fails with a {@link
 *     SimulatedTimeException.WatchdogException}. At least {@link #MIN_WATCHDOG_TIMEOUT}.
 * @param maxVirtualTime The maximum virtual time of a simulation, beyond which it fails with a
 *     {@link SimulatedTimeException.RunawayTimeException}.
 */
public record SimulationOptions(Duration watchdogTimeout, Duration maxVirtualTime) {
    public static final long DEFAULT_WATCHDOG_SECONDS = 30;
    public static final long DEFAULT_MAX_VIRTUAL_TIME_DAYS = 365;
    public static final Duration MIN_WATCHDOG_TIMEOUT = Duration.ofMillis(100);

    public static final SimulationOptions DEFAULT =
            new SimulationOptions(
                    Duration.ofSeconds(DEFAULT_WATCHDOG_SECONDS),
                    Duration.ofDays(DEFAULT_MAX_VIRTUAL_TIME_DAYS));

    public SimulationOptions {
        requireNanosRepresentable("watchdogTimeout", watchdogTimeout);
        requireNanosRepresentable("maxVirtualTime", maxVirtualTime);
        if (watchdogTimeout.compareTo(MIN_WATCHDOG_TIMEOUT) < 0) {
            throw new IllegalArgumentException(
                    "watchdogTimeout must be at least " + MIN_WATCHDOG_TIMEOUT);
        }
        if (!maxVirtualTime.isPositive()) {
            throw new IllegalArgumentException("maxVirtualTime must be positive");
        }
    }

    private static void requireNanosRepresentable(String name, Duration d) {
        try {
            d.toNanos();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(name + " is too large: " + d, e);
        }
    }
}
