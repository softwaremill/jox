package jox;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.concurrent.*;

import static jox.TestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChannelTest {
    @Test
    public void testSimpleSendReceive() throws InterruptedException, ExecutionException {
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
