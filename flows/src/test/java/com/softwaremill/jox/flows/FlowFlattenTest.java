package com.softwaremill.jox.flows;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class FlowFlattenTest {

    @Test
    void flattenTest() throws Exception {
        // given
        Flow<Flow<Integer>> flow = Flows.fromValues(Flows.fromValues(1, 2), Flows.fromValues(5, 9));

        // when
        List<Integer> integers = flow.<Integer>flatten().runToList();

        // then
        assertEquals(List.of(1, 2, 5, 9), integers);
    }

    @Test
    void shouldThrowWhenCalledOnFlowNotContainingFlows() {
        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Flows.fromValues(3, 3).flatten());
        assertEquals("requirement failed: flatten can be called on Flow containing Flows", exception.getMessage());
    }
}
