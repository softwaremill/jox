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
    Segment[] createSegmentChain(int count, long id) {
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
        var ss = createSegmentChain(3, 0);

        // when
        for (int i = 0; i < SEGMENT_SIZE - 1; i++) {
            ss[1].cellInterrupted();
            // nothing should happen
            assertFalse(ss[1].isRemoved());
            assertEquals(ss[1].getPrev(), ss[0]);
            assertEquals(ss[1].getNext(), ss[2]);
            assertEquals(ss[0].getPrev(), null);
            assertEquals(ss[0].getNext(), ss[1]);
            assertEquals(ss[2].getPrev(), ss[1]);
            assertEquals(ss[2].getNext(), null);
        }

        ss[1].cellInterrupted(); // last cell
        assertTrue(ss[1].isRemoved());

        // then
        assertEquals(ss[0].getPrev(), null);
        assertEquals(ss[0].getNext(), ss[2]);
        assertEquals(ss[2].getPrev(), ss[0]);
        assertEquals(ss[2].getNext(), null);
    }

    @Test
    void shouldRemoveMultipleSegments() {
        // given
        var ss = createSegmentChain(5, 0);

        // when
        // first, preventing automatic removal
        assertTrue(ss[1].tryIncPointers());
        assertTrue(ss[2].tryIncPointers());
        assertTrue(ss[3].tryIncPointers());
        // interrupting all cells
        for (int i = 0; i < SEGMENT_SIZE; i++) {
            ss[1].cellInterrupted();
            assertFalse(ss[1].isRemoved());
            ss[2].cellInterrupted();
            assertFalse(ss[2].isRemoved());
            ss[3].cellInterrupted();
            assertFalse(ss[3].isRemoved());
        }
        // decreasing number of pointers, segments become logically removed
        assertTrue(ss[1].decPointers());
        assertTrue(ss[2].decPointers());
        assertTrue(ss[3].decPointers());
        // but the chain is so far untouched
        assertEquals(ss[0].getNext(), ss[1]);
        assertEquals(ss[1].getNext(), ss[2]);
        assertEquals(ss[2].getNext(), ss[3]);
        assertEquals(ss[3].getNext(), ss[4]);
        // finally, calling remove which should clean up
        ss[2].remove();

        // then
        assertEquals(ss[0].getNext(), ss[4]);
        assertEquals(ss[4].getPrev(), ss[0]);
    }

    @Test
    void shouldNotIncrementIncomingPointersIfSegmentRemoved() {
        // given
        var ss = createSegmentChain(1, 0);

        // when
        interruptAllCells(ss[0]);

        // then
        assertFalse(ss[0].tryIncPointers());
    }

    @Test
    void shouldIncrementAndDecrementPointers() {
        // given
        var ss = createSegmentChain(1, 0);

        // when
        assertTrue(ss[0].tryIncPointers());
        assertFalse(ss[0].decPointers());
    }

    @Test
    void shouldNotRemoveSegmentIfThereAreIncomingPointers() {
        // given
        var ss = createSegmentChain(2, 0);

        // when
        assertTrue(ss[0].tryIncPointers());

        for (int i = 0; i < SEGMENT_SIZE; i++) {
            ss[0].cellInterrupted();
            assertFalse(ss[0].isRemoved());
        }

        // decreasing the number of pointers
        assertTrue(ss[0].decPointers());
        assertTrue(ss[0].isRemoved());

        ss[0].remove();
        assertEquals(ss[1].getPrev(), null);
        assertEquals(ss[1].getNext(), null);
    }

    @Test
    void shouldRemoveSegmentsWhenRunConcurrently() throws ExecutionException, InterruptedException {
        // given
        int segmentCount = 30;

        for (int k = 0; k < 1000; k++) {
            var ss = createSegmentChain(segmentCount, 0);

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
        var ss = createSegmentChain(3, 0);

        // when
        // unlinking segment 2 from segment 3
        ss[2].cleanPrev();

        // removing segment 2
        interruptAllCells(ss[1]);

        // then
        assertTrue(ss[1].isRemoved());
        assertEquals(ss[0].getNext(), ss[2]);
        assertEquals(ss[2].getPrev(), null);
    }

    @Test
    void shouldFindAndMoveReferenceForward() {
        // given
        var s = createSegmentChain(4, 0);
        var r = new AtomicReference<>(s[0]);

        // when
        var result = Segment.findAndMoveForward(r, s[0], 2);

        // then
        assertEquals(s[2], result);

        // when
        result = Segment.findAndMoveForward(r, s[0], 5);
        assertEquals(5, result.getId());
        assertEquals(result, s[3].getNext().getNext());
    }

    @Test
    void shouldRemoveOldTailSegment() {
        // given
        var ss = createSegmentChain(2, 0);

        // when
        interruptAllCells(ss[1]);

        // then
        assertTrue(ss[1].isRemoved());
        assertEquals(ss[0].getNext(), ss[1]); // logically, but not physically removed

        // when
        var s2 = Segment.findAndMoveForward(new AtomicReference<>(ss[0]), ss[0], 2);

        // then
        assertEquals(s2, ss[0].getNext());
        assertEquals(s2.getPrev(), ss[0]);
    }

    @Test
    void shouldReturnNextSegmentIfRemoved() {
        // given
        var ss = createSegmentChain(3, 0);
        interruptAllCells(ss[1]);

        // when
        var result = Segment.findAndMoveForward(new AtomicReference<>(ss[0]), ss[0], 1);

        // then
        assertEquals(ss[2], result);
    }

    @Test
    void shouldNotUpdateIfAlreadyUpdated() {
        // given
        var ss = createSegmentChain(3, 0);
        var ref = new AtomicReference<>(ss[2]);

        // when
        var result = Segment.findAndMoveForward(ref, ss[0], 1);

        // then
        assertEquals(ss[1], result);
        assertEquals(ss[2], ref.get());
    }

    @Test
    void shouldUpdatePointersWhenReferenceChanges() {
        // given
        var ss = createSegmentChain(3, 0);
        var ref = new AtomicReference<>(ss[0]);

        // when
        Segment.findAndMoveForward(ref, ss[0], 1);

        // then
        interruptAllCells(ss[1]);
        assertFalse(ss[1].isRemoved()); // shouldn't be removed because there's an incoming pointer

        // when
        Segment.findAndMoveForward(ref, ss[0], 2);

        // then
        assertTrue(ss[1].isRemoved()); // no more pointers -> logically removed
        interruptAllCells(ss[2]);
        assertFalse(ss[2].isRemoved()); // shouldn't be removed because there's an incoming pointer
    }

    @Test
    void shouldConcurrentlyMoveSegmentsForward() throws ExecutionException, InterruptedException {
        // given
        for (int k = 0; k < 1000; k++) {
            var ss = createSegmentChain(1, 0);
            var ref = new AtomicReference<>(ss[0]);
            var observedSegments = new ConcurrentHashMap<Integer, Segment>();

            // when
            scoped(scope -> {
                // creating 10 forks, each moving the reference forward 300 times, to subsequent ids
                for (int f = 0; f < 10; f++) {
                    forkVoid(scope, () -> {
                        var s = ss[0];
                        for (int i = 0; i < 300; i++) {
                            s = Segment.findAndMoveForward(ref, s, i);
                            var previous = observedSegments.put(i, s);
                            if (previous != s && previous != null) {
                                fail("Already observed segment: " + previous + " for id: " + i + ", but found: " + s);
                            }
                        }
                    });
                }
            });
        }
    }

    private void interruptAllCells(Segment s) {
        for (int i = 0; i < SEGMENT_SIZE; i++) {
            s.cellInterrupted();
        }
    }
}
