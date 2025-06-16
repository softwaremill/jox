package com.softwaremill.jox.structured;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * A fork started using {@link Scope#fork}, {@link Scope#forkUser}, {@link
 * UnsupervisedScope#forkCancellable} or {@link UnsupervisedScope#forkUnsupervised}, backed by a
 * (virtual) thread.
 */
public interface Fork<T> {
    /**
     * Blocks until the fork completes with a result.
     *
     * @throws ExecutionException If the fork completed with an exception, and is unsupervised
     *     (started with {@link UnsupervisedScope#forkUnsupervised} or {@link
     *     UnsupervisedScope#forkCancellable}).
     */
    T join() throws InterruptedException, ExecutionException;
}

class ForkUsingResult<T> implements Fork<T> {
    protected final CompletableFuture<T> result;

    ForkUsingResult(CompletableFuture<T> result) {
        this.result = result;
    }

    @Override
    public T join() throws InterruptedException, ExecutionException {
        return result.get();
    }
}
