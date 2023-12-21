package com.softwaremill.jox;

import java.util.concurrent.*;

public class TestUtil {
    public static void scoped(ConsumerWithException<StructuredTaskScope<Object>> f) throws InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // making sure everything runs in a VT
            scope.fork(() -> {
                f.accept(scope);
                return null;
            });
            scope.join().throwIfFailed();
        }
    }

    public static <T> Future<T> fork(StructuredTaskScope<Object> scope, Callable<T> c) {
        var f = new CompletableFuture<T>();
        scope.fork(() -> {
            try {
                f.complete(c.call());
            } catch (Exception ex) {
                f.completeExceptionally(ex);
            }
            return null;
        });
        return f;
    }

    public static Fork<Void> forkCancelable(StructuredTaskScope<Object> scope, RunnableWithException c) {
        return forkCancelable(scope, () -> {
            c.run();
            return null;
        });
    }

    public static <T> Fork<T> forkCancelable(StructuredTaskScope<Object> scope, Callable<T> c) {
        var f = new CompletableFuture<T>();
        var t = Thread.ofVirtual().start(() -> {
            try {
                f.complete(c.call());
            } catch (Exception ex) {
                f.completeExceptionally(ex);
            }
        });
        // supervisor
        scope.fork(() -> {
            try {
                f.get();
            } catch (InterruptedException e) {
                t.interrupt();
            } catch (ExecutionException e) {
                if (!(e.getCause() instanceof InterruptedException)) {
                    throw e;
                } // else ignore, already interrupted
            } finally {
                t.join();
            }
            return null;
        });
        return new Fork<>() {
            @Override
            public T get() throws ExecutionException, InterruptedException {
                return f.get();
            }

            @Override
            public T get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
                return f.get(timeout, unit);
            }

            @Override
            public Object cancel() throws InterruptedException, ExecutionException {
                t.interrupt();
                t.join();
                if (f.isCompletedExceptionally()) {
                    return f.exceptionNow();
                } else {
                    return f.get();
                }
            }
        };
    }

    public static Future<Void> forkVoid(StructuredTaskScope<Object> scope, RunnableWithException r) {
        return fork(scope, () -> {
            r.run();
            return null;
        });
    }

    @FunctionalInterface
    public interface ConsumerWithException<T> {
        void accept(T o) throws Exception;
    }

    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }

    public interface Fork<T> {
        T get() throws ExecutionException, InterruptedException;

        T get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException;

        /**
         * Either an exception, or T. Waits for the fork to complete.
         */
        Object cancel() throws InterruptedException, ExecutionException;
    }

    public static void timed(String label, RunnableWithException block) throws Exception {
        var start = System.nanoTime();
        block.run();
        var end = System.nanoTime();
        System.out.println(label + " took: " + (end - start) / 1_000_000 + " ms");
    }
}
