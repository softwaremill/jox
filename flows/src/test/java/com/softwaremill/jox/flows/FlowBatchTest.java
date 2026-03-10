package com.softwaremill.jox.flows;

import static com.softwaremill.jox.structured.Scopes.supervised;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import com.softwaremill.jox.Channel;

class FlowBatchTest {

    @Test
    void shouldAggregateByCountWhenDownstreamIsBusy() throws Exception {
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
                                    .batch(
                                            10L,
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
}
