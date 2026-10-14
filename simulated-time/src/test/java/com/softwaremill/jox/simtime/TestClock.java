package com.softwaremill.jox.simtime;

public final class TestClock {
    private TestClock() {}

    /** Virtual time elapsed since the start of the current simulation. */
    public static long elapsed() {
        return System.nanoTime() - SimulatedTime.NANO_TIME_ORIGIN;
    }
}
