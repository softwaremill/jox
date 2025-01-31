package com.softwaremill.jox.structured;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Capability granted by an {@link Scopes#supervised(Scoped)} or {@link Scopes#unsupervised(ScopedUnsupervised)}
 * concurrency scope.
 * <p>
 * Represents a capability to fork supervised or unsupervised, asynchronously running computations in a concurrency
 * scope. Such forks can be created using {@link Scope#fork}, {@link Scope#forkUser},
 * {@link UnsupervisedScope#forkCancellable} or {@link UnsupervisedScope#forkUnsupervised}.
 *
 * @see ScopedUnsupervised
 */
public class Scope extends UnsupervisedScope {


    private final StructuredTaskScope<Object> scope;
    private final Supervisor supervisor;
    private final Lock externalSchedulerLock = new ReentrantLock();
    private volatile ActorRef<ExternalScheduler> externalSchedulerActor;

    Scope(Supervisor supervisor) {
        this.scope = new DoNothingScope();
        this.supervisor = supervisor;
    }

    @Override
    StructuredTaskScope<Object> getScope() {
        return scope;
    }

    @Override
    Supervisor getSupervisor() {
        return supervisor;
    }

    /**
     * Starts a fork (logical thread of execution), which is guaranteed to complete before the enclosing
     * {@link Scopes#supervised(Scoped)} block completes.
     * <p>
     * The fork behaves as a daemon thread. That is, if the body of the scope completes successfully, and all other user
     * forks (created using {@link #forkUser(Callable)}) complete successfully, the scope will end, cancelling all
     * running forks (including this one, if it's still running). That is, successful completion of this fork isn't
     * required to end the scope.
     * <p>
     * An exception thrown while evaluating {@code f} will cause the fork to fail and the enclosing scope to end
     * (cancelling all other running forks).
     * <p>
     * For alternate behaviors regarding ending the scope, see {@link #forkUser},
     * {@link UnsupervisedScope#forkCancellable} and {@link UnsupervisedScope#forkUnsupervised}.
     */
    public <T> Fork<T> fork(Callable<T> f) {
        var result = new CompletableFuture<T>();
        getScope().fork(() -> {
            try {
                result.complete(f.call());
            } catch (Throwable e) {
                // we notify the supervisor first, so that if this is the first failing fork in the scope, the supervisor will
                // get first notified of the exception by the "original" (this) fork
                // if the supervisor doesn't end the scope, the exception will be thrown when joining the result; otherwise, not
                // completing the result; any joins will end up being interrupted
                if (!supervisor.forkException(e)) {
                    result.completeExceptionally(e);
                }
            }
            return null;
        });
        return new ForkUsingResult(result);
    }

    /**
     * Starts a fork (logical thread of execution), which is guaranteed to complete before the enclosing
     * {@link Scopes#supervised(Scoped)} block completes.
     * <p>
     * The fork behaves as a user-level thread. That is, the scope won't end until the body of the scope, and all other
     * user forks (including this one) complete successfully. That is, successful completion of this fork is required to
     * end the scope.
     * <p>
     * An exception thrown while evaluating {@code f} will cause the enclosing scope to end (cancelling all other
     * running forks).
     * <p>
     * For alternate behaviors regarding ending the scope, see {@link #fork}, {@link UnsupervisedScope#forkCancellable}
     * and {@link UnsupervisedScope#forkUnsupervised}.
     */
    public <T> Fork<T> forkUser(Callable<T> f) {
        var result = new CompletableFuture<T>();
        getSupervisor().forkStarts();
        getScope().fork(() -> {
            try {
                result.complete(f.call());
                getSupervisor().forkSuccess();
            } catch (Throwable e) {
                if (!supervisor.forkException(e)) {
                    result.completeExceptionally(e);
                }
            }
            return null;
        });
        return new ForkUsingResult(result);
    }

    /**
     * Returns a concurrency-scope-specific runner, which allows scheduling of functions to be run within the current concurrency scope, from
     * the context of arbitrary threads (not necessarily threads that are part of the current concurrency scope).
     * <p>
     * Usage: obtain a runner from within a concurrency scope, while on a fork/thread that is managed by the concurrency scope. Then, pass that
     * runner to the external library. It can then schedule functions (e.g. create forks) to be run within the concurrency scope from arbitary
     * threads, as long as the concurrency scope isn't complete.
     * <p>
     * Execution is scheduled through an {@link ActorRef}, which is lazily created, and bound to an {@link Scope} instances.
     * <p>
     * This method should **only** be used when integrating `Jox` with libraries that manage concurrency on their own, and which run callbacks on
     * a managed thread pool. The logic executed by the third-party library should be entirely contained within the lifetime of this
     * concurrency scope. The sole purpose of this method is to enable running scope-aware logic from threads **other** than Jox-managed.
     * <p>
     * Use with care!
     *
     * @see ExternalRunner#runAsync(ThrowingConsumer) for running functions within the scope
     */
    public ExternalRunner externalRunner() {
        if (externalSchedulerActor == null) {
            externalSchedulerLock.lock();
            try {
                if (externalSchedulerActor == null) {
                    externalSchedulerActor = ActorRef.create(this, r -> r.accept(Scope.this));
                }
            } finally {
                externalSchedulerLock.unlock();
            }
        }
        return new ExternalRunner(externalSchedulerActor);
    }
}

class DoNothingScope extends StructuredTaskScope<Object> {
    public DoNothingScope() {
        super(null, Thread.ofVirtual().factory());
    }
}

interface ExternalScheduler {
    void run(ThrowingConsumer<Scope> r) throws Exception;
}

