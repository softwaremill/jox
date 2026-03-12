package com.softwaremill.jox.flows;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.softwaremill.jox.structured.JoxScopeExecutionException;

class FlowRecoverWithRetryTest {

    @Test
    void shouldRetryRecoveryFlowOnFailure() throws Exception {
        // given
        var attemptCounter = new AtomicInteger(0);
        int maxRetries = 2;
        Flow<Integer> flow =
                Flows.fromValues(1).concat(Flows.failed(new RuntimeException("upstream")));

        // when
        List<Integer> result =
                flow.recoverWithRetry(
                                Schedule.immediate().maxRetries(maxRetries),
                                e -> {
                                    if (e instanceof RuntimeException) {
                                        int attempt = attemptCounter.incrementAndGet();
                                        if (attempt <= maxRetries) {
                                            return Optional.of(
                                                    Flows.failed(
                                                            new RuntimeException(
                                                                    "recovery attempt "
                                                                            + attempt
                                                                            + " failed")));
                                        }
                                        return Optional.of(Flows.fromValues(42));
                                    }
                                    return Optional.empty();
                                })
                        .runToList();

        // then
        assertEquals(List.of(1, 42), result);
        assertEquals(maxRetries + 1, attemptCounter.get());
    }

    @Test
    void shouldPropagateErrorAfterExhaustingRetries() {
        // given
        Flow<Integer> flow =
                Flows.fromValues(1).concat(Flows.failed(new RuntimeException("upstream")));

        // when & then
        var caught =
                assertThrows(
                        JoxScopeExecutionException.class,
                        () ->
                                flow.recoverWithRetry(
                                                Schedule.immediate().maxRetries(2),
                                                e ->
                                                        e instanceof RuntimeException
                                                                ? Optional.of(
                                                                        Flows.failed(
                                                                                new RuntimeException(
                                                                                        "still"
                                                                                            + " failing")))
                                                                : Optional.empty())
                                        .runToList());
        assertInstanceOf(RuntimeException.class, caught.getCause().getCause());
        assertEquals("still failing", caught.getCause().getCause().getMessage());
    }
}
