package com.softwaremill.jox.flows;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FlowFlattenTest {

    @Test
    void flattenTest() throws Exception {
        // given
        Flow<Flow<Integer>> flow = Flows.fromValues(Flows.fromValues(1, 2), Flows.fromValues(5, 9));

        // when
        List<Integer> integers = flow.flatten().runToList();

        // then
        assertEquals(List.of(1, 2, 5, 9), integers);
    }

    @Test
    void flatten_shouldThrowWhenCalledOnFlowNotContainingFlows() {
        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Flows.fromValues(3, 3).flatten());
        assertEquals("requirement failed: flatten can be called on Flow containing Flows", exception.getMessage());
    }

    @Test
    void flattenPar_shouldThrowWhenCalledOnFlowNotContainingFlows() {
        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Flows.fromValues(3, 3).flattenPar(1));
        assertEquals("requirement failed: flattenPar can be called on Flow containing Flows", exception.getMessage());
    }

    @Test
    void shouldPipeAllElementsOfTheChildFlowsIntoTheOutputFlow() throws Exception {
        // given
        var flow = Flows.fromValues(
                Flows.fromValues(10),
                Flows.fromValues(20, 30),
                Flows.fromValues(40, 50, 60)
        );

        // when & then
        List<Integer> actual = flow.flattenPar(10).runToList();
        assertThat(actual, containsInAnyOrder(10, 20, 30, 40, 50, 60));
    }

    @Test
    void shouldHandleEmptyFlow() throws Exception {
        // given
        var flow = Flows.<Flow<?>>empty();

        // when & then
        assertThat(flow.flattenPar(10).runToList(), Matchers.empty());
    }

    @Test
    void shouldHandleSingletonFlow() throws Exception {
        // given
        var flow = Flows.fromValues(Flows.fromValues(10));

        // when & then
        List<Integer> objects = flow.flattenPar(10).runToList();
        assertThat(objects, contains(10));
    }

    @Test
    void shouldNotFlattenNestedFlows() throws Exception {
        // given
        var flow = Flows.fromValues(Flows.fromValues(Flows.fromValues(10)));

        // when
        List<Flow<Integer>> flows = flow.flattenPar(10).runToList();

        // then
        List<Integer> result = new ArrayList<>();
        for (Flow<Integer> f : flows) {
            List<Integer> integers = f.runToList();
            result.addAll(integers);
        }
        assertThat(result, contains(10));
    }

    @Test
    void shouldHandleSubsequentFlattenCalls() throws Exception {
        // given
        var flow = Flows.fromValues(Flows.fromValues(Flows.fromValues(10), Flows.fromValues(20)));

        // when & then
        var result = flow.flattenPar(10)
                         .runToList().stream().flatMap(f -> {
                    try {
                        return f.runToList().stream();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                         .toList();

        assertThat(result, containsInAnyOrder(10, 20));
    }

    @Test
    void shouldRunAtMostParallelismChildFlows() throws Exception {
        // given
        var flow = Flows.fromValues(
                Flows.timeout(Duration.ofMillis(200)).concat(Flows.fromValues(10)),
                Flows.timeout(Duration.ofMillis(100)).concat(Flows.fromValues(20, 30)),
                Flows.fromValues(40, 50, 60)
        );

        // when & then
        // only one flow can run at a time
        assertThat(flow.flattenPar(1).runToList(), contains(10, 20, 30, 40, 50, 60));
        // when parallelism is increased, all flows are run concurrently
        assertThat(flow.flattenPar(3).runToList(), contains(40, 50, 60, 20, 30, 10));
    }
}
