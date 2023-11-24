package jox;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

final class Segment {
    static final int SEGMENT_SIZE = 32;
    private static final int POINTERS_SHIFT = 16;
    static final Segment NULL_SEGMENT = new Segment(-1, null, 0);

    private final long id;
    private final AtomicReferenceArray<Object> data = new AtomicReferenceArray<>(SEGMENT_SIZE);
    private final AtomicReference<Segment> next = new AtomicReference<>(null);
    private final AtomicReference<Segment> prev;
    /**
     * Count of cells that haven't been yet interrupted (initialised to the segment size), plus the number of incoming
     * pointers (shifted by 16 bits to the left). That way, we can inspect & modify both values atomically.
     */
    private final AtomicInteger pointersPlusNotInterrupted;

    Segment(long id, Segment prev, int pointers) {
        this.id = id;
        this.prev = new AtomicReference<>(prev);
        this.pointersPlusNotInterrupted = new AtomicInteger(SEGMENT_SIZE + (pointers << POINTERS_SHIFT));
    }

    long getId() {
        return id;
    }

    Segment getPrev() {
        return prev.get();
    }

    void setPrev(Segment newPrev) {
        prev.set(newPrev);
    }

    void cleanPrev() {
        prev.set(null);
    }

    Segment getNext() {
        return next.get();
    }

    boolean casNext(Segment expected, Segment setTo) {
        return next.compareAndSet(expected, setTo);
    }

    void setNext(Segment newNext) {
        next.set(newNext);
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
        return next.get() == null;
    }

    /**
     * @return {@code true} if this segment is logically removed, that is there are not incoming pointers, and all cells
     * have been interrupted.
     */
    boolean isRemoved() {
        return pointersPlusNotInterrupted.get() == 0;
    }

    /**
     * Increment the number of incoming pointers to this segment.
     *
     * @return {@code false} if the segment is logically removed, in which case the pointer counter is not updated.
     */
    boolean tryIncPointers() {
        int p;
        do {
            p = pointersPlusNotInterrupted.get();
            if (p == 0) {
                return false;
            }
        } while (!pointersPlusNotInterrupted.compareAndSet(p, p + (1 << POINTERS_SHIFT)));
        return true;
    }

    /**
     * Decrements the number of incoming pointers to this segment.
     *
     * @return {@code true} if the segment becomes logically removed.
     */
    boolean decPointers() {
        return pointersPlusNotInterrupted.updateAndGet(p -> p - (1 << POINTERS_SHIFT)) == 0;
    }

    /**
     * Notify the segment that a cell has been interrupted. Should be called at most once for each cell. Removes the
     * segment, if all cells have been interrupted.
     */
    void cellInterrupted() {
        if (pointersPlusNotInterrupted.decrementAndGet() == 0) remove();
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
            if (_prev != null) _prev.setNext(_next);

            // double-checking if _prev & _next are still not removed
            if (_next.isRemoved() && !_next.isTail()) continue;
            if (_prev != null && _prev.isRemoved()) continue;

            // the segment is now removed
            return;
        }
    }

    private Segment aliveSegmentLeft() {
        var s = prev.get();
        while (s != null && s.isRemoved()) {
            s = s.prev.get();
        }
        return s;
    }

    private Segment aliveSegmentRight() {
        var s = next.get();
        while (s.isRemoved() && !s.isTail()) {
            s = s.next.get();
        }
        return s;
    }

    //

    /**
     * Finds or creates a non-removed segment with an id at least {@code id}, starting from {@code start}, and updates
     * the {@code ref} reference to it.
     */
    static Segment findAndMoveForward(AtomicReference<Segment> ref, Segment start, long id) {
        while (true) {
            var segment = findSegment(start, id);
            if (moveForward(ref, segment)) {
                return segment;
            }
        }
    }

    /**
     * Finds a non-removed segment with an id at least {@code id}, starting from {@code start}. New segments are created
     * if needed; this might prompt physical removal of the previously-tail segment.
     */
    private static Segment findSegment(Segment start, long id) {
        var current = start;
        while (current.getId() < id || current.isRemoved()) {
            // create a new segment if needed
            if (current.getNext() == null) {
                var newSegment = new Segment(current.getId() + 1, current, 0);
                if (current.casNext(null, newSegment)) {
                    if (current.isRemoved()) {
                        // the current segment was a tail segment, so if it was logically removed, we need to remove it physically
                        newSegment.remove();
                    }
                }
            }
            current = current.getNext();
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
            // try to update the send segment
            if (ref.compareAndSet(current, to)) {
                // decrement pointers incoming to `to`, as it's no longer referenced via sendSegment
                if (current.decPointers()) {
                    current.remove();
                }
                return true;
            } else {
                // decrement pointers incoming to `to`, as it didn't end up being referenced by sendSegment
                if (to.decPointers()) {
                    to.remove();
                }
                // and repeat
            }
        }
    }
}
