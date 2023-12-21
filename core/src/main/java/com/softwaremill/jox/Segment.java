package com.softwaremill.jox;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

final class Segment {
    // in the first 6 bits, we store the number of cells that haven't been interrupted yet
    // in the second 6 bits, we store the number of cells which haven't been processed by expandBuffer yet (in buffered channels)
    // in bits 13 & 14, we store the number of incoming pointers to this segment
    static final int SEGMENT_SIZE = 32; // 2^5
    private static final int PROCESSED_SHIFT = 6; // to store values between 0 and 32 (inclusive) we need 6 bits
    private static final int POINTERS_SHIFT = 12;
    static final Segment NULL_SEGMENT = new Segment(-1, null, 0, false);

    /**
     * Used in {@code next} to indicate that the segment is closed.
     */
    private enum State {
        CLOSED
    }

    private final long id;
    private final AtomicReferenceArray<Object> data = new AtomicReferenceArray<>(SEGMENT_SIZE);
    /**
     * Possible values: {@code Segment} or {@code State.CLOSED} (union type).
     */
    private final AtomicReference<Object> next = new AtomicReference<>(null);
    private final AtomicReference<Segment> prev;
    /**
     * A single counter that can be inspected & modified atomically, which includes:
     * - the number of incoming pointers (shifted by {@link Segment#POINTERS_SHIFT} to the left)
     * - the number of cells, which haven't been processed by {@code Channel.expandBuffer} or closed yet
     * (shifted by {@link Segment#PROCESSED_SHIFT} to the left)
     * - the number of cells, which haven't been interrupted or closed yet (in the first 6 bits)
     * When this reaches 0, the segment is logically removed.
     */
    private final AtomicInteger pointers_notProcessed_notInterrupted;
    private final boolean countProcessed;

    Segment(long id, Segment prev, int pointers, boolean countProcessed) {
        this.id = id;
        this.prev = new AtomicReference<>(prev);
        this.pointers_notProcessed_notInterrupted = new AtomicInteger(
                SEGMENT_SIZE +
                        (countProcessed ? (SEGMENT_SIZE << PROCESSED_SHIFT) : 0) +
                        (pointers << POINTERS_SHIFT));
        this.countProcessed = countProcessed;
    }

    long getId() {
        return id;
    }

    void cleanPrev() {
        prev.set(null);
    }

    Segment getNext() {
        var s = next.get();
        return s == State.CLOSED ? null : (Segment) s;
    }

    Segment getPrev() {
        return prev.get();
    }

    private boolean setNextIfNull(Segment setTo) {
        return next.compareAndSet(null, setTo);
    }

    Object getCell(int index) {
        return data.get(index);
    }

    void setCell(int index, Object value) {
        data.set(index, value);
    }

    boolean casCell(int index, Object expected, Object newValue) {
        return data.compareAndSet(index, expected, newValue);
    }

    private boolean isTail() {
        return getNext() == null;
    }

    /**
     * @return {@code true} if this segment is logically removed, that is there are no incoming pointers, all cells
     * have been processed by expandBuffer, and all cells have been interrupted.
     */
    boolean isRemoved() {
        return pointers_notProcessed_notInterrupted.get() == 0;
    }

    /**
     * Increment the number of incoming pointers to this segment.
     *
     * @return {@code false} if the segment is logically removed, in which case the pointer counter is not updated.
     */
    boolean tryIncPointers() {
        int p;
        do {
            p = pointers_notProcessed_notInterrupted.get();
            if (p == 0) {
                return false;
            }
        } while (!pointers_notProcessed_notInterrupted.compareAndSet(p, p + (1 << POINTERS_SHIFT)));
        return true;
    }

    /**
     * Decrements the number of incoming pointers to this segment.
     *
     * @return {@code true} if the segment becomes logically removed.
     */
    boolean decPointers() {
        return pointers_notProcessed_notInterrupted.updateAndGet(p -> p - (1 << POINTERS_SHIFT)) == 0;
    }

    /**
     * Notify the segment that a `receive` has been interrupted in the cell.
     * Should be called at most once for each cell. Removes the segment, if it becomes logically removed.
     */
    void cellInterruptedReceiver() {
        if (pointers_notProcessed_notInterrupted.decrementAndGet() == 0) remove();
    }

    /**
     * Notify the segment that a `send` has been interrupted in the cell, or that the cell has been closed. Also marks
     * the cell as processed.
     * <p>
     * Should be called at most once for each cell. Removes the segment, if it becomes logically removed.
     */
    void cellInterruptedSender_orClosed() {
        if (countProcessed) {
            // decrementing both counters in a single operation
            if (pointers_notProcessed_notInterrupted.addAndGet(-(1 << PROCESSED_SHIFT) - 1) == 0) remove();
        } else {
            if (pointers_notProcessed_notInterrupted.decrementAndGet() == 0) remove();
        }
    }

    /**
     * Notify the segment that a cell has been processed by {@code Channel.expandBuffer}. Should not be called
     * if the cell has an interrupted sender.
     * <p>
     * Should be called at most once for each cell. Removes the segment, if it becomes logically removed.
     */
    void cellProcessed_notInterruptedSender() {
        if (pointers_notProcessed_notInterrupted.addAndGet(-(1 << PROCESSED_SHIFT)) == 0) remove();
    }

    /**
     * Physically remove the current segment, unless it's the tail segment. The segment should already be logically
     * removed.
     */
    void remove() {
        while (true) {
            // the tail segment cannot be removed
            if (isTail()) return;

            // find the closest non-removed segments
            var _prev = aliveSegmentLeft();
            var _next = aliveSegmentRight();

            // link next and prev
            _next.prev.updateAndGet(p -> p == null ? null : _prev);
            if (_prev != null) _prev.next.set(_next);

            // double-checking if _prev & _next are still not removed
            if (_next.isRemoved() && !_next.isTail()) continue;
            if (_prev != null && _prev.isRemoved()) continue;

            // the segment is now removed
            return;
        }
    }

    /**
     * Closes the segment chain - sets the {@code next} pointer of the last segment to {@code State.CLOSED}, and returns the last segment.
     */
    Segment close() {
        var s = this;
        while (true) {
            var n = s.next.get();
            if (n == null) { // this is the tail segment
                if (s.next.compareAndSet(null, State.CLOSED)) {
                    return s;
                }
                // else: try again
            } else if (n == State.CLOSED) {
                return s;
            } else {
                s = (Segment) n;
            }
        }
    }

    private Segment aliveSegmentLeft() {
        var s = prev.get();
        while (s != null && s.isRemoved()) {
            s = s.prev.get();
        }
        return s;
    }

    /**
     * Should only be called, if this is not the tail segment.
     */
    private Segment aliveSegmentRight() {
        var n = (Segment) next.get(); // this is not the tail, so there's a next segment
        while (n.isRemoved() && !n.isTail()) {
            n = (Segment) n.next.get(); // again, not tail
        }
        return n;
    }

    //

    /**
     * Finds or creates a non-removed segment with an id at least {@code id}, starting from {@code start}, and updates
     * the {@code ref} reference to it.
     *
     * @return The found segment, or {@code null} if the segment chain is closed.
     */
    static Segment findAndMoveForward(AtomicReference<Segment> ref, Segment start, long id, boolean countProcessed) {
        while (true) {
            var segment = findSegment(start, id, countProcessed);
            if (segment == null) {
                return null;
            }
            if (moveForward(ref, segment)) {
                return segment;
            }
        }
    }

    /**
     * Finds a non-removed segment with an id at least {@code id}, starting from {@code start}. New segments are created
     * if needed; this might prompt physical removal of the previously-tail segment.
     *
     * @return The found segment, or {@code null} if the segment chain is closed.
     */
    private static Segment findSegment(Segment start, long id, boolean countProcessed) {
        var current = start;
        while (current.getId() < id || current.isRemoved()) {
            var n = current.next.get();
            if (n == State.CLOSED) {
                // segment chain is closed, so we can't create a new segment
                return null;
            } else if (n == null) {
                // create a new segment if needed
                var newSegment = new Segment(current.getId() + 1, current, 0, countProcessed);
                if (current.setNextIfNull(newSegment)) {
                    if (current.isRemoved()) {
                        // the current segment was a tail segment, so if it was logically removed, we need to remove it physically
                        current.remove();
                    }
                }
                // else: try again with current
            } else {
                current = (Segment) n;
            }
        }
        return current;
    }

    /**
     * Attempts to move the {@code ref} reference forward to the specified {@code to} segment.
     * If successful, it decrements the pointers incoming to the current segment and removes it if necessary.
     * The pointers incoming to {@code to} are increased.
     *
     * @param to The segment to move the referenced segment to.
     * @return {@code true} if the move was successful, or a newer segment is already set, {@code false} otherwise.
     */
    private static boolean moveForward(AtomicReference<Segment> ref, Segment to) {
        while (true) {
            var current = ref.get();
            // the send segment might be already updated
            if (current.getId() >= to.getId()) {
                return true;
            }
            // trying to increase pointers incoming to `to`
            if (!to.tryIncPointers()) {
                return false;
            }
            // try to update the ref
            if (ref.compareAndSet(current, to)) {
                // decrement pointers incoming to `to`, as it's no longer referenced via ref
                if (current.decPointers()) {
                    current.remove();
                }
                return true;
            } else {
                // decrement pointers incoming to `to`, as it didn't end up being referenced by ref
                if (to.decPointers()) {
                    to.remove();
                }
                // and repeat
            }
        }
    }

    @Override
    public String toString() {
        var n = next.get();
        var p = prev.get();
        var c = pointers_notProcessed_notInterrupted.get();

        var notInterrupted = c & ((1 << PROCESSED_SHIFT) - 1);
        var notProcessed = (c & ((1 << POINTERS_SHIFT) - 1)) >> PROCESSED_SHIFT;
        var pointers = c >> POINTERS_SHIFT;

        return "Segment{" +
                "id=" + id +
                ", next=" + (n == null ? "null" : (n == State.CLOSED ? "closed" : ((Segment) n).id)) +
                ", prev=" + (p == null ? "null" : p.id) +
                ", pointers=" + pointers +
                ", notProcessed=" + notProcessed +
                ", notInterrupted=" + notInterrupted +
                '}';
    }

    // for tests

    void setNext(Segment newNext) {
        next.set(newNext);
    }
}