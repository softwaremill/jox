package com.softwaremill.jox;

import com.softwaremill.jox.structured.UnsupervisedScope;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FlowGroupedTest {

    @Test
    void shouldEmitGroupedElements() throws Exception {
        // when
        List<List<Integer>> result = Flows.fromValues(1, 2, 3, 4, 5, 6)
                .grouped(3)
                .runToList();

        // then
        assertEquals(List.of(List.of(1, 2, 3), List.of(4, 5, 6)), result);
    }

    @Test
    void shouldEmitGroupedElementsAndIncludeRemainingValuesWhenFlowCloses() throws Exception {
        // given
        List<List<Integer>> result = Flows.fromValues(1, 2, 3, 4, 5, 6, 7)
                .grouped(3)
                .runToList();

        // then
        assertEquals(List.of(List.of(1, 2, 3), List.of(4, 5, 6), List.of(7)), result);
    }

    @Test
    @WithUnsupervisedScope
    void shouldReturnFailedFlowWhenTheOriginalFlowIsFailed(UnsupervisedScope scope) throws InterruptedException {
        // given
        RuntimeException failure = new RuntimeException();

        // when
        Object result = Flows.failed(failure)
                .grouped(3)
                .runToChannel(scope)
                .receiveOrClosed();

        // then
        assertInstanceOf(ChannelError.class, result);
        assertEquals(failure, ((ChannelError) result).cause());
    }

    @Test
    void shouldEmitGroupedElementsWithCustomCostFunction() throws Exception {
        // when
        List<List<Integer>> result = Flows.fromValues(1, 2, 3, 4, 5, 6, 5, 3, 1)
                .groupedWeighted(10, n -> (long) (n * 2))
                .runToList();

        // then
        assertEquals(List.of(List.of(1, 2, 3), List.of(4, 5), List.of(6), List.of(5), List.of(3, 1)), result);
    }

    @Test
    @WithUnsupervisedScope
    void shouldReturnFailedFlowWhenCostFunctionThrowsException(UnsupervisedScope scope) {
        // when
        ChannelClosedException exception = assertThrows(ChannelClosedException.class, () ->
                Flows.fromValues(1, 2, 3, 0, 4, 5, 6, 7)
                        .groupedWeighted(150, n -> (long) (100 / n))
                        .runToChannel(scope)
                        .forEach(i -> {
                        }));

        // then
        assertInstanceOf(ArithmeticException.class, exception.getCause());
    }

    @Test
    @WithUnsupervisedScope
    void shouldReturnFailedSourceWhenTheOriginalSourceIsFailed(UnsupervisedScope scope) throws InterruptedException {
        // given
        RuntimeException failure = new RuntimeException();

        // when
        Object result = Flows.failed(failure)
                .groupedWeighted(10, n -> Long.parseLong(n.toString()) * 2)
                .runToChannel(scope)
                .receiveOrClosed();

        // then
        assertInstanceOf(ChannelError.class, result);
        assertEquals(failure, ((ChannelError) result).cause());
    }
}
