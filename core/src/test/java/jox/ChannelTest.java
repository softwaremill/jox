package jox;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChannelTest {
    private final ExecutorService e = Executors.newVirtualThreadPerTaskExecutor();

    @Test
    public void testSimpleSendReceive() throws InterruptedException, ExecutionException {
        // given
        Channel<String> channel = new Channel<>();

        // when
        forkVoid(() -> channel.send("x"));
        var t2 = fork(channel::receive);

        // then
        assertEquals("x", t2.get());
    }

    @Test
    public void testSendReceiveInManyForks() {
        // given
        Channel<Integer> channel = new Channel<>();
        var fs = new HashSet<Future<Void>>();
        var s = new ConcurrentSkipListSet<Integer>();

        // when
        for (int i = 1; i <= 1000; i++) {
            int ii = i;
            forkVoid(() -> channel.send(ii));
        }
        for (int i = 1; i <= 1000; i++) {
            fs.add(forkVoid(() -> s.add(channel.receive())));
        }
        fs.forEach(f -> {
            try {
                f.get();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        // then
        assertEquals(1000, s.size());
    }

    @Test
    public void testSendReceiveManyElementsInTwoForks() throws ExecutionException, InterruptedException {
        // given
        Channel<Integer> channel = new Channel<>();
        var s = new ConcurrentSkipListSet<Integer>();

        // when
        forkVoid(() -> {
            for (int i = 1; i <= 1000; i++) {
                channel.send(i);
            }
        });
        var r = forkVoid(() -> {
            for (int i = 1; i <= 1000; i++) {
                s.add(channel.receive());
            }
        });
        r.get();

        // then
        assertEquals(1000, s.size());
    }

    @Test
    public void performanceTest() {
        for (int j = 1; j <= 10; j++) {
            var max = 1_000_000L;
            var c = new Channel<Integer>();
            timed("rendezvous", () -> {
                forkVoid(() -> {
                    for (int i = 0; i <= max; i++) {
                        c.send(i);
                    }
                });
                var r = fork(() -> {
                    var acc = 0L;
                    for (int i = 0; i <= max; i++) {
                        acc += c.receive();
                    }
                    return acc;
                });

                try {
                    assertEquals(max * (max + 1L) / 2, r.get());
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }

    //

    private <T> Future<T> fork(Callable<T> c) {
        return e.submit(c);
    }

    private Future<Void> forkVoid(RunnableWithException r) {
        return e.submit(() -> {
            r.run();
            return null;
        });
    }

    @FunctionalInterface
    private interface RunnableWithException {
        void run() throws Exception;
    }

    private void timed(String label, Runnable block) {
        var start = System.nanoTime();
        block.run();
        var end = System.nanoTime();
        System.out.println(label + " took: " + (end - start) / 1_000_000 + " ms");
    }
}