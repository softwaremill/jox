package jox;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Stream;

import static jox.CellState.*;
import static jox.Segment.findAndMoveForward;

public class Channel<T> {
    /*
    Inspired by the "Fast and Scalable Channels in Kotlin Coroutines" paper (https://arxiv.org/abs/2211.04986), and
    the Kotlin implementation (https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/common/src/channels/BufferedChannel.kt).

    Notable differences from the Kotlin implementation:
    * we block (virtual) threads, instead of suspend functions
    * in Kotlin's channels, the buffer stores both the elements (in even indexes), and the state for each cell (in odd
      indexes). This would be also possible here, but in two-thread rendezvous tests, this is slightly slower than the
      approach below: we transmit the elements inside objects representing state. This does incur an additional
      allocation in case of the `Buffered` state (when there's a waiting receiver - we can't simply use a constant).
      However, we add a field to `Continuation` (which is a channel-specific class, unlike in Kotlin), to avoid the
      allocation when the sender suspends.
    * as we don't directly store elements in the buffer, we don't need to clear them on interrupt etc. This is done
      automatically when the cell's state is set to something else than a Continuation/Buffered.
    * instead of the `completedExpandBuffersAndPauseFlag` counter, we maintain a counter of cells which haven't been
      processed by `expandBuffer` in each segment. The segment becomes logically removed, only once all cells have
      been interrupted, processed & there are no counters. The segment is notified that a cell is processed after
      `expandBuffer` completes.

    Other notes:
    * we need the previous pointers in segments to physically remove segments full of cells in the interrupted state.
      Segments before such an interrupted segments might still hold awaiting continuations. When physically removing a
      segment, we need to update the `next` pointer of the `previous` ("alive") segment. That way the memory usage is
      bounded by the number of awaiting threads.
    * after a `send`, if we know that R > s, or after a `receive`, when we know that S > r, we can set the `previous`
      pointer in the segment to `null`, so that the previous segments can be GCd. Even if there are still ongoing
      operations on these (previous) segments, and we'll end up wanting to remove such a segment, subsequent channel
      operations won't use them, so the relinking won't be useful.
     */

    private final int capacity;

    /**
     * The total number of `send` operations ever invoked, and a flag indicating if the channel is closed.
     * The flag is shifted by {@link Channel#SENDERS_AND_CLOSED_FLAG_SHIFT} bits.
     * <p>
     * Each {@link Channel#send} invocation gets a unique cell to process.
     */
    private final AtomicLong sendersAndClosedFlag = new AtomicLong(0L);
    private final AtomicLong receivers = new AtomicLong(0L);
    private final AtomicLong bufferEnd;

    /**
     * Segments holding cell states. State can be {@link CellState}, {@link Buffered}, or {@link Continuation}.
     */
    private final AtomicReference<Segment> sendSegment;
    private final AtomicReference<Segment> receiveSegment;
    private final AtomicReference<Segment> bufferEndSegment;

    private final AtomicReference<ChannelClosed> closedReason;

    private final boolean isRendezvous;


    public Channel() {
        this(0);
    }

    public Channel(int capacity) {
        this.capacity = capacity;
        isRendezvous = capacity == 0L;

        var firstSegment = new Segment(0, null, isRendezvous ? 2 : 3, !isRendezvous);

        bufferEnd = new AtomicLong(capacity);
        for (int i = 0; i < capacity; i++) {
            firstSegment.cellProcessed_notInterruptedSender(); // the cells that are initially in the buffer are already processed (expandBuffer won't touch them)
        }

        sendSegment = new AtomicReference<>(firstSegment);
        receiveSegment = new AtomicReference<>(firstSegment);
        // If the capacity is 0, buffer expansion never happens, so the buffer end segment points to a null segment,
        // not the first one. This is also reflected in the pointer counter of firstSegment.
        bufferEndSegment = new AtomicReference<>(isRendezvous ? Segment.NULL_SEGMENT : firstSegment);

        closedReason = new AtomicReference<>(null);
    }

    // *******
    // Sending
    // *******

    /**
     * Send a value to the channel.
     *
     * @param value The value to send. Not {@code null}.
     * @throws ChannelClosedException When the channel is closed.
     */
    public void send(T value) throws InterruptedException {
        var r = sendSafe(value);
        if (r instanceof ChannelClosed c) {
            throw c.toException();
        }
    }

    /**
     * Send a value to the channel. Doesn't throw exceptions when the channel is closed, but returns a value.
     *
     * @param value The value to send. Not {@code null}.
     * @return Either {@code null}, or {@link ChannelClosed}, when the channel is closed.
     */
    public Object sendSafe(T value) throws InterruptedException {
        if (value == null) {
            throw new NullPointerException();
        }
        while (true) {
            // reading the segment before the counter increment - this is needed to find the required segment later
            var segment = sendSegment.get();
            // reserving the next cell
            var scf = sendersAndClosedFlag.getAndIncrement();
            if (isClosed(scf)) {
                return closedReason.get();
            }
            var s = getSendersCounter(scf);

            // calculating the segment id and the index within the segment
            var id = s / Segment.SEGMENT_SIZE;
            var i = (int) (s % Segment.SEGMENT_SIZE);

            // check if `sendSegment` stores a previous segment, if so move the reference forward
            if (segment.getId() != id) {
                segment = findAndMoveForward(sendSegment, segment, id, !isRendezvous);
                if (segment == null) {
                    // the channel has been closed, `s` points to a segment which doesn't exist
                    return closedReason.get();
                }

                // if we still have another segment, the segment must have been removed
                if (segment.getId() != id) {
                    // skipping all interrupted cells, and trying with a new one
                    sendersAndClosedFlag.compareAndSet(s, segment.getId() * Segment.SEGMENT_SIZE);
                    continue;
                }
            }

            var sendResult = updateCellSend(segment, i, s, value);
            switch (sendResult) {
                case AWAITED -> {
                    // the thread was suspended and then resumed by a receiver or by buffer expansion
                    // not clearing the previous pointer, because of the buffering possibility
                    return null;
                }
                case BUFFERED -> {
                    // a receiver is coming, or we are in buffer
                    // similarly as above, not clearing the previous pointer
                    return null;
                }
                case RESUMED -> {
                    // we resumed a receiver - we can be sure that R > s
                    segment.cleanPrev();
                    return null;
                }
                case FAILED -> {
                    // the cell was broken (hence already processed by a receiver) or interrupted (also a receiver
                    // must have been there); in both cases R > s
                    segment.cleanPrev();
                    // trying again with a new cell
                }
                case CLOSED -> {
                    // not cleaning the previous segments - the close procedure might still need it
                    return closedReason.get();
                }
            }
        }
    }

    /**
     * @param segment The segment which stores the cell's state.
     * @param i       The index within the {@code segment}.
     * @param s       Index of the reserved cell.
     * @param value   The value to send.
     * @return {@code true}, if sending was successful; {@code false}, if it should be restarted.
     */
    private SendResult updateCellSend(Segment segment, int i, long s, T value) throws InterruptedException {
        while (true) {
            var state = segment.getCell(i); // reading the current state of the cell; we'll try to update it atomically

            switch (state) {
                case null -> {
                    // reading the buffer end & receiver's counter if needed
                    if (s >= (isRendezvous ? 0 : bufferEnd.get()) && s >= receivers.get()) {
                        // cell is empty, and no receiver, not in buffer -> suspend
                        // storing the value to send as the continuation's payload, so that the receiver can use it
                        var c = new Continuation(value);
                        if (segment.casCell(i, null, c)) {
                            if (c.await(segment, i) == ChannelClosedMarker.CLOSED) {
                                return SendResult.CLOSED;
                            } else {
                                return SendResult.AWAITED;
                            }
                        }
                        // else: CAS unsuccessful, repeat
                    } else {
                        // cell is empty, but a receiver is in progress, or in buffer -> elimination
                        if (segment.casCell(i, null, new Buffered(value))) {
                            return SendResult.BUFFERED;
                        }
                        // else: CAS unsuccessful, repeat
                    }
                }
                case IN_BUFFER -> {
                    // cell just became part of the buffer
                    if (segment.casCell(i, IN_BUFFER, new Buffered(value))) {
                        return SendResult.BUFFERED;
                    }
                    // else: CAS unsuccessful, repeat
                }
                case Continuation c -> {
                    // a receiver is waiting -> trying to resume
                    if (c.tryResume(value)) {
                        segment.setCell(i, DONE);
                        return SendResult.RESUMED;
                    } else {
                        // cell interrupted -> trying with a new one
                        return SendResult.FAILED;
                    }
                }
                case INTERRUPTED_RECEIVE, BROKEN -> {
                    // cell interrupted or poisoned -> trying with a new one
                    return SendResult.FAILED;
                }
                case CLOSED -> {
                    return SendResult.CLOSED;
                }
                default -> throw new IllegalStateException("Unexpected state: " + state);
            }
        }
    }

    // *********
    // Receiving
    // *********

    /**
     * Receive a value from the channel.
     *
     * @throws ChannelClosedException When the channel is closed.
     */
    public T receive() throws InterruptedException {
        var r = receiveSafe();
        if (r instanceof ChannelClosed c) {
            throw c.toException();
        } else {
            //noinspection unchecked
            return (T) r;
        }
    }

    /**
     * Receive a value from the channel. Doesn't throw exceptions when the channel is closed, but returns a value.
     *
     * @return Either a value of type {@code T}, or {@link ChannelClosed}, when the channel is closed.
     */
    public Object receiveSafe() throws InterruptedException {
        while (true) {
            // reading the segment before the counter increment - this is needed to find the required segment later
            var segment = receiveSegment.get();
            // reserving the next cell
            var r = receivers.getAndIncrement();

            // calculating the segment id and the index within the segment
            var id = r / Segment.SEGMENT_SIZE;
            var i = (int) (r % Segment.SEGMENT_SIZE);

            // check if `receiveSegment` stores a previous segment, if so move the reference forward
            if (segment.getId() != id) {
                segment = findAndMoveForward(receiveSegment, segment, id, !isRendezvous);
                if (segment == null) {
                    // the channel has been closed, r points to a segment which doesn't exist
                    return closedReason.get();
                }

                // if we still have another segment, the segment must have been removed
                if (segment.getId() != id) {
                    // skipping all interrupted cells, and trying with a new one
                    receivers.compareAndSet(r, segment.getId() * Segment.SEGMENT_SIZE);
                    continue;
                }
            }

            var result = updateCellReceive(segment, i, r);
            if (result == ReceiveResult.CLOSED) {
                // not cleaning the previous segments - the close procedure might still need it
                return closedReason.get();
            } else {
              /*
                After `updateCellReceive` completes and the channel isn't closed, we can be sure that S > r:
                - if we stored and awaited a continuation, and it was resumed, then a sender must have appeared
                - if we marked the cell as broken, then a sender is in progress in that cell
                - if a continuation was present, then the sender must have been there
                - if the cell was interrupted, that could have been only because of a sender
                - if a value was buffered, that's because there was/is a matching sender

                The only case when S < r is when awaiting on the continuation is interrupted, in which case the
                exception propagates outside of this method.
                */
                segment.cleanPrev();
                if (result != ReceiveResult.FAILED) {
                    return result;
                }
            }
        }
    }

    /**
     * Invariant maintained by receive + expandBuffer: between R and B the number of cells that are empty / IN_BUFFER should be equal
     * to the buffer size. These are the cells that can accept a sender without suspension.
     *
     * @param segment The segment which stores the cell's state.
     * @param i       The index within the {@code segment}.
     * @param r       Index of the reserved cell.
     * @return Either a state-result ({@link ReceiveResult}), or the received value.
     */
    private Object updateCellReceive(Segment segment, int i, long r) throws InterruptedException {
        while (true) {
            var state = segment.getCell(i); // reading the current state of the cell; we'll try to update it atomically
            var switchState = state == null ? IN_BUFFER : state; // we can't combine null+IN_BUFFER in the switch statement, hence cheating a bit here

            switch (switchState) {
                case IN_BUFFER -> { // means that state == null || state == IN_BUFFER
                    if (r >= sendersAndClosedFlag.get()) { // reading the sender's counter
                        // cell is empty, and no sender -> suspend
                        // not using any payload
                        var c = new Continuation(null);
                        if (segment.casCell(i, state, c)) {
                            expandBuffer();
                            var result = c.await(segment, i);
                            if (result == ChannelClosedMarker.CLOSED) {
                                return ReceiveResult.CLOSED;
                            } else {
                                return result;
                            }
                        }
                        // else: CAS unsuccessful, repeat
                    } else {
                        // sender in progress, receiver changed state first -> restart
                        if (segment.casCell(i, state, BROKEN)) {
                            expandBuffer();
                            return ReceiveResult.FAILED;
                        }
                        // else: CAS unsuccessful, repeat
                    }
                }
                case Continuation c -> {
                    if (segment.casCell(i, state, RESUMING)) {
                        // a sender is waiting -> trying to resume
                        if (c.tryResume(0)) {
                            segment.setCell(i, DONE);
                            expandBuffer();
                            return c.getPayload();
                        } else {
                            // cell interrupted -> trying with a new one
                            // the state will be set to INTERRUPTED_SEND by the continuation, meanwhile everybody else will observe RESUMING
                            return ReceiveResult.FAILED;
                        }
                    }
                    // else: CAS unsuccessful, repeat
                }
                case Buffered b -> {
                    segment.setCell(i, DONE);
                    expandBuffer();
                    // an elimination has happened -> finish
                    return b.value();
                }
                case INTERRUPTED_SEND -> {
                    // cell interrupted -> trying with a new one
                    return ReceiveResult.FAILED;
                }
                case RESUMING -> {
                    // expandBuffer() is resuming the sender -> repeat
                    Thread.onSpinWait();
                }
                case CLOSED -> {
                    return ReceiveResult.CLOSED;
                }
                default -> throw new IllegalStateException("Unexpected state: " + state);
            }
        }
    }

    // ****************
    // Buffer expansion
    // ****************

    private void expandBuffer() {
        if (isRendezvous) return;
        while (true) {
            // reading the segment before the counter increment - this is needed to find the required segment later
            var segment = bufferEndSegment.get();
            // reserving the next cell
            var b = bufferEnd.getAndIncrement();

            // calculating the segment id and the index within the segment
            var id = b / Segment.SEGMENT_SIZE;
            var i = (int) (b % Segment.SEGMENT_SIZE);

            // check if `bufferEndSegment` stores a previous segment, if so move the reference forward
            if (segment.getId() != id) {
                segment = findAndMoveForward(bufferEndSegment, segment, id, true);
                if (segment == null) {
                    // the channel has been closed, b points to a segment which doesn't exist, nowhere to expand
                    return;
                }

                // if we still have another segment, the segment must have been removed; this can happen if the current
                // cell has been an interrupted sender (such cells are immediately marked as processed, which can cause
                // segment removal); other cells might have been both interrupted senders/receivers
                if (segment.getId() != id) {
                    // skipping all interrupted cells, and trying with a new one
                    bufferEnd.compareAndSet(b, segment.getId() * Segment.SEGMENT_SIZE);
                    continue;
                }
            }

            var result = updateCellExpandBuffer(segment, i);
            if (result == ExpandBufferResult.DONE) {
                // letting the segment know that the cell is processed, and the segment can be potentially removed
                segment.cellProcessed_notInterruptedSender();
                return;
            } else if (result == ExpandBufferResult.CLOSED) {
                // the cell is already counted as processed by the close procedure
                return;
            }
            // else, the cell must have been an interrupted sender; `Continuation` the properly notifies the segment
        }
    }

    private ExpandBufferResult updateCellExpandBuffer(Segment segment, int i) {
        while (true) {
            var state = segment.getCell(i); // reading the current state of the cell; we'll try to update it atomically

            switch (state) {
                case Continuation c when c.isSender() -> {
                    if (segment.casCell(i, state, RESUMING)) {
                        // a sender is waiting -> trying to resume
                        if (c.tryResume(0)) {
                            segment.setCell(i, new Buffered(c.getPayload()));
                            return ExpandBufferResult.DONE;
                        } else {
                            // cell interrupted -> trying with a new one
                            // the state will be set to INTERRUPTED_SEND by the continuation, meanwhile everybody else will observe RESUMING
                            return ExpandBufferResult.FAILED;
                        }
                    }
                    // else: CAS unsuccessful, repeat
                }
                case Continuation c -> {
                    // must be a receiver continuation - another buffer expansion already happened
                    return ExpandBufferResult.DONE;
                }
                case Buffered b -> {
                    // an element is already buffered; if the ordering of operations was different, we would put IN_BUFFER in that cell and finish
                    return ExpandBufferResult.DONE;
                }
                case INTERRUPTED_SEND -> {
                    // a sender was interrupted - restart
                    return ExpandBufferResult.FAILED;
                }
                case INTERRUPTED_RECEIVE -> {
                    // a receiver continuation must have been here before - another buffer expansion already happened
                    return ExpandBufferResult.DONE;
                }
                case null -> {
                    // the cell is empty, a sender is or will be coming - set the cell as "in buffer" to let the sender know in case it's in progress
                    if (segment.casCell(i, null, IN_BUFFER)) {
                        return ExpandBufferResult.DONE;
                    }
                    // else: CAS unsuccessful, repeat
                }
                case BROKEN -> {
                    // the cell is broken, receive() started another buffer expansion
                    return ExpandBufferResult.DONE;
                }
                case DONE -> {
                    // sender & receiver have already paired up, another buffer expansion already happened
                    return ExpandBufferResult.DONE;
                }
                case RESUMING -> Thread.onSpinWait(); // receive() is resuming the sender -> repeat
                case CLOSED -> {
                    // the channel is closed, all subsequent cells must have already been closed (as we're closing from
                    // the end)
                    return ExpandBufferResult.CLOSED;
                }
                default -> throw new IllegalStateException("Unexpected state: " + state);
            }
        }
    }

    // *******
    // Closing
    // *******

    /**
     * Close the channel, indicating that no more elements will be sent.
     * <p>
     * Any elements that are already buffered will be delivered. Any send operations that are in progress will complete
     * normally, when a receiver arrives. Any pending receive operations will complete with a channel closed result.
     * <p>
     * Subsequent {@link #send(Object)} operations will throw {@link ChannelClosedException}.
     *
     * @throws ChannelClosedException When the channel is already closed.
     */
    public void done() {
        var r = doneSafe();
        if (r instanceof ChannelClosed c) {
            throw c.toException();
        }
    }

    /**
     * Close the channel, indicating that no more elements will be sent. Doesn't throw exceptions when the channel is
     * closed, but returns a value.
     * <p>
     * Any elements that are already buffered will be delivered. Any send operations that are in progress will complete
     * normally, when a receiver arrives. Any pending receive operations will complete with a channel closed result.
     * <p>
     * Subsequent {@link #send(Object)} operations will throw {@link ChannelClosedException}.
     *
     * @return Either {@code null}, or {@link ChannelClosed}, when the channel is already closed.
     */
    public Object doneSafe() {
        return closeSafe(new ChannelClosed.ChannelDone());
    }

    /**
     * Close the channel, indicating an error.
     * <p>
     * Any elements that are already buffered won't be delivered. Any send or receive operations that are in progress
     * will complete with a channel closed result.
     * <p>
     * Subsequent {@link #send(Object)} and {@link #receive()} operations will throw {@link ChannelClosedException}.
     *
     * @param reason The reason of the error. Not {@code null}.
     * @throws ChannelClosedException When the channel is already closed.
     */
    public void error(Throwable reason) {
        if (reason == null) {
            throw new NullPointerException("Error reason cannot be null");
        }
        var r = errorSafe(reason);
        if (r instanceof ChannelClosed c) {
            throw c.toException();
        }
    }

    /**
     * Close the channel, indicating an error. Doesn't throw exceptions when the channel is closed, but returns a value.
     * <p>
     * Any elements that are already buffered won't be delivered. Any send or receive operations that are in progress
     * will complete with a channel closed result.
     * <p>
     * Subsequent {@link #send(Object)} and {@link #receive()} operations will throw {@link ChannelClosedException}.
     *
     * @return Either {@code null}, or {@link ChannelClosed}, when the channel is already closed.
     */
    public Object errorSafe(Throwable reason) {
        return closeSafe(new ChannelClosed.ChannelError(reason));
    }

    private Object closeSafe(ChannelClosed channelClosed) {
        if (!closedReason.compareAndSet(null, channelClosed)) {
            return closedReason.get(); // already closed
        }

        // after this completes, it's guaranteed than no sender with `s >= lastSender` will proceed with the usual
        // sending algorithm, as `send()` will observe that the channel is closed
        var scf = sendersAndClosedFlag.updateAndGet(this::setClosedFlag);
        var lastSender = getSendersCounter(scf);

        // closing the segment chain guarantees that no new segment beyond `lastSegment` will be created
        var lastSegment = sendSegment.get().close();

        if (channelClosed instanceof ChannelClosed.ChannelError) {
            // closing all cells, as this is an error
            closeCellsUntil(0, lastSegment);
        } else {
            closeCellsUntil(lastSender, lastSegment);
        }

        return null;
    }

    private void closeCellsUntil(long lastCellToClose, Segment segment) {
        if (segment == null) {
            // we've reach the end of the segment chain, previous segments have been completed and discarded
            return;
        }

        var lastCellToCloseSegmentId = lastCellToClose / Segment.SEGMENT_SIZE;
        int lastIndexToCloseInSegment;
        if (lastCellToCloseSegmentId == segment.getId()) {
            lastIndexToCloseInSegment = (int) (lastCellToClose % Segment.SEGMENT_SIZE);
        } else if (lastCellToCloseSegmentId < segment.getId()) {
            // the last cell to close is in a segment before this one, so we need to close all cells in this segment
            lastIndexToCloseInSegment = 0;
        } else {
            // the last cell to close is in a segment after this one, so all cells are already closed
            return;
        }

        // closing the cells in reverse order - that way, a later receiver won't be paired with a sender, while an
        // earlier receiver becomes closed
        for (int i = Segment.SEGMENT_SIZE - 1; i >= lastIndexToCloseInSegment; i--) {
            updateCellClose(segment, i);
        }

        closeCellsUntil(lastCellToClose, segment.getPrev());
    }

    private void updateCellClose(Segment segment, int i) {
        while (true) {
            var state = segment.getCell(i);
            switch (state) {
                case null -> {
                    if (segment.casCell(i, null, CLOSED)) {
                        segment.cellInterruptedSender_orClosed();
                        return;
                    }
                }
                case IN_BUFFER -> {
                    if (segment.casCell(i, state, CLOSED)) {
                        segment.cellInterruptedSender_orClosed();
                        return;
                    }
                }
                case Buffered b -> {
                    // discarding the buffered value
                    if (segment.casCell(i, state, CLOSED)) {
                        segment.cellInterruptedSender_orClosed();
                        return;
                    }
                }
                case Continuation c -> {
                    if (segment.casCell(i, state, RESUMING)) {
                        if (c.tryResume(ChannelClosedMarker.CLOSED)) {
                            segment.setCell(i, CLOSED);
                            segment.cellInterruptedSender_orClosed();
                            return;
                        } else {
                            // cell interrupted - the segment counters will be appropriately decremented from the
                            // continuation, depending if this is a sender or receiver; moreover, the cell is already
                            // processed in case this is a receiver
                            return;
                        }
                    }
                }
                case DONE, BROKEN -> {
                    return; // nothing to do - a sender & receiver have already met
                }
                case INTERRUPTED_RECEIVE, INTERRUPTED_SEND -> {
                    return; // nothing to do - segment counters already decremented
                }
                case RESUMING -> Thread.onSpinWait(); // receive() or expandBuffer() are resuming the cell - wait
                default -> throw new IllegalStateException("Unexpected state: " + state);
            }
        }
    }

    public boolean isClosed() {
        return closedReason.get() != null;
    }

    public boolean isDone() {
        return closedReason.get() instanceof ChannelClosed.ChannelDone;
    }

    /**
     * @return {@code null} if the channel is not closed, or if it's closed with {@link ChannelClosed.ChannelDone}.
     */
    public Throwable isError() {
        var reason = closedReason.get();
        if (reason instanceof ChannelClosed.ChannelError e) {
            return e.getCause();
        } else {
            return null;
        }
    }

    // ****
    // Misc
    // ****

    private static final int SENDERS_AND_CLOSED_FLAG_SHIFT = 60;
    private static final long SENDERS_COUNTER_MASK = (1L << SENDERS_AND_CLOSED_FLAG_SHIFT) - 1;

    private long getSendersCounter(long sendersAndClosedFlag) {
        return sendersAndClosedFlag & SENDERS_COUNTER_MASK;
    }

    private boolean isClosed(long sendersAndClosedFlag) {
        return sendersAndClosedFlag >> SENDERS_AND_CLOSED_FLAG_SHIFT == 1;
    }

    private long setClosedFlag(long sendersAndClosedFlag) {
        return sendersAndClosedFlag | (1L << SENDERS_AND_CLOSED_FLAG_SHIFT);
    }

    @Override
    public String toString() {
        var smallestSegment = Stream.of(sendSegment.get(), receiveSegment.get(), bufferEndSegment.get())
                .filter(s -> s != Segment.NULL_SEGMENT)
                .min(Comparator.comparingLong(Segment::getId)).get();

        var scf = sendersAndClosedFlag.get();
        var sendersCounter = getSendersCounter(scf);
        var isClosed = isClosed(scf);

        var sb = new StringBuilder();
        sb.append("Channel(capacity=").append(capacity)
                .append(", closed=").append(isClosed)
                .append(", sendSegment=").append(sendSegment.get().getId()).append(", sendCounter=").append(sendersCounter)
                .append(", receiveSegment=").append(receiveSegment.get().getId()).append(", receiveCounter=").append(receivers.get())
                .append(", bufferEndSegment=").append(bufferEndSegment.get().getId()).append(", bufferEndCounter=").append(bufferEnd.get())
                .append("): \n");
        var s = smallestSegment;
        while (s != null) {
            sb.append("  ").append(s).append(": ");
            for (int i = 0; i < Segment.SEGMENT_SIZE; i++) {
                var state = s.getCell(i);
                switch (state) {
                    case null -> sb.append("E");
                    case IN_BUFFER -> sb.append("IB");
                    case DONE -> sb.append("D");
                    case INTERRUPTED_SEND -> sb.append("IS");
                    case INTERRUPTED_RECEIVE -> sb.append("IR");
                    case BROKEN -> sb.append("B");
                    case RESUMING -> sb.append("R");
                    case CLOSED -> sb.append("C");
                    case Buffered b -> sb.append("V(").append(b.value()).append(")");
                    case Continuation c when c.isSender() -> sb.append("WS(").append(c.getPayload()).append(")");
                    case Continuation c -> sb.append("WR");
                    default -> throw new IllegalStateException("Unexpected value: " + state);
                }
                if (i != Segment.SEGMENT_SIZE - 1) sb.append(",");
            }
            s = s.getNext();
            if (s != null) sb.append("\n");
        }
        return sb.toString();
    }
}

/**
 * Possible return values of {@code Channel#updateCellSend}: one of the enum constants below.
 */
enum SendResult {
    AWAITED,
    BUFFERED,
    RESUMED,
    FAILED,
    CLOSED
}

/**
 * Possible return values of {@code Channel#updateCellReceive}: one of the enum constants below, or the received value.
 */
enum ReceiveResult {
    FAILED,
    CLOSED
}

/**
 * Possible return values of {@code Channel#expandBuffer}: one of the enum constants below, or the received value.
 */
enum ExpandBufferResult {
    DONE,
    FAILED,
    CLOSED
}

// possible states of a cell: one of the enum constants below, Buffered, or Continuation

enum CellState {
    DONE,
    INTERRUPTED_SEND, // the send/receive differentiation is important for expandBuffer
    INTERRUPTED_RECEIVE,
    BROKEN,
    IN_BUFFER, // used to inform a potentially concurrent sender that the cell is now in the buffer
    RESUMING, // expandBuffer is resuming a sender
    CLOSED
}

record Buffered(Object value) {}

final class Continuation {
    /**
     * The number of busy-looping iterations before yielding, during {@link Continuation#await(Segment, int)}.
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
     * @return {@code true}, if the continuation is a sender; {@code false}, if it's a receiver.
     */
    boolean isSender() {
        return payload != null; // senders set the value to send as the payload; moreover, a send value can't be null
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
     * @param segment   The segment in which the cell is located.
     * @param cellIndex The index of the cell for which to change the state to interrupted, if interruption happens.
     * @return The value with which the continuation was resumed.
     */
    Object await(Segment segment, int cellIndex) throws InterruptedException {
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
                        var isSender = isSender();
                        segment.setCell(cellIndex, isSender ? INTERRUPTED_SEND : INTERRUPTED_RECEIVE);

                        // notifying the segment - if all cells become interrupted, the segment can be removed
                        if (isSender) {
                            segment.cellInterruptedSender_orClosed();
                        } else {
                            segment.cellInterruptedReceiver();
                        }

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

// used to resume the continuation when a channel is closed - a special type, not possible to be referenced by the user
enum ChannelClosedMarker {
    CLOSED
}
