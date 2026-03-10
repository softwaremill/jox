package com.softwaremill.jox.flows;

import static com.softwaremill.jox.structured.Scopes.supervised;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import com.softwaremill.jox.Channel;

class FlowConflateTest {

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
                                for (int i = 2; i <= 10; i++) upstream.send(i);
                                upstream.done();
                                canProceed.countDown();
                                return null;
                            });

                    var isFirst = new AtomicBoolean(true);

                    // when
                    List<Integer> result =
                            Flows.fromSource(upstream)
                                    .conflate(Integer::sum)
                                    .tap(
                                            _ -> {
                                                if (isFirst.compareAndSet(true, false)) {
                                                    downstreamBlocked.countDown();
                                                    canProceed.await();
                                                }
                                            })
                                    .runToList();

                    // then — element 1 passes through, remaining elements 2-10 are conflated
                    assertEquals(List.of(1, (2 + 3 + 4 + 5 + 6 + 7 + 8 + 9 + 10)), result);
                    return null;
                });
    }

    @Test
    void shouldAggregateWithCustomSeedWhenDownstreamIsBusy() throws Exception {
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
                                for (int i = 2; i <= 5; i++) upstream.send(i);
                                upstream.done();
                                canProceed.countDown();
                                return null;
                            });

                    var isFirst = new AtomicBoolean(true);

                    // when
                    List<Set<Integer>> result =
                            Flows.fromSource(upstream)
                                    .<Set<Integer>>conflateWithSeed(
                                            Set::of,
                                            (acc, x) -> {
                                                var set = new HashSet<>(acc);
                                                set.add(x);
                                                return set;
                                            })
                                    .tap(
                                            _ -> {
                                                if (isFirst.compareAndSet(true, false)) {
                                                    downstreamBlocked.countDown();
                                                    canProceed.await();
                                                }
                                            })
                                    .runToList();

                    // then — element 1 passes through as Set(1), remaining elements conflated
                    assertEquals(List.of(Set.of(1), Set.of(2, 3, 4, 5)), result);
                    return null;
                });
    }
}
