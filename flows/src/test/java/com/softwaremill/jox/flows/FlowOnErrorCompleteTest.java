package com.softwaremill.jox.flows;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class FlowOnErrorCompleteTest {

    // --- no-arg variant (catches Exception) ---

    @Test
    void noArg_shouldPassThroughElementsWhenNoError() throws Exception {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3);

        // when
        List<Integer> result = flow.onErrorComplete().runToList();

        // then
        assertEquals(List.of(1, 2, 3), result);
    }

    @Test
    void noArg_shouldCompleteOnUpstreamException() throws Exception {
        // given
        Flow<Integer> flow =
                Flows.fromValues(1, 2).concat(Flows.failed(new RuntimeException("boom")));

        // when
        List<Integer> result = flow.onErrorComplete().runToList();

        // then
        assertEquals(List.of(1, 2), result);
    }

    @Test
    void noArg_shouldCompleteOnErrorDuringProcessing() throws Exception {
        // given
        Flow<Integer> flow =
                Flows.fromValues(1, 2, 3)
                        .map(
                                x -> {
                                    if (x == 3) throw new RuntimeException("fail");
                                    return x;
                                });

        // when
        List<Integer> result = flow.onErrorComplete().runToList();

        // then
        assertEquals(List.of(1, 2), result);
    }

    @Test
    void noArg_shouldWorkWithEmptyFlow() throws Exception {
        // given
        Flow<Integer> flow = Flows.empty();

        // when
        List<Integer> result = flow.onErrorComplete().runToList();

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void noArg_shouldWorkWithFlowThatImmediatelyFails() throws Exception {
        // given
        Flow<Integer> flow = Flows.failed(new RuntimeException("immediate"));

        // when
        List<Integer> result = flow.onErrorComplete().runToList();

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void noArg_shouldNotSuppressDownstreamFailure() {
        // given — downstream throws after receiving elements
        Flow<Integer> flow =
                Flows.fromValues(1, 2, 3)
                        .onErrorComplete()
                        .map(
                                x -> {
                                    if (x == 2) throw new RuntimeException("downstream");
                                    return x;
                                });

        // when & then
        RuntimeException thrown = assertThrows(RuntimeException.class, flow::runToList);
        assertEquals("downstream", thrown.getMessage());
    }

    // --- predicate variant (catches Throwable) ---

    @Test
    void predicate_shouldCompleteWhenPredicateMatches() throws Exception {
        // given
        Flow<Integer> flow =
                Flows.fromValues(1, 2)
                        .concat(Flows.failed(new IllegalArgumentException("expected")));

        // when
        List<Integer> result =
                flow.onErrorComplete(e -> e instanceof IllegalArgumentException).runToList();

        // then
        assertEquals(List.of(1, 2), result);
    }

    @Test
    void predicate_shouldPropagateWhenPredicateDoesNotMatch() {
        // given
        Flow<Integer> flow =
                Flows.fromValues(1, 2).concat(Flows.failed(new RuntimeException("unexpected")));

        // when & then
        assertThrows(
                RuntimeException.class,
                () -> flow.onErrorComplete(e -> e instanceof IllegalArgumentException).runToList());
    }

    @Test
    void predicate_shouldNotSuppressDownstreamFailure() {
        // given
        Flow<Integer> flow =
                Flows.fromValues(1, 2, 3)
                        .onErrorComplete(e -> e instanceof RuntimeException)
                        .map(
                                x -> {
                                    if (x == 2) throw new RuntimeException("downstream pf");
                                    return x;
                                });

        // when & then
        RuntimeException thrown = assertThrows(RuntimeException.class, flow::runToList);
        assertEquals("downstream pf", thrown.getMessage());
    }
}
