package com.softwaremill.jox.flows;

import static com.softwaremill.jox.structured.Scopes.supervised;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import com.softwaremill.jox.Channel;
import com.softwaremill.jox.structured.JoxScopeExecutionException;

class FlowBatchWeightedTest {

    @Test
    void shouldPassThroughWhenDownstreamIsReady() throws Exception {
        supervised(
                scope -> {
                    // given
                    Channel<Integer> upstream = Channel.newRendezvousChannel();
                    var consumed = new Semaphore(0);

                    scope.fork(
                            () -> {
                                upstream.send(1);
                                for (int i = 2; i <= 3; i++) {
                                    consumed.acquire();
                                    upstream.send(i);
                                }
                                upstream.done();
                                return null;
                            });

                    // when
                    List<Integer> result =
                            Flows.fromSource(upstream)
                                    .batchWeighted(10L, _ -> 1L, t -> t, Integer::sum)
                                    .tap(_ -> consumed.release())
                                    .runToList();

                    // then
                    assertEquals(List.of(1, 2, 3), result);
                    return null;
                });
    }

    @Test
    void shouldAggregateWhenDownstreamIsBusy() throws Exception {
        supervised(
                scope -> {
                    // given
                    Channel<Integer> upstream = Channel.newRendezvousChannel();
                    var downstreamBlocked = new CountDownLatch(1);
                    var canProceed = new CountDownLatch(1);

                    scope.fork(
                            () -> {
                                upstream.send(1);
                                downstreamBlocked.await();
                                for (int i = 2; i <= 6; i++) upstream.send(i);
                                upstream.done();
                                canProceed.countDown();
                                return null;
                            });

                    var isFirst = new AtomicBoolean(true);

                    // when
                    List<List<Integer>> result =
                            Flows.fromSource(upstream)
                                    .batchWeighted(
                                            10L,
                                            _ -> 1L,
                                            List::of,
                                            (acc, t) -> {
                                                var list = new ArrayList<>(acc);
                                                list.add(t);
                                                return list;
                                            })
                                    .tap(
                                            _ -> {
                                                if (isFirst.compareAndSet(true, false)) {
                                                    downstreamBlocked.countDown();
                                                    canProceed.await();
                                                }
                                            })
                                    .runToList();

                    // then
                    assertEquals(List.of(List.of(1), List.of(2, 3, 4, 5, 6)), result);
                    return null;
                });
    }

    @Test
    void shouldCreateMaximalBatchRespectingCostFunction() throws Exception {
        supervised(
                scope -> {
                    // given
                    Channel<Integer> upstream = Channel.newRendezvousChannel();
                    var downstreamBlocked = new CountDownLatch(1);
                    var canProceed = new CountDownLatch(1);

                    scope.fork(
                            () -> {
                                upstream.send(1);
                                downstreamBlocked.await();
                                // Costs: 2+4=6=maxWeight (fills batch), then 5 exceeds remaining
                                // budget (0), triggering flush
                                for (int i : List.of(2, 4, 5)) upstream.send(i);
                                upstream.done();
                                canProceed.countDown();
                                return null;
                            });

                    var isFirst = new AtomicBoolean(true);

                    // when
                    List<List<Integer>> result =
                            Flows.fromSource(upstream)
                                    .batchWeighted(
                                            6L,
                                            Integer::longValue,
                                            List::of,
                                            (acc, t) -> {
                                                var list = new ArrayList<>(acc);
                                                list.add(t);
                                                return list;
                                            })
                                    .tap(
                                            _ -> {
                                                if (isFirst.compareAndSet(true, false)) {
                                                    downstreamBlocked.countDown();
                                                    canProceed.await();
                                                }
                                            })
                                    .runToList();

                    // then
                    assertEquals(List.of(List.of(1), List.of(2, 4), List.of(5)), result);
                    return null;
                });
    }

    @Test
    void shouldStartNewBatchWhenElementExceedsMaxWeight() throws Exception {
        supervised(
                scope -> {
                    // given
                    Channel<Integer> upstream = Channel.newRendezvousChannel();
                    scope.fork(
                            () -> {
                                for (int i : List.of(10, 20, 30)) upstream.send(i);
                                upstream.done();
                                return null;
                            });

                    // when
                    List<List<Integer>> result =
                            Flows.fromSource(upstream)
                                    .batchWeighted(
                                            3L,
                                            _ -> 5L,
                                            List::of,
                                            (acc, t) -> {
                                                var list = new ArrayList<>(acc);
                                                list.add(t);
                                                return list;
                                            })
                                    .runToList();

                    // then — each element costs 5, exceeding maxWeight=3, so each is its own batch
                    assertEquals(List.of(List.of(10), List.of(20), List.of(30)), result);
                    return null;
                });
    }

    @Test
    void shouldHandleEmptyFlow() throws Exception {
        // when
        List<Integer> result =
                Flows.<Integer>empty()
                        .batchWeighted(10L, _ -> 1L, t -> t, Integer::sum)
                        .runToList();

        // then
        assertEquals(List.of(), result);
    }

    @Test
    void shouldPropagateErrorsFromUpstream() {
        // when & then
        var exception =
                assertThrows(
                        JoxScopeExecutionException.class,
                        () ->
                                Flows.fromValues(1, 2, 3)
                                        .concat(Flows.failed(new IllegalStateException("boom")))
                                        .batchWeighted(10L, _ -> 1L, t -> t, Integer::sum)
                                        .runToList());
        assertInstanceOf(IllegalStateException.class, exception.getCause().getCause());
    }

    @Test
    void shouldPropagateErrorsFromCostFn() {
        // when & then
        var exception =
                assertThrows(
                        JoxScopeExecutionException.class,
                        () ->
                                Flows.fromValues(1, 2, 0, 4)
                                        .batchWeighted(10L, n -> 10L / n, t -> t, Integer::sum)
                                        .runToList());
        assertInstanceOf(ArithmeticException.class, exception.getCause().getCause());
    }

    @Test
    void shouldPropagateErrorsFromSeed() {
        // when & then
        var exception =
                assertThrows(
                        JoxScopeExecutionException.class,
                        () ->
                                Flows.fromValues(1, 2, 3)
                                        .batchWeighted(
                                                10L,
                                                _ -> 1L,
                                                (Integer n) -> {
                                                    if (n == 1)
                                                        throw new IllegalStateException("seed");
                                                    return n;
                                                },
                                                Integer::sum)
                                        .runToList());
        assertInstanceOf(IllegalStateException.class, exception.getCause().getCause());
    }

    @Test
    void shouldPropagateErrorsFromAggregate() throws Exception {
        supervised(
                scope -> {
                    // given
                    Channel<Integer> upstream = Channel.newRendezvousChannel();
                    var downstreamBlocked = new CountDownLatch(1);
                    var canProceed = new CountDownLatch(1);

                    scope.fork(
                            () -> {
                                upstream.send(1);
                                downstreamBlocked.await();
                                try {
                                    upstream.send(2);
                                    upstream.send(3);
                                    upstream.done();
                                } catch (Exception _) {
                                }
                                canProceed.countDown();
                                return null;
                            });

                    var isFirst = new AtomicBoolean(true);

                    // when & then
                    var exception =
                            assertThrows(
                                    JoxScopeExecutionException.class,
                                    () ->
                                            Flows.fromSource(upstream)
                                                    .batchWeighted(
                                                            10L,
                                                            _ -> 1L,
                                                            t -> t,
                                                            (_, _) -> {
                                                                throw new IllegalStateException(
                                                                        "agg");
                                                            })
                                                    .tap(
                                                            _ -> {
                                                                if (isFirst.compareAndSet(
                                                                        true, false)) {
                                                                    downstreamBlocked.countDown();
                                                                    canProceed.await();
                                                                }
                                                            })
                                                    .runToList());
                    assertInstanceOf(IllegalStateException.class, exception.getCause().getCause());
                    return null;
                });
    }
}
