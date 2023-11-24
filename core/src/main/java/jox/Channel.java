package jox;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import static jox.CellState.*;
import static jox.Segment.findAndMoveForward;

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

    Other notes:
    * we need the previous pointers in segments to physically remove segments full of cells in the interrupted state.
      Segments before such an interrupted segments might still hold awaiting continuations. When physically removing a
      segment, we need to update the `next` pointer of the `previous` ("alive") segment. That way the memory usage is
      bounded by the number of awaiting threads.
    * after a `send`, if we know that r >= s, or after a `receive`, when we know that s >= r, we can set the `previous`
      pointer in the segment to `null`, so that the previous segments can be GCd. Even if there are still ongoing
      operations on these (previous) segments, and we'll end up wanting to remove such a segment, subsequent channel
      operations won't use them, so the relinking won't be useful.
     */

    /**
     * The total number of `send` operations ever invoked. Each invocation gets a unique cell to process.
     */
    private final AtomicLong senders = new AtomicLong(0L);
    private final AtomicLong receivers = new AtomicLong(0L);

    /**
     * Segments holding cell states. State can be {@link CellState}, {@link Buffered}, or {@link Continuation}.
     */
    private final AtomicReference<Segment> sendSegment;
    private final AtomicReference<Segment> receiveSegment;
    private final AtomicReference<Segment> bufferEndSegment;

    public Channel() {
        var isRendezvous = true; // TODO: add capacity
        var firstSegment = new Segment(0, null, isRendezvous ? 2 : 3);
        sendSegment = new AtomicReference<>(firstSegment);
        receiveSegment = new AtomicReference<>(firstSegment);
        bufferEndSegment = new AtomicReference<>(isRendezvous ? Segment.NULL_SEGMENT : firstSegment);
    }

    // passed to continuation to set the interrupt state
    private final TriConsumer<Segment, Integer, Object> setStateMethod = Segment::setCell;

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
            // reading the segment before the counter increment - this is needed to find the required segment later
            var segment = sendSegment.get();
            // reserving the next cell
            var s = senders.incrementAndGet();

            // calculating the segment id and the index within the segment
            var id = s / Segment.SEGMENT_SIZE;
            var i = (int) (s % Segment.SEGMENT_SIZE);

            // check if `sendSegment` stores a previous segment, if so move the reference forward
            if (segment.getId() != id) {
                segment = findAndMoveForward(sendSegment, segment, id);

                // if we still have another segment, the cell (as well as all other ones) must have been interrupted
                if (segment.getId() != id) {
                    // skipping all interrupted cells, and trying with a new one
                    senders.compareAndSet(s, segment.getId() * Segment.SEGMENT_SIZE);
                    continue;
                }
            }

            var sendResult = updateCellSend(segment, i, s, value);
            /*
            After `updateCellSend` completes, we can be sure that r >= s:
            - if we stored and awaited a continuation, and it was resumed, then a receiver must have appeared
            - if we buffered a value, then a receiver is in progress in that cell
            - if a continuation was present, then the receiver must have been there
            - if the cell was interrupted, that could have been only because of a receiver
            - same, if the cell is broken

            The only case when r < s is when awaiting on the continuation is interrupted, in which case the exception
            propagates outside of this method.
             */
            segment.cleanPrev();
            if (sendResult) return null;
        }
    }

    /**
     * @param segment The segment in which to store the cell's state.
     * @param i       The index within the {@code segment}.
     * @param s       Index of the reserved cell.
     * @param value   The value to send.
     * @return {@code true}, if sending was successful; {@code false}, if it should be restarted.
     */
    private boolean updateCellSend(Segment segment, int i, long s, T value) throws InterruptedException {
        while (true) {
            var state = segment.getCell(i); // reading the current state of the cell; we'll try to update it atomically
            var r = receivers.get(); // reading the receiver's counter

            switch (state) {
                case null -> {
                    if (s >= r) {
                        // cell is empty, and no receiver -> suspend
                        // storing the value to send as the continuation's payload, so that the receiver can use it
                        var c = new Continuation(value);
                        if (segment.casCell(i, null, c)) {
                            c.await(setStateMethod, segment, i);
                            return true;
                        }
                        // else: CAS unsuccessful, repeat
                    } else {
                        // cell is empty, but a receiver is in progress -> elimination
                        if (segment.casCell(i, null, new Buffered(value))) {
                            return true;
                        }
                        // else: CAS unsuccessful, repeat
                    }
                }
                case Continuation c -> {
                    // a receiver is waiting -> trying to resume
                    if (c.tryResume(value)) {
                        segment.setCell(i, DONE);
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
            // reading the segment before the counter increment - this is needed to find the required segment later
            var segment = receiveSegment.get();
            // reserving the next cell
            var r = receivers.incrementAndGet();

            // calculating the segment id and the index within the segment
            var id = r / Segment.SEGMENT_SIZE;
            var i = (int) (r % Segment.SEGMENT_SIZE);

            // check if `sendSegment` stores a previous segment, if so move the reference forward
            if (segment.getId() != id) {
                segment = findAndMoveForward(receiveSegment, segment, id);

                // if we still have another segment, the cell (as well as all other ones) must have been interrupted
                if (segment.getId() != id) {
                    // skipping all interrupted cells, and trying with a new one
                    senders.compareAndSet(r, segment.getId() * Segment.SEGMENT_SIZE);
                    continue;
                }
            }

            var result = updateCellReceive(segment, i, r);
            /*
            After `updateCellReceive` completes, we can be sure that s >= r:
            - if we stored and awaited a continuation, and it was resumed, then a sender must have appeared
            - if we marked the cell as broken, then a sender is in progress in that cell
            - if a continuation was present, then the sender must have been there
            - if the cell was interrupted, that could have been only because of a sender
            - if a value was buffered, that's because there's a matching sender

            The only case when s < r is when awaiting on the continuation is interrupted, in which case the exception
            propagates outside of this method.
             */
            segment.cleanPrev();
            if (result != UpdateCellReceiveResult.RESTART) {
                return result;
            }
        }
    }

    /**
     * @param r Index of the reserved cell.
     * @return Either a restart ({@link UpdateCellReceiveResult#RESTART}), or the received value.
     */
    private Object updateCellReceive(Segment segment, int i, long r) throws InterruptedException {
        while (true) {
            var state = segment.getCell(i); // reading the current state of the cell; we'll try to update it atomically
            var s = senders.get(); // reading the sender's counter

            switch (state) {
                case null -> {
                    if (r >= s) {
                        // cell is empty, and no sender -> suspend
                        // not using any payload
                        var c = new Continuation(null);
                        if (segment.casCell(i, null, c)) {
                            return c.await(setStateMethod, segment, i);
                        }
                        // else: CAS unsuccessful, repeat
                    } else {
                        // sender in progress, receiver changed state first -> restart
                        if (segment.casCell(i, null, BROKEN)) {
                            return UpdateCellReceiveResult.RESTART;
                        }
                        // else: CAS unsuccessful, repeat
                    }
                }
                case Continuation c -> {
                    // a sender is waiting -> trying to resume
                    if (c.tryResume(0)) {
                        segment.setCell(i, DONE);
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

    /**
     * Possible return values of {@link Channel#updateCellReceive(Segment, int, long)}: one of the enum constants below, or the received value.
     */
    enum UpdateCellReceiveResult {
        RESTART
    }
}

// possible states of a cell: one of the enum constants below, Buffered, or Continuation

enum CellState {
    DONE,
    INTERRUPTED,
    BROKEN;
}

record Buffered(Object value) {}

final class Continuation {
    /**
     * The number of busy-looping iterations before yielding, during {@link Continuation#await(TriConsumer, Segment, int)}.
     * {@code 0}, if there's a single CPU.
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
     * @param segment        The segment in which the cell is located.
     * @param cellIndex      The index of the cell for which to change the state to interrupted, if interruption happens.
     * @return The value with which the continuation was resumed.
     */
    Object await(TriConsumer<Segment, Integer, Object> setStateMethod, Segment segment, int cellIndex) throws InterruptedException {
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
                        setStateMethod.accept(segment, cellIndex, INTERRUPTED);
                        // notifying the segment - if all cells become interrupted, the segment can be removed
                        segment.cellInterrupted();
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
