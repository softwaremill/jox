package com.softwaremill.jox.flows;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

class FlowRunToSetTest {

    @Test
    void shouldCollectElementsIntoSet() throws Exception {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3);

        // when
        Set<Integer> result = flow.runToSet();

        // then
        assertEquals(Set.of(1, 2, 3), result);
    }

    @Test
    void shouldDeduplicateElements() throws Exception {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 2, 3, 3, 3);

        // when
        Set<Integer> result = flow.runToSet();

        // then
        assertEquals(Set.of(1, 2, 3), result);
    }

    @Test
    void shouldReturnEmptySetForEmptyFlow() throws Exception {
        // given
        Flow<Integer> flow = Flows.empty();

        // when
        Set<Integer> result = flow.runToSet();

        // then
        assertTrue(result.isEmpty());
    }
}
