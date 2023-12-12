package jox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests which always use buffered channels.
 */
public class ChannelBufferedTest {
    @Test
    void testSimpleSendReceiveBuffer1() throws InterruptedException {
        // given
        Channel<String> channel = new Channel<>(1);

        // when
        channel.send("x"); // should not block
        var r = channel.receive(); // also should not block

        // then
        assertEquals("x", r);
    }

    @Test
    void testSimpleSendReceiveBuffer2() throws InterruptedException {
        // given
        Channel<String> channel = new Channel<>(2);

        // when
        channel.send("x"); // should not block
        channel.send("y"); // should not block
        var r1 = channel.receive(); // also should not block
        var r2 = channel.receive(); // also should not block

        // then
        assertEquals("x", r1);
        assertEquals("y", r2);
    }
}
