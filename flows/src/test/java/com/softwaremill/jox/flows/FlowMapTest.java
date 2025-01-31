package com.softwaremill.jox.flows;

import com.softwaremill.jox.Channel;
import com.softwaremill.jox.ChannelError;
import com.softwaremill.jox.Source;
import com.softwaremill.jox.structured.JoxScopeExecutionException;
import com.softwaremill.jox.structured.Scopes;
import com.softwaremill.jox.structured.ThrowingFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class FlowMapTest {

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
                emit.apply(i + j);
            }
        });

        mapped.runForeach(results::add);

        // then
        assertEquals(List.of(1, 2, 2, 3, 3, 4), results);
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
        assertThrows(RuntimeException.class, mapped::runDrain);
    }


    @Test
    void shouldUnfoldIterables() throws Exception {
        // given
        var c = Flows.fromValues(List.of("a", "b", "c"), List.of("d", "e"), List.of("f"));

        // when
        var result = c.mapConcat(v -> v).runToList();

        // then
        assertEquals(List.of("a", "b", "c", "d", "e", "f"), result);
    }

    @Test
    void shouldTransformElements() throws Exception {
        // given
        var flow = Flows.fromValues("ab", "cd");

        // when
        var result = flow.mapConcat(str -> str.chars().mapToObj(c -> (char) c).toList())
                         .runToList();

        // then
        assertEquals(List.of('a', 'b', 'c', 'd'), result);
    }

    @Test
    void shouldHandleEmptyLists() throws Exception {
        // given
        var c = Flows.fromValues(List.of(), List.of("a"), List.of(), List.of("b", "c"));

        // when
        var result = c.mapConcat(a -> a)
                      .runToList();

        // then
        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    void shouldPropagateErrorsInMappingFunction() throws Exception {
        // given
        var flow = Flows.fromValues(List.of("a"), List.of("b", "c"), List.of("error here"));
        RuntimeException boom = new RuntimeException("boom");

        // when & then
        var flow2 = flow.mapConcat(element -> {
            if (!element.equals(List.of("error here"))) return element;
            else throw boom;
        });

        Scopes.supervised(scope -> {
            var s = flow2.runToChannel(scope, 0); // so that the error isn't created too early
            assertEquals("a", s.receive());
            assertEquals("b", s.receive());
            assertEquals("c", s.receive());
            var result = s.receiveOrClosed();
            if (result instanceof ChannelError(Throwable error, Channel<?> _)) {
                assertEquals(boom, error);
            }
            return null;
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    void shouldMapOverFlowWithParallelismLimit(int parallelism) throws InterruptedException {
        Scopes.supervised(scope -> {
            // given
            Flow<Integer> flow = Flows.iterate(1, i -> i + 1).take(10);
            AtomicInteger running = new AtomicInteger(0);
            AtomicInteger maxRunning = new AtomicInteger(0);

            ThrowingFunction<Integer, Integer> f = i -> {
                running.incrementAndGet();
                try {
                    Thread.sleep(100);
                    return i * 2;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    running.decrementAndGet();
                }
            };

            // update max running
            scope.fork(() -> {
                int max = 0;
                while (true) {
                    max = Math.max(max, running.get());
                    maxRunning.set(max);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            // when
            List<Integer> result = flow.mapPar(parallelism, f).runToList();

            // then
            assertEquals(List.of(2, 4, 6, 8, 10, 12, 14, 16, 18, 20), result);
            assertEquals(parallelism, maxRunning.get());
            return null;
        });
    }

    @Test
    void shouldMapOverFlowWithParallelismLimit10StressTest() throws Exception {
        for (int i = 1; i <= 100; i++) {
            if (i % 10 == 0) System.out.println("iteration " + i);

            // given
            Flow<Integer> flow = Flows.iterate(1, v -> v + 1).take(10);

            ThrowingFunction<Integer, Integer> f = (Integer x) -> {
                Thread.sleep(50);
                return x * 2;
            };

            // when
            List<Integer> result = flow.mapPar(10, f).runToList();

            // then
            assertEquals(List.of(2, 4, 6, 8, 10, 12, 14, 16, 18, 20), result);
        }
    }

    @Test
    void mapPar_shouldCancelOtherRunningForksWhenThereIsAnError() throws InterruptedException {
        // given
        RuntimeException boom = new RuntimeException("boom");
        Scopes.supervised(scope -> {
            Queue<String> trail = new ConcurrentLinkedQueue<>();
            Flow<Integer> flow = Flows.iterate(1, v -> v + 1).take(10);

            // when
            Source<Integer> s2 = flow.mapPar(2, (Integer i) -> {
                if (i == 4) {
                    Thread.sleep(100);
                    trail.add("exception");
                    throw boom;
                } else {
                    Thread.sleep(200);
                    trail.add("done");
                    return i * 2;
                }
            }).runToChannel(scope);

            // then
            assertEquals(2, s2.receive());
            assertEquals(4, s2.receive());
            assertEquals(boom, ((ChannelError) s2.receiveOrClosed()).cause().getCause().getCause());

            // checking if the forks aren't left running
            Thread.sleep(200);
            assertEquals(List.of("done", "done", "exception"), new ArrayList<>(trail));
            return null;
        });
    }

    @Test
    void mapPar_shouldPropagateErrors() {
        // given
        Flow<Integer> flow = Flows.iterate(1, v -> v + 1).take(10);
        AtomicInteger started = new AtomicInteger();
        RuntimeException boom = new RuntimeException("boom");

        // when
        Flow<Integer> s2 = flow.mapPar(3, (Integer i) -> {
            started.incrementAndGet();
            if (i > 4) {
                throw boom;
            }
            return i * 2;
        });

        // then
        try {
            s2.runToList();
            Assertions.fail("should have thrown");
        } catch (Exception e) {
            assertEquals(boom, e.getCause().getCause());
            assertThat(started.get(), allOf(
                    greaterThanOrEqualTo(4),
                    lessThanOrEqualTo(7) // 4 successful + at most 3 taking up all the permits
            ));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    void testMapParUnorderedWithParallelism(int parallelism) throws InterruptedException {
        Scopes.supervised(scope -> {
            // given
            Flow<Integer> flow = Flows.iterate(1, i -> i + 1).take(10);
            AtomicInteger running = new AtomicInteger(0);
            AtomicInteger maxRunning = new AtomicInteger(0);

            ThrowingFunction<Integer, Integer> f = i -> {
                running.incrementAndGet();
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                    return i * 2;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    running.decrementAndGet();
                }
            };

            // update max running
            scope.fork(() -> {
                int max = 0;
                while (true) {
                    max = Math.max(max, running.get());
                    maxRunning.set(max);
                    TimeUnit.MILLISECONDS.sleep(10);
                }
            });

            // when
            Set<Integer> result = new HashSet<>(flow.mapParUnordered(parallelism, f).runToList());

            // then
            assertEquals(Set.of(2, 4, 6, 8, 10, 12, 14, 16, 18, 20), result);
            assertEquals(parallelism, maxRunning.get());
            return null;
        });
    }

    @Test
    void testMapParUnorderedWithParallelismLimit10StressTest() throws Exception {
        for (int i = 1; i <= 100; i++) {
            if (i % 10 == 0) System.out.println("iteration " + i);

            // given
            Flow<Integer> flow = Flows.iterate(1, j -> j + 1).take(10);

            ThrowingFunction<Integer, Integer> f = j -> {
                TimeUnit.MILLISECONDS.sleep(50);
                return j * 2;
            };

            // when
            List<Integer> result = flow.mapParUnordered(10, f).runToList();

            // then
            assertEquals(Set.of(2, 4, 6, 8, 10, 12, 14, 16, 18, 20), new HashSet<>(result));
        }
    }

    @Test
    void mapParUnordered_testPropagateErrors() {
        // given
        Flow<Integer> flow = Flows.iterate(1, i -> i + 1).take(10);
        AtomicInteger started = new AtomicInteger();
        RuntimeException boom = new RuntimeException("boom");

        // when
        Flow<Integer> flow2 = flow.mapParUnordered(3, i -> {
            started.incrementAndGet();
            if (i > 4) {
                throw boom;
            }
            return i * 2;
        });

        // then
        var exception = assertThrows(JoxScopeExecutionException.class, flow2::runToList);
        assertEquals(boom, exception.getCause().getCause());
        assertThat(started.get(), allOf(
                greaterThanOrEqualTo(2), // 1 needs to start & finish; 2 other need to start; and then the failing one has to start & proceed
                lessThanOrEqualTo(7) // 4 successful + at most 3 taking up all the permits
        ));
    }

    @Test
    void mapParUnordered_testCompleteRunningForksAndNotStartNewOnesWhenMappingFunctionFails() throws InterruptedException {
        Scopes.supervised(scope -> {
            // given
            Queue<String> trail = new ConcurrentLinkedQueue<>();
            Flow<Integer> flow = Flows.iterate(1, i -> i + 1).take(10);
            RuntimeException boom = new RuntimeException("boom");

            // when
            Flow<Integer> flow2 = flow.mapParUnordered(2, i -> {
                if (i == 4) {
                    TimeUnit.MILLISECONDS.sleep(100);
                    trail.add("exception");
                    throw boom;
                } else {
                    TimeUnit.MILLISECONDS.sleep(200);
                    trail.add("done");
                    return i * 2;
                }
            });

            Source<Integer> s2 = flow2.runToChannel(scope);
            Set<Integer> received = Set.of(s2.receive(), s2.receive());
            ChannelError channelError = (ChannelError) s2.receiveOrClosed();

            // then
            assertEquals(Set.of(2, 4), received);
            assertEquals(boom, channelError.cause().getCause().getCause());
            assertTrue(s2.isClosedForReceive());

            // checking if the forks aren't left running
            TimeUnit.MILLISECONDS.sleep(200);

            // the fork that processes 4 should be interrupted, before adding its trail
            assertEquals(List.of("done", "done", "exception"), new ArrayList<>(trail));
            return null;
        });
    }

    @Test
    void mapParUnordered_testCompleteRunningForksAndNotStartNewOnesWhenUpstreamFails() throws InterruptedException {
        // given
        Queue<String> trail = new ConcurrentLinkedQueue<>();
        Flow<Integer> flow = Flows.fromValues(1, 2, 3).concat(Flows.failed(new IllegalStateException()));

        // when
        Flow<Integer> flow2 = flow.mapParUnordered(2, i -> {
            TimeUnit.MILLISECONDS.sleep(100);
            trail.add(Integer.toString(i));
            return i * 2;
        });

        // then
        var exception = assertThrows(JoxScopeExecutionException.class, flow2::runToList);
        assertInstanceOf(IllegalStateException.class, exception.getCause().getCause());

        // checking if the forks aren't left running
        TimeUnit.MILLISECONDS.sleep(200);

        assertThat(new HashSet<>(trail), anyOf(
                is(Set.of("1", "2")),
                is(Set.of("1")),
                is(Set.of("1", "2", "3"))
        ));
    }

    @Test
    void mapParUnordered_testCancelRunningForksWhenSurroundingScopeClosesDueToError() throws InterruptedException {
        Scopes.supervised(scope -> {
            // given
            RuntimeException boom = new RuntimeException("boom");
            Queue<String> trail = new ConcurrentLinkedQueue<>();
            Flow<Integer> flow = Flows.iterate(1, i -> i + 1).take(10);

            // when
            Flow<Integer> flow2 = flow.mapParUnordered(2, i -> {
                if (i == 4) {
                    TimeUnit.MILLISECONDS.sleep(100);
                    trail.add("exception");
                    throw boom;
                } else {
                    TimeUnit.MILLISECONDS.sleep(200);
                    trail.add("done");
                    return i * 2;
                }
            });

            Source<Integer> s2 = flow2.runToChannel(scope);
            Set<Integer> received = Set.of(s2.receive(), s2.receive());
            ChannelError errorOrClosed = (ChannelError) s2.receiveOrClosed();

            // then
            assertEquals(Set.of(2, 4), received);
            assertEquals(boom, errorOrClosed.cause().getCause().getCause());
            assertTrue(s2.isClosedForReceive());

            // wait for all threads to finish
            Thread.sleep(200);
            assertEquals(List.of("done", "done", "exception"), new ArrayList<>(trail));
            return null;
        });
    }

    @Test
    void mapParUnordered_testEmitDownstreamAsSoonAsValueIsReadyRegardlessOfIncomingOrder() throws Exception {
        // given
        Flow<Integer> flow = Flows.iterate(1, i -> i + 1).take(5);
        Map<Integer, Long> delays = Map.of(
                1, 100L,
                2, 10L,
                3, 50L,
                4, 500L,
                5, 200L
        );

        // when
        Flow<Integer> flow2 = flow.mapParUnordered(5, i -> {
            TimeUnit.MILLISECONDS.sleep(delays.get(i));
            return i;
        });
        List<Integer> result = flow2.runToList();

        // then
        assertEquals(delays.keySet(), new HashSet<>(result));
    }

    @Test
    void mapStatefulConcat_shouldDeduplicate() throws Exception {
        // given
        Flow<Integer> c = Flows.fromValues(1, 2, 2, 3, 2, 4, 3, 1, 5);

        // when
        Flow<Integer> flow = c.mapStatefulConcat(() -> new HashSet<Integer>(), (s, e) -> {
            List<Integer> downStream = s.add(e) ? List.of(e) : List.of();
            return Map.entry(s, downStream);
        });

        // then
        assertEquals(List.of(1, 2, 3, 4, 5), flow.runToList());
    }

    @Test
    void mapStateful_shouldCountConsecutive() throws Exception {
        // given
        Flow<String> flow = Flows.fromValues("apple", "apple", "apple", "banana", "orange", "orange", "apple");

        // when
        Flow<Map.Entry<String, Integer>> s = flow.mapStatefulConcat(
                () -> Map.entry(Optional.<String>empty(), 0),
                (state, e) -> {
                    Optional<String> previous = state.getKey();
                    int count = state.getValue();
                    if (previous.isEmpty()) {
                        return Map.entry(Map.entry(Optional.of(e), 1), Collections.emptyList());
                    } else if (previous.get().equals(e)) {
                        return Map.entry(Map.entry(previous, count + 1), Collections.emptyList());
                    } else {
                        return Map.entry(Map.entry(Optional.of(e), 1), List.of(Map.entry(previous.get(), count)));
                    }
                },
                state1 -> state1.getKey().map(v -> Map.entry(v, state1.getValue())));

        // then
        assertEquals(List.of(
                Map.entry("apple", 3),
                Map.entry("banana", 1),
                Map.entry("orange", 2),
                Map.entry("apple", 1)
        ), s.runToList());
    }

    @Test
    void mapStatefulConcat_shouldPropagateErrorsInMappingFunction() throws Exception {
        // given
        Flow<String> flow = Flows.fromValues("a", "b", "c");

        // when
        Flow<String> flow2 = flow.mapStatefulConcat(() -> 0, (index, element) -> {
            if (index < 2) {
                return Map.entry(index + 1, List.of(element));
            } else {
                throw new RuntimeException("boom");
            }
        });

        // then
        Scopes.supervised(scope -> {
            Source<String> c = flow2.runToChannel(scope, 0); // buffer capacity = 0 so that the error isn't created too early
            assertEquals("a", c.receive());
            assertEquals("b", c.receive());
            ChannelError channelError = (ChannelError) c.receiveOrClosed();
            assertEquals("boom", channelError.cause().getMessage());
            return null;
        });
    }

    @Test
    void mapStatefulConcat_shouldPropagateErrorsInCompletionCallback() throws Exception {
        // given
        Flow<String> flow = Flows.fromValues("a", "b", "c");

        // when
        Flow<String> flow2 = flow.mapStatefulConcat(() -> 0, (index, element) -> Map.entry(index + 1, List.of(element)), _ -> {
            throw new RuntimeException("boom");
        });

        // then
        Scopes.supervised(scope -> {
            Source<String> c = flow2.runToChannel(scope, 0); // buffer capacity = 0 so that the error isn't created too early
            assertEquals("a", c.receive());
            assertEquals("b", c.receive());
            assertEquals("c", c.receive());

            ChannelError channelError = (ChannelError) c.receiveOrClosed();
            assertEquals("boom", channelError.cause().getMessage());
            return null;
        });
    }

    @Test
    void mapStateful_shouldZipWithIndex() throws Exception {
        // given
        var flow = Flows.fromValues("a", "b", "c")
                        .mapStateful(() -> 0, (index, element) -> Map.entry(index + 1, Map.entry(element, index)));

        // when & then
        assertEquals(List.of(Map.entry("a", 0), Map.entry("b", 1), Map.entry("c", 2)), flow.runToList());
    }

    @Test
    void mapStateful_shouldCalculateRunningTotal() throws Exception {
        // given
        var flow = Flows.fromValues(1, 2, 3, 4, 5)
                        .mapStateful(() -> 0, (sum, element) -> Map.entry(sum + element, sum), Optional::of);

        // when & then
        assertEquals(List.of(0, 1, 3, 6, 10, 15), flow.runToList());
    }

    @Test
    void mapStateful_shouldEmitDifferentValuesThanIncomingOnes() throws Exception {
        // given
        var flow = Flows.fromValues(1, 2, 3, 4, 5)
                        .mapStateful(() -> 0, (sum, element) -> Map.entry(sum + element, Integer.toString(sum)), n -> Optional.of(Integer.toString(n)));

        // when & then
        assertEquals(List.of("0", "1", "3", "6", "10", "15"), flow.runToList());
    }

    @Test
    void mapStateful_shouldPropagateErrorsInMappingFunction() throws Exception {
        // given
        var flow = Flows.fromValues("a", "b", "c")
                        .mapStateful(() -> 0, (index, element) -> {
                            if (index < 2) return Map.entry(index + 1, element);
                            else throw new RuntimeException("boom");
                        });

        // when & then
        Scopes.supervised(scope -> {
            Source<String> s = flow.runToChannel(scope, 0);
            assertEquals("a", s.receive());
            assertEquals("b", s.receive());
            ChannelError closed = (ChannelError) s.receiveOrClosed();
            assertEquals("boom", closed.cause().getMessage());
            return null;
        });
    }

    @Test
    void mapStateful_shouldPropagateErrorsInCompletionCallback() throws Exception {
        // given
        var flow = Flows.fromValues("a", "b", "c")
                        .mapStateful(() -> 0, (index, element) -> Map.entry(index + 1, element), _ -> {
                            throw new RuntimeException("boom");
                        });

        // when & then
        Scopes.supervised(scope -> {
            Source<String> s = flow.runToChannel(scope, 0);
            assertEquals("a", s.receive());
            assertEquals("b", s.receive());
            assertEquals("c", s.receive());
            ChannelError closed = (ChannelError) s.receiveOrClosed();
            assertEquals("boom", closed.cause().getMessage());
            return null;
        });
    }
}
