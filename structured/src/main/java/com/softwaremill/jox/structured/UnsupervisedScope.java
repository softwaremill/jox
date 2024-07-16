package com.softwaremill.jox.structured;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.StructuredTaskScope;

/**
 * Capability granted by an {@link Scopes#unsupervised(ScopedUnsupervised)} concurrency scope (as well as, via
 * subtyping, by {@link Scopes#supervised(Scoped)}).
 * <p>
 * Represents a capability to fork unsupervised, asynchronously running computations in a concurrency scope. Such forks
 * can be created using {@link Scope#forkUnsupervised} or {@link Scope#forkCancellable}.
 *
 * @see Scopes#supervised(Scoped)
 */
public abstract class UnsupervisedScope {
    abstract StructuredTaskScope<Object> getScope();

    abstract Queue<Runnable> getFinalizers();

    abstract Supervisor getSupervisor();

    abstract void addFinalizer(Runnable f);

    /**
     * Starts a fork (logical thread of execution), which is guaranteed to complete before the enclosing
     * {@link Scopes#supervised(Scoped)}, or {@link Scopes#unsupervised(ScopedUnsupervised)} block completes.
     * <p>
     * In case an exception is thrown while evaluating <code>f</code>, it will be thrown when calling the returned
     * {@link UnsupervisedFork}'s <code>.join()</code> method.
     * <p>
     * Success or failure isn't signalled to the enclosing scope, and doesn't influence the scope's lifecycle.
     * <p>
     * For alternate behaviors, see {@link Scope#fork}, {@link Scope#forkUser}, {@link Scope#forkCancellable}.
     */
    public <T> UnsupervisedFork<T> forkUnsupervised(Callable<T> f) {
        var result = new CompletableFuture<T>();
        getScope().fork(() -> {
            try {
                result.complete(f.call());
            } catch (Throwable e) {
                result.completeExceptionally(e);
            }
            return null;
        });
        return new UnsupervisedForkUsingResult<>(result);
    }
}
