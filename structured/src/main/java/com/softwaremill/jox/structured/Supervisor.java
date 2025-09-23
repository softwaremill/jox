package com.softwaremill.jox.structured;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import com.softwaremill.jox.Channel;

class Supervisor {
    private final AtomicInteger running = new AtomicInteger(0);
    private final CompletableFuture<Object> result = new CompletableFuture<>();
    private final Set<Throwable> otherExceptions = ConcurrentHashMap.newKeySet();
    private final Channel<SupervisorCommand> commands = Channel.newBufferedDefaultChannel();

    void forkStarts() {
        running.incrementAndGet();
    }

    void forkSuccess() {
        int v = running.decrementAndGet();
        if (v == 0) {
            result.complete(null);
            commands.done();
        }
    }

    boolean forkException(Throwable e) {
        if (!result.completeExceptionally(e)) {
            otherExceptions.add(e);
        } else {
            commands.error(e);
        }
        return true;
    }

    void join() throws ExecutionException, InterruptedException {
        result.get();
    }

    void addSuppressedErrors(Throwable e) {
        for (Throwable e2 : otherExceptions) {
            if (!e.equals(e2)) {
                e.addSuppressed(e2);
            }
        }
    }

    Channel<SupervisorCommand> getCommands() {
        return commands;
    }
}

sealed interface SupervisorCommand permits RunFork {}

record RunFork<T>(Callable<T> f) implements SupervisorCommand {}
