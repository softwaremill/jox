package com.softwaremill.jox.flows;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

class ScheduleTest {

    @Test
    void immediateScheduleYieldsZeroDurations() {
        // when
        Iterator<Duration> it = Schedule.immediate().maxRetries(3).newIterator();

        // then
        List<Duration> durations = collect(it);
        assertEquals(List.of(Duration.ZERO, Duration.ZERO, Duration.ZERO), durations);
    }

    @Test
    void fixedIntervalScheduleYieldsConstantDurations() {
        // when
        Iterator<Duration> it =
                Schedule.fixedInterval(Duration.ofMillis(100)).maxRetries(3).newIterator();

        // then
        List<Duration> durations = collect(it);
        assertEquals(
                List.of(Duration.ofMillis(100), Duration.ofMillis(100), Duration.ofMillis(100)),
                durations);
    }

    @Test
    void maxRetriesLimitsIteratorSize() {
        // when
        Iterator<Duration> it = Schedule.immediate().maxRetries(0).newIterator();

        // then
        assertFalse(it.hasNext());
    }

    @Test
    void exponentialBackoffDoublesEachInterval() {
        // when
        Iterator<Duration> it =
                Schedule.exponentialBackoff(Duration.ofMillis(100)).maxRetries(4).newIterator();

        // then
        List<Duration> durations = collect(it);
        assertEquals(
                List.of(
                        Duration.ofMillis(100),
                        Duration.ofMillis(200),
                        Duration.ofMillis(400),
                        Duration.ofMillis(800)),
                durations);
    }

    @Test
    void maxIntervalCapsLongDurations() {
        // when
        Iterator<Duration> it =
                Schedule.exponentialBackoff(Duration.ofMillis(100))
                        .maxInterval(Duration.ofMillis(300))
                        .maxRetries(4)
                        .newIterator();

        // then
        List<Duration> durations = collect(it);
        assertEquals(
                List.of(
                        Duration.ofMillis(100),
                        Duration.ofMillis(200),
                        Duration.ofMillis(300),
                        Duration.ofMillis(300)),
                durations);
    }

    @Test
    void maxRetriesRejectsNegativeValue() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> Schedule.immediate().maxRetries(-1));
    }

    private static List<Duration> collect(Iterator<Duration> it) {
        List<Duration> result = new ArrayList<>();
        it.forEachRemaining(result::add);
        return result;
    }
}
