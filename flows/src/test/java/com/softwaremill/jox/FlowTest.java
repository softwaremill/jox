package com.softwaremill.jox;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3);

        // when
        Source<Integer> source = flow.runToChannel();

        // then
        assertEquals(1, source.receive());
        assertEquals(2, source.receive());
        assertEquals(3, source.receive());
    }

    @Test
    void shouldReturnOriginalSourceWhenRunningASourcedBackedFlow() throws Throwable {
        // given
        Channel<Integer> channel = Channel.newUnlimitedChannel();
        Flow<Integer> flow = Flows.fromSource(channel);

        // when
        Source<Integer> receivedChannel = flow.runToChannel();

        // then
        assertEquals(channel, receivedChannel);
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