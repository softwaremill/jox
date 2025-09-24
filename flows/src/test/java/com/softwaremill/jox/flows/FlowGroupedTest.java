package com.softwaremill.jox.flows;

import static com.softwaremill.jox.structured.Race.timeout;
import static com.softwaremill.jox.structured.Scopes.supervised;
import static java.lang.Thread.sleep;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.softwaremill.jox.Channel;
import com.softwaremill.jox.ChannelClosedException;
import com.softwaremill.jox.ChannelError;
import com.softwaremill.jox.ChannelErrorException;
import com.softwaremill.jox.structured.JoxScopeExecutionException;

public class FlowGroupedTest {

    @Test
    void shouldEmitGroupedElements() throws Exception {
        // when
        List<List<Integer>> result = Flows.fromValues(1, 2, 3, 4, 5, 6).grouped(3).runToList();

        // then
        assertEquals(List.of(List.of(1, 2, 3), List.of(4, 5, 6)), result);
    }

    @Test
    void shouldEmitGroupedElementsAndIncludeRemainingValuesWhenFlowCloses() throws Exception {
        // given
        List<List<Integer>> result = Flows.fromValues(1, 2, 3, 4, 5, 6, 7).grouped(3).runToList();

        // then
        assertEquals(List.of(List.of(1, 2, 3), List.of(4, 5, 6), List.of(7)), result);
    }

    @Test
    void shouldReturnFailedFlowWhenTheOriginalFlowIsFailed() throws InterruptedException {
        supervised(
                scope -> {
                    // given
                    RuntimeException failure = new RuntimeException();

                    // when
                    Object result =
                            Flows.failed(failure).grouped(3).runToChannel(scope).receiveOrClosed();

                    // then
                    assertInstanceOf(ChannelError.class, result);
                    assertEquals(failure, ((ChannelError) result).cause());
                    return null;
                });
    }

    @Test
    void shouldEmitGroupedElementsWithCustomCostFunction() throws Exception {
        // when
        List<List<Integer>> result =
                Flows.fromValues(1, 2, 3, 4, 5, 6, 5, 3, 1)
                        .groupedWeighted(10, n -> (long) (n * 2))
                        .runToList();

        // then
        assertEquals(
                List.of(List.of(1, 2, 3), List.of(4, 5), List.of(6), List.of(5), List.of(3, 1)),
                result);
    }

    @Test
    void shouldReturnFailedFlowWhenCostFunctionThrowsException() throws InterruptedException {
        supervised(
                scope -> {
                    // when
                    ChannelClosedException exception =
                            assertThrows(
                                    ChannelClosedException.class,
                                    () ->
                                            Flows.fromValues(1, 2, 3, 0, 4, 5, 6, 7)
                                                    .groupedWeighted(150, n -> (long) (100 / n))
                                                    .runToChannel(scope)
                                                    .forEach(_ -> {}));

                    // then
                    assertInstanceOf(ArithmeticException.class, exception.getCause());
                    return null;
                });
    }

    @Test
    void groupedWeighted_shouldReturnFailedSourceWhenTheOriginalSourceIsFailed()
            throws InterruptedException {
        supervised(
                scope -> {
                    // given
                    RuntimeException failure = new RuntimeException();

                    // when
                    Object result =
                            Flows.failed(failure)
                                    .groupedWeighted(10, n -> Long.parseLong(n.toString()) * 2)
                                    .runToChannel(scope)
                                    .receiveOrClosed();

                    // then
                    assertInstanceOf(ChannelError.class, result);
                    assertEquals(failure, ((ChannelError) result).cause());
                    return null;
                });
    }

    @Test
    void groupedWithin_shouldGroupFirstBatchOfElementsDueToLimitAndSecondBatchDueToTimeout()
            throws InterruptedException {
        supervised(
                scope -> {
                    // given
                    var c = Channel.<Integer>newUnlimitedChannel();
                    var start = System.nanoTime();
                    scope.fork(
                            () -> {
                                c.send(1);
                                c.send(2);
                                c.send(3);
                                sleep(Duration.ofMillis(50));
                                c.send(4);
                                sleep(
                                        Duration.ofMillis(
                                                200)); // to ensure the timeout is executed before
                                // the channel closes
                                c.done();
                                return null;
                            });

                    // when
                    var elementsWithEmittedTimeOffset =
                            Flows.fromSource(c)
                                    .groupedWithin(3, Duration.ofMillis(100))
                                    .map(
                                            s ->
                                                    Map.entry(
                                                            s,
                                                            Duration.ofNanos(
                                                                    System.nanoTime() - start)))
                                    .runToList();

                    // then
                    assertEquals(
                            List.of(List.of(1, 2, 3), List.of(4)),
                            elementsWithEmittedTimeOffset.stream().map(Map.Entry::getKey).toList());
                    // first batch is emitted immediately as it fills up
                    Duration firstBatchTime = elementsWithEmittedTimeOffset.get(0).getValue();
                    assertThat(firstBatchTime, lessThan(Duration.ofMillis(50)));

                    // second batch is emitted after 100ms timeout after 50ms sleep after the first
                    // batch
                    Duration secondBatchTime = elementsWithEmittedTimeOffset.get(1).getValue();
                    assertThat(secondBatchTime, greaterThanOrEqualTo(Duration.ofMillis(100)));
                    return null;
                });
    }

    @Test
    void groupedWithin_shouldGroupFirstBatchOfElementsDueToTimeoutAndSecondBatchDueToLimit()
            throws InterruptedException {
        supervised(
                scope -> {
                    // given
                    var c = Channel.<Integer>newUnlimitedChannel();
                    var start = System.nanoTime();
                    scope.fork(
                            () -> {
                                c.send(1);
                                c.send(2);
                                sleep(Duration.ofMillis(150));
                                c.send(3);
                                c.send(4);
                                c.send(5);
                                c.done();
                                return null;
                            });

                    // when
                    var elementsWithEmittedTimeOffset =
                            Flows.fromSource(c)
                                    .groupedWithin(3, Duration.ofMillis(100))
                                    .map(
                                            s ->
                                                    Map.entry(
                                                            s,
                                                            Duration.ofNanos(
                                                                    System.nanoTime() - start)))
                                    .runToList();

                    // then
                    assertEquals(
                            List.of(List.of(1, 2), List.of(3, 4, 5)),
                            elementsWithEmittedTimeOffset.stream().map(Map.Entry::getKey).toList());
                    // first batch is emitted after 100ms timeout
                    Duration firstBatchTime = elementsWithEmittedTimeOffset.get(0).getValue();
                    assertThat(
                            firstBatchTime,
                            allOf(
                                    greaterThanOrEqualTo(Duration.ofMillis(100)),
                                    lessThan(Duration.ofMillis(150))));

                    // second batch is emitted immediately after 200ms sleep
                    Duration secondBatchTime = elementsWithEmittedTimeOffset.get(1).getValue();
                    assertThat(secondBatchTime, greaterThanOrEqualTo(Duration.ofMillis(150)));

                    return null;
                });
    }

    @Test
    void
            groupedWithin_shouldWakeUpOnNewElementAndSendItImmediatelyAfterFirstBatchIsSentAndChannelGoesToTimeoutMode()
                    throws InterruptedException {
        supervised(
                scope -> {
                    // given
                    var c = Channel.<Integer>newUnlimitedChannel();
                    var start = System.nanoTime();
                    scope.fork(
                            () -> {
                                c.send(1);
                                c.send(2);
                                c.send(3);
                                sleep(Duration.ofMillis(200));
                                c.send(3);
                                sleep(
                                        Duration.ofMillis(
                                                200)); // to ensure the timeout is executed before
                                // the channel closes
                                c.done();
                                return null;
                            });

                    // when
                    var elementsWithEmittedTimeOffset =
                            Flows.fromSource(c)
                                    .groupedWithin(3, Duration.ofMillis(100))
                                    .map(
                                            s ->
                                                    new AbstractMap.SimpleEntry<>(
                                                            s,
                                                            Duration.ofNanos(
                                                                    System.nanoTime() - start)))
                                    .runToList();

                    // then
                    assertEquals(
                            List.of(List.of(1, 2, 3), List.of(3)),
                            elementsWithEmittedTimeOffset.stream().map(Map.Entry::getKey).toList());

                    // first batch is emitted immediately as it fills up
                    Duration firstBatchTime = elementsWithEmittedTimeOffset.get(0).getValue();
                    assertThat(firstBatchTime, lessThan(Duration.ofMillis(50)));

                    // second batch is emitted immediately after 100ms timeout after 200ms sleep
                    Duration secondBatchTime = elementsWithEmittedTimeOffset.get(1).getValue();
                    assertThat(
                            secondBatchTime,
                            allOf(
                                    greaterThanOrEqualTo(Duration.ofMillis(200)),
                                    lessThan(Duration.ofMillis(250))));
                    return null;
                });
    }

    @Test
    void groupedWithin_shouldSendTheGroupOnlyOnceWhenTheChannelIsClosed()
            throws InterruptedException {
        supervised(
                scope -> {
                    // given
                    var c = Channel.<Integer>newUnlimitedChannel();
                    scope.fork(
                            () -> {
                                c.send(1);
                                c.send(2);
                                c.done();
                                return null;
                            });

                    // when
                    List<List<Integer>> result =
                            timeout(
                                    Duration.ofSeconds(2).toMillis(),
                                    () ->
                                            Flows.fromSource(c)
                                                    .groupedWithin(3, Duration.ofMinutes(5))
                                                    .runToList());

                    // then
                    assertEquals(List.of(List.of(1, 2)), result);
                    return null;
                });
    }

    @Test
    void groupedWithin_shouldReturnFailedSourceWhenTheOriginalSourceIsFailed()
            throws InterruptedException {
        supervised(
                scope -> {
                    // given
                    var failure = new RuntimeException();
                    Flow<List<Object>> flow =
                            Flows.failed(failure).groupedWithin(3, Duration.ofSeconds(10));

                    // when
                    ChannelError result = (ChannelError) flow.runToChannel(scope).receiveOrClosed();

                    // then
                    assertEquals(failure, result.cause().getCause().getCause());
                    return null;
                });
    }

    @Test
    void
            groupedWeightedWithin_shouldGroupElementsOnTimeoutInFirstBatchAndConsiderMaxWeightInRemainingBatches()
                    throws InterruptedException {
        supervised(
                scope -> {
                    // given
                    var c = Flow.<Integer>newChannelWithBufferSizeFromScope();
                    scope.fork(
                            () -> {
                                c.send(1);
                                c.send(2);
                                sleep(Duration.ofMillis(150));
                                c.send(3);
                                c.send(4);
                                c.send(5);
                                c.send(6);
                                c.done();
                                return null;
                            });

                    Flow<List<Integer>> flow =
                            Flows.fromSource(c)
                                    .groupedWeightedWithin(
                                            10, Duration.ofMillis(100), n -> (long) (n * 2));

                    // when
                    var result = flow.runToList();

                    // then
                    assertEquals(
                            List.of(List.of(1, 2), List.of(3, 4), List.of(5), List.of(6)), result);
                    return null;
                });
    }

    @Test
    void groupedWeightedWithin_shouldReturnFailedSourceWhenCostFunctionThrowsException()
            throws InterruptedException {
        supervised(
                scope -> {
                    // given
                    Flow<List<Integer>> flow =
                            Flows.fromValues(1, 2, 3, 0, 4, 5, 6, 7)
                                    .groupedWeightedWithin(
                                            150, Duration.ofMillis(100), n -> (long) (100 / n));

                    // when
                    ChannelErrorException exception =
                            assertThrows(
                                    ChannelErrorException.class,
                                    () -> flow.runToChannel(scope).forEach(_ -> {}));

                    // then
                    assertInstanceOf(
                            ArithmeticException.class, exception.getCause().getCause().getCause());
                    return null;
                });
    }

    @Test
    void groupedWeightedWithin_shouldReturnFailedSourceWhenOriginalSourceIsFailed()
            throws InterruptedException {
        supervised(
                scope -> {
                    // given
                    var failure = new RuntimeException();

                    // when
                    var result =
                            Flows.<Integer>failed(failure)
                                    .groupedWeightedWithin(
                                            10, Duration.ofMillis(100), n -> (long) (n * 2))
                                    .runToChannel(scope)
                                    .receiveOrClosed();

                    // then
                    assertEquals(ChannelError.class, result.getClass());
                    assertEquals(failure, ((ChannelError) result).cause().getCause().getCause());
                    return null;
                });
    }

    @Test
    void groupBy_shouldHandleEmptyFlow() throws Exception {
        // when
        List<Integer> result =
                Flows.<Integer>empty().groupBy(10, i -> i % 10, _ -> f -> f).runToList();

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void groupBy_shouldHandleSingleElementFlow() throws Exception {
        // when
        List<Integer> result =
                Flows.fromValues(42).groupBy(10, i -> i % 10, _ -> f -> f).runToList();

        // then
        assertEquals(List.of(42), result);
    }

    @Test
    void groupBy_shouldHandleSingleElementFlowStressTest() throws Exception {
        // Stress test to validate the data race fix - run the same single-element flow grouping
        // many times
        for (int i = 0; i < 100000; i++) {
            List<Integer> result =
                    Flows.fromValues(42).groupBy(10, x -> x % 10, _ -> f -> f).runToList();
            assertEquals(List.of(42), result);
        }
    }

    @Test
    void groupBy_shouldCreateSimpleGroupsWithoutReachingParallelismLimit() throws Exception {
        // given
        record Group(int v, List<Integer> values) {}

        Flow.StatefulMapper<Integer, Group, Iterable<Group>> mapper =
                (g, i) -> {
                    var newValues = new ArrayList<>(g.values);
                    newValues.add(i);
                    return Map.entry(new Group(g.v, newValues), Collections.emptyList());
                };

        // when
        var result =
                new HashSet<>(
                        Flows.fromValues(10, 11, 12, 13, 20, 23, 33, 30)
                                .groupBy(
                                        10,
                                        i -> i % 10,
                                        v ->
                                                f ->
                                                        f.mapStatefulConcat(
                                                                () -> new Group(v, List.of()),
                                                                mapper,
                                                                Optional::of))
                                .runToList());

        // then
        assertEquals(
                Set.of(
                        new Group(0, List.of(10, 20, 30)),
                        new Group(1, List.of(11)),
                        new Group(2, List.of(12)),
                        new Group(3, List.of(13, 23, 33))),
                result);
    }

    @Test
    void groupBy_shouldCompleteGroupsWhenParallelismLimitIsReached() throws Exception {
        // given
        record Group(int v, List<Integer> values) {}

        Flow.StatefulMapper<Integer, Group, Iterable<Group>> mapper =
                (g, i) -> {
                    var newValues = new ArrayList<>(g.values);
                    newValues.add(i);
                    return Map.entry(new Group(g.v, newValues), Collections.emptyList());
                };

        // when
        List<Group> result =
                Flows.fromValues(10, 11, 12, 13, 20, 23, 33, 30)
                        .groupBy(
                                1,
                                i -> i % 10,
                                v ->
                                        f ->
                                                f.mapStatefulConcat(
                                                        () -> new Group(v, List.of()),
                                                        mapper,
                                                        Optional::of))
                        .runToList()
                        .stream()
                        .map(g -> new Group(g.v, g.values.stream().sorted().toList()))
                        .toList();

        // then
        assertEquals(
                List.of(
                        new Group(0, List.of(10)),
                        new Group(1, List.of(11)),
                        new Group(2, List.of(12)),
                        new Group(3, List.of(13)),
                        new Group(0, List.of(20)),
                        new Group(3, List.of(23, 33)),
                        new Group(0, List.of(30))),
                result);
    }

    @Test
    void
            groupBy_shouldNotExceedParallelismLimitCompletingEarliestActiveChildFlowsAsDoneWhenNecessary()
                    throws Exception {
        // given
        record Group(int v, List<Integer> values) {}

        Flow.StatefulMapper<Integer, Group, Iterable<Group>> mapper =
                (g, i) -> {
                    var newValues = new ArrayList<>(g.values);
                    newValues.add(i);
                    return Map.entry(new Group(g.v, newValues), Collections.emptyList());
                };

        // when
        Set<Group> result =
                new HashSet<>(
                        Flows.fromValues(10, 11, 12, 22, 21, 20, 32, 13, 42, 30, 23, 31)
                                .groupBy(
                                        3,
                                        i -> i % 10,
                                        v ->
                                                f ->
                                                        f.mapStatefulConcat(
                                                                () -> new Group(v, List.of()),
                                                                mapper,
                                                                Optional::of))
                                .runToList());

        // then
        assertEquals(
                Set.of(
                        new Group(0, List.of(10, 20, 30)),
                        new Group(1, List.of(11, 21)),
                        new Group(1, List.of(31)),
                        new Group(2, List.of(12, 22, 32, 42)),
                        new Group(3, List.of(13, 23))),
                result);
    }

    @Test
    void groupBy_shouldHandleLargeFlows() throws Exception {
        // given
        record Group(int v, List<Integer> values) {}

        Flow.StatefulMapper<Integer, Group, Iterable<Group>> mapper =
                (g, i) -> {
                    var newValues = new ArrayList<>(g.values);
                    newValues.add(i);
                    return Map.entry(new Group(g.v, newValues), Collections.emptyList());
                };

        // when
        var input = IntStream.rangeClosed(1, 100000).boxed().toList();
        List<Group> result =
                Flows.fromIterable(input)
                        .groupBy(
                                100,
                                i -> i % 100,
                                v ->
                                        f ->
                                                f.mapStatefulConcat(
                                                        () -> new Group(v, List.of()),
                                                        mapper,
                                                        Optional::of))
                        .runToList();

        // then
        assertEquals(100, result.size());
        assertEquals(
                input.stream().mapToInt(Integer::intValue).sum(),
                result.stream()
                        .mapToInt(g -> g.values.stream().mapToInt(Integer::intValue).sum())
                        .sum());
    }

    @Test
    void groupBy_shouldHandleNonIntegerGroupingKeys() throws Exception {
        // given
        record Group(String v, List<Integer> values) {}

        Flow.StatefulMapper<Integer, Group, Iterable<Group>> mapper =
                (g, i) -> {
                    var newValues = new ArrayList<>(g.values);
                    newValues.add(i);
                    return Map.entry(new Group(g.v, newValues), Collections.emptyList());
                };

        // when
        Set<Group> result =
                new HashSet<>(
                        Flows.fromValues(10, 11, 12, 13, 20, 23, 33, 30)
                                .groupBy(
                                        10,
                                        i -> i % 2 == 0 ? "even" : "odd",
                                        v ->
                                                f ->
                                                        f.mapStatefulConcat(
                                                                () -> new Group(v, List.of()),
                                                                mapper,
                                                                Optional::of))
                                .runToList());

        // then
        assertEquals(
                Set.of(
                        new Group("even", List.of(10, 12, 20, 30)),
                        new Group("odd", List.of(11, 13, 23, 33))),
                result);
    }

    @Test
    void groupBy_shouldGroupWhenChildProcessingIsSlow() throws Exception {
        // given
        var input = IntStream.rangeClosed(1, 30).boxed().toList();

        // when
        List<Integer> result =
                Flows.fromIterable(input)
                        .groupBy(
                                1,
                                _ -> 0,
                                _ ->
                                        f ->
                                                f.tap(
                                                        _ -> {
                                                            // the number of elements exceeds the
                                                            // buffer, the parent will get blocked
                                                            sleep(Duration.ofMillis(10));
                                                        }))
                        .runToList();

        // then
        assertEquals(input, result);
    }

    @Test
    void groupBy_shouldPropagateErrorsFromChildFlows() {
        var exception =
                assertThrows(
                        JoxScopeExecutionException.class,
                        () ->
                                Flows.fromValues(10, 11, 12, 13, 20, 23, 33, 30)
                                        .groupBy(
                                                10,
                                                i -> i % 10,
                                                _ ->
                                                        f ->
                                                                f.tap(
                                                                        i -> {
                                                                            if (i == 13)
                                                                                throw new RuntimeException(
                                                                                        "boom!");
                                                                        }))
                                        .runToList());
        assertEquals("boom!", exception.getCause().getCause().getMessage());
    }

    @Test
    void groupBy_shouldPropagateErrorsFromChildFlowsWhenParentIsBlockedOnSending() {
        var exception =
                assertThrows(
                        JoxScopeExecutionException.class,
                        () -> {
                            Flows.fromValues(
                                            IntStream.rangeClosed(1, 100)
                                                    .boxed()
                                                    .toArray(Integer[]::new))
                                    .groupBy(
                                            1,
                                            _ -> 0,
                                            _ ->
                                                    f ->
                                                            f.tap(
                                                                    _ -> {
                                                                        sleep(
                                                                                Duration.ofMillis(
                                                                                        100));
                                                                        throw new RuntimeException(
                                                                                "boom!");
                                                                    }))
                                    .runToList();
                        });
        assertEquals("boom!", exception.getCause().getCause().getMessage());
    }

    @Test
    void groupBy_shouldPropagateRuntimeExceptionErrorsFromParentFlows() {
        var exception =
                assertThrows(
                        JoxScopeExecutionException.class,
                        () -> {
                            Flows.fromValues(10, 11, 12, 13, 20, 23, 33, 30)
                                    .concat(Flows.failed(new RuntimeException("boom!")))
                                    .groupBy(10, i -> i % 10, _ -> f -> f)
                                    .runToList();
                        });
        assertEquals("boom!", exception.getCause().getCause().getMessage());
    }

    @Test
    void
            groupBy_shouldThrowIllegalStateExceptionWhenChildStreamIsCompletedByUserProvidedTransformation() {
        assertThrows(
                JoxScopeExecutionException.class,
                () ->
                        Flows.fromValues(10, 20, 30)
                                .tap(_ -> sleep(Duration.ofMillis(100)))
                                .groupBy(10, i -> i % 10, _ -> f -> f.take(1))
                                .runToList());
    }
}
