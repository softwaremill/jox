package com.softwaremill.jox;

import static com.softwaremill.jox.Segment.SEGMENT_SIZE;
import static com.softwaremill.jox.SegmentTest.createSegmentChain;
import static com.softwaremill.jox.SegmentTest.sendInterruptAllCells;
import static com.softwaremill.jox.TestUtil.forkVoid;
import static com.softwaremill.jox.TestUtil.scoped;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

public class SegmentRendezvousTest {
    // a field which we use for testing where VarHandle is required
    private Segment someSegment;
    private static final VarHandle SOME_SEGMENT;

    static {
        try {
            MethodHandles.Lookup l =
                    MethodHandles.privateLookupIn(
                            SegmentRendezvousTest.class, MethodHandles.lookup());
            SOME_SEGMENT =
                    l.findVarHandle(SegmentRendezvousTest.class, "someSegment", Segment.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Test
    void segmentShouldBecomeRemovedOnceAllCellsInterrupted() {
        // given
        var ss = createSegmentChain(3, 0, true);

        // when
        for (int i = 0; i < SEGMENT_SIZE - 1; i++) {
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

        ss[1].cellInterruptedReceiver(); // last cell
        assertTrue(ss[1].isRemoved());

        // then
        assertNull(ss[0].getPrev());
        assertEquals(ss[0].getNext(), ss[2]);
        assertEquals(ss[2].getPrev(), ss[0]);
        assertNull(ss[2].getNext());
    }

    @Test
    void shouldRemoveMultipleSegments() {
        // given
        var ss = createSegmentChain(5, 0, true);

        // when
        // first, preventing automatic removal
        assertTrue(ss[1].tryIncPointers());
        assertTrue(ss[2].tryIncPointers());
        assertTrue(ss[3].tryIncPointers());
        // interrupting all cells
        for (int i = 0; i < SEGMENT_SIZE; i++) {
            ss[1].cellInterruptedReceiver();
            assertFalse(ss[1].isRemoved());
            ss[2].cellInterruptedReceiver();
            assertFalse(ss[2].isRemoved());
            ss[3].cellInterruptedReceiver();
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
        var ss = createSegmentChain(1, 0, true);

        // when
        sendInterruptAllCells(ss[0]);

        // then
        assertFalse(ss[0].tryIncPointers());
    }

    @Test
    void shouldIncrementAndDecrementPointersInSegment() {
        // given
        var ss = createSegmentChain(1, 0, true);

        // when
        assertTrue(ss[0].tryIncPointers());
        assertFalse(ss[0].decPointers());
    }

    @Test
    void shouldNotRemoveSegmentIfThereAreIncomingPointers() {
        // given
        var ss = createSegmentChain(2, 0, true);

        // when
        assertTrue(ss[0].tryIncPointers());

        for (int i = 0; i < SEGMENT_SIZE; i++) {
            ss[0].cellInterruptedReceiver();
            assertFalse(ss[0].isRemoved());
        }

        // decreasing the number of pointers
        assertTrue(ss[0].decPointers());
        assertTrue(ss[0].isRemoved());

        ss[0].remove();
        assertNull(ss[1].getPrev());
        assertNull(ss[1].getNext());
    }

    @Test
    void shouldRemoveSegmentsWhenRunConcurrently() throws ExecutionException, InterruptedException {
        // given
        int segmentCount = 30;

        for (int k = 0; k < 1000; k++) {
            var ss = createSegmentChain(segmentCount, 0, true);

            // when
            scoped(
                    scope -> {
                        // first interrupting all cells but one in segments 2-(segmentCount-1))
                        for (int i = 1; i < ss.length - 1; i++) {
                            for (int j = 0; j < SEGMENT_SIZE - 1; j++) {
                                ss[i].cellInterruptedReceiver();
                            }
                        }

                        // then, running (segmentCount-2) forks which will interrupt the last cell
                        // in each segment
                        for (int i = 1; i < ss.length - 1; i++) {
                            int ii = i;
                            forkVoid(
                                    scope,
                                    () -> {
                                        ss[ii].cellInterruptedReceiver();
                                    });
                        }
                    });

            // then, the segments should be removed
            for (int i = 1; i < ss.length - 1; i++) {
                assertTrue(ss[i].isRemoved());
            }

            assertNull(ss[0].getPrev());
            assertEquals(ss[0].getNext(), ss[segmentCount - 1]);
            assertEquals(ss[segmentCount - 1].getPrev(), ss[0]);
            assertNull(ss[segmentCount - 1].getNext());
        }
    }

    @Test
    void shouldNotResurrectAnUnlikedSegment() {
        // given
        var ss = createSegmentChain(3, 0, true);

        // when
        // unlinking segment 2 from segment 3
        ss[2].cleanPrev();

        // removing segment 2
        sendInterruptAllCells(ss[1]);

        // then
        assertTrue(ss[1].isRemoved());
        assertEquals(ss[0].getNext(), ss[2]);
        assertEquals(ss[2].getPrev(), null);
    }

    @Test
    void shouldFindAndMoveSegmentReferenceForward() {
        // given
        var s = createSegmentChain(4, 0, true);
        someSegment = s[0];

        // when
        var result = Segment.findAndMoveForward(SOME_SEGMENT, this, s[0], 2);

        // then
        assertEquals(s[2], result);

        // when
        result = Segment.findAndMoveForward(SOME_SEGMENT, this, s[0], 5);
        assertEquals(5, result.getId());
        assertEquals(result, s[3].getNext().getNext());
    }

    @Test
    void shouldMoveReferenceForwardIfClosedAndFoundSegmentExists() {
        // given
        var s = createSegmentChain(4, 0, true);
        someSegment = s[0];

        // when
        s[0].close();
        var result = Segment.findAndMoveForward(SOME_SEGMENT, this, s[0], 3);

        // then
        assertEquals(s[3], result);
        assertEquals(s[3], someSegment);
    }

    @Test
    void shouldNotMoveReferenceForwardIfClosedAndFoundSegmentDoesNotExist() {
        // given
        var s = createSegmentChain(4, 0, true);
        someSegment = s[0];

        // when
        s[0].close();
        var result = Segment.findAndMoveForward(SOME_SEGMENT, this, s[0], 5);

        // then
        assertNull(result);
        assertEquals(s[0], someSegment);
    }

    @Test
    void shouldRemoveOldTailSegment() {
        // given
        var ss = createSegmentChain(2, 0, true);

        // when
        sendInterruptAllCells(ss[1]);

        // then
        assertTrue(ss[1].isRemoved());
        assertEquals(ss[0].getNext(), ss[1]); // logically, but not physically removed

        // when
        someSegment = ss[0];
        var s2 = Segment.findAndMoveForward(SOME_SEGMENT, this, ss[0], 2);

        // then
        assertEquals(s2, ss[0].getNext());
        assertEquals(s2.getPrev(), ss[0]);
    }

    @Test
    void shouldReturnNextSegmentIfRemoved() {
        // given
        var ss = createSegmentChain(3, 0, true);
        sendInterruptAllCells(ss[1]);

        // when
        someSegment = ss[0];
        var result = Segment.findAndMoveForward(SOME_SEGMENT, this, ss[0], 1);

        // then
        assertEquals(ss[2], result);
    }

    @Test
    void shouldNotUpdateSegmentReferenceIfAlreadyUpdated() {
        // given
        var ss = createSegmentChain(3, 0, true);
        someSegment = ss[2];

        // when
        var result = Segment.findAndMoveForward(SOME_SEGMENT, this, ss[0], 1);

        // then
        assertEquals(ss[1], result);
        assertEquals(ss[2], someSegment);
    }

    @Test
    void shouldUpdateSegmentPointersWhenReferenceChanges() {
        // given
        var ss = createSegmentChain(3, 0, true);
        someSegment = ss[0];

        // when
        Segment.findAndMoveForward(SOME_SEGMENT, this, ss[0], 1);

        // then
        sendInterruptAllCells(ss[1]);
        assertFalse(ss[1].isRemoved()); // shouldn't be removed because there's an incoming pointer

        // when
        Segment.findAndMoveForward(SOME_SEGMENT, this, ss[0], 2);

        // then
        assertTrue(ss[1].isRemoved()); // no more pointers -> logically removed
        sendInterruptAllCells(ss[2]);
        assertFalse(ss[2].isRemoved()); // shouldn't be removed because there's an incoming pointer
    }

    @Test
    void shouldConcurrentlyMoveSegmentsForward() throws ExecutionException, InterruptedException {
        // given
        for (int k = 0; k < 1000; k++) {
            var ss = createSegmentChain(1, 0, true);
            someSegment = ss[0];
            var observedSegments = new ConcurrentHashMap<Integer, Segment>();

            // when
            scoped(
                    scope -> {
                        // creating 10 forks, each moving the reference forward 300 times, to
                        // subsequent ids
                        for (int f = 0; f < 10; f++) {
                            forkVoid(
                                    scope,
                                    () -> {
                                        var s = ss[0];
                                        for (int i = 0; i < 300; i++) {
                                            s =
                                                    Segment.findAndMoveForward(
                                                            SOME_SEGMENT, this, s, i);
                                            var previous = observedSegments.put(i, s);
                                            if (previous != s && previous != null) {
                                                fail(
                                                        "Already observed segment: "
                                                                + previous
                                                                + " for id: "
                                                                + i
                                                                + ", but found: "
                                                                + s);
                                            }
                                        }
                                    });
                        }
                    });
        }
    }
}
