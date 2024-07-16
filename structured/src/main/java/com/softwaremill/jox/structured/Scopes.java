package com.softwaremill.jox.structured;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import static com.softwaremill.jox.structured.Util.uninterruptible;

public class Scopes {
    /**
     * Starts a new concurrency scope, which allows starting forks in the given code block <code>f</code>. Forks can be
     * started using {@link UnsupervisedScope#forkUnsupervised} and {@link UnsupervisedScope#forkCancellable}. All forks
     * are guaranteed to complete before this scope completes.
     * <p>
     * It is advisable to use {@link #supervised(Scoped)} scopes if possible, as they minimise the chances of an error
     * to go unnoticed.
     * <p>
     * The scope is ran in unsupervised mode, that is:
     *
     * <ul>
     * <li>the scope ends once the <code>f</code> body completes; this causes any running forks started within
     * <code>f</code> to be cancelled
     * <li>the scope completes (that is, this method returns) only once all forks started by <code>f</code> have
     * completed (either successfully, or with an exception)
     * <li>fork failures aren't handled in any special way, but can be inspected using {@link Fork#join()}
     * </ul>
     * <p>
     * Upon successful completion, returns the result of evaluating <code>f</code>. Upon failure, that is an exception
     * thrown by <code>f</code>, it is re-thrown.
     *
     * @see #supervised(Scoped) Starts a scope in supervised mode
     */
    public static <T> T unsupervised(ScopedUnsupervised<T> f) throws Exception {
        return scopedWithCapability(new Scope(new NoOpSupervisor()), f::run);
    }

    /**
     * Starts a new concurrency scope, which allows starting forks in the given code block <code>f</code>. Forks can be
     * started using {@link Scope#fork}, {@link Scope#forkUser}, {@link UnsupervisedScope#forkCancellable} or
     * {@link UnsupervisedScope#forkUnsupervised}. All forks are guaranteed to complete before this scope completes.
     * <p>
     * The scope is ran in supervised mode, that is:
     *
     * <ul>
     * <li>the scope ends once all user, supervised forks (started using {@link Scope#forkUser}), including the
     * <code>f</code> body, succeed. Forks started using {@link Scope#fork}  (daemon) don't have to complete
     * successfully for the scope to end.
     * <li>the scope also ends once the first supervised fork (including the <code>f</code> main body) fails with an
     * exception
     * <li>when the scope <strong>ends</strong>, all running forks are cancelled
     * <li>the scope <strong>completes</strong> (that is, this method returns) only once all forks started by
     * <code>f</code> have completed (either successfully, or with an exception)
     * </ul>
     * <p>
     * Upon successful completion, returns the result of evaluating <code>f</code>. Upon failure, the exception that
     * caused the scope to end is re-thrown (regardless if the exception was thrown from the main body, or from a fork).
     * Any other exceptions that occur when completing the scope are added as suppressed.
     *
     * @see #unsupervised(ScopedUnsupervised) Starts a scope in unsupervised mode
     */
    public static <T> T supervised(Scoped<T> f) throws Exception {
        var s = new DefaultSupervisor();
        var capability = new Scope(s);
        try {
            return scopedWithCapability(capability, cap2 -> {
                var mainBodyFork = capability.forkUser(() -> f.run(capability));
                // might throw if any supervised fork threw
                s.join();
                // if no exceptions, the main f-fork must be done by now
                return mainBodyFork.join();
            });
        } catch (Throwable e) {
            // all forks are guaranteed to have finished: some might have ended up throwing exceptions (InterruptedException or
            // others), but only the first one is propagated below. That's why we add all the other exceptions as suppressed.
            s.addSuppressedErrors(e);
            throw e;
        }
    }

    private static void throwWithSuppressed(List<Exception> es) throws Exception {
        Exception e = es.get(0);
        for (int i = 1; i < es.size(); i++) {
            e.addSuppressed(es.get(i));
        }
        throw e;
    }

    private static <T> T runFinalizers(T result, Exception caught, Queue<Runnable> fs) throws Exception {
        if (fs.isEmpty()) {
            if (caught == null) {
                return result;
            } else {
                throw caught;
            }
        } else {
            List<Exception> errors = new ArrayList<>();
            if (caught != null) {
                errors.add(caught);
            }

            // running the finalizers in reverse order
            Stack<Runnable> fs2 = new Stack<>();
            while (!fs.isEmpty()) {
                fs2.push(fs.poll());
            }

            List<Exception> es = uninterruptible(() -> {
                for (Runnable f : fs2) {
                    try {
                        f.run();
                    } catch (Exception e) {
                        errors.add(e);
                    }
                }
                return errors;
            });

            if (!es.isEmpty()) {
                throwWithSuppressed(es);
            }
            return result;
        }
    }

    static <T> T scopedWithCapability(Scope capability, Scoped<T> f) throws Exception {
        var scope = capability.getScope();
        var finalizers = capability.getFinalizers();

        try {
            T t;
            try {
                try {
                    t = f.run(capability);
                } finally {
                    scope.shutdown();
                    scope.join();
                }
                // join might have been interrupted
            } finally {
                scope.close();
            }

            // running the finalizers only once we are sure that all child threads have been terminated, so that no new
            // finalizers are added, and none are lost
            return runFinalizers(t, null, finalizers);
        } catch (Exception e) {
            return runFinalizers(null, e, finalizers);
        }
    }
}
