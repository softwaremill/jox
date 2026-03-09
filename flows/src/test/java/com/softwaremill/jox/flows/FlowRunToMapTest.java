package com.softwaremill.jox.flows;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

class FlowRunToMapTest {

    @Test
    void shouldCollectElementsIntoMap() throws Exception {
        // given
        Flow<String> flow = Flows.fromValues("a", "bb", "ccc");

        // when
        Map<String, Integer> result = flow.runToMap(s -> s, String::length);

        // then
        assertEquals(Map.of("a", 1, "bb", 2, "ccc", 3), result);
    }

    @Test
    void shouldReturnEmptyMapForEmptyFlow() throws Exception {
        // given
        Flow<String> flow = Flows.empty();

        // when
        Map<String, Integer> result = flow.runToMap(s -> s, String::length);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldOverwriteDuplicateKeys() throws Exception {
        // given
        Flow<String> flow = Flows.fromValues("a", "ab", "abc");

        // when
        Map<Character, Integer> result = flow.runToMap(s -> s.charAt(0), String::length);

        // then — last value wins
        assertEquals(Map.of('a', 3), result);
    }

    @Test
    void shouldPropagateErrors() {
        // given
        RuntimeException boom = new RuntimeException("boom");
        Flow<Integer> flow = Flows.fromValues(1, 2).concat(Flows.failed(boom));

        // when & then
        RuntimeException thrown =
                assertThrows(RuntimeException.class, () -> flow.runToMap(i -> i, i -> i * 10));
        assertEquals("boom", thrown.getMessage());
    }
}
