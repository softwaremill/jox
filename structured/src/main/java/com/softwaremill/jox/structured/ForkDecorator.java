package com.softwaremill.jox.structured;

import java.util.concurrent.Callable;

/**
 * Decorates the body of a fork, so that state of the thread which starts the fork can be restored
 * around it.
 *
 * <p>A {@link java.util.function.UnaryOperator} of {@link Callable} wouldn't do: {@link #decorate}
 * is generic in the fork's result type, which keeps the decorated body's type the same as the
 * original one. Otherwise, the result would have to be cast.
 *
 * <p>Can't be a lambda, as a lambda can't implement a generic method; use a named class, an
 * anonymous one, or - for a stateless decorator - a single-element enum.
 *
 * <p>A single instance is shared by all the forks of all the scopes which run within the binding of
 * {@link Scope#FORK_DECORATOR} - the bound value isn't copied - so implementations should be
 * thread-safe, and keep any per-fork state in the returned {@link Callable}.
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
