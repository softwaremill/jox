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
    // rendezvous segment = where countProcessed is false

    @Test
    void rendezvousSegmentShouldBecomeRemovedOnceAllCellsInterrupted() {
        // given
        var ss = createSegmentChain(3, 0, false);

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

    @Test
    void shouldRemoveMultipleRendezvousSegments() {
        // given
        var ss = createSegmentChain(5, 0, false);

        // when
        // first, preventing automatic removal
        assertTrue(ss[1].tryIncPointers());
        assertTrue(ss[2].tryIncPointers());
        assertTrue(ss[3].tryIncPointers());
        // interrupting all cells
        for (int i = 0; i < SEGMENT_SIZE; i++) {
            ss[1].cellInterruptedSender();
            assertFalse(ss[1].isRemoved());
            ss[2].cellInterruptedSender();
            assertFalse(ss[2].isRemoved());
            ss[3].cellInterruptedSender();
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
    void shouldNotIncrementIncomingPointersIfRendezvousSegmentRemoved() {
        // given
        var ss = createSegmentChain(1, 0, false);

        // when
        interruptAllCells(ss[0]);

        // then
        assertFalse(ss[0].tryIncPointers());
    }

    @Test
    void shouldIncrementAndDecrementPointersInRendezvousSegment() {
        // given
        var ss = createSegmentChain(1, 0, false);

        // when
        assertTrue(ss[0].tryIncPointers());
        assertFalse(ss[0].decPointers());
    }

    @Test
    void shouldNotRemoveRendezvousSegmentIfThereAreIncomingPointers() {
        // given
        var ss = createSegmentChain(2, 0, false);

        // when
        assertTrue(ss[0].tryIncPointers());

        for (int i = 0; i < SEGMENT_SIZE; i++) {
            ss[0].cellInterruptedSender();
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
    void shouldRemoveRendezvousSegmentsWhenRunConcurrently() throws ExecutionException, InterruptedException {
        // given
        int segmentCount = 30;

        for (int k = 0; k < 1000; k++) {
            var ss = createSegmentChain(segmentCount, 0, false);

            // when
            scoped(scope -> {
                // first interrupting all cells but one in segments 2-(segmentCount-1))
                for (int i = 1; i < ss.length - 1; i++) {
                    for (int j = 0; j < SEGMENT_SIZE - 1; j++) {
                        ss[i].cellInterruptedSender();
                    }
                }

                // then, running (segmentCount-2) forks which will interrupt the last cell in each segment
                for (int i = 1; i < ss.length - 1; i++) {
                    int ii = i;
                    forkVoid(scope, () -> {
                        ss[ii].cellInterruptedSender();
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
    void shouldNotResurrectAnUnlikedRendezvousSegment() {
        // given
        var ss = createSegmentChain(3, 0, false);

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
    void shouldFindAndMoveRendezvousSegmentReferenceForward() {
        // given
        var s = createSegmentChain(4, 0, false);
        var r = new AtomicReference<>(s[0]);

        // when
        var result = Segment.findAndMoveForward(r, s[0], 2, false);

        // then
        assertEquals(s[2], result);

        // when
        result = Segment.findAndMoveForward(r, s[0], 5, false);
        assertEquals(5, result.getId());
        assertEquals(result, s[3].getNext().getNext());
    }

    @Test
    void shouldRemoveOldTailRendezvousSegment() {
        // given
        var ss = createSegmentChain(2, 0, false);

        // when
        interruptAllCells(ss[1]);

        // then
        assertTrue(ss[1].isRemoved());
        assertEquals(ss[0].getNext(), ss[1]); // logically, but not physically removed

        // when
        var s2 = Segment.findAndMoveForward(new AtomicReference<>(ss[0]), ss[0], 2, false);

        // then
        assertEquals(s2, ss[0].getNext());
        assertEquals(s2.getPrev(), ss[0]);
    }

    @Test
    void shouldReturnNextRendezvousSegmentIfRemoved() {
        // given
        var ss = createSegmentChain(3, 0, false);
        interruptAllCells(ss[1]);

        // when
        var result = Segment.findAndMoveForward(new AtomicReference<>(ss[0]), ss[0], 1, false);

        // then
        assertEquals(ss[2], result);
    }

    @Test
    void shouldNotUpdateRendezvousSegmentReferenceIfAlreadyUpdated() {
        // given
        var ss = createSegmentChain(3, 0, false);
        var ref = new AtomicReference<>(ss[2]);

        // when
        var result = Segment.findAndMoveForward(ref, ss[0], 1, false);

        // then
        assertEquals(ss[1], result);
        assertEquals(ss[2], ref.get());
    }

    @Test
    void shouldUpdateRendezvousSegmentPointersWhenReferenceChanges() {
        // given
        var ss = createSegmentChain(3, 0, false);
        var ref = new AtomicReference<>(ss[0]);

        // when
        Segment.findAndMoveForward(ref, ss[0], 1, false);

        // then
        interruptAllCells(ss[1]);
        assertFalse(ss[1].isRemoved()); // shouldn't be removed because there's an incoming pointer

        // when
        Segment.findAndMoveForward(ref, ss[0], 2, false);

        // then
        assertTrue(ss[1].isRemoved()); // no more pointers -> logically removed
        interruptAllCells(ss[2]);
        assertFalse(ss[2].isRemoved()); // shouldn't be removed because there's an incoming pointer
    }

    @Test
    void shouldConcurrentlyMoveRendezvousSegmentsForward() throws ExecutionException, InterruptedException {
        // given
        for (int k = 0; k < 1000; k++) {
            var ss = createSegmentChain(1, 0, false);
            var ref = new AtomicReference<>(ss[0]);
            var observedSegments = new ConcurrentHashMap<Integer, Segment>();

            // when
            scoped(scope -> {
                // creating 10 forks, each moving the reference forward 300 times, to subsequent ids
                for (int f = 0; f < 10; f++) {
                    forkVoid(scope, () -> {
                        var s = ss[0];
                        for (int i = 0; i < 300; i++) {
                            s = Segment.findAndMoveForward(ref, s, i, false);
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

    private Segment[] createSegmentChain(int count, long id, boolean countProcessed) {
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

    private void interruptAllCells(Segment s) {
        for (int i = 0; i < SEGMENT_SIZE; i++) {
            s.cellInterruptedSender();
        }
    }
}
