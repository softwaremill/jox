package com.softwaremill.jox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.ExecutionException;

import static com.softwaremill.jox.TestUtil.forkVoid;
import static com.softwaremill.jox.TestUtil.scoped;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Tests which always use unlimited channels.
 */
public class ChannelUnlimitedTest {
    @Test
    @Timeout(1)
    void testSimpleSendReceiveUnlimited() throws InterruptedException {
        // given
        Channel<String> channel = Channel.newUnlimitedChannel();

        // when
        channel.send("x"); // should not block
        channel.send("y"); // should not block
        var r1 = channel.receive(); // also should not block
        var r2 = channel.receive(); // also should not block

        // then
        assertEquals("x", r1);
        assertEquals("y", r2);
    }

    @Test
    @Timeout(1)
    void shouldReceiveFromAChannelUntilDone() throws InterruptedException {
        // given
        Channel<Integer> c = Channel.newUnlimitedChannel();
        c.send(1);
        c.send(2);
        c.send(3);
        c.done();

        // when
        var r1 = c.receiveSafe();
        var r2 = c.receiveSafe();
        var r3 = c.receiveSafe();
        var r4 = c.receiveSafe();

        // then
        assertEquals(1, r1);
        assertEquals(2, r2);
        assertEquals(3, r3);
        assertInstanceOf(ChannelClosed.class, r4);
    }

    @Test
    @Timeout(1)
    void shouldNotReceiveFromAChannelInCaseOfAnError() throws InterruptedException {
        // given
        Channel<Integer> c = Channel.newUnlimitedChannel();
        c.send(1);
        c.send(2);
        c.error(new RuntimeException());

        // when
        var r1 = c.receiveSafe();
        var r2 = c.receiveSafe();

        // then
        assertInstanceOf(ChannelError.class, r1);
        assertInstanceOf(ChannelError.class, r2);
    }
}
