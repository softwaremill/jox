package com.softwaremill.jox.structured;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class ActorTest {
    interface Test1 {
        long f(int x);
    }

    @Test
    void shouldInvokeMethodsOnTheActor() throws ExecutionException, InterruptedException {
        Scopes.supervised(scope -> {
            var state = new AtomicLong(0);
            Test1 logic = x -> {
                state.addAndGet(x);
                return state.get();
            };

            var ref = Actor.create(scope, logic);

            assertEquals(10, ref.<Long>ask(l1 -> l1.f(10)));
            assertEquals(30, ref.<Long>ask(l -> l.f(20)));
            return null;
        });
    }

    @Test
    void shouldProtectTheInternalStateOfTheActor() throws ExecutionException, InterruptedException {
        Scopes.supervised(scope -> {
            var state = new AtomicLong(0);
            Test1 logic = x -> {
                state.addAndGet(x);
                return state.get();
            };

            var ref = Actor.create(scope, logic);

            int outer = 1000;
            int inner = 1000;

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
        Scopes.supervised(scope -> {
            var isClosed = new AtomicBoolean(false);
            var thrown = assertThrows(RuntimeException.class, () -> {
                var state = new AtomicLong(0);
                Test1 logic = x -> {
                    state.addAndGet(x);
                    if (state.get() > 2) throw new RuntimeException("too much");
                    return state.get();
                };

                var ref = Actor.create(scope, logic, l -> isClosed.set(true));

                ref.ask(l -> l.f(5));
            });

            assertEquals("too much", thrown.getMessage());
            assertTrue(isClosed.get());
            return null;
        });
    }

    @Test
    void shouldEndTheScopeWhenAnExceptionIsThrownWhenHandlingTell() throws ExecutionException, InterruptedException {
        Scopes.supervised(scope -> {
            var thrown = assertThrows(RuntimeException.class, () -> {
                Test1 logic = x -> {
                    throw new RuntimeException("boom");
                };

                var ref = Actor.create(scope, logic);
                ref.tell(l -> l.f(5));
                Thread.sleep(1000);
            });
            assertEquals("boom", thrown.getMessage());
            return null;
        });
    }
}
