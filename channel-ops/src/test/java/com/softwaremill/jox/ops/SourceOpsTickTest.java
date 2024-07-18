package com.softwaremill.jox.ops;

import com.softwaremill.jox.structured.Scopes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SourceOpsTickTest {
    @Test
    void testTickRegularly() throws Exception {
        Scopes.supervised(scope -> {
            long start = System.currentTimeMillis();
            var c = new SourceOps(scope).tick(100, "tick").toSource();

            assertEquals("tick", c.receive());
            long elapsed = System.currentTimeMillis() - start;
            assertTrue(elapsed >= 0L && elapsed <= 50L);

            assertEquals("tick", c.receive());
            elapsed = System.currentTimeMillis() - start;
            assertTrue(elapsed >= 100L && elapsed <= 150L);

            assertEquals("tick", c.receive());
            elapsed = System.currentTimeMillis() - start;
            assertTrue(elapsed >= 200L && elapsed <= 250L);

            return null;
        });
    }

    @Test
    void testTickImmediatelyInCaseOfSlowConsumerAndThenResumeNormal() throws Exception {
        Scopes.supervised(scope -> {
            long start = System.currentTimeMillis();
            var c = new SourceOps(scope).tick(100, "tick").toSource();

            // Simulating a slow consumer
            Thread.sleep(200);
            assertEquals("tick", c.receive()); // a tick should be waiting
            long elapsed = System.currentTimeMillis() - start;
            assertTrue(elapsed >= 200L && elapsed <= 250L);

            assertEquals("tick", c.receive()); // and immediately another, as the interval between send-s has passed
            elapsed = System.currentTimeMillis() - start;
            assertTrue(elapsed >= 200L && elapsed <= 250L);

            return null;
        });
    }
}
