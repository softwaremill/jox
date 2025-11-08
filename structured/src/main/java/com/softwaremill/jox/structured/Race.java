package com.softwaremill.jox.structured;

import static com.softwaremill.jox.structured.Scopes.supervised;

import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import com.softwaremill.jox.Channel;

public final class Race {
    /**
     * The result of computation {@code f}, if it took less than {@code millis} ms, and a {@link
     * TimeoutException} otherwise.
     *
     * @throws TimeoutException If {@code f} took more than {@code millis}.
     */
    public static <T> T timeout(long millis, Callable<T> f)
            throws TimeoutException, InterruptedException {
        var result =
                raceResult(
                        f::call,
                        () -> {
                            Thread.sleep(millis);
                            return TIMEOUT;
                        });

        if (result instanceof Timeout) {
            throw new TimeoutException("Computation didn't finish within " + millis + "ms");
        } else {
            //noinspection unchecked
            return (T) result;
        }
    }

    /**
     * Returns the result of the first computation to complete successfully, or if all fail - throws
     * the first exception.
     */
    public static <T> T race(Callable<T> f1, Callable<T> f2) throws InterruptedException {
        return race(List.of(f1, f2));
    }

    /**
     * Returns the result of the first computation to complete successfully, or if all fail - throws
     * the first exception.
     */
    public static <T> T race(Callable<T> f1, Callable<T> f2, Callable<T> f3)
            throws InterruptedException {
        return race(List.of(f1, f2, f3));
    }

    /**
     * Returns the result of the first computation to complete successfully, or if all fail - throws
     * the first exception.
     */
    public static <T> T race(List<Callable<T>> fs) throws InterruptedException {
        var exceptions = new ArrayDeque<Exception>();

        try {
            return supervised(
                    scope -> {
                        var branchResults = Channel.newBufferedChannel(fs.size());
                        for (Callable<T> f : fs) {
                            scope.forkUnsupervised(
                                    () -> {
                                        try {
                                            T r = f.call();
                                            branchResults.send(
                                                    r == null ? NULL_WRAPPER_IN_RACE : r);
                                        } catch (Exception e) {
                                            branchResults.send(new ExceptionWrapperInRace(e));
                                        }
                                        return null;
                                    });
                        }

                        int left = fs.size();
                        while (left > 0) {
                            var first = branchResults.receive();
                            if (first instanceof ExceptionWrapperInRace(Exception e)) {
                                exceptions.add(e);
                            } else if (first instanceof NullWrapperInRace) {
                                return null;
                            } else {
                                //noinspection unchecked
                                return (T) first;
                            }
                            left--;
                        }

                        // if we get here, there must be an exception
                        throw exceptions.pollFirst();
                    });
        } catch (JoxScopeExecutionException e) {
            while (!exceptions.isEmpty()) {
                e.addSuppressed(exceptions.pollFirst());
            }
            throw e;
        }
    }

    /**
     * Returns the result of the first computation to complete (either successfully or with an
     * exception).
     */
    public static <T> T raceResult(Callable<T> f1, Callable<T> f2) throws InterruptedException {
        return raceResult(List.of(f1, f2));
    }

    /**
     * Returns the result of the first computation to complete (either successfully or with an
     * exception).
     */
    public static <T> T raceResult(Callable<T> f1, Callable<T> f2, Callable<T> f3)
            throws InterruptedException {
        return raceResult(List.of(f1, f2, f3));
    }

    /**
     * Returns the result of the first computation to complete (either successfully or with an
     * exception).
     */
    public static <T> T raceResult(List<Callable<T>> fs) throws InterruptedException {
        var result =
                race(
                        fs.stream()
                                .<Callable<Object>>map(
                                        f ->
                                                () -> {
                                                    try {
                                                        return f.call();
                                                    } catch (Exception e) {
                                                        return new ExceptionWrapperInRaceResult(e);
                                                    }
                                                })
                                .toList());
        if (result instanceof ExceptionWrapperInRaceResult(Exception e)) {
            throw new JoxScopeExecutionException(e);
        } else {
            //noinspection unchecked
            return (T) result;
        }
    }

    private record NullWrapperInRace() {}

    private static final NullWrapperInRace NULL_WRAPPER_IN_RACE = new NullWrapperInRace();

    private record ExceptionWrapperInRace(Exception e) {}

    private record ExceptionWrapperInRaceResult(Exception e) {}

    private record Timeout() {}

    private static final Timeout TIMEOUT = new Timeout();
}
