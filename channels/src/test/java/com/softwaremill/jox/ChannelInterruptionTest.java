package com.softwaremill.jox;

import static com.softwaremill.jox.TestUtil.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

import org.junit.jupiter.api.Test;

public class ChannelInterruptionTest {
    @Test
    void testSendReceiveAfterSendInterrupt() throws Exception {
        // given
        Channel<String> channel = Channel.newRendezvousChannel();

        // when
        scoped(
                scope -> {
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
        Channel<String> channel = Channel.newRendezvousChannel();

        // when
        scoped(
                scope -> {
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
        scoped(
                scope -> {
                    for (int i = 0; i < 100; i++) {
                        // given
                        Channel<String> channel = Channel.newRendezvousChannel();

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
        scoped(
                scope -> {
                    Channel<String> channel = Channel.newRendezvousChannel();
                    Set<String> received = ConcurrentHashMap.newKeySet();

                    // starting with a single receive
                    forkVoid(
                            scope,
                            () -> {
                                received.add(channel.receive());
                            });
                    // wait for the `receive` to suspend
                    Thread.sleep(100);

                    // then, starting subsequent receives, and interrupting them
                    for (int i = 0; i < 1000; i++) {
                        var s = new Semaphore(0);
                        var f =
                                forkCancelable(
                                        scope,
                                        () -> {
                                            s.release(); // letting the main thread know that
                                            // the receive has started
                                            channel.receive();
                                        });
                        s.acquire();
                        f.cancel();
                    }

                    // then, starting one more receive
                    forkVoid(
                            scope,
                            () -> {
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

    @Test
    void testManyInterruptedReceivesShouldNotLeakMemory()
            throws InterruptedException, ExecutionException {
        var ch = Channel.newRendezvousChannel();

        scoped(
                scope -> {
                    var forks = new Fork[300];
                    for (int i = 0; i < forks.length; i++) {
                        forks[i] =
                                forkCancelable(
                                        scope,
                                        () -> {
                                            ch.receive();
                                        });
                    }

                    Thread.sleep(500); // waiting for all forks to suspend
                    for (var f : forks) {
                        f.cancel();
                    }

                    // checking the number of segments
                    var segments = countOccurrences(ch.toString(), "Segment{");
                    assertEquals(2, segments, "More than 2 segments found in channel:\n" + ch);
                });
    }

    @Test
    void testManyInterruptedSendsShouldNotLeakMemory()
            throws InterruptedException, ExecutionException {
        var ch = Channel.<String>newBufferedChannel(1);
        ch.send("x");

        scoped(
                scope -> {
                    var forks = new Fork[300];
                    for (int i = 0; i < forks.length; i++) {
                        forks[i] =
                                forkCancelable(
                                        scope,
                                        () -> {
                                            ch.send("y");
                                        });
                    }

                    Thread.sleep(500); // waiting for all forks to suspend
                    for (var f : forks) {
                        f.cancel();
                    }

                    // checking the number of segments
                    var segments = countOccurrences(ch.toString(), "Segment{");
                    assertEquals(2, segments, "More than 2 segments found in channel:\n" + ch);
                });
    }
}
