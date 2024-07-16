package com.softwaremill.jox.structured;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import static com.softwaremill.jox.structured.Scopes.unsupervised;

public class Race {
    /**
     * The result of computation <code>f</code>, if it took less than <code>millis</code> ms, and a
     * {@link TimeoutException} otherwise.
     *
     * @throws TimeoutException If <code>f</code> took more than <code>millis</code>.
     */
    public static <T> T timeout(long millis, Callable<T> f) throws Exception {
        return raceResult(f, () -> {
            Thread.sleep(millis);
            throw new TimeoutException("Computation didn't finish within " + millis + "ms");
        });
    }

    /**
     * Returns the result of the first computation to complete successfully, or if all fail - throws the first
     * exception.
     */
    public static <T> T race(Callable<T> f1, Callable<T> f2) throws Exception {
        return race(List.of(f1, f2));
    }

    /**
     * Returns the result of the first computation to complete successfully, or if all fail - throws the first
     * exception.
     */
    public static <T> T race(Callable<T> f1, Callable<T> f2, Callable<T> f3) throws Exception {
        return race(List.of(f1, f2, f3));
    }

    /**
     * Returns the result of the first computation to complete successfully, or if all fail - throws the first
     * exception.
     */
    public static <T> T race(List<Callable<T>> fs) throws Exception {
        return unsupervised(scope -> {
            var result = new ArrayBlockingQueue<>(fs.size());
            fs.forEach(f -> {
                scope.forkUnsupervised(() -> {
                    try {
                        var r = f.call();
                        if (r == null) {
                            result.add(new NullWrapperInRace());
                        } else {
                            result.add(r);
                        }
                    } catch (Exception e) {
                        result.add(new ExceptionWrapperInRace(e));
                    }
                    return null;
                });
            });

            var left = fs.size();
            var exceptions = new ArrayList<Exception>();
            while (left > 0) {
                var first = result.take();
                if (first instanceof ExceptionWrapperInRace ew) {
                    exceptions.add(ew.e);
                } else if (first instanceof NullWrapperInRace) {
                    return null;
                } else {
                    return (T) first;
                }
                left -= 1;
            }

            var firstException = exceptions.getFirst();
            for (int i = 1; i < exceptions.size(); i++) {
                firstException.addSuppressed(exceptions.get(i));
            }
            throw firstException;
        });
    }


    /**
     * Returns the result of the first computation to complete (either successfully or with an exception).
     */
    public static <T> T raceResult(Callable<T> f1, Callable<T> f2) throws Exception {
        return raceResult(List.of(f1, f2));
    }

    /**
     * Returns the result of the first computation to complete (either successfully or with an exception).
     */
    public static <T> T raceResult(Callable<T> f1, Callable<T> f2, Callable<T> f3) throws Exception {
        return raceResult(List.of(f1, f2, f3));
    }

    /**
     * Returns the result of the first computation to complete (either successfully or with an exception).
     */
    public static <T> T raceResult(List<Callable<T>> fs) throws Exception {
        var result = race(fs.stream().<Callable<Object>>map(f -> () -> {
            try {
                return f.call();
            } catch (Exception e) {
                return new ExceptionWrapperInRaceResult(e);
            }
        }).toList());
        if (result instanceof ExceptionWrapperInRaceResult ew) {
            throw ew.e;
        } else {
            return (T) result;
        }
    }

    private record NullWrapperInRace() {}

    private record ExceptionWrapperInRace(Exception e) {}

    private record ExceptionWrapperInRaceResult(Exception e) {}
}
