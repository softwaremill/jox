package com.softwaremill.jox.structured;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class ActorRefTest {
    interface ITest {
        long f(int x);
    }

    @Test
    void shouldInvokeMethodsOnTheActor() throws ExecutionException, InterruptedException {
        Scopes.supervised(scope -> {
            // given
            var state = new AtomicLong(0);
            ITest logic = x -> {
                state.addAndGet(x);
                return state.get();
            };

            var ref = ActorRef.create(scope, logic);

            // when & then
            assertEquals(10, ref.<Long>ask(l1 -> l1.f(10)));
            assertEquals(30, ref.<Long>ask(l -> l.f(20)));
            return null;
        });
    }

    @Test
    void shouldProtectTheInternalStateOfTheActor() throws ExecutionException, InterruptedException {
        Scopes.supervised(scope -> {
            // given
            var state = new AtomicLong(0);
            ITest logic = x -> {
                state.addAndGet(x);
                return state.get();
            };

            var ref = ActorRef.create(scope, logic);

            int outer = 1000;
            int inner = 1000;


            // when & then
            var forks = IntStream.rangeClosed(1, outer)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        IntStream.rangeClosed(1, inner).forEach(j -> {
                            try {
                                ref.ask(l -> l.f(1));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }))
                    .toList();

            forks.forEach(CompletableFuture::join);

            assertEquals(outer * inner, ref.<Long>ask(l -> l.f(0)));
            return null;
        });
    }

    @Test
    void shouldRunTheCloseCallbackBeforeRethrowingTheException() throws ExecutionException, InterruptedException {
        // given
        var isClosed = new AtomicBoolean(false);

        // when
        var thrown = Scopes.supervised(scope ->
             assertThrows(RuntimeException.class, () -> {
                var state = new AtomicLong(0);
                ITest logic = x -> {
                    state.addAndGet(x);
                    if (state.get() > 2) throw new RuntimeException("too much");
                    return state.get();
                };

                var ref = ActorRef.create(scope, logic, l -> isClosed.set(true));

                ref.ask(l -> l.f(5));
            })
        );

        // then
        assertEquals("too much", thrown.getMessage());
        assertTrue(isClosed.get());
    }

    @Test
    void shouldEndTheScopeWhenAnExceptionIsThrownWhenHandlingTell() {
        // when
        var thrown = assertThrows(ExecutionException.class, () ->
                Scopes.supervised(scope -> {
                    ITest logic = x -> {
                        throw new RuntimeException("boom");
                    };

                    var ref = ActorRef.create(scope, logic);
                    ref.tell(l -> l.f(5));
                    Thread.sleep(1000);
                    return null;
                })
        );

        // then
        assertEquals("boom", thrown.getCause().getMessage());
    }
}
