package com.softwaremill.jox.structured;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public interface CancellableFork<T> extends UnsupervisedFork<T> {
    /**
     * Interrupts the fork, and blocks until it completes with a result.
     *
     * @throws Exception When the cancelled fork threw an exception. This includes {@link InterruptedException}, which
     *                   might be thrown when cancelling the fork.
     */
    T cancel() throws Exception;

    /**
     * Interrupts the fork, and returns immediately, without waiting for the fork to complete. Note that the enclosing scope will only
     * complete once all forks have completed.
     */
    void cancelNow();
}

class CancellableForkUsingResult<T> extends ForkUsingResult<T> implements CancellableFork<T> {
    private final Semaphore done;
    private final AtomicBoolean started;

    CancellableForkUsingResult(CompletableFuture<T> result, Semaphore done, AtomicBoolean started) {
        super(result);
        this.done = done;
        this.started = started;
    }

    @Override
    public T cancel() throws Exception {
        cancelNow();
        return join();
    }

    @Override
    public void cancelNow() {
        // will cause the scope to end, interrupting the task if it hasn't yet finished (or potentially never starting it)
        done.release();
        if (!started.getAndSet(true)) {
            result.completeExceptionally(new InterruptedException("fork was cancelled before it started"));
        }
    }
}
