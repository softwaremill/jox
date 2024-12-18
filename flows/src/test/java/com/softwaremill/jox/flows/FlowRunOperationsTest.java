package com.softwaremill.jox.flows;

import com.softwaremill.jox.Channel;
import com.softwaremill.jox.Source;
import com.softwaremill.jox.structured.Scopes;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FlowRunOperationsTest {

    @Test
    void runForEach_shouldRun() throws Throwable {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3);
        List<Integer> results = new ArrayList<>();

        // when
        flow.runForeach(results::add);

        // then
        assertEquals(List.of(1, 2, 3), results);
    }

    @Test
    void runToList_shouldRun() throws Throwable {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3);

        // when
        List<Integer> results = flow.runToList();

        // then
        assertEquals(List.of(1, 2, 3), results);
    }

    @Test
    void runToChannel_shouldRun() throws Throwable {
        Scopes.unsupervised(scope -> {
            // given
            Flow<Integer> flow = Flows.fromValues(1, 2, 3);

            // when
            Source<Integer> source = flow.runToChannel(scope);

            // then
            assertEquals(1, source.receive());
            assertEquals(2, source.receive());
            assertEquals(3, source.receive());
            return null;
        });
    }

    @Test
    void runToChannel_shouldReturnOriginalSourceWhenRunningASourcedBackedFlow() throws Throwable {
        Scopes.unsupervised(scope -> {
            // given
            Channel<Integer> channel = Channel.newUnlimitedChannel();
            Flow<Integer> flow = Flows.fromSource(channel);

            // when
            Source<Integer> receivedChannel = flow.runToChannel(scope);

            // then
            assertEquals(channel, receivedChannel);
            return null;
        });
    }

    @Test
    void runToChannel_shouldRunWithBufferSizeDefinedInScope() throws Throwable {
        ScopedValue.where(Channel.BUFFER_SIZE, 2).call(() -> {
            Scopes.unsupervised(scope -> {
                // given
                Flow<Integer> flow = Flows.fromValues(1, 2, 3);

                // when
                Source<Integer> source = flow.runToChannel(scope);

                // then
                assertEquals(1, source.receive());
                assertEquals(2, source.receive());
                assertEquals(3, source.receive());
                return null;
            });
            return null;
        });
    }

    @Test
    void runToChannelWithBufferCapacity_shouldRun() throws Throwable {
        Scopes.unsupervised(scope -> {
            // given
            Flow<Integer> flow = Flows.fromValues(1, 2, 3);

            // when
            Source<Integer> source = flow.runToChannel(scope, 2);

            // then
            assertEquals(1, source.receive());
            assertEquals(2, source.receive());
            assertEquals(3, source.receive());
            return null;
        });
    }

    @Test
    void runToChannelWithBufferCapacity_shouldReturnOriginalSourceWhenRunningASourcedBackedFlow() throws Throwable {
        Scopes.unsupervised(scope -> {
            // given
            Channel<Integer> channel = Channel.newUnlimitedChannel();
            Flow<Integer> flow = Flows.fromSource(channel);

            // when
            Source<Integer> receivedChannel = flow.runToChannel(scope, 10);

            // then
            assertEquals(channel, receivedChannel);
            return null;
        });
    }

    @Test
    void runFold_shouldThrowExceptionForFailedFlow() {
        // given
        Flow<?> flow = Flows.failed(new IllegalStateException());

        // when & then
        assertThrows(IllegalStateException.class, () ->
                flow
                        .runFold(0, (acc, n) -> Integer.valueOf(acc.toString() + n)));
    }

    @Test
    void runFold_shouldThrowExceptionThrownInFunctionF() {
        // given
        Flow<Integer> flow = Flows.fromValues(1);

        // when & then
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            flow
                    .runFold(0, (acc, n) -> { throw new RuntimeException("Function `f` is broken"); });
        });
        assertEquals("Function `f` is broken", thrown.getMessage());
    }

    @Test
    void runFold_shouldReturnZeroValueFromFoldOnEmptySource() throws Exception {
        // given
        Flow<?> flow = Flows.empty();

        // when & then
        assertEquals(0,
                flow
                        .runFold(0, (acc, n) -> Integer.valueOf(acc.toString() + n)));
    }

    @Test
    void runFold_shouldReturnFoldOnNonEmptyFold() throws Exception {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2);

        // when & then
        assertEquals(3, flow.runFold(0, Integer::sum));
    }

    @Test
    void runTakeLast_shouldThrowChannelClosedExceptionErrorForSourceFailedWithoutException() {
        // given
        Flow<?> flow = Flows.failed(new IllegalStateException());

        // when & then
        assertThrows(IllegalStateException.class, () -> flow.runTakeLast(1));
    }

    @Test
    void runTakeLast_shouldFailToTakeLastWhenNIsLessThanZero() {
        // given
        Flow<?> flow = Flows.empty();

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> flow.runTakeLast(-1));
        assertEquals("requirement failed: n must be >= 0", exception.getMessage());
    }

    @Test
    void runTakeLast_shouldReturnEmptyListForEmptySource() throws Exception {
        // given
        Flow<?> flow = Flows.empty();

        // when & then
        assertEquals(List.of(), flow.runTakeLast(1));
    }

    @Test
    void runTakeLast_shouldReturnEmptyListWhenNIsZeroAndListIsNotEmpty() throws Exception {
        // given
        Flow<Integer> flow = Flows.fromValues(1);

        // when & then
        assertEquals(List.of(), flow.runTakeLast(0));
    }

    @Test
    void runTakeLast_shouldReturnListWithAllElementsIfSourceIsSmallerThanRequestedNumber() throws Exception {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2);

        // when & then
        assertEquals(List.of(1, 2), flow.runTakeLast(3));
    }

    @Test
    void runTakeLast_shouldReturnLastNElementsFromSource() throws Exception {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3, 4, 5);

        // when & then
        assertEquals(List.of(4, 5), flow.runTakeLast(2));
    }

    @Test
    void runReduce_shouldThrowNoSuchElementExceptionForReduceOverEmptySource() {
        // given
        Flow<Integer> flow = Flows.empty();

        // when & then
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> flow.runReduce(Integer::sum));
        assertEquals("cannot reduce an empty flow", exception.getMessage());
    }

    @Test
    void runReduce_shouldThrowExceptionThrownInFunctionFWhenFThrows() {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2);

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                flow.runReduce((a, b) -> {throw new RuntimeException("Function `f` is broken");}));
        assertEquals("Function `f` is broken", exception.getMessage());
    }

    @Test
    void runReduce_shouldReturnFirstElementFromReduceOverSingleElementSource() throws Exception {
        // given
        Flow<Integer> flow = Flows.fromValues(1);

        // when & then
        assertEquals(1, flow.runReduce(Integer::sum));
    }

    @Test
    void runReduce_shouldRunReduceOverNonEmptySource() throws Exception {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2);

        // when & then
        assertEquals(3, flow.runReduce(Integer::sum));
    }

    @Test
    void runLast_shouldThrowNoSuchElementExceptionForEmptySource() {
        // given
        Flow<?> flow = Flows.empty();

        // when & then
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> flow.runLast());
        assertEquals("cannot obtain last element from an empty source", exception.getMessage());
    }

    @Test
    void runLast_shouldThrowRuntimeExceptionWithMessageDuringRetrieval() {
        // given
        Flow<?> flow = Flows.failed(new RuntimeException("source is broken"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> flow.runLast());
        assertEquals("source is broken", exception.getMessage());
    }

    @Test
    void runLast_shouldReturnLastElementForNonEmptySource() throws Exception {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2);

        // when & then
        assertEquals(2, flow.runLast());
    }

    @Test
    void runLastOptional_returnNoneForEmptyFlow() throws Exception {
        // given
        Flow<?> flow = Flows.empty();

        // when & then
        assertEquals(Optional.empty(), flow.runLastOptional());
    }

    @Test
    void runLastOptional_returnSomeForNonEmptyFlow() throws Exception {
        // given
        Flow<Integer> s = Flows.fromValues(1, 2, 3);

        // when & then
        assertEquals(Optional.of(3), s.runLastOptional());
    }

    @Test
    void runLastOptional_throwExceptionWithMessageDuringRetrieval() {
        // given
        Flow<?> flow = Flows.failed(new RuntimeException("source is broken"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> flow.runLastOptional());
        assertEquals("source is broken", exception.getMessage());
    }

    @Test
    void runPipeToSink_pipeOneSourceToAnother() throws ExecutionException, InterruptedException {
        Scopes.supervised(scope -> {
            Flow<Integer> c1 = Flows.fromValues(1, 2, 3);
            Channel<Integer> c2 = new Channel<>();

            scope.fork(() -> {
                c1.runPipeToSink(c2, false);
                c2.done();
                return null;
            });

            assertEquals(List.of(1, 2, 3), c2.toList());
            return null;
        });
    }

    @Test
    void runPipeToSink_pipeOneSourceToAnotherWithDonePropagation() throws ExecutionException, InterruptedException {
        Scopes.supervised(scope -> {
            Flow<Integer> c1 = Flows.fromValues(1, 2, 3);
            Channel<Integer> c2 = new Channel<>();

            scope.fork(() -> {
                c1.runPipeToSink(c2, true);
                return null;
            });

            assertEquals(List.of(1, 2, 3), c2.toList());
            return null;
        });
    }
}
