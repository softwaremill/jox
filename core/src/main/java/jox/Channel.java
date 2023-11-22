package jox;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiConsumer;

import static jox.CellState.*;

public class Channel<T> {
    /*
    Inspired by the "Fast and Scalable Channels in Kotlin Coroutines" paper (https://arxiv.org/abs/2211.04986), and
    the Kotlin implementation (https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/common/src/channels/BufferedChannel.kt).

    Notable differences from the Kotlin implementation:
    * in Kotlin's channels, the buffer stores both the elements (in even indexes), and the state for each cell (in odd
      indexes). This would be also possible here, but in two-thread rendezvous tests, this is slightly slower than the
      approach below: we transmit the elements inside objects representing state. This does incur an additional
      allocation in case of the `Buffered` state (when there's a waiting receiver - we can't simply use a constant).
      However, we add a field to `Continuation` (which is a channel-specific class, unlike in Kotlin), to avoid the
      allocation when the sender suspends.
    * as we don't directly store elements in the buffer, we don't need to clear them on interrupt etc. This is done
      automatically when the cell's state is set to something else than a Continuation/Buffered.
     */

    /**
     * The total number of `send` operations ever invoked. Each invocation gets a unique cell to process.
     */
    private final AtomicLong senders = new AtomicLong(0L);
    private final AtomicLong receivers = new AtomicLong(0L);
    /**
     * The buffer holding the state of each cell. State can be {@link CellState}, {@link Buffered}, or {@link Continuation}.
     */
    private final AtomicReferenceArray<Object> buffer = new AtomicReferenceArray<>(20_000_000); // TODO

    private void setState(Object state, long index) {
        buffer.set((int) index, state);
    }

    // passed to continuation to set the interrupt state
    private final BiConsumer<Object, Long> setStateMethod = this::setState;

    private Object getState(long index) {
        return buffer.get((int) index);
    }

    private boolean casState(long index, Object expected, Object newValue) {
        return buffer.compareAndSet((int) index, expected, newValue);
    }

    //

    /**
     * Send a value to the channel.
     * TODO: throw exceptions when the channel is closed
     *
     * @param value The value to send. Not {@code null}.
     */
    public void send(T value) throws InterruptedException {
        sendSafe(value); // TODO exceptions
    }

    /**
     * Send a value to the channel. Doesn't throw exceptions when the channel is closed.
     *
     * @param value The value to send. Not {@code null}.
     * @return Either {@code null}, or TODO: an exception when the channel is closed (the exception is not thrown.)
     */
    public Object sendSafe(T value) throws InterruptedException {
        if (value == null) {
            throw new NullPointerException();
        }
        while (true) {
            var s = senders.incrementAndGet(); // reserving the next cell
            if (updateCellSend(s, value)) return null;
        }
    }

    /**
     * @param s     Index of the reserved cell.
     * @param value The value to send.
     * @return {@code true}, if sending was successful; {@code false}, if it should be restarted.
     */
    private boolean updateCellSend(long s, T value) throws InterruptedException {
        while (true) {
            var state = getState(s); // reading the current state of the cell; we'll try to update it atomically
            var r = receivers.get(); // reading the receiver's counter

            switch (state) {
                case null -> {
                    if (s >= r) {
                        // cell is empty, and no receiver -> suspend
                        // storing the value to send as the continuation's payload, so that the receiver can use it
                        var c = new Continuation(value);
                        if (casState(s, null, c)) {
                            c.await(setStateMethod, s);
                            return true;
                        }
                        // else: CAS unsuccessful, repeat
                    } else {
                        // cell is empty, but a receiver is in progress -> elimination
                        if (casState(s, null, new Buffered(value))) {
                            return true;
                        }
                        // else: CAS unsuccessful, repeat
                    }
                }
                case Continuation c -> {
                    // a receiver is waiting -> trying to resume
                    if (c.tryResume(value)) {
                        setState(DONE, s);
                        return true;
                    } else {
                        // cell interrupted -> trying with a new one
                        return false;
                    }
                }
                case INTERRUPTED, BROKEN -> {
                    // cell interrupted or poisoned -> trying with a new one
                    return false;
                }
                default -> throw new IllegalStateException("Unexpected state: " + state);
            }
        }
    }

    //

    /**
     * Receive a value from the channel.
     * TODO: throw exceptions when the channel is closed
     */
    public T receive() throws InterruptedException {
        //noinspection unchecked
        return (T) receiveSafe();
    }

    /**
     * Receive a value from the channel. Doesn't throw exceptions when the channel is closed.
     *
     * @return Either a value of type {@code T}, or TODO: an exception when the channel is closed (the exception is not thrown.)
     */
    public Object receiveSafe() throws InterruptedException {
        while (true) {
            var r = receivers.incrementAndGet(); // reserving the next cell
            var result = updateCellReceive(r);
            if (result != UpdateCellReceiveResult.RESTART) {
                return result;
            }
        }
    }

    /**
     * @param r Index of the reserved cell.
     * @return Either a restart ({@link UpdateCellReceiveResult#RESTART}), or the received value.
     */
    private Object updateCellReceive(long r) throws InterruptedException {
        while (true) {
            var state = getState(r); // reading the current state of the cell; we'll try to update it atomically
            var s = senders.get(); // reading the sender's counter

            switch (state) {
                case null -> {
                    if (r >= s) {
                        // cell is empty, and no sender -> suspend
                        // not using any payload
                        var c = new Continuation(null);
                        if (casState(r, null, c)) {
                            return c.await(setStateMethod, r);
                        }
                        // else: CAS unsuccessful, repeat
                    } else {
                        // sender in progress, receiver changed state first -> restart
                        if (casState(r, null, BROKEN)) {
                            return UpdateCellReceiveResult.RESTART;
                        }
                        // else: CAS unsuccessful, repeat
                    }
                }
                case Continuation c -> {
                    // a sender is waiting -> trying to resume
                    if (c.tryResume(0)) {
                        setState(DONE, r);
                        return c.getPayload();
                    } else {
                        // cell interrupted -> trying with a new one
                        return UpdateCellReceiveResult.RESTART;
                    }
                }
                case Buffered b -> {
                    // an elimination has happened -> finish
                    return b.value();
                }
                case INTERRUPTED -> {
                    // cell interrupted -> trying with a new one
                    return UpdateCellReceiveResult.RESTART;
                }
                default -> throw new IllegalStateException("Unexpected state: " + state);
            }
        }
    }
}

// possible return values of updateCellReceive: one of the enum constants below, or the received value

enum UpdateCellReceiveResult {
    RESTART
}

// possible states of a cell: one of the enum constants below, Buffered, or Continuation

enum CellState {
    DONE,
    INTERRUPTED,
    BROKEN;
}

// a java record called Buffered with a single value field; the type should be T
record Buffered(Object value) {}

final class Continuation {
    /**
     * The number of busy-looping iterations before yielding, during {@link Continuation#await(Runnable)}. {@code 0}, if there's a single CPU.
     */
    private static final int SPINS = Runtime.getRuntime().availableProcessors() == 1 ? 0 : 10000;

    private final Thread creatingThread;
    private volatile Object data; // set using DATA var handle

    private final Object payload;

    Continuation(Object payload) {
        this.payload = payload;
        this.creatingThread = Thread.currentThread();
    }

    /**
     * Resume the continuation with the given value.
     *
     * @param value Should not be {@code null}.
     * @return {@code true} tf the continuation was resumed successfully. {@code false} if it was interrupted.
     */
    boolean tryResume(Object value) {
        var result = Continuation.DATA.compareAndSet(this, null, value);
        LockSupport.unpark(creatingThread);
        return result;
    }

    /**
     * Await for the continuation to be resumed.
     *
     * @param setStateMethod The method to call which will change the cell's state to interrupted, if interruption happens.
     * @param cellIndex      The index of the cell for which to change the state to interrupted, if interruption happens.
     * @return The value with which the continuation was resumed.
     */
    Object await(BiConsumer<Object, Long> setStateMethod, long cellIndex) throws InterruptedException {
        var spinIterations = SPINS;
        while (data == null) {
            if (spinIterations > 0) {
                Thread.onSpinWait();
                spinIterations -= 1;
            } else {
                LockSupport.park();

                if (Thread.interrupted()) {
                    // potential race with `tryResume`
                    if (Continuation.DATA.compareAndSet(this, null, ContinuationMarker.INTERRUPTED)) {
                        setStateMethod.accept(INTERRUPTED, cellIndex);
                        throw new InterruptedException();
                    } else {
                        // another thread already set the data; setting the interrupt status (so that the next blocking
                        // operation throws), and continuing
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        return data;
    }

    Object getPayload() {
        return payload;
    }

    //

    private static final VarHandle DATA;

    static {
        var l = MethodHandles.lookup();
        try {
            DATA = l.findVarHandle(Continuation.class, "data", Object.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}

// the marker value is used only to mark in the continuation's `data` that interruption won the race with `tryResume`
enum ContinuationMarker {
    INTERRUPTED
}