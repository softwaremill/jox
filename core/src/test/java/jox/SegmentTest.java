package jox;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static jox.Segment.SEGMENT_SIZE;
import static jox.TestUtil.forkVoid;
import static jox.TestUtil.scoped;
import static org.junit.jupiter.api.Assertions.*;

public class SegmentTest {
    @Test
    void segmentShouldBecomeRemovedOnceAllCellsInterruptedAndProcessed() {
        // given
        var ss = createSegmentChain(3, 0, true);

        // when
        // receiver-interrupting all cells
        for (int i = 0; i < SEGMENT_SIZE; i++) {
            ss[1].cellInterruptedReceiver();
            // nothing should happen
            assertFalse(ss[1].isRemoved());
            assertEquals(ss[1].getPrev(), ss[0]);
            assertEquals(ss[1].getNext(), ss[2]);
            assertEquals(ss[0].getPrev(), null);
            assertEquals(ss[0].getNext(), ss[1]);
            assertEquals(ss[2].getPrev(), ss[1]);
            assertEquals(ss[2].getNext(), null);
        }

        // processing all cells but one
        for (int i = 0; i < SEGMENT_SIZE - 1; i++) {
            ss[1].cellProcessed_notInterruptedSender();
            // nothing should happen
            assertFalse(ss[1].isRemoved());
            assertEquals(ss[1].getPrev(), ss[0]);
            assertEquals(ss[1].getNext(), ss[2]);
            assertEquals(ss[0].getPrev(), null);
            assertEquals(ss[0].getNext(), ss[1]);
            assertEquals(ss[2].getPrev(), ss[1]);
            assertEquals(ss[2].getNext(), null);
        }

        ss[1].cellProcessed_notInterruptedSender(); // last cell
        assertTrue(ss[1].isRemoved());

        // then
        assertEquals(ss[0].getPrev(), null);
        assertEquals(ss[0].getNext(), ss[2]);
        assertEquals(ss[2].getPrev(), ss[0]);
        assertEquals(ss[2].getNext(), null);
    }

    @Test
    void segmentShouldBecomeRemovedOnceAllCellsSendInterrupted() {
        // given
        var ss = createSegmentChain(3, 0, true);

        // when
        for (int i = 0; i < SEGMENT_SIZE - 1; i++) {
            ss[1].cellInterruptedSender();
            // nothing should happen
            assertFalse(ss[1].isRemoved());
            assertEquals(ss[1].getPrev(), ss[0]);
            assertEquals(ss[1].getNext(), ss[2]);
            assertEquals(ss[0].getPrev(), null);
            assertEquals(ss[0].getNext(), ss[1]);
            assertEquals(ss[2].getPrev(), ss[1]);
            assertEquals(ss[2].getNext(), null);
        }

        ss[1].cellInterruptedSender(); // last cell
        assertTrue(ss[1].isRemoved());

        // then
        assertEquals(ss[0].getPrev(), null);
        assertEquals(ss[0].getNext(), ss[2]);
        assertEquals(ss[2].getPrev(), ss[0]);
        assertEquals(ss[2].getNext(), null);
    }

    static Segment[] createSegmentChain(int count, long id, boolean countProcessed) {
        var segments = new Segment[count];
        var thisSegment = new Segment(id, null, 0, countProcessed);
        segments[0] = thisSegment;
        for (int i = 1; i < count; i++) {
            var nextSegment = new Segment(id + i, thisSegment, 0, countProcessed);
            thisSegment.setNext(nextSegment);
            segments[i] = nextSegment;
            thisSegment = nextSegment;
        }
        return segments;
    }

    static void interruptAllCells(Segment s) {
        for (int i = 0; i < SEGMENT_SIZE; i++) {
            s.cellInterruptedSender();
        }
    }
}
