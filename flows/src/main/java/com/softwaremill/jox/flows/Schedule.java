package com.softwaremill.jox.flows;

import java.time.Duration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * Describes a retry schedule as a sequence of intervals between retry attempts. A schedule is a
 * supplier of iterators over durations, re-created on each usage.
 *
 * <p>Schedules are infinite by default. Use {@link #maxRetries(int)} to limit the number of
 * retries.
 */
public final class Schedule {
    private static final Duration MAX_DURATION = Duration.ofDays(3650);

    private final Supplier<Iterator<Duration>> intervals;

    private Schedule(Supplier<Iterator<Duration>> intervals) {
        this.intervals = intervals;
    }

    /** Returns a new iterator over the intervals for this schedule. */
    Iterator<Duration> newIterator() {
        return intervals.get();
    }

    // --- Factory methods ---

    /**
     * Infinite schedule with zero delay between attempts.
     *
     * @return A schedule yielding {@link Duration#ZERO} between each attempt.
     */
    public static Schedule immediate() {
        return new Schedule(() -> infiniteIterator(Duration.ZERO));
    }

    /**
     * Infinite schedule with a fixed interval between attempts.
     *
     * @param interval The duration between attempts.
     * @return A schedule yielding the given interval between each attempt.
     */
    public static Schedule fixedInterval(Duration interval) {
        return new Schedule(() -> infiniteIterator(interval));
    }

    /**
     * Infinite exponential backoff schedule (base 2). Each interval is double the previous,
     * starting from {@code initial}. Intervals are capped at ~3650 days to avoid overflow.
     *
     * @param initial The initial interval.
     * @return A schedule with exponentially increasing intervals.
     */
    public static Schedule exponentialBackoff(Duration initial) {
        return new Schedule(
                () ->
                        new Iterator<>() {
                            Duration current = initial;

                            public boolean hasNext() {
                                return true;
                            }

                            public Duration next() {
                                Duration result = current;
                                // double, capping to avoid overflow
                                long millis = current.toMillis();
                                current =
                                        millis > MAX_DURATION.toMillis() / 2
                                                ? MAX_DURATION
                                                : Duration.ofMillis(millis * 2);
                                return result;
                            }
                        });
    }

    // --- Modifiers ---

    /**
     * Limits total retries. The resulting schedule's iterator yields exactly {@code retries}
     * intervals, allowing {@code retries + 1} total attempts (the initial attempt + retries).
     *
     * @param retries The maximum number of retries (must be >= 0).
     * @return A schedule limited to the given number of retries.
     */
    public Schedule maxRetries(int retries) {
        if (retries < 0) {
            throw new IllegalArgumentException("retries must be >= 0");
        }
        Schedule self = this;
        return new Schedule(
                () -> {
                    Iterator<Duration> delegate = self.intervals.get();
                    return new Iterator<>() {
                        int remaining = retries;

                        public boolean hasNext() {
                            return remaining > 0 && delegate.hasNext();
                        }

                        public Duration next() {
                            if (!hasNext()) throw new NoSuchElementException();
                            remaining--;
                            return delegate.next();
                        }
                    };
                });
    }

    /**
     * Caps each interval to the given maximum.
     *
     * @param max The maximum duration for any single interval.
     * @return A schedule with each interval capped to the given maximum.
     */
    public Schedule maxInterval(Duration max) {
        Schedule self = this;
        return new Schedule(
                () -> {
                    Iterator<Duration> delegate = self.intervals.get();
                    return new Iterator<>() {
                        public boolean hasNext() {
                            return delegate.hasNext();
                        }

                        public Duration next() {
                            Duration d = delegate.next();
                            return d.compareTo(max) > 0 ? max : d;
                        }
                    };
                });
    }

    private static Iterator<Duration> infiniteIterator(Duration value) {
        return new Iterator<>() {
            public boolean hasNext() {
                return true;
            }

            public Duration next() {
                return value;
            }
        };
    }
}
