package com.softwaremill.jox.structured;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

import static com.softwaremill.jox.structured.Scopes.supervised;

public class Par {
    /**
     * Runs the given computations in parallel. If any fails because of an exception, or if any returns an application
     * error, other computations are interrupted. Then, the exception is re-thrown, or the error value returned.
     */
    public static <T> List<T> par(List<Callable<T>> fs) throws Exception {
        return supervised(scope -> {
            var forks = fs.stream().map(f -> scope.fork(f)).toList();
            var results = new ArrayList<T>();
            for (Fork<T> fork : forks) {
                results.add(fork.join());
            }
            return results;
        });
    }

    /**
     * Runs the given computations in parallel, with at most <code>parallelism</code> running in parallel at the same
     * time. If any computation fails because of an exception, or if any returns an application error, other
     * computations are interrupted. Then, the exception is re-thrown, or the error value returned.
     */
    public static <T> List<T> parLimit(int parallelism, List<Callable<T>> fs) throws Exception {
        return supervised(scope -> {
            var s = new Semaphore(parallelism);
            var forks = fs.stream().map(f -> scope.fork(() -> {
                s.acquire();
                var r = f.call();
                // no try-finally as there's no point in releasing in case of an exception, as any newly started forks will be interrupted
                s.release();
                return r;
            })).toList();
            var results = new ArrayList<T>();
            for (Fork<T> fork : forks) {
                results.add(fork.join());
            }
            return results;
        });
    }
}
