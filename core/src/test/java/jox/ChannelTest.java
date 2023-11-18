package jox;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.concurrent.*;

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
    public void testSendReceiveInManyForks() throws ExecutionException, InterruptedException {
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
    public void testSendReceiveManyElementsInTwoForks() throws ExecutionException, InterruptedException {
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
    public void performanceTest() throws Exception {
        for (int j = 1; j <= 3; j++) {
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

    //

    private void scoped(ConsumerWithException<StructuredTaskScope<Object>> f) throws InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // making sure everything runs in a VT
            scope.fork(() -> {
                f.accept(scope);
                return null;
            });
            scope.join().throwIfFailed();
        }
    }

    private <T> Future<T> fork(StructuredTaskScope<Object> scope, Callable<T> c) {
        var f = new CompletableFuture<T>();
        scope.fork(() -> {
            try {
                f.complete(c.call());
            } catch (Exception ex) {
                f.completeExceptionally(ex);
            }
            return null;
        });
        return f;
    }

    private Future<Void> forkVoid(StructuredTaskScope<Object> scope, RunnableWithException r) {
        return fork(scope, () -> {
            r.run();
            return null;
        });
    }

    @FunctionalInterface
    private interface ConsumerWithException<T> {
        void accept(T o) throws Exception;
    }

    @FunctionalInterface
    private interface RunnableWithException {
        void run() throws Exception;
    }

    private void timed(String label, RunnableWithException block) throws Exception {
        var start = System.nanoTime();
        block.run();
        var end = System.nanoTime();
        System.out.println(label + " took: " + (end - start) / 1_000_000 + " ms");
    }
}