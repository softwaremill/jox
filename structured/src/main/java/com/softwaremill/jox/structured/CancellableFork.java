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
    private final Semaphore done;
    // Manual AtomicBoolean: 0 = initially false, 1 = true
    private volatile byte started;
    private static final byte TRUE = 1;

    // VarHandle for atomic operations on the 'started' field
    private static final VarHandle STARTED;
    static {
        try {
            MethodHandles.Lookup l = // MethodHandles.lookup()
                    MethodHandles.privateLookupIn(
                            CancellableForkUsingResult.class, MethodHandles.lookup());
            STARTED = l.findVarHandle(CancellableForkUsingResult.class, "started", byte.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    CancellableForkUsingResult(Semaphore done) {
        this.done = done;
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
        return (byte) STARTED.getAndSet(this, TRUE) == 0;
    }
}
