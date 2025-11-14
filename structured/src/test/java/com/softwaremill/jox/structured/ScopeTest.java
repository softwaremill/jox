package com.softwaremill.jox.structured;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

public class ScopeTest {

    @Test
    void externalRunnerShouldBeInitializedOnce() throws ExecutionException, InterruptedException {
        // given
        Queue<ActorRef<ExternalScheduler>> results = new ConcurrentLinkedQueue<>();
        int numberOfCalls = 1_000_000;

        // when
        Scopes.supervised(
                scope -> {
                    for (int i = 0; i < numberOfCalls; i++) {
                        scope.forkUser(
                                () -> {
                                    results.add(scope.externalRunner().scheduler());
                                    return null;
                                });
                    }
                    return null;
                });

        // then
        assertEquals(numberOfCalls, results.size());

        ActorRef<ExternalScheduler> peek = results.peek(); // all elements should be the same
        results.forEach(r -> assertEquals(peek, r));
    }

    /**
     * @see com.softwaremill.jox.structured.CancellableForkUsingResult#cancel
     */
    @Test
    void testForkCancelBehavior() throws InterruptedException, ExecutionException {
        var run = new AtomicBoolean(false);
        var fork =
                (CancellableForkUsingResult<Integer>)
                        new Scope()
                                .forkCancellable(
                                        () -> {
                                            run.set(true);
                                            return 42;
                                        });
        ExecutionException ee = assertThrows(ExecutionException.class, fork::cancel);
        assertInstanceOf(InterruptedException.class, ee.getCause());
        assertFalse(run.get());

        ee = assertThrows(ExecutionException.class, fork::join);
        assertInstanceOf(InterruptedException.class, ee.getCause());

        ee = assertThrows(ExecutionException.class, fork::cancel);
        assertInstanceOf(InterruptedException.class, ee.getCause());
    }
}
