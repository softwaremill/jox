package com.softwaremill.jox;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.softwaremill.jox.CellState.*;
import static com.softwaremill.jox.Segment.findAndMoveForward;

/**
 * Channel is a thread-safe data structure which exposes three basic operations:
 * <p>
 * - {@link Channel#send(Object)}-ing a value to the channel. Values can't be {@code null}.
 * - {@link Channel#receive()}-ing a value from the channel
 * - closing the channel using {@link Channel#done()} or {@link Channel#error(Throwable)}
 * <p>
 * There are three channel flavors:
 * <p>
 * - rendezvous channels, where senders and receivers must meet to exchange values
 * - buffered channels, where a given number of sent values might be buffered, before subsequent `send`s block
 * - unlimited channels, where an unlimited number of values might be buffered, hence `send` never blocks
 * <p>
 * The no-argument {@link Channel} constructor creates a rendezvous channel, while a buffered channel can be created
 * by providing a positive integer to the constructor. A rendezvous channel behaves like a buffered channel with
 * buffer size 0. An unlimited channel can be created using {@link Channel#newUnlimitedChannel()}.
 * <p>
 * In a rendezvous channel, senders and receivers block, until a matching party arrives (unless one is already waiting).
 * Similarly, buffered channels block if the buffer is full (in case of senders), or in case of receivers, if the
 * buffer is empty and there are no waiting senders.
 * <p>
 * All blocking operations behave properly upon interruption.
 * <p>
 * Channels might be closed, either because no more values will be produced by the source (using
 * {@link Channel#done()}), or because there was an error while producing or processing the received values (using
 * {@link Channel#error(Throwable)}).
 * <p>
 * After closing, no more values can be sent to the channel. If the channel is "done", any pending sends will be
 * completed normally. If the channel is in an "error" state, pending sends will be interrupted and will return with
 * the reason for the closure.
 * <p>
 * In case the channel is closed, one of the {@link ChannelClosedException}s is thrown. Alternatively, you can call
 * the less type-safe, but more exception-safe {@link Channel#sendSafe(Object)} and {@link Channel#receiveSafe()}
 * methods, which do not throw in case the channel is closed, but return one of the {@link ChannelClosed} values.
 *
 * @param <T> The type of the values processed by the channel.
 */
public final class Channel<T> implements Source<T>, Sink<T> {
    /*
    Inspired by the "Fast and Scalable Channels in Kotlin Coroutines" paper (https://arxiv.org/abs/2211.04986), and
    the Kotlin implementation (https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/common/src/channels/BufferedChannel.kt).

    Notable differences from the Kotlin implementation:
    * we block (virtual) threads, instead of suspend functions
    * in Kotlin's channels, the buffer stores both the values (in even indexes), and the state for each cell (in odd
      indexes). This would be also possible here, but in two-thread rendezvous tests, this is slightly slower than the
      approach below: we transmit the values inside objects representing state. This does incur an additional
      allocation in case of the `Buffered` state (when there's a waiting receiver - we can't simply use a constant).
      However, we add a field to `Continuation` (which is a channel-specific class, unlike in Kotlin), to avoid the
      allocation when the sender suspends.
    * as we don't directly store values in the buffer, we don't need to clear them on interrupt etc. This is done
      automatically when the cell's state is set to something else than a Continuation/Buffered.
    * instead of the `completedExpandBuffersAndPauseFlag` counter, we maintain a counter of cells which haven't been
      interrupted & processed by `expandBuffer` in each segment. The segment becomes logically removed, only once all
      cells have been interrupted, processed & there are no pointers. The segment is notified that a cell is interrupted
      directly from `Continuation`, and that a cell is processed after `expandBuffer` completes.
    * the close procedure is a bit different - the Kotlin version does this "cooperatively", that is multiple threads
      that observe that the channel is closing participate in appropriate state mutations. This doesn't seem to be
      necessary in this implementation. Instead, `close()` sets the closed flag and closes the segment chain - which
      constraints the number of cells to close. `send()` observes the closed status right away, `receive()` observes
      it via the closed segment chain, or via closed cells. Same as in Kotlin, the cells are closed in reverse order.
      All eligible cells are attempted to be closed, so it's guaranteed that each operation will observe the closing
      appropriately.

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
     * Segments holding cell states. State can be {@link CellState}, {@link Continuation}, {@link SelectInstance}, or a user-provided buffered value.
     */
    private final AtomicReference<Segment> sendSegment;
    private final AtomicReference<Segment> receiveSegment;
    private final AtomicReference<Segment> bufferEndSegment;

    private final AtomicReference<ChannelClosed> closedReason;

    private final boolean isRendezvous;
    private final boolean isUnlimited;

    /**
     * Creates a rendezvous channel.
     */
    public Channel() {
        this(0);
    }

    /**
     * Creates a buffered channel (when capacity is positive), or a rendezvous channel if the capacity is 0.
     */
    public Channel(int capacity) {
        if (capacity < UNLIMITED_CAPACITY) {
            throw new IllegalArgumentException("Capacity must be 0 (rendezvous), positive (buffered) or -1 (unlimited channels).");
        }

        this.capacity = capacity;
        isRendezvous = capacity == 0L;
        isUnlimited = capacity == UNLIMITED_CAPACITY;
        var isRendezvousOrUnlimited = isRendezvous || isUnlimited;

        var firstSegment = new Segment(0, null, isRendezvousOrUnlimited ? 2 : 3, isRendezvousOrUnlimited);

        sendSegment = new AtomicReference<>(firstSegment);
        receiveSegment = new AtomicReference<>(firstSegment);
        // If the capacity is 0 or -1, buffer expansion never happens, so the buffer end segment points to a null segment,
        // not the first one. This is also reflected in the pointer counter of firstSegment.
        bufferEndSegment = new AtomicReference<>(isRendezvousOrUnlimited ? Segment.NULL_SEGMENT : firstSegment);

        bufferEnd = new AtomicLong(capacity);

        closedReason = new AtomicReference<>(null);
    }

    public static <T> Channel<T> newUnlimitedChannel() {
        return new Channel<>(UNLIMITED_CAPACITY);
    }

    private static final int UNLIMITED_CAPACITY = -1;

    // *******
    // Sending
    // *******

    @Override
    public void send(T value) throws InterruptedException {
        var r = sendSafe(value);
        if (r instanceof ChannelClosed c) {
            throw c.toException();
        }
    }

    @Override
    public Object sendSafe(T value) throws InterruptedException {
        return doSend(value, null, null);
    }

    /**
     * @return If {@code select} & {@code selectClause} is {@code null}: {@code null} when the value was sent, or
     * {@link ChannelClosed}, when the channel is closed. Otherwise, might also return {@link StoredSelectClause}.
     */
    private Object doSend(T value, SelectInstance select, SelectClause<?> selectClause) throws InterruptedException {
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
                segment = findAndMoveForward(sendSegment, segment, id);
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

            var sendResult = updateCellSend(segment, i, s, value, select, selectClause);
            if (sendResult == SendResult.BUFFERED) {
                // a receiver is coming, or we are in buffer
                // similarly as above, not clearing the previous pointer
                return null;
            } else if (sendResult == SendResult.AWAITED) {
                // the thread was suspended and then resumed by a receiver or by buffer expansion
                // not clearing the previous pointer, because of the buffering possibility
                return null;
            } else if (sendResult == SendResult.RESUMED) {
                // we resumed a receiver - we can be sure that R > s
                segment.cleanPrev();
                return null;
            } else if (sendResult instanceof StoredSelectClause ss) {
                // we stored a select instance - there's no matching receive, not clearing the previous segment
                return ss;
            } else if (sendResult == SendResult.FAILED) {
                // the cell was broken (hence already processed by a receiver) or interrupted (also a receiver
                // must have been there); in both cases R > s
                segment.cleanPrev();
                // trying again with a new cell
            } else if (sendResult == SendResult.CLOSED) {
                // not cleaning the previous segments - the close procedure might still need it
                return closedReason.get();
            } else {
                throw new IllegalStateException("Unexpected result: " + sendResult);
            }
        }
    }

    /**
     * @param segment The segment which stores the cell's state.
     * @param i       The index within the {@code segment}.
     * @param s       Index of the reserved cell.
     * @param value   The value to send.
     * @return One of {@link SendResult}, or {@link StoredSelectClause} if {@code select} is not {@code null}.
     */
    private Object updateCellSend(Segment segment, int i, long s, T value, SelectInstance select, SelectClause<?> selectClause) throws InterruptedException {
        while (true) {
            var state = segment.getCell(i); // reading the current state of the cell; we'll try to update it atomically

            if (state == null) {
                // reading the buffer end & receiver's counter if needed
                if (!isUnlimited && s >= (isRendezvous ? 0 : bufferEnd.get()) && s >= receivers.get()) {
                    // cell is empty, and no receiver, not in buffer -> suspend
                    if (select != null) {
                        // cell is empty, no receiver, and we are in a select -> store the select instance
                        // and await externally; the value to send is stored in the selectClause
                        var storedSelect = new StoredSelectClause(select, segment, i, true, selectClause, value);
                        if (segment.casCell(i, state, storedSelect)) {
                            return storedSelect;
                        }
                        // else: CAS unsuccessful, repeat
                    } else {
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
                    }
                } else {
                    // cell is empty, but a receiver is in progress, or in buffer -> elimination
                    if (segment.casCell(i, null, value)) {
                        return SendResult.BUFFERED;
                    }
                    // else: CAS unsuccessful, repeat
                }
            } else if (state == IN_BUFFER) {
                // cell just became part of the buffer
                if (segment.casCell(i, IN_BUFFER, value)) {
                    return SendResult.BUFFERED;
                }
                // else: CAS unsuccessful, repeat
            } else if (state instanceof Continuation c) {
                // a receiver is waiting -> trying to resume
                if (c.tryResume(value)) {
                    segment.setCell(i, DONE);
                    return SendResult.RESUMED;
                } else {
                    // when cell interrupted -> trying with a new one
                    // when close in progress -> subsequent cells are already closed, this will be detected in the next iteration
                    return SendResult.FAILED;
                }
            } else if (state instanceof StoredSelectClause ss) {
                // Setting the payload first, before the memory barrier created by potentially setting `SelectInstance.state`.
                // The state is the read in select's main thread. Since we have this send-cell exclusively, no other thread
                // will attempt to call `setPayload`.
                ss.setPayload(value);

                // a select clause is waiting -> trying to resume
                if (ss.getSelect().trySelect(ss)) {
                    segment.setCell(i, DONE);
                    return SendResult.RESUMED;
                } else {
                    // select unsuccessful -> trying with a new one
                    return SendResult.FAILED;
                }
            } else if (state == INTERRUPTED_RECEIVE || state == BROKEN) {
                // cell interrupted or poisoned -> trying with a new one
                return SendResult.FAILED;
            } else if (state == CLOSED) {
                return SendResult.CLOSED;
            } else {
                throw new IllegalStateException("Unexpected state: " + state);
            }
        }
    }

    // *********
    // Receiving
    // *********

    @Override
    public T receive() throws InterruptedException {
        var r = receiveSafe();
        if (r instanceof ChannelClosed c) {
            throw c.toException();
        } else {
            //noinspection unchecked
            return (T) r;
        }
    }

    @Override
    public Object receiveSafe() throws InterruptedException {
        return doReceive(null, null);
    }

    /**
     * @return If {@code select} & {@code selectClause} is {@code null}: the received value, or {@link ChannelClosed},
     * when the channel is closed. Otherwise, might also return {@link StoredSelectClause}.
     */
    private Object doReceive(SelectInstance select, SelectClause<?> selectClause) throws InterruptedException {
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
                segment = findAndMoveForward(receiveSegment, segment, id);
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

            var result = updateCellReceive(segment, i, r, select, selectClause);
            if (result == ReceiveResult.CLOSED) {
                // not cleaning the previous segments - the close procedure might still need it
                return closedReason.get();
            } else {
              /*
                After `updateCellReceive` completes and the channel isn't closed, we can be sure that S > r, unless
                we stored the given select instance:
                - if we stored and awaited a continuation, and it was resumed, then a sender must have appeared
                - if we marked the cell as broken, then a sender is in progress in that cell
                - if a continuation was present, then the sender must have been there
                - if the cell was interrupted, that could have been only because of a sender
                - if a value was buffered, that's because there was/is a matching sender

                The only cases when S <= r are when:
                - awaiting on the continuation is interrupted, in which case the exception propagates outside of this method
                - we stored the given select instance (in an empty / in-buffer cell)
                */
                if (!(result instanceof StoredSelectClause)) {
                    segment.cleanPrev();
                }
                if (result != ReceiveResult.FAILED) {
                    return result;
                }
            }
        }
    }

    /**
     * Invariant maintained by receive + expandBuffer: between R and B the number of cells that are empty / IN_BUFFER should be equal
     * to the buffer size. These are the cells that can accept a sender without suspension.
     * <p>
     * This method might suspend (and be interrupted) only if {@code select} is {@code null}.
     *
     * @param segment The segment which stores the cell's state.
     * @param i       The index within the {@code segment}.
     * @param r       Index of the reserved cell.
     * @param select  The select instance of which this receive is part of, or {@code null} (along with {@code selectClause}) if this is a direct receive call.
     * @return Either a state-result ({@link ReceiveResult}), {@link StoredSelectClause} in case {@code select} is not {@code null}, or the received value.
     */
    private Object updateCellReceive(Segment segment, int i, long r, SelectInstance select, SelectClause<?> selectClause) throws InterruptedException {
        while (true) {
            var state = segment.getCell(i); // reading the current state of the cell; we'll try to update it atomically

            if (state == null || state == IN_BUFFER) {
                if (r >= getSendersCounter(sendersAndClosedFlag.get())) { // reading the sender's counter
                    if (select != null) {
                        // cell is empty, no sender, and we are in a select -> store the select instance
                        // and await externally
                        var storedSelect = new StoredSelectClause(select, segment, i, false, selectClause, null);
                        if (segment.casCell(i, state, storedSelect)) {
                            expandBuffer();
                            return storedSelect;
                        }
                        // else: CAS unsuccessful, repeat
                    } else {
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
                    }
                } else {
                    // sender in progress, receiver changed state first -> restart
                    if (segment.casCell(i, state, BROKEN)) {
                        expandBuffer();
                        return ReceiveResult.FAILED;
                    }
                    // else: CAS unsuccessful, repeat
                }
            } else if (state instanceof Continuation c) {
                // resolving a potential race with `expandBuffer`
                if (segment.casCell(i, state, RESUMING)) {
                    // a sender is waiting -> trying to resume
                    if (c.tryResume(0)) {
                        segment.setCell(i, DONE);
                        expandBuffer();
                        return c.getPayload();
                    } else {
                        // when cell interrupted -> trying with a new one
                        // the state will be set to INTERRUPTED_SEND by the continuation, meanwhile everybody else will observe RESUMING
                        // when close in progress -> the cell state will be updated to CLOSED, subsequent cells are already closed,
                        // which will be detected in the next iteration
                        return ReceiveResult.FAILED;
                    }
                }
                // else: CAS unsuccessful, repeat
            } else if (state instanceof StoredSelectClause ss) {
                // resolving a potential race with `expandBuffer`
                if (segment.casCell(i, state, RESUMING)) {
                    // a send clause is waiting -> trying to resume
                    if (ss.getSelect().trySelect(ss)) {
                        segment.setCell(i, DONE);
                        expandBuffer();
                        return ss.getPayload();
                    } else {
                        // when select fails (another clause is selected, select is interrupted, closed etc.) -> trying with a new one
                        // the state will be set to INTERRUPTED_SEND by the cleanup, meanwhile everybody else will observe RESUMING
                        return ReceiveResult.FAILED;
                    }
                }
                // else: CAS unsuccessful, repeat
            } else if (state instanceof CellState) {
                if (state == INTERRUPTED_SEND) {
                    // cell interrupted -> trying with a new one
                    return ReceiveResult.FAILED;
                } else if (state == RESUMING) {
                    // expandBuffer() is resuming the sender -> repeat
                    Thread.onSpinWait();
                } else if (state == CLOSED) {
                    return ReceiveResult.CLOSED;
                } else {
                    throw new IllegalStateException("Unexpected state: " + state);
                }
            } else { // buffered value
                segment.setCell(i, DONE);
                expandBuffer();
                // an elimination has happened -> finish
                return state;
            }
        }
    }

    // ****************
    // Buffer expansion
    // ****************

    private void expandBuffer() {
        if (isRendezvous || isUnlimited) return;
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
                segment = findAndMoveForward(bufferEndSegment, segment, id);
                if (segment == null) {
                    // the channel has been closed, b points to a segment which doesn't exist, nowhere to expand
                    return;
                }

                // if we still have another segment, the segment must have been removed; this can happen if the current
                // cell has been an interrupted receiver (such cells are immediately marked as processed, which can cause
                // segment removal); other cells might have been both interrupted senders/receivers (but all interrupted
                // senders must have already been processed by expandBuffer)
                if (segment.getId() != id) {
                    // skipping all interrupted cells as an optimization
                    bufferEnd.compareAndSet(b, segment.getId() * Segment.SEGMENT_SIZE);
                    // another buffer expansion already happened for this cell (in the removed segment)
                    return;
                }
            }

            var result = updateCellExpandBuffer(segment, i);
            if (result == ExpandBufferResult.FAILED) {
                // the cell must have been an interrupted sender; notifying the segment that the cell is now processed
                // and trying with a new cell
                segment.cellProcessed();
            } else {
                // done or closed
                return;
            }
        }
    }

    private ExpandBufferResult updateCellExpandBuffer(Segment segment, int i) {
        while (true) {
            var state = segment.getCell(i); // reading the current state of the cell; we'll try to update it atomically

            if (state == null) {
                // the cell is empty, a sender is or will be coming - set the cell as "in buffer" to let the sender know in case it's in progress
                if (segment.casCell(i, null, IN_BUFFER)) {
                    return ExpandBufferResult.DONE;
                }
                // else: CAS unsuccessful, repeat
            } else if (state == DONE) {
                // sender & receiver have already paired up, another buffer expansion already happened
                return ExpandBufferResult.DONE;
            } else if (state instanceof Continuation c && c.isSender()) {
                if (segment.casCell(i, state, RESUMING)) {
                    // a sender is waiting -> trying to resume
                    if (c.tryResume(0)) {
                        segment.setCell(i, c.getPayload());
                        return ExpandBufferResult.DONE;
                    } else {
                        // when cell interrupted -> trying with a new one
                        // the state will be set to INTERRUPTED_SEND by the continuation, meanwhile everybody else will observe RESUMING
                        // when close in progress -> the cell state will be updated to CLOSED, subsequent cells are already closed,
                        // which will be detected in the next iteration
                        return ExpandBufferResult.FAILED;
                    }
                }
                // else: CAS unsuccessful, repeat
            } else if (state instanceof Continuation) {
                // must be a receiver continuation - another buffer expansion already happened
                return ExpandBufferResult.DONE;
            } else if (state instanceof StoredSelectClause ss && ss.isSender()) {
                if (segment.casCell(i, state, RESUMING)) {
                    // a send clause is waiting -> trying to resume
                    if (ss.getSelect().trySelect(ss)) {
                        segment.setCell(i, ss.getPayload());
                        return ExpandBufferResult.DONE;
                    } else {
                        // select unsuccessful -> trying with a new one
                        // the state will be set to INTERRUPTED_SEND by the cleanup, meanwhile everybody else will observe RESUMING
                        return ExpandBufferResult.FAILED;
                    }
                }
                // else: CAS unsuccessful, repeat
            } else if (state instanceof StoredSelectClause) {
                // must be a receiver clause of the select - another buffer expansion already happened
                return ExpandBufferResult.DONE;
            } else if (state instanceof CellState) {
                if (state == INTERRUPTED_SEND) {
                    // a sender was interrupted - restart
                    return ExpandBufferResult.FAILED;
                } else if (state == INTERRUPTED_RECEIVE) {
                    // a receiver continuation must have been here before - another buffer expansion already happened
                    return ExpandBufferResult.DONE;
                } else if (state == BROKEN) {
                    // the cell is broken, receive() started another buffer expansion
                    return ExpandBufferResult.DONE;
                } else if (state == RESUMING) {
                    Thread.onSpinWait(); // receive() is resuming the sender -> repeat
                } else if (state == CLOSED) {
                    // the channel is closed, all subsequent cells must have already been closed (as we're closing from
                    // the end)
                    return ExpandBufferResult.CLOSED;
                } else {
                    throw new IllegalStateException("Unexpected state: " + state);
                }
            } else {
                // buffered value: if the ordering of operations was different, we would put IN_BUFFER in that cell and finish
                return ExpandBufferResult.DONE;
            }
        }
    }

    // *******
    // Closing
    // *******

    @Override
    public void done() {
        var r = doneSafe();
        if (r instanceof ChannelClosed c) {
            throw c.toException();
        }
    }

    @Override
    public Object doneSafe() {
        return closeSafe(new ChannelDone());
    }

    @Override
    public void error(Throwable reason) {
        if (reason == null) {
            throw new NullPointerException("Error reason cannot be null");
        }
        var r = errorSafe(reason);
        if (r instanceof ChannelClosed c) {
            throw c.toException();
        }
    }

    @Override
    public Object errorSafe(Throwable reason) {
        return closeSafe(new ChannelError(reason));
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

        if (channelClosed instanceof ChannelError) {
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
            if (state == null) {
                if (segment.casCell(i, null, CLOSED)) {
                    // we treat closing a cell same as if it there was an interrupted receiver: there's no need
                    // for the cell to be processed by `expandBuffer`, and segments full of closed cells can be
                    // safely removed
                    segment.cellInterruptedReceiver();
                    return;
                }
            } else if (state == IN_BUFFER) {
                if (segment.casCell(i, state, CLOSED)) {
                    segment.cellInterruptedReceiver();
                    return;
                }
            } else if (state instanceof Continuation c) {
                // potential race with sender/receiver resuming the continuation - resolved by synchronizing on
                // `Continuation.data`: only one thread will successfully change its value from `null`
                if (c.tryResume(ChannelClosedMarker.CLOSED)) {
                    segment.setCell(i, CLOSED);
                    segment.cellInterruptedReceiver();
                    return;
                } else {
                    // when cell interrupted - the segment counters will be appropriately decremented from the
                    // continuation, depending on if this is a sender or receiver; moreover, for interrupted senders
                    // this will be processed by expandBuffer as we are closing cells from the end
                    // otherwise, the cell might be completed with a value, and the result is processed normally
                    // on another thread
                    return;
                }
            } else if (state instanceof StoredSelectClause ss) {
                ss.getSelect().channelClosed(ss, closedReason.get());
                // not setting the state & updating counters, as each non-selected stored select cell will be
                // cleaned up, setting an interrupted state (and informing the segment)
                // until this happens, other (concurrent) invocations of `channelClosed` or `trySelect` will still
                // observe the `StoredSelectClause` here: however, the select's internal state is changed, making
                // senders/receivers proceed to the next cell; in other words, the race in changing the cell's state
                // is delegated to the select's state

                return;
            } else if (state == DONE || state == BROKEN) {
                // nothing to do - a sender & receiver have already met
                return;
            } else if (state == INTERRUPTED_RECEIVE || state == INTERRUPTED_SEND) {
                // nothing to do - segment counters already decremented
                return;
            } else if (state == RESUMING) {
                Thread.onSpinWait(); // receive() or expandBuffer() are resuming the cell - wait
            } else {
                // buffered value: discarding
                if (segment.casCell(i, state, CLOSED)) {
                    segment.cellInterruptedReceiver();
                    return;
                }
            }
        }
    }

    @Override
    public ChannelClosed closedForSend() {
        return isClosed(sendersAndClosedFlag.get()) ? closedReason.get() : null;
    }

    @Override
    public ChannelClosed closedForReceive() {
        if (isClosed(sendersAndClosedFlag.get())) {
            var cr = closedReason.get(); // cannot be null
            if (cr instanceof ChannelError) {
                return cr;
            } else {
                // channel is done, checking if there's anything left to receive
                return hasValuesToReceive() ? null : cr;
            }
        } else {
            return null;
        }
    }

    private boolean hasValuesToReceive() {
        while (true) {
            // reading the segment before the counter - this is needed to find the required segment later
            var segment = receiveSegment.get();
            // r is the cell which will be used by the next receive
            var r = receivers.get();
            var s = getSendersCounter(sendersAndClosedFlag.get());

            if (s <= r) {
                // for sure, nothing is buffered / no senders are waiting
                return false;
            }

            // calculating the segment id and the index within the segment
            var id = r / Segment.SEGMENT_SIZE;
            var i = (int) (r % Segment.SEGMENT_SIZE);

            // check if `receiveSegment` stores a previous segment, if so move the reference forward
            if (segment.getId() != id) {
                segment = findAndMoveForward(receiveSegment, segment, id);
                if (segment == null) {
                    // the channel has been closed, r points to a segment which doesn't exist
                    return false;
                }

                // if we still have another segment, the segment must have been removed
                if (segment.getId() != id) {
                    // skipping all interrupted cells, and trying with a new one
                    receivers.compareAndSet(r, segment.getId() * Segment.SEGMENT_SIZE);
                    continue;
                }
            }

            // we know that s > r
            segment.cleanPrev();

            if (hasValueToReceive(segment, i, r)) {
                return true;
            } else {
                // nothing to receive, we can (try to, if not already done) bump the counter and try again
                receivers.compareAndSet(r, r + 1);
            }
        }
    }

    private boolean hasValueToReceive(Segment segment, int i, long r) {
        while (true) {
            var state = segment.getCell(i); // reading the current state of the cell

            if (state == null || state == IN_BUFFER) {
                // this can only happen if a sender is in progress (we checked before that s > r)
                // waiting what the sender is going to do -> repeat
                Thread.onSpinWait();
            } else if (state instanceof Continuation c) {
                // a receiver might have gotten suspended while hasValuesToReceive() is running - then, no value to receive here & the r counter is updated.
                return c.isSender();
            } else if (state instanceof StoredSelectClause ss) {
                return ss.isSender(); // as above
            } else if (state instanceof CellState) {
                if (state == INTERRUPTED_SEND || state == INTERRUPTED_RECEIVE) {
                    // cell interrupted -> nothing to receive; in case of an interrupted receiver, the counter is already updated
                    return false;
                } else if (state == RESUMING) {
                    // receive() or expandBuffer() is resuming the sender -> repeat
                    Thread.onSpinWait();
                } else if (state == CLOSED) {
                    return false;
                } else if (state == DONE || state == BROKEN) {
                    // a concurrent receiver already finished / poisoned the cell
                    return false;
                } else {
                    throw new IllegalStateException("Unexpected state: " + state);
                }
            } else {
                // buffered value
                return true;
            }
        }
    }

    // **************
    // Select clauses
    // **************

    private static final Function<Object, Object> IDENTITY = Function.identity();

    @Override
    public SelectClause<T> receiveClause() {
        //noinspection unchecked
        return receiveClause((Function<T, T>) IDENTITY);
    }

    @Override
    public <U> SelectClause<U> receiveClause(Function<T, U> callback) {
        return new SelectClause<>() {
            @Override
            Channel<?> getChannel() {
                return Channel.this;
            }

            @Override
            Object register(SelectInstance select) {
                try {
                    return doReceive(select, this);
                } catch (InterruptedException e) {
                    // not possible, as we provide a select, so no suspension should happen
                    throw new IllegalStateException(e);
                }
            }

            @Override
            U transformedRawValue(Object rawValue) {
                //noinspection unchecked
                return callback.apply((T) rawValue);
            }
        };
    }

    @Override
    public SelectClause<Void> sendClause(T value) {
        return sendClause(value, () -> null);
    }

    @Override
    public <U> SelectClause<U> sendClause(T value, Supplier<U> callback) {
        return new SelectClause<>() {
            @Override
            Channel<?> getChannel() {
                return Channel.this;
            }

            @Override
            Object register(SelectInstance select) {
                try {
                    var result = doSend(value, select, this);
                    // we can't return null, the actual value doesn't matter
                    return result == null ? SentClauseMarker.SENT : result;
                } catch (InterruptedException e) {
                    // not possible, as we provide a select, so no suspension should happen
                    throw new IllegalStateException(e);
                }
            }

            @Override
            U transformedRawValue(Object rawValue) {
                return callback.get();
            }
        };
    }

    void cleanupStoredSelectClause(Segment segment, int i, boolean isSender) {
        // We treat the cell as if it was interrupted - the code is same as in `Continuation.await`;
        // there's no need to resolve races with `SelectInstance.trySelect`, as cleanup is called either when a clause
        // is selected, a channel is closed, or during re-registration. In all cases `trySelect` would fail.
        // In other words, the races are resolved by synchronizing on `SelectInstance.state`.
        segment.setCell(i, isSender ? INTERRUPTED_SEND : INTERRUPTED_RECEIVE);

        // notifying the segment - if all cells become interrupted, the segment can be removed
        if (isSender) {
            segment.cellInterruptedSender();
        } else {
            segment.cellInterruptedReceiver();
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
        //noinspection OptionalGetWithoutIsPresent
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
                if (state == null) {
                    sb.append("E");
                } else if (state == IN_BUFFER) {
                    sb.append("IB");
                } else if (state == DONE) {
                    sb.append("D");
                } else if (state == INTERRUPTED_SEND) {
                    sb.append("IS");
                } else if (state == INTERRUPTED_RECEIVE) {
                    sb.append("IR");
                } else if (state == BROKEN) {
                    sb.append("B");
                } else if (state == RESUMING) {
                    sb.append("R");
                } else if (state == CLOSED) {
                    sb.append("C");
                } else if (state instanceof Continuation c && c.isSender()) {
                    sb.append("WS(").append(c.getPayload()).append(")");
                } else if (state instanceof Continuation) {
                    sb.append("WR");
                } else if (state instanceof StoredSelectClause ss && ss.isSender()) {
                    sb.append("SS");
                } else if (state instanceof StoredSelectClause) {
                    sb.append("SR");
                } else {
                    // buffered value
                    sb.append("V(").append(state).append(")");
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
 * Possible return values of {@code Channel#updateCellReceive}: one of the enum constants below, the received value or {@link SelectStored} (used for cleanup).
 */
enum ReceiveResult {
    FAILED,
    CLOSED
}

record SelectStored(Segment segment, int i) {}

/**
 * Possible return values of {@code Channel#expandBuffer}: one of the enum constants below, or the received value.
 */
enum ExpandBufferResult {
    DONE,
    FAILED,
    CLOSED
}

// possible states of a cell: one of the enum constants below, Continuation, SelectInstance or a buffered value (directly as given by the user)

enum CellState {
    DONE,
    INTERRUPTED_SEND, // the send/receive differentiation is important for expandBuffer
    INTERRUPTED_RECEIVE,
    BROKEN,
    IN_BUFFER, // used to inform a potentially concurrent sender that the cell is now in the buffer
    RESUMING, // expandBuffer is resuming a sender
    CLOSED
}

final class Continuation {
    /**
     * The number of busy-looping iterations before yielding, during {@link Continuation#await(Segment, int)}.
     * {@code 0}, if there's a single CPU. When there's no more than 4 CPUs, we use {@code 128} iterations: this is
     * based on the (limited) testing that we've done with various systems. Otherwise, we use 1024 iterations.
     * This might need revisiting when more testing & more benchmarks are available.
     */
    static final int SPINS;

    static {
        var nproc = Runtime.getRuntime().availableProcessors();
        SPINS = (nproc == 1) ? 0 : ((nproc <= 4) ? (1 << 7) : (1 << 10));
    }

    private final Thread creatingThread;
    @SuppressWarnings("unused")
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
     * @return {@code true} if the continuation was resumed successfully. {@code false} if it was interrupted.
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
                            segment.cellInterruptedSender();
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

// used as a result of SendClause.register, instead of null, to indicate that the select clause has been selected during registration
enum SentClauseMarker {
    SENT
}
