package com.softwaremill.jox;

import static com.softwaremill.jox.TestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

public class ChannelTrySendReceiveTest {

    // ********
    // trySend
    // ********

    @Test
    void trySend_buffered_shouldSendWhenBufferHasSpace() {
        Channel<String> ch = Channel.newBufferedChannel(2);
        assertTrue(ch.trySend("a"));
        assertTrue(ch.trySend("b"));
    }

    @Test
    void trySend_buffered_shouldReturnFalseWhenBufferFull() {
        Channel<String> ch = Channel.newBufferedChannel(1);
        assertTrue(ch.trySend("a"));
        assertFalse(ch.trySend("b")); // buffer full, no receiver
    }

    @Test
    void trySend_rendezvous_shouldReturnFalseWhenNoReceiver() {
        Channel<String> ch = Channel.newRendezvousChannel();
        assertFalse(ch.trySend("a"));
    }

    @Test
    void trySend_rendezvous_shouldSendWhenReceiverWaiting()
            throws InterruptedException, ExecutionException {
        Channel<String> ch = Channel.newRendezvousChannel();
        scoped(
                scope -> {
                    var f = fork(scope, ch::receive);
                    Thread.sleep(50);
                    assertTrue(ch.trySend("x"));
                    assertEquals("x", f.get());
                });
    }

    @Test
    void trySend_unlimited_shouldAlwaysSend() {
        Channel<String> ch = Channel.newUnlimitedChannel();
        for (int i = 0; i < 1000; i++) {
            assertTrue(ch.trySend("v" + i));
        }
    }

    @Test
    void trySend_closedDone_shouldThrow() {
        Channel<String> ch = Channel.newBufferedChannel(1);
        ch.done();
        assertThrows(ChannelDoneException.class, () -> ch.trySend("x"));
    }

    @Test
    void trySend_closedError_shouldThrow() {
        Channel<String> ch = Channel.newBufferedChannel(1);
        ch.error(new RuntimeException("boom"));
        assertThrows(ChannelErrorException.class, () -> ch.trySend("x"));
    }

    @Test
    void trySend_nullValue_shouldThrowNPE() {
        Channel<String> ch = Channel.newBufferedChannel(1);
        assertThrows(NullPointerException.class, () -> ch.trySend(null));
    }

    // ****************
    // trySendOrClosed
    // ****************

    @Test
    void trySendOrClosed_buffered_shouldReturnNullOnSuccess() {
        Channel<String> ch = Channel.newBufferedChannel(2);
        assertNull(ch.trySendOrClosed("a"));
    }

    @Test
    void trySendOrClosed_buffered_shouldReturnSentinelWhenFull() {
        Channel<String> ch = Channel.newBufferedChannel(1);
        assertNull(ch.trySendOrClosed("a")); // success
        Object result = ch.trySendOrClosed("b");
        assertNotNull(result);
        assertFalse(result instanceof ChannelClosed);
    }

    @Test
    void trySendOrClosed_closedDone_shouldReturnChannelDone() {
        Channel<String> ch = Channel.newBufferedChannel(1);
        ch.done();
        assertInstanceOf(ChannelDone.class, ch.trySendOrClosed("x"));
    }

    @Test
    void trySendOrClosed_closedError_shouldReturnChannelError() {
        Channel<String> ch = Channel.newBufferedChannel(1);
        ch.error(new RuntimeException("boom"));
        assertInstanceOf(ChannelError.class, ch.trySendOrClosed("x"));
    }

    // **********
    // tryReceive
    // **********

    @Test
    void tryReceive_buffered_shouldReceiveBufferedValue() throws InterruptedException {
        Channel<String> ch = Channel.newBufferedChannel(2);
        ch.send("a");
        ch.send("b");
        assertEquals("a", ch.tryReceive());
        assertEquals("b", ch.tryReceive());
    }

    @Test
    void tryReceive_buffered_shouldReturnNullWhenEmpty() {
        Channel<String> ch = Channel.newBufferedChannel(2);
        assertNull(ch.tryReceive());
    }

    @Test
    void tryReceive_rendezvous_shouldReturnNullWhenNoSender() {
        Channel<String> ch = Channel.newRendezvousChannel();
        assertNull(ch.tryReceive());
    }

    @Test
    void tryReceive_rendezvous_shouldReceiveWhenSenderWaiting()
            throws InterruptedException, ExecutionException {
        Channel<String> ch = Channel.newRendezvousChannel();
        scoped(
                scope -> {
                    forkVoid(scope, () -> ch.send("x"));
                    Thread.sleep(50);
                    assertEquals("x", ch.tryReceive());
                });
    }

    @Test
    void tryReceive_unlimited_shouldReceiveBufferedValues() throws InterruptedException {
        Channel<String> ch = Channel.newUnlimitedChannel();
        ch.send("a");
        ch.send("b");
        assertEquals("a", ch.tryReceive());
        assertEquals("b", ch.tryReceive());
        assertNull(ch.tryReceive());
    }

    @Test
    void tryReceive_closedDone_noValues_shouldThrow() {
        Channel<String> ch = Channel.newBufferedChannel(1);
        ch.done();
        assertThrows(ChannelDoneException.class, () -> ch.tryReceive());
    }

    @Test
    void tryReceive_closedError_shouldThrow() {
        Channel<String> ch = Channel.newBufferedChannel(1);
        ch.error(new RuntimeException("boom"));
        assertThrows(ChannelErrorException.class, () -> ch.tryReceive());
    }

    // ********************
    // tryReceiveOrClosed
    // ********************

    @Test
    void tryReceiveOrClosed_shouldReturnNullWhenEmpty() {
        Channel<String> ch = Channel.newBufferedChannel(2);
        assertNull(ch.tryReceiveOrClosed());
    }

    @Test
    void tryReceiveOrClosed_shouldReturnValueWhenAvailable() throws InterruptedException {
        Channel<String> ch = Channel.newBufferedChannel(2);
        ch.send("hello");
        assertEquals("hello", ch.tryReceiveOrClosed());
    }

    @Test
    void tryReceiveOrClosed_closedDone_noValues_shouldReturnChannelDone() {
        Channel<String> ch = Channel.newBufferedChannel(1);
        ch.done();
        assertInstanceOf(ChannelDone.class, ch.tryReceiveOrClosed());
    }

    @Test
    void tryReceiveOrClosed_closedError_shouldReturnChannelError() {
        Channel<String> ch = Channel.newBufferedChannel(1);
        ch.error(new RuntimeException("boom"));
        assertInstanceOf(ChannelError.class, ch.tryReceiveOrClosed());
    }

    @Test
    void tryReceiveOrClosed_closedDone_withBufferedValues_shouldReturnValuesThenDone()
            throws InterruptedException {
        Channel<String> ch = Channel.newBufferedChannel(3);
        ch.send("a");
        ch.send("b");
        ch.done();

        assertEquals("a", ch.tryReceiveOrClosed());
        assertEquals("b", ch.tryReceiveOrClosed());
        assertInstanceOf(ChannelDone.class, ch.tryReceiveOrClosed());
    }

    @Test
    void tryReceiveOrClosed_closedError_withBufferedValues_shouldReturnError()
            throws InterruptedException {
        Channel<String> ch = Channel.newBufferedChannel(3);
        ch.send("a");
        ch.send("b");
        ch.error(new RuntimeException("boom"));

        // error() discards buffered values — should return ChannelError immediately
        assertInstanceOf(ChannelError.class, ch.tryReceiveOrClosed());
    }

    // *************************
    // Mixed send/receive tests
    // *************************

    @Test
    void trySend_thenTryReceive_roundTrip_buffered() {
        Channel<Integer> ch = Channel.newBufferedChannel(5);
        for (int i = 0; i < 5; i++) {
            assertTrue(ch.trySend(i));
        }
        for (int i = 0; i < 5; i++) {
            assertEquals(i, ch.tryReceive());
        }
        assertNull(ch.tryReceive());
    }

    @Test
    void trySend_thenTryReceive_roundTrip_unlimited() {
        Channel<Integer> ch = Channel.newUnlimitedChannel();
        for (int i = 0; i < 100; i++) {
            assertTrue(ch.trySend(i));
        }
        for (int i = 0; i < 100; i++) {
            assertEquals(i, ch.tryReceive());
        }
        assertNull(ch.tryReceive());
    }

    @Test
    void trySend_mixedWithRegularReceive() throws InterruptedException, ExecutionException {
        Channel<String> ch = Channel.newBufferedChannel(2);
        assertTrue(ch.trySend("a"));
        assertTrue(ch.trySend("b"));
        assertEquals("a", ch.receive());
        assertEquals("b", ch.receive());
    }

    @Test
    void regularSend_thenTryReceive() throws InterruptedException {
        Channel<String> ch = Channel.newBufferedChannel(2);
        ch.send("a");
        ch.send("b");
        assertEquals("a", ch.tryReceive());
        assertEquals("b", ch.tryReceive());
    }

    // *************
    // Stress tests
    // *************

    @Test
    void concurrentTrySendAndTryReceive_noValueLossOrDuplication()
            throws InterruptedException, ExecutionException {
        Channel<Integer> ch = Channel.newBufferedChannel(64);
        int numProducers = 4;
        int numConsumers = 4;
        int itemsPerProducer = 10_000;

        var sentCount = new AtomicInteger(0);
        var receivedValues = new ConcurrentSkipListSet<Integer>();

        scoped(
                scope -> {
                    // producers: trySend in a loop
                    for (int p = 0; p < numProducers; p++) {
                        int producerBase = p * itemsPerProducer;
                        forkVoid(
                                scope,
                                () -> {
                                    for (int i = 0; i < itemsPerProducer; i++) {
                                        int val = producerBase + i;
                                        while (!ch.trySend(val)) {
                                            Thread.yield();
                                        }
                                        sentCount.incrementAndGet();
                                    }
                                });
                    }

                    // consumers: tryReceive in a loop
                    int totalItems = numProducers * itemsPerProducer;
                    for (int c = 0; c < numConsumers; c++) {
                        forkVoid(
                                scope,
                                () -> {
                                    while (receivedValues.size() < totalItems) {
                                        Integer v = ch.tryReceive();
                                        if (v != null) {
                                            receivedValues.add(v);
                                        } else {
                                            Thread.yield();
                                        }
                                    }
                                });
                    }
                });

        int totalItems = numProducers * itemsPerProducer;
        assertEquals(totalItems, sentCount.get());
        assertEquals(totalItems, receivedValues.size());
    }

    @Test
    void concurrentTrySendWithRegularReceive() throws InterruptedException, ExecutionException {
        Channel<Integer> ch = Channel.newBufferedChannel(16);
        int total = 10_000;
        var received = new ConcurrentSkipListSet<Integer>();

        scoped(
                scope -> {
                    // producer: trySend
                    forkVoid(
                            scope,
                            () -> {
                                for (int i = 0; i < total; i++) {
                                    while (!ch.trySend(i)) {
                                        Thread.yield();
                                    }
                                }
                                ch.done();
                            });

                    // consumer: regular receive
                    forkVoid(
                                    scope,
                                    () -> {
                                        while (true) {
                                            var r = ch.receiveOrClosed();
                                            if (r instanceof ChannelDone) break;
                                            received.add((Integer) r);
                                        }
                                    })
                            .get();
                });

        assertEquals(total, received.size());
    }

    @Test
    void concurrentRegularSendWithTryReceive() throws InterruptedException, ExecutionException {
        Channel<Integer> ch = Channel.newBufferedChannel(16);
        int total = 10_000;
        var received = new ConcurrentSkipListSet<Integer>();

        scoped(
                scope -> {
                    // producer: regular send
                    forkVoid(
                            scope,
                            () -> {
                                for (int i = 0; i < total; i++) {
                                    ch.send(i);
                                }
                                ch.done();
                            });

                    // consumer: tryReceive
                    forkVoid(
                                    scope,
                                    () -> {
                                        while (true) {
                                            var r = ch.tryReceiveOrClosed();
                                            if (r instanceof ChannelDone) break;
                                            if (r != null) {
                                                received.add((Integer) r);
                                            } else {
                                                Thread.yield();
                                            }
                                        }
                                    })
                            .get();
                });

        assertEquals(total, received.size());
    }

    @Test
    void trySend_rendezvous_concurrentStress() throws InterruptedException, ExecutionException {
        Channel<Integer> ch = Channel.newRendezvousChannel();
        int total = 1000;
        var received = new ConcurrentSkipListSet<Integer>();

        scoped(
                scope -> {
                    // sender: trySend
                    forkVoid(
                            scope,
                            () -> {
                                for (int i = 0; i < total; i++) {
                                    while (!ch.trySend(i)) {
                                        Thread.yield();
                                    }
                                }
                                ch.done();
                            });

                    // receiver: regular receive
                    forkVoid(
                                    scope,
                                    () -> {
                                        while (true) {
                                            var r = ch.receiveOrClosed();
                                            if (r instanceof ChannelDone) break;
                                            received.add((Integer) r);
                                        }
                                    })
                            .get();
                });

        assertEquals(total, received.size());
    }

    @Test
    void tryReceive_rendezvous_concurrentStress() throws InterruptedException, ExecutionException {
        Channel<Integer> ch = Channel.newRendezvousChannel();
        int total = 1000;
        var received = new ConcurrentSkipListSet<Integer>();

        scoped(
                scope -> {
                    // sender: regular send
                    forkVoid(
                            scope,
                            () -> {
                                for (int i = 0; i < total; i++) {
                                    ch.send(i);
                                }
                                ch.done();
                            });

                    // receiver: tryReceive
                    forkVoid(
                                    scope,
                                    () -> {
                                        while (true) {
                                            var r = ch.tryReceiveOrClosed();
                                            if (r instanceof ChannelDone) break;
                                            if (r != null) {
                                                received.add((Integer) r);
                                            } else {
                                                Thread.yield();
                                            }
                                        }
                                    })
                            .get();
                });

        assertEquals(total, received.size());
    }

    @Test
    void segmentCleanup_afterManyTrySendFailures() throws InterruptedException, ExecutionException {
        // trySend on a rendezvous channel with no receiver: should not leak segments
        Channel<Integer> ch = Channel.newRendezvousChannel();
        for (int i = 0; i < 10_000; i++) {
            assertFalse(ch.trySend(i));
        }
        // channel should still be functional
        scoped(
                scope -> {
                    forkVoid(scope, () -> ch.send(42));
                    Thread.sleep(50);
                    assertEquals(42, ch.tryReceive());
                });
    }

    @Test
    void segmentCleanup_afterManyTryReceiveFailures() throws InterruptedException {
        // tryReceive on an empty channel: should not leak segments
        Channel<Integer> ch = Channel.newBufferedChannel(2);
        for (int i = 0; i < 10_000; i++) {
            assertNull(ch.tryReceive());
        }
        // channel should still be functional
        ch.send(42);
        assertEquals(42, ch.tryReceive());
    }

    @Test
    void trySendTryReceive_bufferedCapacity1() {
        Channel<Integer> ch = Channel.newBufferedChannel(1);
        assertTrue(ch.trySend(1));
        assertEquals(1, ch.tryReceive());
    }

    @Test
    void trySendTryReceive_bufferedCapacity10() {
        Channel<Integer> ch = Channel.newBufferedChannel(10);
        assertTrue(ch.trySend(1));
        assertEquals(1, ch.tryReceive());
    }

    @Test
    void trySendTryReceive_unlimited() {
        Channel<Integer> ch = Channel.newUnlimitedChannel();
        assertTrue(ch.trySend(1));
        assertEquals(1, ch.tryReceive());
    }
}
