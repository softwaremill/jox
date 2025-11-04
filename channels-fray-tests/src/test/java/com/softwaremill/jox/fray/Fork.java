package com.softwaremill.jox.fray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

interface Fork<T> {
    void start();

    void interrupt();

    T join() throws InterruptedException;

    static Fork<Void> newNoResult(RunnableWithException runnable) {
        var thread =
                new Thread(
                        () -> {
                            try {
                                runnable.run();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
        return new Fork<>() {
            @Override
            public void start() {
                thread.start();
            }

            @Override
            public void interrupt() {
                thread.interrupt();
            }

            @Override
            public Void join() throws InterruptedException {
                thread.join();
                return null;
            }
        };
    }

    static <T> Fork<T> newWithResult(Callable<T> callable) {
        var result = new AtomicReference<T>();
        var thread =
                new Thread(
                        () -> {
                            try {
                                result.set(callable.call());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });

        return new Fork<>() {
            @Override
            public void start() {
                thread.start();
            }

            @Override
            public void interrupt() {
                thread.interrupt();
            }

            @Override
            public T join() throws InterruptedException {
                thread.join();
                return result.get();
            }
        };
    }

    static void startAll(Fork<?>... fork) {
        for (Fork<?> f : fork) {
            f.start();
        }
    }

    @SafeVarargs
    static <T> List<T> joinAll(Fork<T>... fork) throws InterruptedException {
        var result = new ArrayList<T>();
        for (Fork<T> f : fork) {
            result.add(f.join());
        }
        return result;
    }
}
