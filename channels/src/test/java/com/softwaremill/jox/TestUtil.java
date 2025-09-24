package com.softwaremill.jox;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TestUtil {
    public static void scoped(ConsumerWithException<VirtualThreadScope> f)
            throws InterruptedException, ExecutionException {
        var scope = new VirtualThreadScope();
        // Run the test logic in a virtual thread
        var mainTask =
                Thread.ofVirtual()
                        .start(
                                () -> {
                                    try {
                                        f.accept(scope);
                                    } catch (Exception e) {
                                        scope.completeExceptionally(e);
                                    }
                                });
        mainTask.join();
        scope.waitForCompletion();
    }

    public static <T> Future<T> fork(VirtualThreadScope scope, Callable<T> c) {
        var f = new CompletableFuture<T>();
        Thread.ofVirtual()
                .start(
                        () -> {
                            try {
                                f.complete(c.call());
                            } catch (Exception ex) {
                                f.completeExceptionally(ex);
                            }
                        });
        scope.addThread(f);
        return f;
    }

    public static Fork<Void> forkCancelable(VirtualThreadScope scope, RunnableWithException c) {
        return forkCancelable(
                scope,
                () -> {
                    c.run();
                    return null;
                });
    }

    public static <T> Fork<T> forkCancelable(VirtualThreadScope scope, Callable<T> c) {
        var f = new CompletableFuture<T>();
        var t =
                Thread.ofVirtual()
                        .start(
                                () -> {
                                    try {
                                        f.complete(c.call());
                                    } catch (Exception ex) {
                                        f.completeExceptionally(ex);
                                    }
                                });

        return new Fork<>() {
            @Override
            public T get() throws ExecutionException, InterruptedException {
                return f.get();
            }

            @Override
            public T get(long timeout, TimeUnit unit)
                    throws ExecutionException, InterruptedException, TimeoutException {
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

    public static Future<Void> forkVoid(VirtualThreadScope scope, RunnableWithException r) {
        return fork(
                scope,
                () -> {
                    r.run();
                    return null;
                });
    }

    // Simple scope implementation that tracks virtual threads
    // Once StructuredTaskScope is stabilized, we'll use that
    public static class VirtualThreadScope {
        private final List<CompletableFuture<?>> futures = new ArrayList<>();
        private volatile Exception exception;

        public synchronized void addThread(CompletableFuture<?> future) {
            futures.add(future);
        }

        public void completeExceptionally(Exception e) {
            this.exception = e;
        }

        public void waitForCompletion() throws InterruptedException, ExecutionException {
            if (exception != null) {
                throw new ExecutionException(exception);
            }
            // Wait for all futures to complete
            synchronized (this) {
                for (CompletableFuture<?> future : futures) {
                    try {
                        future.get();
                    } catch (ExecutionException e) {
                        if (exception == null) {
                            exception = (Exception) e.getCause();
                        }
                    }
                }
            }
            if (exception != null) {
                throw new ExecutionException(exception);
            }
        }
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

        T get(long timeout, TimeUnit unit)
                throws ExecutionException, InterruptedException, TimeoutException;

        /** Either an exception, or T. Waits for the fork to complete. */
        Object cancel() throws InterruptedException, ExecutionException;
    }

    public static void timed(String label, RunnableWithException block) throws Exception {
        var start = System.nanoTime();
        block.run();
        var end = System.nanoTime();
        System.out.println(label + " took: " + (end - start) / 1_000_000 + " ms");
    }

    public static List<String> drainChannel(Channel<String> ch) throws InterruptedException {
        var result = new ArrayList<String>();
        while (true) {
            var e = ch.receiveOrClosed();
            if (e instanceof ChannelDone) {
                return result;
            } else {
                result.add((String) e);
            }
        }
    }

    public static int countOccurrences(String str, String subStr) {
        int count = 0;
        int idx = 0;

        while ((idx = str.indexOf(subStr, idx)) != -1) {
            count++;
            idx += subStr.length();
        }

        return count;
    }
}
