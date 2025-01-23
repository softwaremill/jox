package com.softwaremill.jox;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static com.softwaremill.jox.TestUtil.forkCancelable;
import static com.softwaremill.jox.TestUtil.scoped;
import static org.junit.jupiter.api.Assertions.*;

public class ChannelClosedTest {
    @Test
    void testClosed_noValues_whenError() throws InterruptedException {
        // given
        Channel<Integer> c = new Channel<>();
        RuntimeException reason = new RuntimeException();

        // when
        c.error(reason);

        // then
        assertTrue(c.isClosedForReceive());
        assertTrue(c.isClosedForSend());
        assertEquals(new ChannelError(reason, c), c.receiveOrClosed());
    }

    @Test
    void testClosed_noValues_whenDone() throws InterruptedException {
        // given
        Channel<Integer> c = new Channel<>();

        // when
        c.done();

        // then
        assertTrue(c.isClosedForReceive());
        assertTrue(c.isClosedForSend());
        assertEquals(new ChannelDone(c), c.receiveOrClosed());
    }

    @Test
    void testClosed_hasSuspendedValues_whenDone() throws InterruptedException, ExecutionException {
        // given
        Channel<Integer> c = new Channel<>();

        // when
        scoped(scope -> {
            var f = forkCancelable(scope, () -> {
                c.send(1);
            });

            try {
                Thread.sleep(100); // let the send suspend
                c.done();

                // then
                assertFalse(c.isClosedForReceive());
                assertTrue(c.isClosedForSend());
            } finally {
                f.cancel();
            }
        });
    }

    @Test
    void testClosed_hasBufferedValues_whenDone() throws InterruptedException {
        // given
        Channel<Integer> c = new Channel<>(5);

        // when
        c.send(1);
        c.send(2);
        c.done();

        // then
        assertFalse(c.isClosedForReceive());
        assertTrue(c.isClosedForSend());
    }

    @Test
    void testClosed_hasValues_whenError() throws InterruptedException {
        // given
        Channel<Integer> c = new Channel<>(5);

        // when
        c.send(1);
        c.send(2);
        c.error(new RuntimeException());

        // then
        assertTrue(c.isClosedForReceive());
        assertTrue(c.isClosedForSend());
    }
}
