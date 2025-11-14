package com.softwaremill.jox.structured;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

public interface CancellableFork<T> extends Fork<T> {
    /**
     * Interrupts the fork, and blocks until it completes with a result.
     *
     * @throws ExecutionException When the cancelled fork threw an exception.
     */
    T cancel() throws InterruptedException, ExecutionException;

    /**
     * Interrupts the fork, and returns immediately, without waiting for the fork to complete. Note
     * that the enclosing scope will only complete once all forks have completed.
     */
    void cancelNow();
}

final class CancellableForkUsingResult<T> extends ForkUsingResult<T> implements CancellableFork<T> {
    /** interrupt signal */
    final Semaphore done = new Semaphore(0);
    private volatile boolean started;
    /** VarHandle for atomic operations on the 'started' field */
    private static final VarHandle STARTED;
    static {
        try {
            MethodHandles.Lookup l = // MethodHandles.lookup()
                    MethodHandles.privateLookupIn(
                            CancellableForkUsingResult.class, MethodHandles.lookup());
            STARTED = l.findVarHandle(CancellableForkUsingResult.class, "started", boolean.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public T cancel() throws InterruptedException, ExecutionException {
        cancelNow();
        return join(); // ForkUsingResult#join throws InterruptedException,ExecutionException
    }

    @Override
    public void cancelNow() {
        // will cause the scope to end, interrupting the task if it hasn't yet finished (or
        // potentially never starting it)
        done.release();
        if (checkNotStartedThenStart()) { // !started.getAndSet(true)
            completeExceptionally(new InterruptedException("fork was cancelled before it started"));
        }
    }

    boolean checkNotStartedThenStart() {
        return (Boolean) STARTED.getAndSet(this, true) == false;
    }
}
