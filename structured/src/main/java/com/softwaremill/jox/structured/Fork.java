package com.softwaremill.jox.structured;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * A fork started using {@link Scope#fork}, {@link Scope#forkUser}, {@link Scope#forkCancellable} or
 * {@link Scope#forkUnsupervised}, backed by a (virtual) thread.
 */
@FunctionalInterface
public interface Fork<T> {
    /**
     * Blocks until the fork completes with a result.
     *
     * @throws ExecutionException If the fork completed with an exception, and is unsupervised
     *     (started with {@link Scope#forkUnsupervised} or {@link Scope#forkCancellable}).
     */
    T join() throws InterruptedException, ExecutionException;
}

class ForkUsingResult<T> extends CompletableFuture<T> implements Fork<T> {
    /***
     In runtime throws InterruptedException, ExecutionException
     One parent: {@link CompletableFuture#join()} doesn't throw any checked exception ⇒
     we can't add them.
     But the caller's code sees {@link Fork#join()}, which throws checked exceptions, so
     everything looks fine!
     @see CompletableFuture#get()
     */
    @Override
    public T join() {
        try {
            return get();
        } catch (Exception e) { // InterruptedException, ExecutionException, CancellationException
            throw SneakyThrows.sneakyThrow(e);
        }
    }
}
