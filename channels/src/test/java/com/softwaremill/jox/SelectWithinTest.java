package com.softwaremill.jox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.softwaremill.jox.Select.*;
import static com.softwaremill.jox.TestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

public class SelectWithinTest {
    @Test
    void testSelectWithin_shouldReturnValueWhenClauseCompletesBeforeTimeout()
            throws InterruptedException, TimeoutException {
        // given
        Channel<String> ch1 = Channel.newBufferedChannel(1);
        Channel<String> ch2 = Channel.newBufferedChannel(1);
        ch1.send("value");

        // when
        String result = selectWithin(Duration.ofMillis(100), ch1.receiveClause(), ch2.receiveClause());

        // then
        assertEquals("value", result);
    }

    @Test
    void testSelectWithin_shouldThrowTimeoutExceptionWhenTimeoutElapses() {
        // given
        Channel<String> ch1 = Channel.newBufferedChannel(1);
        Channel<String> ch2 = Channel.newBufferedChannel(1);

        // when/then
        var exception = assertThrows(TimeoutException.class,
                () -> selectWithin(Duration.ofMillis(50), ch1.receiveClause(), ch2.receiveClause()));
        assertTrue(exception.getMessage().contains("Select timed out after 50 ms"));
    }

    @Test
    void testSelectWithin_shouldThrowChannelClosedExceptionWhenChannelIsClosed() {
        // given
        Channel<String> ch1 = Channel.newBufferedChannel(1);
        Channel<String> ch2 = Channel.newBufferedChannel(1);
        ch1.done();

        // when/then
        assertThrows(ChannelDoneException.class,
                () -> selectWithin(Duration.ofMillis(100), ch1.receiveClause(), ch2.receiveClause()));
    }

    @Test
    void testSelectWithin_shouldThrowChannelErrorExceptionWhenChannelHasError() {
        // given
        Channel<String> ch1 = Channel.newBufferedChannel(1);
        Channel<String> ch2 = Channel.newBufferedChannel(1);
        RuntimeException error = new RuntimeException("test error");
        ch1.error(error);

        // when/then
        var exception = assertThrows(ChannelErrorException.class,
                () -> selectWithin(Duration.ofMillis(100), ch1.receiveClause(), ch2.receiveClause()));
        assertEquals(error, exception.getCause());
    }

    @Test
    void testSelectWithin_shouldSelectFirstAvailableClauseBeforeTimeout()
            throws InterruptedException, TimeoutException {
        // given
        Channel<String> ch1 = Channel.newBufferedChannel(1);
        Channel<String> ch2 = Channel.newBufferedChannel(1);
        ch1.send("first");
        ch2.send("second");

        // when
        String result = selectWithin(Duration.ofMillis(100), ch1.receiveClause(), ch2.receiveClause());

        // then
        assertEquals("first", result); // Should be biased towards first clause
    }

    @Test
    void testSelectWithin_shouldWorkWithSendClauses()
            throws InterruptedException, TimeoutException, ExecutionException {
        // given
        Channel<String> ch1 = Channel.newRendezvousChannel();
        Channel<String> ch2 = Channel.newRendezvousChannel();

        scoped(scope -> {
            // Fork a receiver that will accept the send
            forkVoid(scope, () -> {
                Thread.sleep(30); // Small delay to ensure select is waiting
                ch1.receive();
            });

            // when
            String result = selectWithin(Duration.ofMillis(100),
                    ch1.sendClause("sent_value", () -> "send_successful"),
                    ch2.sendClause("other_value", () -> "other_send"));

            // then
            assertEquals("send_successful", result);
        });
    }

    @Test
    void testSelectWithin_shouldHandleInterruption() throws ExecutionException, InterruptedException {
        // given
        Channel<String> ch1 = Channel.newBufferedChannel(1);
        Channel<String> ch2 = Channel.newBufferedChannel(1);

        scoped(scope -> {
            var future = forkCancelable(scope, () -> {
                try {
                    return selectWithin(Duration.ofSeconds(10), ch1.receiveClause(), ch2.receiveClause());
                } catch (InterruptedException e) {
                    return e; // Return the exception for testing
                } catch (TimeoutException e) {
                    return e; // Return timeout exception for testing
                }
            });

            // when
            Thread.sleep(50);
            future.cancel();

            // then - the future should be cancelled/interrupted
            var result = future.get();
            assertInstanceOf(InterruptedException.class, result);
        });
    }

    @Test
    void testSelectWithin_shouldRejectNegativeTimeout() {
        // given
        Channel<String> ch = Channel.newBufferedChannel(1);

        // when/then
        assertThrows(IllegalArgumentException.class, () -> selectWithin(Duration.ofMillis(-1), ch.receiveClause()));
    }

    @Test
    void testSelectWithin_shouldRejectZeroTimeout() {
        // given
        Channel<String> ch = Channel.newBufferedChannel(1);

        // when/then
        assertThrows(IllegalArgumentException.class, () -> selectWithin(Duration.ZERO, ch.receiveClause()));
    }

    // Tests for selectOrClosedWithin method

    @Test
    void testSelectOrClosedWithin_shouldReturnValueWhenClauseCompletesBeforeTimeout() throws InterruptedException {
        // given
        Channel<String> ch1 = Channel.newBufferedChannel(1);
        Channel<String> ch2 = Channel.newBufferedChannel(1);
        ch1.send("value");

        // when
        Object result = selectOrClosedWithin(Duration.ofMillis(100), "timeout", ch1.receiveClause(),
                ch2.receiveClause());

        // then
        assertEquals("value", result);
    }

    @Test
    void testSelectOrClosedWithin_shouldReturnTimeoutValueWhenTimeoutElapses() throws InterruptedException {
        // given
        Channel<String> ch1 = Channel.newBufferedChannel(1);
        Channel<String> ch2 = Channel.newBufferedChannel(1);

        // when
        Object result = selectOrClosedWithin(Duration.ofMillis(50), "TIMEOUT", ch1.receiveClause(),
                ch2.receiveClause());

        // then
        assertEquals("TIMEOUT", result);
    }

    @Test
    void testSelectOrClosedWithin_shouldReturnChannelClosedWhenChannelIsDone() throws InterruptedException {
        // given
        Channel<String> ch1 = Channel.newBufferedChannel(1);
        Channel<String> ch2 = Channel.newBufferedChannel(1);
        ch1.done();

        // when
        Object result = selectOrClosedWithin(Duration.ofMillis(100), "timeout", ch1.receiveClause(),
                ch2.receiveClause());

        // then
        assertInstanceOf(ChannelDone.class, result);
        assertEquals(ch1, ((ChannelDone) result).channel());
    }

    @Test
    void testSelectOrClosedWithin_shouldReturnChannelErrorWhenChannelHasError() throws InterruptedException {
        // given
        Channel<String> ch1 = Channel.newBufferedChannel(1);
        Channel<String> ch2 = Channel.newBufferedChannel(1);
        RuntimeException error = new RuntimeException("test error");
        ch1.error(error);

        // when
        Object result = selectOrClosedWithin(Duration.ofMillis(100), "timeout", ch1.receiveClause(),
                ch2.receiveClause());

        // then
        assertInstanceOf(ChannelError.class, result);
        assertEquals(error, ((ChannelError) result).cause());
    }

    @Test
    void testSelectOrClosedWithin_shouldSelectFirstAvailableClauseBeforeTimeout() throws InterruptedException {
        // given
        Channel<String> ch1 = Channel.newBufferedChannel(1);
        Channel<String> ch2 = Channel.newBufferedChannel(1);
        ch1.send("first");
        ch2.send("second");

        // when
        Object result = selectOrClosedWithin(Duration.ofMillis(100), "timeout", ch1.receiveClause(),
                ch2.receiveClause());

        // then
        assertEquals("first", result); // Should be biased towards first clause
    }

    @Test
    void testSelectOrClosedWithin_shouldWorkWithTransformations() throws InterruptedException {
        // given
        Channel<String> ch1 = Channel.newBufferedChannel(1);
        Channel<String> ch2 = Channel.newBufferedChannel(1);
        ch1.send("value");

        // when
        Object result = selectOrClosedWithin(Duration.ofMillis(100), "timeout",
                ch1.receiveClause(s -> s.toUpperCase()),
                ch2.receiveClause(s -> s.toLowerCase()));

        // then
        assertEquals("VALUE", result);
    }

    @Test
    void testSelectOrClosedWithin_shouldHandleNullTimeoutValue() throws InterruptedException {
        // given
        Channel<String> ch1 = Channel.newBufferedChannel(1);
        Channel<String> ch2 = Channel.newBufferedChannel(1);

        // when
        Object result = selectOrClosedWithin(Duration.ofMillis(50), null, ch1.receiveClause(), ch2.receiveClause());

        // then
        assertNull(result);
    }

    @Test
    void testSelectOrClosedWithin_shouldRejectNegativeTimeout() {
        // given
        Channel<String> ch = Channel.newBufferedChannel(1);

        // when/then
        assertThrows(IllegalArgumentException.class,
                () -> selectOrClosedWithin(Duration.ofMillis(-1), "timeout", ch.receiveClause()));
    }

    @Test
    void testSelectOrClosedWithin_shouldRejectZeroTimeout() {
        // given
        Channel<String> ch = Channel.newBufferedChannel(1);

        // when/then
        assertThrows(IllegalArgumentException.class,
                () -> selectOrClosedWithin(Duration.ZERO, "timeout", ch.receiveClause()));
    }

    // Integration tests

    @Test
    @Timeout(2) // Test timeout to prevent hanging
    void testSelectWithin_shouldWorkWithRendezvousChannels() throws InterruptedException, ExecutionException {
        // given
        Channel<String> ch1 = Channel.newRendezvousChannel();
        Channel<String> ch2 = Channel.newRendezvousChannel();

        scoped(scope -> {
            // Fork a sender that will provide a value after a short delay
            forkVoid(scope, () -> {
                Thread.sleep(30);
                ch2.send("rendezvous_value");
            });

            // when
            var future = fork(scope,
                    () -> selectWithin(Duration.ofMillis(200), ch1.receiveClause(), ch2.receiveClause()));

            // then
            assertEquals("rendezvous_value", future.get());
        });
    }

    @Test
    @Timeout(2) // Test timeout to prevent hanging
    void testSelectWithin_shouldTimeoutWithRendezvousChannels() {
        // given
        Channel<String> ch1 = Channel.newRendezvousChannel();
        Channel<String> ch2 = Channel.newRendezvousChannel();

        // when/then
        assertThrows(TimeoutException.class,
                () -> selectWithin(Duration.ofMillis(50), ch1.receiveClause(), ch2.receiveClause()));
    }

    @Test
    void testSelectOrClosedWithin_shouldCleanupTimeoutThreadOnInterruption()
            throws ExecutionException, InterruptedException {
        // given
        Channel<String> ch1 = Channel.newBufferedChannel(1);
        Channel<String> ch2 = Channel.newBufferedChannel(1);

        scoped(scope -> {
            // when
            var future = forkCancelable(scope, () -> {
                try {
                    return selectOrClosedWithin(Duration.ofSeconds(10), "timeout", ch1.receiveClause(),
                            ch2.receiveClause());
                } catch (InterruptedException e) {
                    return e; // Return the exception for testing
                }
            });

            Thread.sleep(50);
            future.cancel();

            // then
            var result = future.get();
            assertInstanceOf(InterruptedException.class, result);
        });
    }
}