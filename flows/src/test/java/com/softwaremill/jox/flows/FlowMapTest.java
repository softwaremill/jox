package com.softwaremill.jox.flows;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import com.softwaremill.jox.ChannelError;
import com.softwaremill.jox.ChannelErrorException;
import com.softwaremill.jox.Source;
import com.softwaremill.jox.structured.Scopes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
            if (result instanceof ChannelError(Throwable error)) {
                assertEquals(boom, error);
            }
            return null;
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    void shouldMapOverFlowWithParallelismLimit(int parallelism) throws ExecutionException, InterruptedException {
        Scopes.supervised(scope -> {
            // given
            Flow<Integer> flow = Flows.iterate(1, i -> i + 1).take(10);
            AtomicInteger running = new AtomicInteger(0);
            AtomicInteger maxRunning = new AtomicInteger(0);

            Function<Integer, Integer> f = i -> {
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

            Function<Integer, Integer> f = (Integer x) -> {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return x * 2;
            };

            // when
            List<Integer> result = flow.mapPar(10, f).runToList();

            // then
            assertEquals(List.of(2, 4, 6, 8, 10, 12, 14, 16, 18, 20), result);
        }
    }

    @Test
    void mapPar_shouldCancelOtherRunningForksWhenThereIsAnError() throws ExecutionException, InterruptedException {
        // given
        RuntimeException boom = new RuntimeException("boom");
        Scopes.supervised(scope -> {
            Queue<String> trail = new ConcurrentLinkedQueue<>();
            Flow<Integer> flow = Flows.iterate(1, v -> v + 1).take(10);

            // when
            Source<Integer> s2 = flow.mapPar(2, (Integer i) -> {
                try {
                    if (i == 4) {
                        Thread.sleep(100);
                        trail.add("exception");
                        throw boom;
                    } else {
                        Thread.sleep(200);
                        trail.add("done");
                        return i * 2;
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).runToChannel(scope);

            // then
            assertEquals(2, s2.receive());
            assertEquals(4, s2.receive());
            assertEquals(boom, ((ChannelError) s2.receiveOrClosed()).cause().getCause());

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
            assertEquals(boom, e.getCause());
            assertThat(started.get(), allOf(
                    greaterThanOrEqualTo(4),
                    lessThanOrEqualTo(7) // 4 successful + at most 3 taking up all the permits
            ));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    void testMapParUnorderedWithParallelism(int parallelism) throws ExecutionException, InterruptedException {
        Scopes.supervised(scope -> {
            // given
            Flow<Integer> flow = Flows.iterate(1, i -> i + 1).take(10);
            AtomicInteger running = new AtomicInteger(0);
            AtomicInteger maxRunning = new AtomicInteger(0);

            Function<Integer, Integer> f = i -> {
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

            Function<Integer, Integer> f = j -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                    return j * 2;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
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
        Exception exception = assertThrows(ChannelErrorException.class, flow2::runToList);
        assertEquals(boom, exception.getCause());
        assertThat(started.get(), allOf(
                greaterThanOrEqualTo(2), // 1 needs to start & finish; 2 other need to start; and then the failing one has to start & proceed
                lessThanOrEqualTo(7) // 4 successful + at most 3 taking up all the permits
        ));
    }

    @Test
    void mapParUnordered_testCompleteRunningForksAndNotStartNewOnesWhenMappingFunctionFails() throws InterruptedException, ExecutionException {
        Scopes.supervised(scope -> {
            // given
            Queue<String> trail = new ConcurrentLinkedQueue<>();
            Flow<Integer> flow = Flows.iterate(1, i -> i + 1).take(10);
            RuntimeException boom = new RuntimeException("boom");

            // when
            Flow<Integer> flow2 = flow.mapParUnordered(2, i -> {
                try {
                    if (i == 4) {
                        TimeUnit.MILLISECONDS.sleep(100);
                        trail.add("exception");
                        throw boom;
                    } else {
                        TimeUnit.MILLISECONDS.sleep(200);
                        trail.add("done");
                        return i * 2;
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            Source<Integer> s2 = flow2.runToChannel(scope);
            Set<Integer> received = Set.of(s2.receive(), s2.receive());
            ChannelError channelError = (ChannelError) s2.receiveOrClosed();

            // then
            assertEquals(Set.of(2, 4), received);
            assertEquals(boom, channelError.cause().getCause());
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
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            trail.add(Integer.toString(i));
            return i * 2;
        });

        // then
        ChannelErrorException exception = assertThrows(ChannelErrorException.class, flow2::runToList);
        assertInstanceOf(IllegalStateException.class, exception.getCause());

        // checking if the forks aren't left running
        TimeUnit.MILLISECONDS.sleep(200);

        assertThat(new HashSet<>(trail), anyOf(
                is(Set.of("1", "2")),
                is(Set.of("1")),
                is(Set.of("1", "2", "3"))
        ));
    }

    @Test
    void mapParUnordered_testCancelRunningForksWhenSurroundingScopeClosesDueToError() throws InterruptedException, ExecutionException {
        Scopes.supervised(scope -> {
            // given
            RuntimeException boom = new RuntimeException("boom");
            Queue<String> trail = new ConcurrentLinkedQueue<>();
            Flow<Integer> flow = Flows.iterate(1, i -> i + 1).take(10);

            // when
            Flow<Integer> flow2 = flow.mapParUnordered(2, i -> {
                try {
                    if (i == 4) {
                        TimeUnit.MILLISECONDS.sleep(100);
                        trail.add("exception");
                        throw boom;
                    } else {
                        TimeUnit.MILLISECONDS.sleep(200);
                        trail.add("done");
                        return i * 2;
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            Source<Integer> s2 = flow2.runToChannel(scope);
            Set<Integer> received = Set.of(s2.receive(), s2.receive());
            ChannelError errorOrClosed = (ChannelError) s2.receiveOrClosed();

            // then
            assertEquals(Set.of(2, 4), received);
            assertEquals(boom, errorOrClosed.cause().getCause());
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
            try {
                TimeUnit.MILLISECONDS.sleep(delays.get(i));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return i;
        });
        List<Integer> result = flow2.runToList();

        // then
        assertEquals(delays.keySet(), new HashSet<>(result));
    }
}
