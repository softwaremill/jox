package com.softwaremill.jox.structured;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

sealed interface Supervisor permits NoOpSupervisor, DefaultSupervisor {
    void forkStarts();

    void forkSuccess();

    boolean forkException(Throwable e);
}

final class NoOpSupervisor implements Supervisor {
    @Override
    public void forkStarts() {}

    @Override
    public void forkSuccess() {}

    @Override
    public boolean forkException(Throwable e) {
        return false;
    }
}

final class DefaultSupervisor implements Supervisor {
    private final AtomicInteger running = new AtomicInteger(0);
    private final CompletableFuture<Object> result = new CompletableFuture<>();
    private final Set<Throwable> otherExceptions = ConcurrentHashMap.newKeySet();

    @Override
    public void forkStarts() {
        running.incrementAndGet();
    }

    @Override
    public void forkSuccess() {
        int v = running.decrementAndGet();
        if (v == 0) {
            result.complete(null);
        }
    }

    @Override
    public boolean forkException(Throwable e) {
        if (!result.completeExceptionally(e)) {
            otherExceptions.add(e);
        }
        return true;
    }

    public void join() throws ExecutionException, InterruptedException {
        result.get();
    }

    public void addSuppressedErrors(Throwable e) {
        for (Throwable e2 : otherExceptions) {
            if (!e.equals(e2)) {
                e.addSuppressed(e2);
            }
        }
    }
}
