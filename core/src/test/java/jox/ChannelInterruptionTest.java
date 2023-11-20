package jox;

import org.junit.jupiter.api.Test;

import static jox.TestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChannelInterruptionTest {
    @Test
    public void testSendReceiveAfterSendInterrupt() throws Exception {
        // given
        Channel<String> channel = new Channel<>();

        // when
        scoped(scope -> {
            var t1 = forkCancelable(scope, () -> channel.send("x"));
            t1.cancel();

            forkVoid(scope, () -> channel.send("y"));
            var t3 = fork(scope, channel::receive);

            // then
            assertEquals("y", t3.get());
        });
    }

    @Test
    public void testSendReceiveAfterReceiveInterrupt() throws Exception {
        // given
        Channel<String> channel = new Channel<>();

        // when
        scoped(scope -> {
            var t1 = forkCancelable(scope, channel::receive);
            t1.cancel();

            forkVoid(scope, () -> channel.send("x"));
            var t3 = fork(scope, channel::receive);

            // then
            assertEquals("x", t3.get());
        });
    }

    @Test
    public void testRaceInterruptAndSend() throws Exception {
        // when
        scoped(scope -> {
            for (int i = 0; i < 100; i++) {
                // given
                Channel<String> channel = new Channel<>();

                var t1 = forkCancelable(scope, () -> channel.send("x"));
                var t2 = fork(scope, channel::receive);
                t1.cancel();

                if (t1.cancel() instanceof InterruptedException) {
                    // the `receive` from t2 has not happened yet
                    forkVoid(scope, () -> channel.send("y"));
                    assertEquals("y", t2.get());
                } else {
                    // the `receive` from t2 has already happened
                    assertEquals("x", t2.get());
                }
            }
        });
    }
}
