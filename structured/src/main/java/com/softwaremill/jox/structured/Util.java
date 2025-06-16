package com.softwaremill.jox.structured;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class Util {
    /**
     * Prevent {@code f} from being interrupted. Any interrupted exceptions that occur while
     * evaluating {@code f} will be re-thrown once it completes.
     */
    public static <T> T uninterruptible(Callable<T> f)
            throws ExecutionException, InterruptedException {
        return Scopes.unsupervised(
                c -> {
                    var fork = c.forkUnsupervised(f);
                    InterruptedException caught = null;
                    try {
                        while (true) {
                            try {
                                return fork.join();
                            } catch (InterruptedException e) {
                                caught = e;
                            }
                        }
                    } finally {
                        if (caught != null) {
                            throw caught;
                        }
                    }
                });
    }
}
