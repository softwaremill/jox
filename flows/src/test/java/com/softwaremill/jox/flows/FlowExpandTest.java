package com.softwaremill.jox.flows;

import static com.softwaremill.jox.flows.TestIterators.continually;
import static com.softwaremill.jox.flows.TestIterators.single;
import static com.softwaremill.jox.structured.Scopes.supervised;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import org.junit.jupiter.api.Test;

import com.softwaremill.jox.Channel;
import com.softwaremill.jox.structured.JoxScopeExecutionException;

class FlowExpandTest {

    @Test
    void shouldEmitFromInfiniteExpanderWhenDownstreamIsFaster() throws Exception {
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
                                    .expand(n -> continually(n))
                                    .take(5)
                                    .runToList();

                    // then
                    assertEquals(List.of(1, 1, 1, 1, 1), result);
                    return null;
                });
    }

    @Test
    void shouldDrainRemainingIteratorAfterUpstreamCompletes() throws Exception {
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
                                    .expand(n -> List.of(n * 10, n * 100, n * 1000).iterator())
                                    .runToList();

                    // then
                    assertEquals(List.of(10, 100, 1000), result);
                    return null;
                });
    }

    @Test
    void shouldReplaceIteratorWhenNewUpstreamElementArrives() throws Exception {
        supervised(
                scope -> {
                    // given
                    Channel<Integer> upstream = Channel.newRendezvousChannel();
                    var firstExpanded = new CountDownLatch(1);
                    var element2Sent = new CountDownLatch(1);

                    scope.fork(
                            () -> {
                                upstream.send(1);
                                firstExpanded.await();
                                upstream.send(2);
                                element2Sent.countDown();
                                upstream.done();
                                return null;
                            });

                    // when
                    List<Integer> result =
                            Flows.fromSource(upstream)
                                    .expand(n -> List.of(n * 10, n * 100, n * 1000).iterator())
                                    .tap(
                                            n -> {
                                                if (n == 10) {
                                                    firstExpanded.countDown();
                                                    element2Sent.await();
                                                }
                                            })
                                    .runToList();

                    // then — 10 emitted first; while consumer processes it, expand receives
                    // element 2, discarding remaining iterator (100, 1000)
                    assertEquals(List.of(10, 20, 200, 2000), result);
                    return null;
                });
    }

    @Test
    void shouldEmitFromSingleElementExpanderWithGatedUpstream() throws Exception {
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
                                    .expand(n -> single(n * 10))
                                    .tap(_ -> consumed.release())
                                    .runToList();

                    // then
                    assertEquals(List.of(10, 20, 30), result);
                    return null;
                });
    }

    @Test
    void shouldHandleEmptyFlow() throws Exception {
        // when
        List<Integer> result =
                Flows.<Integer>empty().expand(n -> List.of(n, n).iterator()).runToList();

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
                                Flows.fromValues(1)
                                        .concat(Flows.failed(new IllegalStateException("boom")))
                                        .expand(n -> continually(n))
                                        .take(100)
                                        .runToList());
        assertInstanceOf(IllegalStateException.class, exception.getCause().getCause());
    }

    @Test
    void shouldPropagateErrorsFromExpander() {
        // when & then
        var exception =
                assertThrows(
                        JoxScopeExecutionException.class,
                        () ->
                                Flows.fromValues(1, 2, 3)
                                        .<Integer>expand(
                                                _ -> {
                                                    throw new IllegalStateException("expander");
                                                })
                                        .runToList());
        assertInstanceOf(IllegalStateException.class, exception.getCause().getCause());
    }

    @Test
    void shouldPropagateErrorsFromIterator() {
        // when & then
        var exception =
                assertThrows(
                        JoxScopeExecutionException.class,
                        () ->
                                Flows.fromValues(1)
                                        .expand(
                                                _ ->
                                                        new Iterator<Integer>() {
                                                            public boolean hasNext() {
                                                                return true;
                                                            }

                                                            public Integer next() {
                                                                throw new IllegalStateException(
                                                                        "iterator");
                                                            }
                                                        })
                                        .take(5)
                                        .runToList());
        assertInstanceOf(IllegalStateException.class, exception.getCause().getCause());
    }
}
