package com.softwaremill.jox.structured;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.softwaremill.jox.structured.Util.throwUnwrappedExecutionException;

/**
 * A fork started using {@link Scope#fork}, {@link Scope#forkUser}, {@link Scope#forkCancellable} or
 * {@link Scope#forkUnsupervised}, backed by a (virtual) thread.
 */
public interface Fork<T> {
    /**
     * Blocks until the fork completes with a result.
     *
     * @throws Exception If the fork completed with an exception, and is unsupervised (started with
     *                   {@link Scope#forkUnsupervised} or {@link Scope#forkCancellable}).
     */
    T join() throws Exception;
}

class ForkUsingResult<T> implements Fork<T> {
    private final CompletableFuture<T> result;

    ForkUsingResult(CompletableFuture<T> result) {
        this.result = result;
    }

    @Override
    public T join() throws Exception {
        try {
            return result.get();
        } catch (ExecutionException ee) {
            throwUnwrappedExecutionException(ee);
            return null;
        }
    }
}
