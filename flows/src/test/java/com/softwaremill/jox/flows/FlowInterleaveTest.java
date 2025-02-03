package com.softwaremill.jox.flows;

import com.softwaremill.jox.Channel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FlowInterleaveTest {

    @Test
    void shouldInterleaveWithEmptySource() throws Exception {
        // given
        var c1 = Flows.fromValues(1, 2, 3);
        var c2 = Flows.fromValues();

        // when
        var result = c1.interleave(c2, 1, false, 10)
                .runToList();

        // then
        assertEquals(List.of(1, 2, 3), result);
    }

    @Test
    void shouldInterleaveTwoSourcesWithSegmentSizeEqualTo1() throws Exception {
        // given
        var c1 = Flows.fromValues(1, 3, 5);
        var c2 = Flows.fromValues(2, 4, 6);

        // when
        var result = c1.interleave(c2, 1, false, 10)
                .runToList();

        // then
        assertEquals(List.of(1, 2, 3, 4, 5, 6), result);
    }

    @Test
    void shouldInterleaveTwoSourcesWithSegmentSizeEqualTo1AndDifferentLengths() throws Exception {
        // given
        var c1 = Flows.fromValues(1, 3, 5);
        var c2 = Flows.fromValues(2, 4, 6, 8, 10, 12);

        // when
        var result = c1.interleave(c2, 1, false, 10)
                .runToList();

        // then
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 8, 10, 12), result);
    }

    @Test
    void shouldInterleaveWithDefaultBufferCapacity() throws Exception {
        // given
        var c1 = Flows.fromValues(1, 3, 5);
        var c2 = Flows.fromValues(2, 4, 6, 8, 10, 12);

        // when
        var result = c1.interleave(c2, 1, false)
                .runToList();

        // then
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 8, 10, 12), result);
    }

    @Test
    void shouldInterleaveWithBufferCapacityTakenFromScope() throws Exception {
        // given
        var c1 = Flows.fromValues(1, 3, 5);
        var c2 = Flows.fromValues(2, 4, 6, 8, 10, 12);

        // when
        var result = ScopedValue.where(Channel.BUFFER_SIZE, 10)
                .call(() -> c1.interleave(c2, 1, false).runToList());

        // then
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 8, 10, 12), result);
    }

    @Test
    void shouldInterleaveTwoSourcesSegmentSizeBiggerThan1() throws Exception {
        // given
        var c1 = Flows.fromValues(1, 2, 3, 4);
        var c2 = Flows.fromValues(10, 20, 30, 40);

        // when
        var result = c1.interleave(c2, 2, false, 10)
                .runToList();

        // then
        assertEquals(List.of(1, 2, 10, 20, 3, 4, 30, 40), result);
    }

    @Test
    void shouldInterleaveTwoSourcesWitSegmentSizeBiggerThan1AndDifferentLengths() throws Exception {
        // given
        var c1 = Flows.fromValues(1, 2, 3, 4, 5, 6, 7);
        var c2 = Flows.fromValues(10, 20, 30, 40);

        // when
        var result = c1.interleave(c2, 2, false, 10)
                .runToList();

        // then
        assertEquals(List.of(1, 2, 10, 20, 3, 4, 30, 40, 5, 6, 7), result);
    }

    @Test
    void shouldInterleaveTwoSourcesWithDifferentLengthsAndCompleteEagerly() throws Exception {
        // given
        var c1 = Flows.fromValues(1, 3, 5);
        var c2 = Flows.fromValues(2, 4, 6, 8, 10, 12);

        // when
        var result = c1.interleave(c2, 1, true, 10)
                .runToList();

        // then
        assertEquals(List.of(1, 2, 3, 4, 5, 6), result);
    }

    @Test
    void shouldWhenEmptyInterleaveWithNonEmptySourceAndCompleteEagerly() throws Exception {
        // given
        var c1 = Flows.fromValues();
        var c2 = Flows.fromValues(1, 2, 3);

        // when
        var s1 = c1.interleave(c2, 1, true, 10)
                .runToList();

        // then
        assertTrue(s1.isEmpty());
    }

    @Test
    void shouldInterleaveWithEmptySourceAndCompleteEagerly() throws Exception {
        // given
        var c1 = Flows.fromValues(1, 2, 3);
        var c2 = Flows.fromValues();

        // when
        var s1 = c1.interleave(c2, 1, true, 10);

        // then
        assertEquals(List.of(1), s1.runToList());
    }

    @Test
    void shouldInterleaveWithBufferCapacityEqualTo0() throws Exception {
        // given
        var c1 = Flows.fromValues(1, 2, 3);
        var c2 = Flows.fromValues(10, 20, 30);

        // when
        var s1 = c1.interleave(c2, 1, true, 0);

        // then
        assertEquals(List.of(1, 10, 2, 20, 3, 30), s1.runToList());
    }
}
