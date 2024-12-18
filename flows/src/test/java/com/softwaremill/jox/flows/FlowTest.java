package com.softwaremill.jox.flows;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import com.softwaremill.jox.Channel;
import com.softwaremill.jox.ChannelClosedException;
import com.softwaremill.jox.Source;
import com.softwaremill.jox.structured.Scopes;
import org.junit.jupiter.api.Test;

class FlowTest {

    @Test
    void shouldRunForEach() throws Throwable {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3);
        List<Integer> results = new ArrayList<>();

        // when
        flow.runForeach(results::add);

        // then
        assertEquals(List.of(1, 2, 3), results);
    }

    @Test
    void shouldRunToList() throws Throwable {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3);

        // when
        List<Integer> results = flow.runToList();

        // then
        assertEquals(List.of(1, 2, 3), results);
    }

    @Test
    void shouldRunToChannel() throws Throwable {
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
    void shouldRunToChannelWithBufferSizeDefinedInScope() throws Throwable {
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
    void shouldReturnOriginalSourceWhenRunningASourcedBackedFlow() throws Throwable {
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
    void shouldThrowExceptionForFailedFlow() {
        assertThrows(IllegalStateException.class, () -> {
            Flows.failed(new IllegalStateException())
                    .runFold(0, (acc, n) -> Integer.valueOf(acc.toString() + n));
        });
    }

    @Test
    void shouldThrowExceptionThrownInFunctionF() {
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            Flows.fromValues(1)
                    .runFold(0, (acc, n) -> { throw new RuntimeException("Function `f` is broken"); });
        });
        assertEquals("Function `f` is broken", thrown.getMessage());
    }

    @Test
    void shouldReturnZeroValueFromFoldOnEmptySource() throws Exception {
        assertEquals(0, Flows.empty().runFold(0, (acc, n) -> Integer.valueOf(acc.toString() + n)));
    }

    @Test
    void shouldReturnFoldOnNonEmptyFold() throws Exception {
        assertEquals(3, Flows.fromValues(1, 2).runFold(0, Integer::sum));
    }

    @Test
    void shouldTakeFromSimpleFlow() throws Exception {
        Flow<Integer> f = Flows.fromValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<Integer> result = f.take(5).runToList();
        assertEquals(List.of(1, 2, 3, 4, 5), result);
    }

    @Test
    void shouldTakeFromAsyncFlow() throws Exception {
        Flow<Integer> f = Flows.fromValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .buffer(16);
        List<Integer> result = f.take(5).runToList();
        assertEquals(List.of(1, 2, 3, 4, 5), result);
    }

    @Test
    void shouldTakeAllIfFlowEndsSooner() throws Exception {
        Flow<Integer> f = Flows.fromValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<Integer> result = f.take(50).runToList();
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), result);
    }

    @Test
    void shouldWorkWithASingleAsyncBoundary() throws Throwable {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3, 4, 5)
                .buffer(3);

        // when
        List<Integer> integers = flow.runToList();

        // then
        assertEquals(List.of(1, 2, 3, 4, 5), integers);
    }

    @Test
    void shouldWorkWithMultipleAsyncBoundaries() throws Throwable {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3, 4, 5)
                .buffer(3)
                .map(i -> i * 2)
                .buffer(2)
                .map(i -> i + 1)
                .buffer(5);

        // when
        List<Integer> integers = flow.runToList();

        // then
        assertEquals(List.of(3, 5, 7, 9, 11), integers);
    }

    @Test
    void shouldPropagateErrorsWhenUsingBuffer() {
        Exception exception = assertThrows(ChannelClosedException.class, () -> {
            Flows.fromValues(1, 2, 3)
                    .map(value -> { throw new IllegalStateException(); })
                    .buffer(5)
                    .runToList();
        });
        assertInstanceOf(IllegalStateException.class, exception.getCause());
    }

    @Test
    void shouldMap() throws Throwable {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3);
        List<String> results = new ArrayList<>();

        // when
        Flow<String> mapped = flow.map(Object::toString);
        mapped.runForeach(results::add);

        // then
        assertEquals(List.of("1", "2", "3"), results);
    }

    @Test
    void shouldMapUsingEmit() throws Throwable {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3);
        List<Integer> results = new ArrayList<>();

        // when
        Flow<Integer> mapped = flow.mapUsingEmit(i -> emit -> {
            for (int j = 0; j < 2; j++) {
                try {
                    emit.apply(i + j);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        });

        mapped.runForeach(results::add);

        // then
        assertEquals(List.of(1, 2, 2, 3, 3, 4), results);
    }

    @Test
    void shouldFilter() throws Throwable {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3, 4, 5);
        List<Integer> results = new ArrayList<>();

        // when
        flow.filter(i -> i % 2 == 0)
                .runForeach(results::add);

        // then
        assertEquals(List.of(2, 4), results);
    }

    @Test
    void shouldTap() throws Throwable {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3);
        List<Integer> results = new ArrayList<>();

        // when
        flow
                .tap(results::add)
                .map(i -> i * 2)
                .runForeach(j -> {
                });

        // then
        assertEquals(List.of(1, 2, 3), results);
    }

    @Test
    void shouldFlatMap() throws Throwable {
        // given
        Flow<Integer> flow = Flows.fromValues(10, 20, 30);
        List<Integer> results = new ArrayList<>();

        // when
        flow
                .flatMap(i -> Flows.fromValues(i + 1, i + 2))
                .runForeach(results::add);

        // then
        assertEquals(List.of(11, 12, 21, 22, 31, 32), results);
    }

    @Test
    void shouldPropagateErrorFromFlatMap() {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3);

        // when
        Flow<Integer> mapped = flow.flatMap(i -> {
            if (i == 2) {
                throw new RuntimeException("error");
            }
            return Flows.fromValues(i * 2);
        });

        // then
        assertThrows(RuntimeException.class, () -> mapped.runForeach(i -> {}));
    }
}