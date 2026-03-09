package com.softwaremill.jox.flows;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.softwaremill.jox.structured.JoxScopeExecutionException;

class FlowOnErrorRecoverTest {

    @Test
    void shouldRecoverWithValueDerivedFromException() throws Exception {
        // given
        Flow<Integer> flow = Flows.fromValues(1).concat(Flows.failed(new RuntimeException("msg")));

        // when
        List<Integer> result = flow.onErrorRecover(e -> e.getMessage().length()).runToList();

        // then
        assertEquals(List.of(1, 3), result);
    }

    @Test
    void shouldPropagateExceptionThrownByRecoveryFunction() {
        // given
        Flow<Integer> flow =
                Flows.fromValues(1).concat(Flows.failed(new RuntimeException("original")));

        // when & then
        JoxScopeExecutionException caught =
                assertThrows(
                        JoxScopeExecutionException.class,
                        () ->
                                flow.onErrorRecover(
                                                e -> {
                                                    throw new IllegalStateException(
                                                            "recovery failed");
                                                })
                                        .runToList());
        assertInstanceOf(IllegalStateException.class, caught.getCause().getCause());
    }
}
