package com.softwaremill.jox;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

final class Segment {
    /*
    - in the first 6 bits, we store the number of cells that haven't been both interrupted & processed.
      In rendezvous/unlimited channels, cells are processed immediately when they become interrupted.
      In buffered channels:
      - interrupted send cells become processed by `expandBuffer`
      - interrupted receive cells become processed immediately when interrupted
    - in bits 7 & 8, we store the number of incoming pointers to this segment
     */

    static final int SEGMENT_SIZE = 32; // 2^5
    private static final int POINTERS_SHIFT = 6; // to store values between 0 and 32 (inclusive) we need 6 bits
    static final Segment NULL_SEGMENT = new Segment(-1, null, 0, false);

    /**
     * Used in {@code next} to indicate that the segment is closed.
     */
    private enum State {
        CLOSED
    }

    // immutable state

    private final long id;
    private final boolean isRendezvousOrUnlimited;

    // mutable state

    private final Object[] data = new Object[SEGMENT_SIZE];
    /**
     * Possible values: {@code Segment} or {@code State.CLOSED} (union type).
     */
    private volatile Object next = null;
    private volatile Segment prev;
    /**
     * A single counter that can be inspected & modified atomically, which includes:
     * - the number of incoming pointers (shifted by {@link Segment#POINTERS_SHIFT} to the left)
     * - the number of cells, which haven't been interrupted & processed yet (in the first 6 bits)
     * When this reaches 0, the segment is logically removed.
     */
    private volatile int pointers_notProcessedAndInterrupted;

    // var handles for mutable state

    private static final VarHandle DATA;
    private static final VarHandle NEXT;
    private static final VarHandle PREV;
    private static final VarHandle POINTERS_NOT_PROCESSED_AND_INTERRUPTED;

    static {
        try {
            MethodHandles.Lookup l = MethodHandles.privateLookupIn(Segment.class, MethodHandles.lookup());
            DATA = MethodHandles.arrayElementVarHandle(Object[].class);
            NEXT = l.findVarHandle(Segment.class, "next", Object.class);
            PREV = l.findVarHandle(Segment.class, "prev", Segment.class);
            POINTERS_NOT_PROCESSED_AND_INTERRUPTED = l.findVarHandle(Segment.class, "pointers_notProcessedAndInterrupted", int.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    //

    Segment(long id, Segment prev, int pointers, boolean isRendezvousOrUnlimited) {
        this.id = id;
        this.prev = prev;
        this.pointers_notProcessedAndInterrupted = SEGMENT_SIZE + (pointers << POINTERS_SHIFT);
        this.isRendezvousOrUnlimited = isRendezvousOrUnlimited;
    }

    long getId() {
        return id;
    }

    void cleanPrev() {
        prev = null;
    }

    Segment getNext() {
        var s = next;
        return s == State.CLOSED ? null : (Segment) s;
    }

    Segment getPrev() {
        return prev;
    }

    private boolean setNextIfNull(Segment setTo) {
        return NEXT.compareAndSet(this, null, setTo);
    }

    Object getCell(int index) {
        return DATA.getVolatile(data, index);
    }

    void setCell(int index, Object value) {
        DATA.setVolatile(data, index, value);
    }

    boolean casCell(int index, Object expected, Object newValue) {
        return DATA.compareAndSet(data, index, expected, newValue);
    }

    private boolean isTail() {
        return getNext() == null;
    }

    /**
     * @return {@code true} if this segment is logically removed, that is there are no incoming pointers and all cells
     * have been interrupted & processed.
     */
    boolean isRemoved() {
        return pointers_notProcessedAndInterrupted == 0;
    }

    /**
     * Increment the number of incoming pointers to this segment.
     *
     * @return {@code false} if the segment is logically removed, in which case the pointer counter is not updated.
     */
    boolean tryIncPointers() {
        int p;
        do {
            p = pointers_notProcessedAndInterrupted;
            if (p == 0) {
                return false;
            }
        } while (!POINTERS_NOT_PROCESSED_AND_INTERRUPTED.compareAndSet(this, p, p + (1 << POINTERS_SHIFT)));
        return true;
    }

    /**
     * Decrements the number of incoming pointers to this segment.
     *
     * @return {@code true} if the segment becomes logically removed.
     */
    boolean decPointers() {
        // pointers_notProcessedAndInterrupted.updateAndGet(p -> p - (1 << POINTERS_SHIFT)) == 0
        var toAdd = -(1 << POINTERS_SHIFT);
        while (true) {
            var currentP = pointers_notProcessedAndInterrupted;
            var updated = POINTERS_NOT_PROCESSED_AND_INTERRUPTED.compareAndSet(this, currentP, currentP + toAdd);
            if (updated) {
                return currentP + toAdd == 0; // is the new result 0?
            }
        }
    }

    /**
     * Notify the segment that a `receive` has been interrupted in the cell.
     * <p>
     * Should be called at most once for each cell. Removes the segment, if it becomes logically removed.
     */
    void cellInterruptedReceiver() {
        // decrementAndGet() == 0
        if ((int) POINTERS_NOT_PROCESSED_AND_INTERRUPTED.getAndAdd(this, -1) == 1) remove();
    }

    /**
     * Notify the segment that a `send` has been interrupted in the cell.
     * <p>
     * Should be called at most once for each cell. Removes the segment, if it becomes logically removed.
     */
    void cellInterruptedSender() {
        // in rendezvous/unlimited channels, cells are immediately processed when interrupted
        if (isRendezvousOrUnlimited) {
            // decrementAndGet() == 0
            if ((int) POINTERS_NOT_PROCESSED_AND_INTERRUPTED.getAndAdd(this, -1) == 1) remove();
        }
    }

    /**
     * Notify the segment that an interrupted sender cell has been processed by {@code Channel.expandBuffer}.
     * <p>
     * Should be called at most once for each cell. Removes the segment, if it becomes logically removed.
     */
    void cellProcessed() {
        // decrementAndGet() == 0
        if ((int) POINTERS_NOT_PROCESSED_AND_INTERRUPTED.getAndAdd(this, -1) == 1) remove();
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
            // _next.prev.update(p -> p == null ? null : _prev);
            var prevOfNextUpdated = false;
            do {
                var currentPrevOfNext = _next.prev;
                if (currentPrevOfNext == null) {
                    // leaving null
                    prevOfNextUpdated = true;
                } else {
                    prevOfNextUpdated = PREV.compareAndSet(_next, currentPrevOfNext, _prev);
                }

            } while (!prevOfNextUpdated);
            if (_prev != null) _prev.next = _next;

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
            var n = s.next;
            if (n == null) { // this is the tail segment
                if (NEXT.compareAndSet(s, null, State.CLOSED)) {
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
        var s = prev;
        while (s != null && s.isRemoved()) {
            s = s.prev;
        }
        return s;
    }

    /**
     * Should only be called, if this is not the tail segment.
     */
    private Segment aliveSegmentRight() {
        var n = (Segment) next; // this is not the tail, so there's a next segment
        while (n.isRemoved() && !n.isTail()) {
            n = (Segment) n.next; // again, not tail
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
    static Segment findAndMoveForward(VarHandle segmentVarHandle, Object segmentThis, Segment start, long id) {
        while (true) {
            var segment = findSegment(start, id);
            if (segment == null) {
                return null;
            }
            if (moveForward(segmentVarHandle, segmentThis, segment)) {
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
    private static Segment findSegment(Segment start, long id) {
        var current = start;
        while (current.getId() < id || current.isRemoved()) {
            var n = current.next;
            if (n == State.CLOSED) {
                // segment chain is closed, so we can't create a new segment
                return null;
            } else if (n == null) {
                // create a new segment if needed
                var newSegment = new Segment(current.getId() + 1, current, 0, start.isRendezvousOrUnlimited);
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
    private static boolean moveForward(VarHandle segmentVarHandle, Object segmentThis, Segment to) {
        while (true) {
            var current = (Segment) segmentVarHandle.getVolatile(segmentThis);
            // the send segment might be already updated
            if (current.getId() >= to.getId()) {
                return true;
            }
            // trying to increase pointers incoming to `to`
            if (!to.tryIncPointers()) {
                return false;
            }
            // try to update the ref
            if (segmentVarHandle.compareAndSet(segmentThis, current, to)) {
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
        var n = next;
        var p = prev;
        var c = pointers_notProcessedAndInterrupted;

        var notProcessedAndInterrupted = (c & ((1 << POINTERS_SHIFT) - 1));
        var pointers = c >> POINTERS_SHIFT;

        return "Segment{" +
                "id=" + id +
                ", next=" + (n == null ? "null" : (n == State.CLOSED ? "closed" : ((Segment) n).id)) +
                ", prev=" + (p == null ? "null" : p.id) +
                ", pointers=" + pointers +
                ", notProcessedAndInterrupted=" + notProcessedAndInterrupted +
                '}';
    }

    // for tests

    void setNext(Segment newNext) {
        NEXT.set(this, newNext);
    }
}
