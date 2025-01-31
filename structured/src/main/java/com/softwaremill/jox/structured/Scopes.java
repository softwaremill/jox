package com.softwaremill.jox.structured;

import java.util.concurrent.ExecutionException;

public class Scopes {
    /**
     * Starts a new concurrency scope, which allows starting forks in the given code block {@code f}. Forks can be
     * started using {@link UnsupervisedScope#forkUnsupervised} and {@link UnsupervisedScope#forkCancellable}. All forks
     * are guaranteed to complete before this scope completes.
     * <p>
     * It is advisable to use {@link #supervised(Scoped)} scopes if possible, as they minimise the chances of an error
     * to go unnoticed.
     * <p>
     * The scope is ran in unsupervised mode, that is:
     *
     * <ul>
     * <li>the scope ends once the {@code f} body completes; this causes any running forks started within
     * {@code f} to be cancelled
     * <li>the scope completes (that is, this method returns) only once all forks started by {@code f} have
     * completed (either successfully, or with an exception)
     * <li>fork failures aren't handled in any special way, but can be inspected using {@link Fork#join()}
     * </ul>
     * <p>
     * Upon successful completion, returns the result of evaluating {@code f}. Upon failure, that is an exception
     * thrown by {@code f}, it is re-thrown, wrapped with an {@link JoxScopeExecutionException}.
     *
     * @throws JoxScopeExecutionException When the scope's body throws an exception
     * @see #supervised(Scoped) Starts a scope in supervised mode
     */
    public static <T> T unsupervised(ScopedUnsupervised<T> f) throws InterruptedException {
        return scopedWithCapability(new Scope(new NoOpSupervisor()), f::run);
    }

    /**
     * Starts a new concurrency scope, which allows starting forks in the given code block {@code f}. Forks can be
     * started using {@link Scope#fork}, {@link Scope#forkUser}, {@link UnsupervisedScope#forkCancellable} or
     * {@link UnsupervisedScope#forkUnsupervised}. All forks are guaranteed to complete before this scope completes.
     * <p>
     * The scope is ran in supervised mode, that is:
     *
     * <ul>
     * <li>the scope ends once all user, supervised forks (started using {@link Scope#forkUser}), including the
     * {@code f} body, succeed. Forks started using {@link Scope#fork}  (daemon) don't have to complete
     * successfully for the scope to end.
     * <li>the scope also ends once the first supervised fork (including the {@code f} main body) fails with an
     * exception
     * <li>when the scope <strong>ends</strong>, all running forks are cancelled
     * <li>the scope <strong>completes</strong> (that is, this method returns) only once all forks started by
     * {@code f} have completed (either successfully, or with an exception)
     * </ul>
     * <p>
     * Upon successful completion, returns the result of evaluating {@code f}. Upon failure, the exception that
     * caused the scope to end is re-thrown, wrapped in an {@link JoxScopeExecutionException} (regardless if the exception was
     * thrown from the main body, or from a fork). Any other exceptions that occur when completing the scope are added
     * as suppressed.
     *
     * @throws JoxScopeExecutionException When the main body, or any of the forks, throw an exception
     * @see #unsupervised(ScopedUnsupervised) Starts a scope in unsupervised mode
     */
    public static <T> T supervised(Scoped<T> f) throws InterruptedException {
        var s = new DefaultSupervisor();
        var capability = new Scope(s);
        try {
            var rawScope = capability.getScope();
            try {
                try {
                    var mainBodyFork = capability.forkUser(() -> f.run(capability));
                    // might throw if any supervised fork threw
                    s.join();
                    // if no exceptions, the main f-fork must be done by now
                    return mainBodyFork.join();
                } finally {
                    rawScope.shutdown();
                    rawScope.join();
                }
                // join might have been interrupted
            } finally {
                rawScope.close();
            }

            // all forks are guaranteed to have finished: some might have ended up throwing exceptions (InterruptedException or
            // others), but only the first one is propagated below. That's why we add all the other exceptions as suppressed.
        } catch (ExecutionException e) {
            // unwrapping execution exception from CompletableFutures to custom exception
            JoxScopeExecutionException joxScopeExecutionException = new JoxScopeExecutionException(e.getCause());
            s.addSuppressedErrors(joxScopeExecutionException);
            throw joxScopeExecutionException;
        } catch (Throwable e) {
            s.addSuppressedErrors(e);
            throw e;
        }
    }

    static <T> T scopedWithCapability(Scope capability, Scoped<T> f) throws InterruptedException {
        var scope = capability.getScope();

        try {
            try {
                return f.run(capability);
            } catch (Exception e) {
                throw new JoxScopeExecutionException(e);
            } finally {
                scope.shutdown();
                scope.join();
            }
            // join might have been interrupted
        } finally {
            scope.close();
        }
    }
}
