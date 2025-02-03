package com.softwaremill.jox.structured;

import org.junit.jupiter.api.Test;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScopeTest {

    @Test
    void externalRunnerShouldBeInitializedOnce() throws ExecutionException, InterruptedException {
        // given
        Queue<ActorRef<ExternalScheduler>> results = new ConcurrentLinkedQueue<>();
        int numberOfCalls = 1_000_000;

        // when
        Scopes.supervised(scope -> {
            for (int i = 0; i < numberOfCalls; i++) {
                scope.forkUser(() -> {
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
}
