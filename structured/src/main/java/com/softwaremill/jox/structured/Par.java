package com.softwaremill.jox.structured;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

import static com.softwaremill.jox.structured.Scopes.supervised;

public class Par {
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
