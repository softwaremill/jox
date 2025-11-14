package com.softwaremill.jox.structured;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.softwaremill.jox.Channel;

final class Supervisor {
    private final AtomicInteger runningUserForks = new AtomicInteger(0);
    // used a single-complete cell to record the first exception (or success)
    private final CompletableFuture<Object> result = new CompletableFuture<>();
    private final Set<Throwable> otherExceptions = ConcurrentHashMap.newKeySet();
    private final Channel<SupervisorCommand> commands = Channel.newBufferedDefaultChannel();

    void forkUserStarts() {
        runningUserForks.incrementAndGet();
    }

    void forkUserSuccess() {
        int v = runningUserForks.decrementAndGet();
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

interface SupervisorCommand {}

interface RunFork<T> extends SupervisorCommand, Callable<T> {}
