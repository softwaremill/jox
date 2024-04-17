package com.softwaremill.jox;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static com.softwaremill.jox.TestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

public class ChannelRendezvousTest {
    @Test
    void testSimpleSendReceive() throws InterruptedException, ExecutionException {
        // given
        Channel<String> channel = new Channel<>();

        // when
        scoped(scope -> {
            forkVoid(scope, () -> channel.send("x"));
            var t2 = fork(scope, channel::receive);

            // then
            assertEquals("x", t2.get());
        });
    }

    @Test
    void testSendReceiveInManyForks() throws ExecutionException, InterruptedException {
        // given
        Channel<Integer> channel = new Channel<>();
        var fs = new HashSet<Future<Void>>();
        var s = new ConcurrentSkipListSet<Integer>();

        // when
        scoped(scope -> {
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

    @Test
    void testSendReceiveManyElementsInTwoForks() throws ExecutionException, InterruptedException {
        // given
        Channel<Integer> channel = new Channel<>();
        var s = new ConcurrentSkipListSet<Integer>();

        // when
        scoped(scope -> {
            forkVoid(scope, () -> {
                for (int i = 1; i <= 1000; i++) {
                    channel.send(i);
                }
            });
            forkVoid(scope, () -> {
                for (int i = 1; i <= 1000; i++) {
                    s.add(channel.receive());
                }
            }).get();

            // then
            assertEquals(1000, s.size());
        });
    }

    @Test
    void testSendWaitsForRendezvous() throws ExecutionException, InterruptedException {
        // given
        Channel<Integer> channel = new Channel<>();
        var trail = new ConcurrentLinkedQueue<String>();

        // when
        scoped(scope -> {
            forkVoid(scope, () -> {
                channel.send(1);
                trail.add("S");
            });
            forkVoid(scope, () -> {
                channel.send(2);
                trail.add("S");
            });
            forkVoid(scope, () -> {
                Thread.sleep(100L);
                trail.add("R1");
                var r1 = channel.receive();
                Thread.sleep(100L);
                trail.add("R2");
                var r2 = channel.receive();
                assertEquals(Set.of(1, 2), Set.of(r1, r2));
            }).get();

            Thread.sleep(100L); // allow trail to be finished

            // then
            assertEquals(List.of("R1", "S", "R2", "S"), trail.stream().toList());
        });
    }

    @Test
    void pendingReceivesShouldGetNotifiedThatChannelIsDone() throws InterruptedException, ExecutionException {
        // given
        Channel<Integer> c = new Channel<>();
        scoped(scope -> {
            var f = fork(scope, c::receiveOrClosed);

            // when
            Thread.sleep(100L);
            c.done();

            // then
            assertEquals(new ChannelDone(), f.get());

            // should be rejected immediately
            assertEquals(new ChannelDone(), c.receiveOrClosed());
        });
    }

    @Test
    void pendingSendsShouldGetNotifiedThatChannelIsErrored() throws InterruptedException, ExecutionException {
        // given
        Channel<Integer> c = new Channel<>();
        scoped(scope -> {
            var f = fork(scope, () -> c.sendOrClosed(1));

            // when
            Thread.sleep(100L);
            c.error(new RuntimeException());

            // then
            assertInstanceOf(ChannelError.class, f.get());

            // should be rejected immediately
            assertInstanceOf(ChannelError.class, c.sendOrClosed(2));
        });
    }

    @Test
    @Disabled("moved to RendezvousBenchmark, left here for development purposes")
    void performanceTest() throws Exception {
        for (int j = 1; j <= 10; j++) {
            var max = 10_000_000L;
            var c = new Channel<Integer>();
            timed("rendezvous", () -> {
                scoped(scope -> {
                    forkVoid(scope, () -> {
                        for (int i = 0; i <= max; i++) {
                            c.send(i);
                        }
                    });
                    var result = fork(scope, () -> {
                        var acc = 0L;
                        for (int i = 0; i <= max; i++) {
                            acc += c.receive();
                        }
                        return acc;
                    });

                    assertEquals(max * (max + 1L) / 2, result.get());
                });
            });
        }
    }
}
