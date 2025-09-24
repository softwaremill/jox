package com.softwaremill.jox.structured;

public class Scopes {
    /**
     * Starts a new concurrency scope, which allows starting forks in the given code block {@code
     * f}. Forks can be started using {@link Scope#fork}, {@link Scope#forkUser}, {@link
     * Scope#forkCancellable} or {@link Scope#forkUnsupervised}. All forks are guaranteed to
     * complete before this scope completes.
     *
     * <p>The scope is ran in supervised mode, that is:
     *
     * <ul>
     *   <li>the scope ends once all user, supervised forks (started using {@link Scope#forkUser}),
     *       including the {@code f} body, succeed. Forks started using {@link Scope#fork} (daemon)
     *       don't have to complete successfully for the scope to end.
     *   <li>the scope also ends once the first supervised fork (including the {@code f} main body)
     *       fails with an exception
     *   <li>when the scope <strong>ends</strong>, all running forks are cancelled
     *   <li>the scope <strong>completes</strong> (that is, this method returns) only once all forks
     *       started by {@code f} have completed (either successfully, or with an exception)
     * </ul>
     *
     * <p>Upon successful completion, returns the result of evaluating {@code f}. Upon failure, the
     * exception that caused the scope to end is re-thrown, wrapped in an {@link
     * JoxScopeExecutionException} (regardless if the exception was thrown from the main body, or
     * from a fork). Any other exceptions that occur when completing the scope are added as
     * suppressed.
     *
     * @throws JoxScopeExecutionException When the main body, or any of the forks, throw an
     *     exception
     */
    public static <T> T supervised(Scoped<T> f) throws InterruptedException {
        return new Scope().run(f);
    }
}
