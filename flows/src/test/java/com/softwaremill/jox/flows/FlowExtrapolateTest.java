package com.softwaremill.jox.flows;

import static com.softwaremill.jox.flows.TestIterators.continually;
import static com.softwaremill.jox.flows.TestIterators.single;
import static com.softwaremill.jox.structured.Scopes.supervised;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import org.junit.jupiter.api.Test;

import com.softwaremill.jox.Channel;

class FlowExtrapolateTest {

    @Test
    void shouldRepeatLastElementWhenDownstreamIsFaster() throws Exception {
        supervised(
                scope -> {
                    // given
                    Channel<Integer> upstream = Channel.newRendezvousChannel();
                    scope.fork(
                            () -> {
                                upstream.send(1);
                                upstream.done();
                                return null;
                            });

                    // when
                    List<Integer> result =
                            Flows.fromSource(upstream)
                                    .<Integer>extrapolate(n -> continually(n))
                                    .take(5)
                                    .runToList();

                    // then
                    assertEquals(List.of(1, 1, 1, 1, 1), result);
                    return null;
                });
    }

    @Test
    void shouldSwitchToLatestUpstreamElement() throws Exception {
        supervised(
                scope -> {
                    // given
                    Channel<Integer> upstream = Channel.newRendezvousChannel();
                    var extrapolated1Consumed = new CountDownLatch(1);

                    scope.fork(
                            () -> {
                                upstream.send(1);
                                extrapolated1Consumed.await();
                                upstream.send(2);
                                upstream.done();
                                return null;
                            });

                    // when
                    List<Integer> result =
                            Flows.fromSource(upstream)
                                    .<Integer>extrapolate(n -> single(n * 10))
                                    .tap(
                                            n -> {
                                                if (n == 10) extrapolated1Consumed.countDown();
                                            })
                                    .runToList();

                    // then
                    assertEquals(List.of(1, 10, 2, 20), result);
                    return null;
                });
    }

    @Test
    void shouldEmitInitialElementBeforeUpstream() throws Exception {
        supervised(
                scope -> {
                    // given
                    Channel<Integer> upstream = Channel.newRendezvousChannel();
                    var initialConsumed = new CountDownLatch(1);

                    scope.fork(
                            () -> {
                                initialConsumed.await();
                                upstream.send(10);
                                upstream.done();
                                return null;
                            });

                    // when
                    List<Integer> result =
                            Flows.fromSource(upstream)
                                    .<Integer>extrapolate(
                                            _ -> Collections.emptyIterator(), Optional.of(0))
                                    .tap(_ -> initialConsumed.countDown())
                                    .take(2)
                                    .runToList();

                    // then
                    assertEquals(List.of(0, 10), result);
                    return null;
                });
    }

    @Test
    void shouldPassThroughElementsWithoutInitial() throws Exception {
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
                                    .<Integer>extrapolate(_ -> Collections.emptyIterator())
                                    .tap(_ -> consumed.release())
                                    .runToList();

                    // then
                    assertEquals(List.of(1, 2, 3), result);
                    return null;
                });
    }

    @Test
    void shouldExtrapolateEachElementToMultipleValues() throws Exception {
        supervised(
                scope -> {
                    // given
                    Channel<Integer> upstream = Channel.newRendezvousChannel();
                    var lastExtrapolated1Consumed = new CountDownLatch(1);

                    scope.fork(
                            () -> {
                                upstream.send(1);
                                lastExtrapolated1Consumed.await();
                                upstream.send(2);
                                upstream.done();
                                return null;
                            });

                    // when
                    List<Integer> result =
                            Flows.fromSource(upstream)
                                    .<Integer>extrapolate(n -> List.of(n * 10, n * 100).iterator())
                                    .tap(
                                            n -> {
                                                if (n == 100) lastExtrapolated1Consumed.countDown();
                                            })
                                    .runToList();

                    // then
                    assertEquals(List.of(1, 10, 100, 2, 20, 200), result);
                    return null;
                });
    }

    @Test
    void shouldHandleEmptyFlowWithInitial() throws Exception {
        // when
        List<Integer> result =
                Flows.<Integer>empty()
                        .<Integer>extrapolate(_ -> Collections.emptyIterator(), Optional.of(42))
                        .take(1)
                        .runToList();

        // then
        assertEquals(List.of(42), result);
    }
}
