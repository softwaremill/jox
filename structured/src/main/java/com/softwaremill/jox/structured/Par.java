package com.softwaremill.jox.structured;

import static com.softwaremill.jox.structured.Scopes.supervised;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

public class Par {
    /**
     * Runs the given computations in parallel. If any fails because of an exception, or if any
     * returns an application error, other computations are interrupted. Then, the exception is
     * re-thrown, or the error value returned.
     */
    public static <T> List<T> par(List<Callable<T>> fs) throws InterruptedException {
        return supervised(
                scope -> {
                    var forksAndResults = new ArrayList<>(fs.size());
                    for (Callable<T> f : fs) {
                        forksAndResults.add(scope.fork(f));
                    }
                    return collect(forksAndResults);
                });
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> collect(ArrayList<Object> forksAndResults)
            throws InterruptedException, ExecutionException {
        for (int i = 0, len = forksAndResults.size(); i < len; i++) {
            var fork = (Fork<T>) forksAndResults.get(i);
            forksAndResults.set(i, fork.join());
        }
        return (List<T>) forksAndResults;
    }

    /**
     * Runs the given computations in parallel, with at most {@code parallelism} running in parallel
     * at the same time. If any computation fails because of an exception, or if any returns an
     * application error, other computations are interrupted. Then, the exception is re-thrown, or
     * the error value returned.
     */
    public static <T> List<T> parLimit(int parallelism, List<Callable<T>> fs)
            throws InterruptedException {
        return supervised(
                scope -> {
                    var s = new Semaphore(parallelism);
                    var forksAndResults = new ArrayList<>(fs.size());
                    for (Callable<T> f : fs) {
                        forksAndResults.add(
                                scope.fork(
                                        () -> {
                                            s.acquire();
                                            T r = f.call();
                                            // no try-finally as there's no
                                            // point in releasing in case of an
                                            // exception, as any newly started
                                            // forks will be interrupted
                                            s.release();
                                            return r;
                                        }));
                    }
                    return collect(forksAndResults);
                });
    }
}
