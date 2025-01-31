package com.softwaremill.jox.flows;

import com.softwaremill.jox.ChannelError;
import com.softwaremill.jox.structured.Scopes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FlowSlidingTest {

    @Test
    void shouldCreateSlidingWindowsForN2AndStep1() throws Exception {
        List<List<Integer>> result = Flows.fromValues(1, 2, 3, 4)
                                          .sliding(2, 1)
                                          .runToList();

        assertEquals(List.of(List.of(1, 2), List.of(2, 3), List.of(3, 4)), result);
    }

    @Test
    void shouldCreateSlidingWindowsForN3AndStep1() throws Exception {
        List<List<Integer>> result = Flows.fromValues(1, 2, 3, 4, 5)
                                          .sliding(3, 1)
                                          .runToList();

        assertEquals(List.of(List.of(1, 2, 3), List.of(2, 3, 4), List.of(3, 4, 5)), result);
    }

    @Test
    void shouldCreateSlidingWindowsForN2AndStep2() throws Exception {
        List<List<Integer>> result = Flows.fromValues(1, 2, 3, 4, 5)
                                          .sliding(2, 2)
                                          .runToList();

        assertEquals(List.of(List.of(1, 2), List.of(3, 4), List.of(5)), result);
    }

    @Test
    void shouldCreateSlidingWindowsForN3AndStep2() throws Exception {
        List<List<Integer>> result = Flows.fromValues(1, 2, 3, 4, 5, 6)
                                          .sliding(3, 2)
                                          .runToList();

        assertEquals(List.of(List.of(1, 2, 3), List.of(3, 4, 5), List.of(5, 6)), result);
    }

    @Test
    void shouldCreateSlidingWindowsForN1AndStep2() throws Exception {
        List<List<Integer>> result = Flows.fromValues(1, 2, 3, 4, 5)
                                          .sliding(1, 2)
                                          .runToList();

        assertEquals(List.of(List.of(1), List.of(3), List.of(5)), result);
    }

    @Test
    void shouldCreateSlidingWindowsForN2AndStep3() throws Exception {
        List<List<Integer>> result = Flows.fromValues(1, 2, 3, 4, 5, 6)
                                          .sliding(2, 3)
                                          .runToList();

        assertEquals(List.of(List.of(1, 2), List.of(4, 5)), result);
    }

    @Test
    void shouldCreateSlidingWindowsForN2AndStep3With1ElementRemainingInTheEnd() throws Exception {
        List<List<Integer>> result = Flows.fromValues(1, 2, 3, 4, 5, 6, 7)
                                          .sliding(2, 3)
                                          .runToList();
        assertEquals(List.of(List.of(1, 2), List.of(4, 5), List.of(7)), result);
    }

    @Test
    void shouldReturnFailedSourceWhenTheOriginalSourceIsFailed() throws Exception {
        Scopes.unsupervised(scope -> {
            RuntimeException failure = new RuntimeException();
            ChannelError received = (ChannelError) Flows.failed(failure)
                                                        .sliding(1, 2)
                                                        .runToChannel(scope)
                                                        .receiveOrClosed();
            assertEquals(failure, received.cause());
            return null;
        });
    }
}
