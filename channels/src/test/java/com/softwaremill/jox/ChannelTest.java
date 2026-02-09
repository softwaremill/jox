package com.softwaremill.jox;

import static com.softwaremill.jox.TestUtil.forkVoid;
import static com.softwaremill.jox.TestUtil.scoped;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

/** Channel tests which are run for various capacities. */
public class ChannelTest {
    @TestWithCapacities
    void testSendReceiveInManyForks(int capacity) throws ExecutionException, InterruptedException {
        // given
        Channel<Integer> channel =
                capacity == 0
                        ? Channel.newRendezvousChannel()
                        : Channel.newBufferedChannel(capacity);
        var fs = new HashSet<Future<Void>>();
        var s = new ConcurrentSkipListSet<Integer>();

        // when
        scoped(
                scope -> {
                    for (int i = 1; i <= 1000; i++) {
                        int ii = i;
                        forkVoid(scope, () -> channel.send(ii));
                    }
                    for (int i = 1; i <= 1000; i++) {
                        fs.add(forkVoid(scope, () -> s.add(channel.receive())));
                    }
                    for (Future<Void> f : fs) {
                        f.get();
                    }

                    // then
                    assertEquals(1000, s.size());
                });
    }

    @TestWithCapacities
    void testSendReceiveManyElementsInTwoForks(int capacity)
            throws ExecutionException, InterruptedException {
        // given
        Channel<Integer> channel =
                capacity == 0
                        ? Channel.newRendezvousChannel()
                        : Channel.newBufferedChannel(capacity);
        var s = new ConcurrentSkipListSet<Integer>();

        // when
        scoped(
                scope -> {
                    forkVoid(
                            scope,
                            () -> {
                                for (int i = 1; i <= 1000; i++) {
                                    channel.send(i);
                                }
                            });
                    forkVoid(
                                    scope,
                                    () -> {
                                        for (int i = 1; i <= 1000; i++) {
                                            s.add(channel.receive());
                                        }
                                    })
                            .get();

                    // then
                    assertEquals(1000, s.size());
                });
    }

    @Test
    void testNullItem() throws InterruptedException {
        Channel<Object> ch = Channel.newBufferedChannel(4);
        assertThrows(NullPointerException.class, () -> ch.send(null));
    }

    @Test
    void testEstimateSize_bufferedChannel_empty() {
        Channel<Integer> ch = Channel.newBufferedChannel(10);
        assertEquals(0, ch.estimateSize());
    }

    @Test
    void testEstimateSize_bufferedChannel_withValues() throws InterruptedException {
        Channel<Integer> ch = Channel.newBufferedChannel(10);
        assertEquals(0, ch.estimateSize());

        ch.send(1);
        ch.send(2);
        ch.send(3);
        assertTrue(ch.estimateSize() >= 3, "Expected at least 3 items, got " + ch.estimateSize());

        ch.receive();
        assertTrue(
                ch.estimateSize() >= 2,
                "Expected at least 2 items after receive, got " + ch.estimateSize());

        ch.receive();
        ch.receive();
        assertEquals(0, ch.estimateSize());
    }

    @Test
    void testEstimateSize_bufferedChannel_afterClose() throws InterruptedException {
        Channel<Integer> ch = Channel.newBufferedChannel(10);
        ch.send(1);
        ch.send(2);
        ch.send(3);

        ch.done();
        assertTrue(
                ch.estimateSize() >= 3,
                "Estimate should still work after close, got " + ch.estimateSize());

        ch.receive();
        assertTrue(
                ch.estimateSize() >= 2,
                "Estimate should reflect remaining buffered values, got " + ch.estimateSize());
    }

    @Test
    void testEstimateSize_unlimitedChannel_growth() throws InterruptedException {
        Channel<Integer> ch = Channel.newUnlimitedChannel();

        for (int i = 0; i < 1000; i++) {
            ch.send(i);
        }

        long estimate = ch.estimateSize();
        assertTrue(
                estimate >= 900,
                "Expected at least 900 items in unlimited channel, got " + estimate);
        assertTrue(estimate <= 1000, "Expected at most 1000 items, got " + estimate);

        // Receive some and check size decreases
        for (int i = 0; i < 500; i++) {
            ch.receive();
        }

        estimate = ch.estimateSize();
        assertTrue(
                estimate >= 400,
                "Expected at least 400 items after receiving 500, got " + estimate);
        assertTrue(estimate <= 500, "Expected at most 500 items, got " + estimate);
    }

    @Test
    void testEstimateSize_rendezvousChannel() throws InterruptedException, ExecutionException {
        Channel<Integer> ch = Channel.newRendezvousChannel();
        assertEquals(0, ch.estimateSize());

        scoped(
                scope -> {
                    // Start a sender in background
                    forkVoid(
                            scope,
                            () -> {
                                try {
                                    Thread.sleep(10);
                                    ch.send(1);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                    // Rendezvous should have 0 or very small estimate
                    Thread.sleep(20);
                    long estimate = ch.estimateSize();
                    assertTrue(
                            estimate <= 1,
                            "Rendezvous channel should have small estimate, got " + estimate);

                    // Receive the value
                    ch.receive();
                    assertEquals(0, ch.estimateSize());
                });
    }

    @Test
    void testEstimateSize_concurrentModification() throws InterruptedException, ExecutionException {
        Channel<Integer> ch = Channel.newBufferedChannel(1000);

        scoped(
                scope -> {
                    // 10 senders
                    for (int i = 0; i < 10; i++) {
                        forkVoid(
                                scope,
                                () -> {
                                    for (int j = 0; j < 100; j++) {
                                        ch.send(j);
                                    }
                                });
                    }

                    // 5 receivers
                    for (int i = 0; i < 5; i++) {
                        forkVoid(
                                scope,
                                () -> {
                                    for (int j = 0; j < 50; j++) {
                                        ch.receive();
                                    }
                                });
                    }

                    // Check estimate is within bounds during concurrent operations
                    Thread.sleep(50);
                    long estimate = ch.estimateSize();
                    assertTrue(estimate >= 0, "Estimate should be non-negative, got " + estimate);
                    assertTrue(
                            estimate <= 1000,
                            "Estimate should not exceed capacity significantly, got " + estimate);
                });

        // After all operations, estimate should be around 750 (1000 sent - 250 received)
        long finalEstimate = ch.estimateSize();
        assertTrue(
                finalEstimate >= 700,
                "Expected around 750 items after concurrent ops, got " + finalEstimate);
        assertTrue(
                finalEstimate <= 800,
                "Expected around 750 items after concurrent ops, got " + finalEstimate);
    }

    @Test
    void testEstimateSize_neverNegative() throws InterruptedException, ExecutionException {
        Channel<Integer> ch = Channel.newRendezvousChannel();

        scoped(
                scope -> {
                    // Start receivers before senders
                    for (int i = 0; i < 5; i++) {
                        forkVoid(
                                scope,
                                () -> {
                                    Thread.sleep(10);
                                    ch.receive();
                                });
                    }

                    // Even with waiting receivers, estimate should be >= 0
                    Thread.sleep(20);
                    long estimate = ch.estimateSize();
                    assertTrue(estimate >= 0, "Estimate should never be negative, got " + estimate);

                    // Send values to waiting receivers
                    for (int i = 0; i < 5; i++) {
                        ch.send(i);
                    }

                    assertEquals(0, ch.estimateSize());
                });
    }

    @Test
    void testEstimateSize_afterError() throws InterruptedException {
        Channel<Integer> ch = Channel.newBufferedChannel(10);
        ch.send(1);
        ch.send(2);

        ch.error(new RuntimeException("test error"));

        // Estimate should still work after error
        long estimate = ch.estimateSize();
        assertTrue(estimate >= 0, "Estimate should work after error, got " + estimate);
    }

    @Test
    void testEstimateSize_bufferedChannel_waitingSendersBeyondCapacity()
            throws InterruptedException, ExecutionException {
        Channel<Integer> ch = Channel.newBufferedChannel(2);
        ch.send(1);
        ch.send(2);
        // Buffer is now full, estimate should reflect 2 buffered values
        assertEquals(2, ch.estimateSize());

        scoped(
                scope -> {
                    // Start a sender that will block because the buffer is full
                    forkVoid(scope, () -> ch.send(3));

                    // Give the blocked sender time to increment the senders counter
                    Thread.sleep(50);

                    // Estimate includes the waiting sender: documented behavior
                    long estimate = ch.estimateSize();
                    assertTrue(
                            estimate >= 2,
                            "Expected at least 2 (buffered values), got " + estimate);
                    assertTrue(
                            estimate <= 3,
                            "Expected at most 3 (buffered + waiting sender), got " + estimate);

                    // Receive one to unblock the waiting sender
                    ch.receive();
                });
    }
}
