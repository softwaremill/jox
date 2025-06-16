package com.softwaremill.jox.structured;

import static com.softwaremill.jox.structured.Scopes.scopedWithCapability;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Capability granted by an {@link Scopes#unsupervised(ScopedUnsupervised)} concurrency scope (as
 * well as, via subtyping, by {@link Scopes#supervised(Scoped)}).
 *
 * <p>Represents a capability to fork unsupervised, asynchronously running computations in a
 * concurrency scope. Such forks can be created using {@link UnsupervisedScope#forkUnsupervised} or
 * {@link UnsupervisedScope#forkCancellable}.
 *
 * @see Scopes#supervised(Scoped)
 */
public abstract class UnsupervisedScope {
    abstract StructuredTaskScope<Object> getScope();

    abstract Supervisor getSupervisor();

    /**
     * Starts a fork (logical thread of execution), which is guaranteed to complete before the
     * enclosing {@link Scopes#supervised(Scoped)}, or {@link
     * Scopes#unsupervised(ScopedUnsupervised)} block completes.
     *
     * <p>In case an exception is thrown while evaluating {@code f}, it will be thrown when calling
     * the returned {@link Fork}'s <code>.join()</code> method.
     *
     * <p>Success or failure isn't signalled to the enclosing scope, and doesn't influence the
     * scope's lifecycle.
     *
     * <p>For alternate behaviors, see {@link Scope#fork}, {@link Scope#forkUser}, {@link
     * #forkCancellable}.
     */
    public <T> Fork<T> forkUnsupervised(Callable<T> f) {
        var result = new CompletableFuture<T>();
        getScope()
                .fork(
                        () -> {
                            try {
                                result.complete(f.call());
                            } catch (Throwable e) {
                                result.completeExceptionally(e);
                            }
                            return null;
                        });
        return new ForkUsingResult<>(result);
    }

    /**
     * Starts a fork (logical thread of execution), which is guaranteed to complete before the
     * enclosing {@link Scopes#supervised(Scoped)}, or {@link
     * Scopes#unsupervised(ScopedUnsupervised)} block completes, and which can be cancelled
     * on-demand.
     *
     * <p>In case an exception is thrown while evaluating {@code f}, it will be thrown when calling
     * the returned {@link CancellableFork}'s {@code .join()} method.
     *
     * <p>The fork is unsupervised (similarly to {@link #forkUnsupervised(Callable)}), hence success
     * or failure isn't signalled to the enclosing scope and doesn't influence the scope's
     * lifecycle.
     *
     * <p>For alternate behaviors, see {@link Scope#fork}, {@link Scope#forkUser} and {@link
     * #forkUnsupervised}.
     *
     * <p>Implementation note: a cancellable fork is created by starting a nested scope in a fork,
     * and then starting a fork there. Hence, it is more expensive than {@link Scope#fork}, as two
     * virtual threads are started.
     */
    public <T> CancellableFork<T> forkCancellable(Callable<T> f) {
        var result = new CompletableFuture<T>();
        // forks can be never run, if they are cancelled immediately - we need to detect this, not
        // to await on result.get()
        var started = new AtomicBoolean(false);
        // interrupt signal
        var done = new Semaphore(0);
        getScope()
                .fork(
                        () -> {
                            var nestedCapability = new Scope(new NoOpSupervisor());
                            scopedWithCapability(
                                    nestedCapability,
                                    cap2 -> {
                                        nestedCapability
                                                .getScope()
                                                .fork(
                                                        () -> {
                                                            // "else" means that the fork is already
                                                            // cancelled, so doing nothing in that
                                                            // case
                                                            if (!started.getAndSet(true)) {
                                                                try {
                                                                    result.complete(f.call());
                                                                } catch (Exception e) {
                                                                    result.completeExceptionally(e);
                                                                }
                                                            }

                                                            done.release(); // the nested scope
                                                            // can now finish
                                                            return null;
                                                        });
                                        done.acquire();
                                        return null;
                                    });
                            return null;
                        });
        return new CancellableForkUsingResult<>(result, done, started);
    }
}
