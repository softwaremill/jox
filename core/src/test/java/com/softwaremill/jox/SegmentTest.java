package com.softwaremill.jox;

import org.junit.jupiter.api.Test;

import static com.softwaremill.jox.Segment.SEGMENT_SIZE;
import static org.junit.jupiter.api.Assertions.*;

public class SegmentTest {
    @Test
    void segmentShouldBecomeRemovedOnceAllCellsInterruptedAndProcessed() {
        // given
        var ss = createSegmentChain(3, 0, false);

        // when
        // receiver-interrupting all cells
        for (int i = 0; i < SEGMENT_SIZE; i++) {
            ss[1].cellInterruptedReceiver();
            // nothing should happen
            assertFalse(ss[1].isRemoved());
            assertEquals(ss[1].getPrev(), ss[0]);
            assertEquals(ss[1].getNext(), ss[2]);
            assertNull(ss[0].getPrev());
            assertEquals(ss[0].getNext(), ss[1]);
            assertEquals(ss[2].getPrev(), ss[1]);
            assertNull(ss[2].getNext());
        }

        // processing all cells but one
        for (int i = 0; i < SEGMENT_SIZE - 1; i++) {
            ss[1].cellProcessed_notInterruptedSender();
            // nothing should happen
            assertFalse(ss[1].isRemoved());
            assertEquals(ss[1].getPrev(), ss[0]);
            assertEquals(ss[1].getNext(), ss[2]);
            assertNull(ss[0].getPrev());
            assertEquals(ss[0].getNext(), ss[1]);
            assertEquals(ss[2].getPrev(), ss[1]);
            assertNull(ss[2].getNext());
        }

        ss[1].cellProcessed_notInterruptedSender(); // last cell
        assertTrue(ss[1].isRemoved());

        // then
        assertNull(ss[0].getPrev());
        assertEquals(ss[0].getNext(), ss[2]);
        assertEquals(ss[2].getPrev(), ss[0]);
        assertNull(ss[2].getNext());
    }

    @Test
    void segmentShouldBecomeRemovedOnceAllCellsSenderInterrupted() {
        // given
        var ss = createSegmentChain(3, 0, false);

        // when
        for (int i = 0; i < SEGMENT_SIZE - 1; i++) {
            ss[1].cellInterruptedSender();
            // nothing should happen
            assertFalse(ss[1].isRemoved());
            assertEquals(ss[1].getPrev(), ss[0]);
            assertEquals(ss[1].getNext(), ss[2]);
            assertNull(ss[0].getPrev());
            assertEquals(ss[0].getNext(), ss[1]);
            assertEquals(ss[2].getPrev(), ss[1]);
            assertNull(ss[2].getNext());
        }

        ss[1].cellInterruptedSender(); // last cell
        assertTrue(ss[1].isRemoved());

        // then
        assertNull(ss[0].getPrev());
        assertEquals(ss[0].getNext(), ss[2]);
        assertEquals(ss[2].getPrev(), ss[0]);
        assertNull(ss[2].getNext());
    }

    @Test
    void shouldReturnTheLastSegmentWhenClosing() {
        // given
        var ss = createSegmentChain(3, 0, false);

        // when
        var s = ss[0].close();

        // then
        assertEquals(ss[2].getId(), s.getId());
    }

    static Segment[] createSegmentChain(int count, long id, boolean isRendezvous) {
        var segments = new Segment[count];
        var thisSegment = new Segment(id, null, 0, isRendezvous);
        segments[0] = thisSegment;
        for (int i = 1; i < count; i++) {
            var nextSegment = new Segment(id + i, thisSegment, 0, isRendezvous);
            thisSegment.setNext(nextSegment);
            segments[i] = nextSegment;
            thisSegment = nextSegment;
        }
        return segments;
    }

    static void sendInterruptAllCells(Segment s) {
        for (int i = 0; i < SEGMENT_SIZE; i++) {
            s.cellInterruptedSender();
        }
    }
}
