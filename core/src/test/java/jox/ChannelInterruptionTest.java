package jox;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.SECONDS;
import static jox.TestUtil.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChannelInterruptionTest {
    @Test
    void testSendReceiveAfterSendInterrupt() throws Exception {
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
    void testSendReceiveAfterReceiveInterrupt() throws Exception {
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
    void testRaceInterruptAndSend() throws Exception {
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

    @Test
    void testReceiveManyInterruptsReceive() throws ExecutionException, InterruptedException {
        scoped(scope -> {
            Channel<String> channel = new Channel<>();
            Set<String> received = ConcurrentHashMap.newKeySet();

            // starting with a single receive
            forkVoid(scope, () -> {
                received.add(channel.receive());
            });
            // wait for the `receive` to suspend
            Thread.sleep(100);

            // then, starting subsequent receives, and interrupting them
            for (int i = 0; i < 1000; i++) {
                var s = new Semaphore(0);
                var f = forkCancelable(scope, () -> {
                    s.release(); // letting the main thread know that the receive has started
                    channel.receive();
                });
                s.acquire();
                f.cancel();
            }

            // then, starting one more receive
            forkVoid(scope, () -> {
                received.add(channel.receive());
            });
            // wait for the `receive` to suspend
            Thread.sleep(100);

            // send two elements, wait for completion
            channel.send("a");
            channel.send("b");

            // check the results
            await().atMost(1, SECONDS).until(() -> received.equals(Set.of("a", "b")));
        });
    }
}
