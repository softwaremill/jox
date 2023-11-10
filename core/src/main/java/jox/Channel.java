package jox;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.LockSupport;

import static jox.CellState.*;

public class Channel<T> {
    private final AtomicLong senders = new AtomicLong(0L);
    private final AtomicLong receivers = new AtomicLong(0L);
    private final AtomicReferenceArray<Object> buffer = new AtomicReferenceArray<>(40_000_000); // TODO

    private void setElement(T value, long index) {
        buffer.setPlain((int) index * 2, value);
    }

    private void setState(Object state, long index) {
        buffer.set((int) index * 2 + 1, state);
    }

    private T getElement(long index) {
        //noinspection unchecked
        return (T) buffer.getPlain((int) index * 2);
    }

    private Object getState(long index) {
        return buffer.get((int) index * 2 + 1);
    }

    private boolean casState(long index, Object expected, Object newValue) {
        return buffer.compareAndSet((int) index * 2 + 1, expected, newValue);
    }

    //

    public void send(T value) throws InterruptedException {
        sendSafe(value); // TODO exceptions
    }

    /**
     * @return T | ???
     */
    public Object sendSafe(T value) throws InterruptedException {
        while (true) {
            var s = senders.incrementAndGet();
            setElement(value, s);
            if (updateCellSend(s)) return null;
        }
    }

    private boolean updateCellSend(long s) throws InterruptedException {
        while (true) {
            var state = getState(s);
            var r = receivers.get();

            switch (state) {
                case null -> {
                    if (s >= r) {
                        var c = new Continuation();
                        if (casState(s, null, c)) {
                            c.await(() -> {
                                setElement(null, s);
                                setState(INTERRUPTED, s);
                            });
                            return true;
                        }
                        // else: next iteration
                    } else {
                        if (casState(s, null, BUFFERED)) {
                            return true;
                        }
                        // else: next iteration
                    }
                }
                case Continuation c -> {
                    if (c.tryResume()) {
                        setState(DONE, s);
                        return true;
                    } else {
                        setElement(null, s);
                        return false;
                    }
                }
                case INTERRUPTED, BROKEN -> {
                    setElement(null, s);
                    return false;
                }
                default -> throw new IllegalStateException("Unexpected state: " + state);
            }
        }
    }

    //

    public T receive() throws InterruptedException {
        return (T) receiveSafe(); // TODO exceptions
    }

    public Object receiveSafe() throws InterruptedException {
        while (true) {
            var r = receivers.incrementAndGet();
            if (updateCellReceive(r)) {
                var e = getElement(r);
                setElement(null, r);
                return e;
            }
        }
    }

    private boolean updateCellReceive(long r) throws InterruptedException {
        while (true) {
            var state = getState(r);
            var s = senders.get();

            switch (state) {
                case null -> {
                    if (r >= s) {
                        var c = new Continuation();
                        if (casState(s, null, c)) {
                            c.await(() -> setState(INTERRUPTED, r));
                            return true;
                        }
                        // else: next iteration
                    } else {
                        if (casState(s, null, BROKEN)) {
                            return false;
                        }
                        // else: next iteration
                    }
                }
                case Continuation c -> {
                    if (c.tryResume()) {
                        setState(DONE, r);
                        return true;
                    } else {
                        return false;
                    }
                }
                case BUFFERED -> {
                    return true;
                }
                case INTERRUPTED -> {
                    return false;
                }
                default -> throw new IllegalStateException("Unexpected state: " + state);
            }
        }
    }
}

enum CellState {
    DONE,
    INTERRUPTED,
    BUFFERED,
    BROKEN;
}

class Continuation {
    private final Thread creatingThread = Thread.currentThread();
    private volatile int state = 0; // 0 - initial; 1 - done; 2 - interrupted

    public boolean tryResume() {
        var result = Continuation.STATE.compareAndSet(this, 0, 1);
        LockSupport.unpark(creatingThread);
        return result;
    }

    public void await(Runnable onInterrupt) throws InterruptedException {
        var spinIterations = 1000;
        var yieldIterations = 2;
        while (state == 0) {
            if (spinIterations > 0) {
                Thread.onSpinWait();
                spinIterations -= 1;
            } else if (yieldIterations > 0) {
                Thread.yield();
                yieldIterations -= 1;
            } else {
                LockSupport.park();
            }

            if (Thread.interrupted()) {
                Continuation.STATE.compareAndSet(this, 0, 2); // TODO if
                var e = new InterruptedException();

                try {
                    onInterrupt.run();
                } catch (Throwable ee) {
                    e.addSuppressed(ee);
                }

                throw e;
            }
        }
    }

    //

    private static final VarHandle STATE;

    static {
        var l = MethodHandles.lookup();
        try {
            STATE = l.findVarHandle(Continuation.class, "state", int.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}