package com.softwaremill.jox.structured;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.softwaremill.jox.structured.Scopes.unsupervised;

public class Race {
    /**
     * The result of computation {@code f}, if it took less than {@code millis} ms, and a {@link TimeoutException}
     * otherwise.
     *
     * @throws TimeoutException If {@code f} took more than {@code millis}.
     */
    public static <T> T timeout(long millis, Callable<T> f) throws TimeoutException, ExecutionException, InterruptedException {
        var result = raceResult(f::call, () -> {
            Thread.sleep(millis);
            return new Timeout();
        });

        if (result instanceof Timeout) {
            throw new TimeoutException("Computation didn't finish within " + millis + "ms");
        } else {
            return (T) result;
        }
    }

    /**
     * Returns the result of the first computation to complete successfully, or if all fail - throws the first
     * exception.
     */
    public static <T> T race(Callable<T> f1, Callable<T> f2) throws ExecutionException, InterruptedException {
        return race(List.of(f1, f2));
    }

    /**
     * Returns the result of the first computation to complete successfully, or if all fail - throws the first
     * exception.
     */
    public static <T> T race(Callable<T> f1, Callable<T> f2, Callable<T> f3) throws ExecutionException, InterruptedException {
        return race(List.of(f1, f2, f3));
    }

    /**
     * Returns the result of the first computation to complete successfully, or if all fail - throws the first
     * exception.
     */
    public static <T> T race(List<Callable<T>> fs) throws ExecutionException, InterruptedException {
        var exceptions = new ArrayDeque<Exception>();

        try {
            return unsupervised(scope -> {
                var branchResults = new ArrayBlockingQueue<>(fs.size());
                fs.forEach(f -> {
                    scope.forkUnsupervised(() -> {
                        try {
                            var r = f.call();
                            if (r == null) {
                                branchResults.add(new NullWrapperInRace());
                            } else {
                                branchResults.add(r);
                            }
                        } catch (Exception e) {
                            branchResults.add(new ExceptionWrapperInRace(e));
                        }
                        return null;
                    });
                });

                var left = fs.size();
                while (left > 0) {
                    var first = branchResults.take();
                    if (first instanceof ExceptionWrapperInRace ew) {
                        exceptions.add(ew.e);
                    } else if (first instanceof NullWrapperInRace) {
                        return null;
                    } else {
                        return (T) first;
                    }
                    left -= 1;
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
     * Returns the result of the first computation to complete (either successfully or with an exception).
     */
    public static <T> T raceResult(Callable<T> f1, Callable<T> f2) throws ExecutionException, InterruptedException {
        return raceResult(List.of(f1, f2));
    }

    /**
     * Returns the result of the first computation to complete (either successfully or with an exception).
     */
    public static <T> T raceResult(Callable<T> f1, Callable<T> f2, Callable<T> f3) throws ExecutionException, InterruptedException {
        return raceResult(List.of(f1, f2, f3));
    }

    /**
     * Returns the result of the first computation to complete (either successfully or with an exception).
     */
    public static <T> T raceResult(List<Callable<T>> fs) throws ExecutionException, InterruptedException {
        var result = race(fs.stream().<Callable<Object>>map(f -> () -> {
            try {
                return f.call();
            } catch (Exception e) {
                return new ExceptionWrapperInRaceResult(e);
            }
        }).toList());
        if (result instanceof ExceptionWrapperInRaceResult ew) {
            throw new ExecutionException(ew.e);
        } else {
            return (T) result;
        }
    }

    private record NullWrapperInRace() {}

    private record ExceptionWrapperInRace(Exception e) {}

    private record ExceptionWrapperInRaceResult(Exception e) {}

    private record Timeout() {}
}
