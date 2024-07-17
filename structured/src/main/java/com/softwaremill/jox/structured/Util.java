package com.softwaremill.jox.structured;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class Util {
    /**
     * Prevent {@code f} from being interrupted. Any interrupted exceptions that occur while evaluating
     * {@code f} will be re-thrown once it completes.
     */
    public static <T> T uninterruptible(Callable<T> f) throws Exception {
        return Scopes.unsupervised(c -> {
            var fork = c.forkUnsupervised(f);
            Exception caught = null;
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

    static void throwUnwrappedExecutionException(ExecutionException ee) throws Exception {
        switch (ee.getCause()) {
            case Exception e -> throw e;
            case Error e -> throw e;
            default -> throw new RuntimeException(ee.getCause());
        }
    }
}
