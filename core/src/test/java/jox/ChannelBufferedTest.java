package jox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

import static jox.TestUtil.forkVoid;
import static jox.TestUtil.scoped;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Tests which always use buffered channels.
 */
public class ChannelBufferedTest {
    @Test
    @Timeout(1)
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
    @Timeout(1)
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

    @Test
    @Timeout(2)
    void testBufferCapacityStaysTheSameAfterSendsReceives() throws ExecutionException, InterruptedException {
        // given
        Channel<Integer> channel = new Channel<>(2);
        var trail = new ConcurrentLinkedQueue<String>();

        // when
        scoped(scope -> {
            forkVoid(scope, () -> {
                channel.send(1); // should not block
                channel.send(2); // should not block
                channel.send(3);
                channel.send(4);
            });

            // then
            Thread.sleep(100L);
            assertEquals(1, channel.receive());
            Thread.sleep(100L);
            assertEquals(2, channel.receive());
            Thread.sleep(100L);
            assertEquals(3, channel.receive());
            Thread.sleep(100L);
            assertEquals(4, channel.receive());

            channel.send(5); // should not block
            channel.send(6); // should not block
        });
    }

    @Test
    @Timeout(1)
    void shouldReceiveFromAChannelUntilDone() throws InterruptedException {
        // given
        Channel<Integer> c = new Channel<>(3);
        c.send(1);
        c.send(2);
        c.done();

        // when
        var r1 = c.receiveSafe();
        var r2 = c.receiveSafe();
        var r3 = c.receiveSafe();

        // then
        assertEquals(1, r1);
        assertEquals(2, r2);
        assertInstanceOf(ChannelClosed.class, r3);
    }

    @Test
    @Timeout(1)
    void shouldNotReceiveFromAChannelInCaseOfAnError() throws InterruptedException {
        // given
        Channel<Integer> c = new Channel<>(3);
        c.send(1);
        c.send(2);
        c.error(new RuntimeException());

        // when
        var r1 = c.receiveSafe();
        var r2 = c.receiveSafe();

        // then
        assertInstanceOf(ChannelClosed.ChannelError.class, r1);
        assertInstanceOf(ChannelClosed.ChannelError.class, r2);
    }
}
