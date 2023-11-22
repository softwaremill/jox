package jox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SegmentTest {
    Segment createSegmentChain(int count, long id) {
        var thisSegment = new Segment(id, null, 0);
        if (count <= 1) {
            return thisSegment;
        } else {
            var nextSegment = createSegmentChain(count - 1, id + 1);
            thisSegment.setNext(nextSegment);
            nextSegment.setPrev(thisSegment);
            return thisSegment;
        }
    }

    @Test
    void segmentShouldBecomeRemovedOnceAllCellsInterrupted() {
        // given
        var s = createSegmentChain(3, 0);
        var s1 = s;
        var s2 = s1.getNext();
        var s3 = s2.getNext();

        // when
        for (int i = 0; i < 31; i++) {
            s2.cellInterrupted();
            // nothing should happen
            assertFalse(s2.isRemoved());
            assertEquals(s2.getPrev(), s1);
            assertEquals(s2.getNext(), s3);
            assertEquals(s1.getPrev(), null);
            assertEquals(s1.getNext(), s2);
            assertEquals(s3.getPrev(), s2);
            assertEquals(s3.getNext(), null);
        }

        s2.cellInterrupted(); // last cell
        assertTrue(s2.isRemoved());

        // then
        assertEquals(s1.getPrev(), null);
        assertEquals(s1.getNext(), s3);
        assertEquals(s3.getPrev(), s1);
        assertEquals(s3.getNext(), null);
    }

    @Test
    void shouldRemoveMultipleSegments() {
        // given
        var s = createSegmentChain(5, 0);
        var s1 = s;
        var s2 = s1.getNext();
        var s3 = s2.getNext();
        var s4 = s3.getNext();
        var s5 = s4.getNext();

        // when
        // first, preventing automatic removal
        assertTrue(s2.tryIncPointers());
        assertTrue(s3.tryIncPointers());
        assertTrue(s4.tryIncPointers());
        // interrupting all cells
        for (int i = 0; i < 32; i++) {
            s2.cellInterrupted();
            assertFalse(s2.isRemoved());
            s3.cellInterrupted();
            assertFalse(s3.isRemoved());
            s4.cellInterrupted();
            assertFalse(s4.isRemoved());
        }
        // decreasing number of pointers, segments become logically removed
        assertTrue(s2.decPointers());
        assertTrue(s3.decPointers());
        assertTrue(s4.decPointers());
        // but the chaing is so far untouched
        assertEquals(s1.getNext(), s2);
        assertEquals(s2.getNext(), s3);
        assertEquals(s3.getNext(), s4);
        assertEquals(s4.getNext(), s5);
        // finally, calling remove which should clean up
        s3.remove();

        // then
        assertEquals(s1.getNext(), s5);
        assertEquals(s5.getPrev(), s1);
    }

    @Test
    void shouldNotIncrementIncomingPointersIfSegmentRemoved() {
        // given
        var s = createSegmentChain(1, 0);

        // when
        for (int i = 0; i < 32; i++) {
            s.cellInterrupted();
        }

        // then
        assertFalse(s.tryIncPointers());
    }

    @Test
    void shouldIncrementAndDecrementPointers() {
        // given
        var s = createSegmentChain(1, 0);

        // when
        assertTrue(s.tryIncPointers());
        assertFalse(s.decPointers());
    }

    @Test
    void shouldNotRemoveSegmentIfThereAreIncomingPointers() {
        // given
        var s = createSegmentChain(2, 0);
        var s1 = s;
        var s2 = s1.getNext();

        // when
        assertTrue(s.tryIncPointers());

        for (int i = 0; i < 32; i++) {
            s.cellInterrupted();
            assertFalse(s.isRemoved());
        }

        // decreasing the number of pointers
        assertTrue(s.decPointers());
        assertTrue(s.isRemoved());

        s.remove();
        assertEquals(s2.getPrev(), null);
        assertEquals(s2.getNext(), null);
    }
}
