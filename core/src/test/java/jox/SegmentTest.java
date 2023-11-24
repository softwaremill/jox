package jox;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static jox.Segment.SEGMENT_SIZE;
import static jox.TestUtil.forkVoid;
import static jox.TestUtil.scoped;
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

    Segment[] createSegmentChainAsArray(int count, long id) {
        var segments = new Segment[count];
        var thisSegment = new Segment(id, null, 0);
        segments[0] = thisSegment;
        for (int i = 1; i < count; i++) {
            var nextSegment = new Segment(id + i, thisSegment, 0);
            thisSegment.setNext(nextSegment);
            segments[i] = nextSegment;
            thisSegment = nextSegment;
        }
        return segments;
    }

    @Test
    void segmentShouldBecomeRemovedOnceAllCellsInterrupted() {
        // given
        var s = createSegmentChain(3, 0);
        var s1 = s;
        var s2 = s1.getNext();
        var s3 = s2.getNext();

        // when
        for (int i = 0; i < SEGMENT_SIZE - 1; i++) {
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
        for (int i = 0; i < SEGMENT_SIZE; i++) {
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
        for (int i = 0; i < SEGMENT_SIZE; i++) {
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

        for (int i = 0; i < SEGMENT_SIZE; i++) {
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

    @Test
    void shouldRemoveSegmentsWhenRunConcurrently() throws ExecutionException, InterruptedException {
        // given
        int segmentCount = 30;

        for (int k = 0; k < 1000; k++) {
            var ss = createSegmentChainAsArray(segmentCount, 0);

            // when
            scoped(scope -> {
                // first interrupting all cells but one in segments 2-(segmentCount-1))
                for (int i = 1; i < ss.length - 1; i++) {
                    for (int j = 0; j < SEGMENT_SIZE - 1; j++) {
                        ss[i].cellInterrupted();
                    }
                }

                // then, running (segmentCount-2) forks which will interrupt the last cell in each segment
                for (int i = 1; i < ss.length - 1; i++) {
                    int ii = i;
                    forkVoid(scope, () -> {
                        ss[ii].cellInterrupted();
                    });
                }
            });

            // then, the segments should be removed
            for (int i = 1; i < ss.length - 1; i++) {
                assertTrue(ss[i].isRemoved());
            }

            assertEquals(ss[0].getPrev(), null);
            assertEquals(ss[0].getNext(), ss[segmentCount - 1]);
            assertEquals(ss[segmentCount - 1].getPrev(), ss[0]);
            assertEquals(ss[segmentCount - 1].getNext(), null);
        }
    }

    @Test
    void shouldNotResurrectAnUnlikedSegment() {
        // given
        var ss = createSegmentChainAsArray(3, 0);

        // when
        // unlinking segment 2 from segment 3
        ss[2].cleanPrev();

        // removing segment 2
        for (int i = 0; i < SEGMENT_SIZE; i++) {
            ss[1].cellInterrupted();
        }

        // then
        assertTrue(ss[1].isRemoved());
        assertEquals(ss[0].getNext(), ss[2]);
        assertEquals(ss[2].getPrev(), null);
    }
}
