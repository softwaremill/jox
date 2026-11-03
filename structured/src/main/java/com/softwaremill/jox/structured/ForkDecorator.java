package com.softwaremill.jox.structured;

import java.util.concurrent.Callable;

/**
 * Decorates the body of a fork, so that state of the thread which starts the fork can be restored
 * around it.
 *
 * <p>A single instance is shared by all the forks of all the scopes which run within the binding of
 * {@link Scope#FORK_DECORATOR}, so implementations should be thread-safe, and keep any per-fork
 * state in the returned {@link Callable}.
 *
 * @see Scope#FORK_DECORATOR
 */
public interface ForkDecorator {
    /**
     * Returns the body to run in the fork, given the original one. Called on the thread which calls
     * {@link Scope#fork} (or one of its variants), so that this thread's state can be captured, and
     * restored around {@code f} when the fork runs.
     */
    <T> Callable<T> decorate(Callable<T> f);
}
