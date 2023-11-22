package jox;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

final class Segment {
    private static final int SEGMENT_SIZE = 32;
    private static final int POINTERS_SHIFT = 16;

    private final long id;
    private final AtomicReferenceArray<Object> data = new AtomicReferenceArray<>(SEGMENT_SIZE);
    private final AtomicReference<Segment> next = new AtomicReference<>(null);
    private final AtomicReference<Segment> prev;
    // count of cells that haven't been yet interrupted (initialised to the segment size), plus the number of
    // incoming pointers (shifted by 16 bits to the left).
    private final AtomicInteger pointersPlusNotInterrupted;

    Segment(long id, Segment prev, int pointers) {
        this.id = id;
        this.prev = new AtomicReference<>(prev);
        this.pointersPlusNotInterrupted = new AtomicInteger(SEGMENT_SIZE + (pointers << POINTERS_SHIFT));
    }

    Segment getPrev() {
        return prev.get();
    }

    void setPrev(Segment newPrev) {
        prev.set(newPrev);
    }

    Segment getNext() {
        return next.get();
    }

    void setNext(Segment newNext) {
        next.set(newNext);
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
            _next.setPrev(_prev);
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
}
