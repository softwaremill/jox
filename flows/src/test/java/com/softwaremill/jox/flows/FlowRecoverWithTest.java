package com.softwaremill.jox.flows;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.softwaremill.jox.structured.JoxScopeExecutionException;
import com.softwaremill.jox.structured.ThrowingFunction;

class FlowRecoverWithTest {

    @Test
    void shouldPassThroughElementsWhenUpstreamSucceeds() throws Throwable {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3);
        ThrowingFunction<Throwable, Optional<Flow<Integer>>> recoveryFunction =
                e ->
                        e instanceof IllegalArgumentException
                                ? Optional.of(Flows.fromValues(42))
                                : Optional.empty();

        // when
        List<Integer> result = flow.recoverWith(recoveryFunction).runToList();

        // then
        assertEquals(List.of(1, 2, 3), result);
    }

    @Test
    void shouldSwitchToRecoveryFlowOnHandledException() throws Throwable {
        // given
        IllegalArgumentException exception = new IllegalArgumentException("test error");
        Flow<Integer> flow = Flows.fromValues(1, 2).concat(Flows.failed(exception));
        ThrowingFunction<Throwable, Optional<Flow<Integer>>> recoveryFunction =
                e ->
                        e instanceof IllegalArgumentException
                                ? Optional.of(Flows.fromValues(3, 4, 5))
                                : Optional.empty();

        // when
        List<Integer> result = flow.recoverWith(recoveryFunction).runToList();

        // then
        assertEquals(List.of(1, 2, 3, 4, 5), result);
    }

    @Test
    void shouldPropagateUnhandledExceptions() {
        // given
        RuntimeException exception = new RuntimeException("unhandled error");
        Flow<Integer> flow = Flows.fromValues(1, 2).concat(Flows.failed(exception));
        ThrowingFunction<Throwable, Optional<Flow<Integer>>> recoveryFunction =
                e ->
                        e instanceof IllegalArgumentException
                                ? Optional.of(Flows.fromValues(42))
                                : Optional.empty();

        // when & then
        JoxScopeExecutionException caught =
                assertThrows(
                        JoxScopeExecutionException.class,
                        () -> flow.recoverWith(recoveryFunction).runToList());
        assertEquals(exception, caught.getCause().getCause());
    }

    @Test
    void shouldSwitchToEmptyRecoveryFlow() throws Throwable {
        // given
        IllegalArgumentException exception = new IllegalArgumentException("test error");
        Flow<Integer> flow = Flows.fromValues(1, 2).concat(Flows.failed(exception));
        ThrowingFunction<Throwable, Optional<Flow<Integer>>> recoveryFunction =
                e ->
                        e instanceof IllegalArgumentException
                                ? Optional.of(Flows.empty())
                                : Optional.empty();

        // when
        List<Integer> result = flow.recoverWith(recoveryFunction).runToList();

        // then
        assertEquals(List.of(1, 2), result);
    }

    @Test
    void shouldPropagateErrorFromRecoveryFlow() {
        // given
        IllegalArgumentException upstreamException = new IllegalArgumentException("upstream error");
        RuntimeException recoveryException = new RuntimeException("recovery error");
        Flow<Integer> flow = Flows.fromValues(1, 2).concat(Flows.failed(upstreamException));
        ThrowingFunction<Throwable, Optional<Flow<Integer>>> recoveryFunction =
                e ->
                        e instanceof IllegalArgumentException
                                ? Optional.of(Flows.failed(recoveryException))
                                : Optional.empty();

        // when & then
        JoxScopeExecutionException caught =
                assertThrows(
                        JoxScopeExecutionException.class,
                        () -> flow.recoverWith(recoveryFunction).runToList());
        assertEquals(recoveryException, caught.getCause().getCause());
    }

    @Test
    void shouldWorkWithEmptyUpstreamFlow() throws Throwable {
        // given
        Flow<Integer> flow = Flows.<Integer>empty();
        ThrowingFunction<Throwable, Optional<Flow<Integer>>> recoveryFunction =
                e ->
                        e instanceof IllegalArgumentException
                                ? Optional.of(Flows.fromValues(42))
                                : Optional.empty();

        // when
        List<Integer> result = flow.recoverWith(recoveryFunction).runToList();

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldPropagateErrorFromRecoveryFlowAfterPartialEmission() {
        // given
        IllegalArgumentException upstreamException = new IllegalArgumentException("upstream error");
        RuntimeException recoveryException = new RuntimeException("recovery error");
        Flow<Integer> flow = Flows.fromValues(1, 2).concat(Flows.failed(upstreamException));
        Flow<Integer> recoveryFlow = Flows.fromValues(3, 4).concat(Flows.failed(recoveryException));
        ThrowingFunction<Throwable, Optional<Flow<Integer>>> recoveryFunction =
                e ->
                        e instanceof IllegalArgumentException
                                ? Optional.of(recoveryFlow)
                                : Optional.empty();

        // when & then
        JoxScopeExecutionException caught =
                assertThrows(
                        JoxScopeExecutionException.class,
                        () -> flow.recoverWith(recoveryFunction).runToList());
        assertEquals(recoveryException, caught.getCause().getCause());
    }

    @Test
    void shouldPropagateExceptionWhenRecoveryFunctionThrows() {
        // given
        IllegalArgumentException exception = new IllegalArgumentException("test error");
        Flow<Integer> flow = Flows.fromValues(1, 2).concat(Flows.failed(exception));
        ThrowingFunction<Throwable, Optional<Flow<Integer>>> recoveryFunction =
                e -> {
                    throw exception;
                };

        // when & then
        JoxScopeExecutionException caught =
                assertThrows(
                        JoxScopeExecutionException.class,
                        () -> flow.recoverWith(recoveryFunction).runToList());
        assertEquals(exception, caught.getCause().getCause());
    }
}
