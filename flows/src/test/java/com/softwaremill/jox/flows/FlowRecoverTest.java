package com.softwaremill.jox.flows;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.softwaremill.jox.structured.JoxScopeExecutionException;
import com.softwaremill.jox.structured.ThrowingFunction;

class FlowRecoverTest {

    @Test
    void shouldPassThroughElementsWhenUpstreamFlowSucceeds() throws Throwable {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3);
        ThrowingFunction<Throwable, Optional<Integer>> recoveryFunction =
                e -> e instanceof IllegalArgumentException ? Optional.of(42) : Optional.empty();

        // when
        List<Integer> result = flow.recover(recoveryFunction).runToList();

        // then
        assertEquals(List.of(1, 2, 3), result);
    }

    @Test
    void shouldEmitRecoveryValueWhenUpstreamFlowFailsWithHandledException() throws Throwable {
        // given
        IllegalArgumentException exception = new IllegalArgumentException("test error");
        Flow<Integer> flow = Flows.fromValues(1, 2).concat(Flows.failed(exception));
        ThrowingFunction<Throwable, Optional<Integer>> recoveryFunction =
                e -> e instanceof IllegalArgumentException ? Optional.of(42) : Optional.empty();

        // when
        List<Integer> result = flow.recover(recoveryFunction).runToList();

        // then
        assertEquals(List.of(1, 2, 42), result);
    }

    @Test
    void shouldNotEmitRecoveryValueWhenDownstreamFlowFailsWithHandledException() {
        // given
        IllegalArgumentException exception = new IllegalArgumentException("test error");
        ThrowingFunction<Throwable, Optional<Integer>> recoveryFunction =
                e -> e instanceof IllegalArgumentException ? Optional.of(42) : Optional.empty();
        Flow<Integer> flow =
                Flows.fromValues(1, 2).recover(recoveryFunction).concat(Flows.failed(exception));

        // when & then
        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, flow::runToList);
        assertEquals("test error", thrown.getMessage());
    }

    @Test
    void shouldPropagateUnhandledExceptions() {
        // given
        RuntimeException exception = new RuntimeException("unhandled error");
        Flow<Integer> flow = Flows.fromValues(1, 2).concat(Flows.failed(exception));
        ThrowingFunction<Throwable, Optional<Integer>> recoveryFunction =
                e -> e instanceof IllegalArgumentException ? Optional.of(42) : Optional.empty();

        // when & then
        JoxScopeExecutionException caught =
                assertThrows(
                        JoxScopeExecutionException.class,
                        () -> flow.recover(recoveryFunction).runToList());
        assertEquals(exception, caught.getCause().getCause());
    }

    @Test
    void shouldHandleMultipleExceptionTypes() throws Throwable {
        // given
        IllegalArgumentException exception = new IllegalArgumentException("test error");
        Flow<Integer> flow = Flows.fromValues(1, 2).concat(Flows.failed(exception));
        ThrowingFunction<Throwable, Optional<Integer>> recoveryFunction =
                e -> {
                    if (e instanceof IllegalArgumentException) return Optional.of(42);
                    if (e instanceof RuntimeException) return Optional.of(99);
                    return Optional.empty();
                };

        // when
        List<Integer> result = flow.recover(recoveryFunction).runToList();

        // then
        assertEquals(List.of(1, 2, 42), result);
    }

    @Test
    void shouldWorkWithDifferentRecoveryValueTypes() throws Throwable {
        // given
        IllegalArgumentException exception = new IllegalArgumentException("test error");
        Flow<String> flow = Flows.fromValues("1", "2").concat(Flows.failed(exception));
        ThrowingFunction<Throwable, Optional<String>> recoveryFunction =
                e ->
                        e instanceof IllegalArgumentException
                                ? Optional.of("recovered")
                                : Optional.empty();

        // when
        List<String> result = flow.recover(recoveryFunction).runToList();

        // then
        assertEquals(List.of("1", "2", "recovered"), result);
    }

    @Test
    void shouldHandleExceptionsDuringFlowProcessing() throws Throwable {
        // given
        Flow<Integer> flow =
                Flows.fromValues(1, 2, 3)
                        .map(
                                x -> {
                                    if (x == 2)
                                        throw new IllegalArgumentException("processing error");
                                    return x;
                                });
        ThrowingFunction<Throwable, Optional<Integer>> recoveryFunction =
                e -> e instanceof IllegalArgumentException ? Optional.of(42) : Optional.empty();

        // when
        List<Integer> result = flow.recover(recoveryFunction).runToList();

        // then
        assertEquals(List.of(1, 42), result);
    }

    @Test
    void shouldWorkWithEmptyFlows() throws Throwable {
        // given
        Flow<Integer> flow = Flows.<Integer>empty();
        ThrowingFunction<Throwable, Optional<Integer>> recoveryFunction =
                e -> e instanceof IllegalArgumentException ? Optional.of(42) : Optional.empty();

        // when
        List<Integer> result = flow.recover(recoveryFunction).runToList();

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldPropagateExceptionsWhenRecoveryFunctionThrowsError() {
        // given
        IllegalArgumentException exception = new IllegalArgumentException("test error");
        Flow<Integer> flow = Flows.fromValues(1, 2).concat(Flows.failed(exception));
        ThrowingFunction<Throwable, Optional<Integer>> recoveryFunction =
                e -> {
                    throw exception;
                };

        // when & then
        JoxScopeExecutionException caught =
                assertThrows(
                        JoxScopeExecutionException.class,
                        () -> flow.recover(recoveryFunction).runToList());
        assertEquals(exception, caught.getCause().getCause());
    }
}
