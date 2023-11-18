package jox;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.concurrent.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChannelTest {
    @Test
    public void testSimpleSendReceive() throws InterruptedException, ExecutionException {
        // given
        Channel<String> channel = new Channel<>();

        // when
        scoped(scope -> {
            forkVoid(scope, () -> {
                forkVoid(scope, () -> channel.send("x"));
                var t2 = fork(scope, channel::receive);

                // then
                assertEquals("x", t2.get());
            });
            return null;
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
            fs.forEach(f -> {
                try {
                    f.get();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            });

            // then
            assertEquals(1000, s.size());
            return null;
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
            var r = forkVoid(scope, () -> {
                for (int i = 1; i <= 1000; i++) {
                    s.add(channel.receive());
                }
            });
            try {
                r.get();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            // then
            assertEquals(1000, s.size());
            return null;
        });
    }

    @Test
    public void performanceTest() {
        for (int j = 1; j <= 3; j++) {
            var max = 10_000_000L;
            var c = new Channel<Integer>();
            timed("rendezvous", () -> {
                try {
                    scoped(scope -> {
                        forkVoid(scope, () -> {
                            forkVoid(scope, () -> {
                                for (int i = 0; i <= max; i++) {
                                    c.sendSafe(i);
                                }
                            });
                            var r = fork(scope, () -> {
                                var acc = 0L;
                                for (int i = 0; i <= max; i++) {
                                    acc += (Integer) c.receiveSafe();
                                }
                                return acc;
                            });

                            try {
                                assertEquals(max * (max + 1L) / 2, r.get());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                throw new RuntimeException(ex);
                            }
                        });
                        return null;
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
        }
    }

    //

    private void scoped(Function<StructuredTaskScope, Void> f) throws InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            f.apply(scope);
            scope.join().throwIfFailed();
        }
    }

    private <T> Future<T> fork(StructuredTaskScope scope, Callable<T> c) {
        var f = new CompletableFuture<T>();
        scope.fork(() -> {
            try {
                f.complete(c.call());
            } catch (Exception ex) {
                ex.printStackTrace();
                f.completeExceptionally(ex);
            }
            return null;
        });
        return f;
    }

    private Future<Void> forkVoid(StructuredTaskScope scope, RunnableWithException r) {
        return fork(scope, () -> {
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