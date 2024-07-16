package com.softwaremill.jox.structured;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SupervisedTest {
    @Test
    void testSupervisedWaitsUntilAllForksComplete() throws Exception {
        Trail trail = new Trail();

        int result = Scopes.supervised(scope -> {
            scope.forkUser(() -> {
                Thread.sleep(200);
                trail.add("a");
                return null;
            });

            scope.forkUser(() -> {
                Thread.sleep(100);
                trail.add("b");
                return null;
            });

            return 2;
        });

        assertEquals(2, result);
        trail.add("done");
        assertIterableEquals(Arrays.asList("b", "a", "done"), trail.get());
    }

    @Test
    void testSupervisedOnlyWaitsUntilUserForksComplete() throws Exception {
        Trail trail = new Trail();

        int result = Scopes.supervised(scope -> {
            scope.fork(() -> {
                Thread.sleep(200);
                trail.add("a");
                return null;
            });

            scope.forkUser(() -> {
                Thread.sleep(100);
                trail.add("b");
                return null;
            });

            return 2;
        });

        assertEquals(2, result);
        trail.add("done");
        assertIterableEquals(Arrays.asList("b", "done"), trail.get());
    }

    @Test
    void testSupervisedInterruptsOnceAnyForkEndsWithException() {
        Trail trail = new Trail();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            Scopes.supervised(scope -> {
                scope.forkUser(() -> {
                    Thread.sleep(300);
                    trail.add("a");
                    return null;
                });

                scope.forkUser(() -> {
                    Thread.sleep(200);
                    throw new RuntimeException("x");
                });

                scope.forkUser(() -> {
                    Thread.sleep(100);
                    trail.add("b");
                    return null;
                });

                return 2;
            });
        });

        assertEquals("x", exception.getMessage());
        trail.add("done");
        assertIterableEquals(Arrays.asList("b", "done"), trail.get());
    }

    @Test
    void testSupervisedInterruptsMainBodyOnceForkEndsWithException() {
        Trail trail = new Trail();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            Scopes.supervised(scope -> {
                scope.forkUser(() -> {
                    Thread.sleep(200);
                    throw new RuntimeException("x");
                });

                Thread.sleep(300);
                trail.add("a");
                return null;
            });
        });

        assertEquals("x", exception.getMessage());
        trail.add("done");
        assertIterableEquals(List.of("done"), trail.get());
    }

    @Test
    void testSupervisedDoesNotInterruptIfUnsupervisedForkEndsWithException() throws Exception {
        Trail trail = new Trail();

        int result = Scopes.supervised(scope -> {
            scope.forkUser(() -> {
                Thread.sleep(300);
                trail.add("a");
                return null;
            });

            scope.forkUnsupervised(() -> {
                Thread.sleep(200);
                throw new RuntimeException("x");
            });

            scope.forkUser(() -> {
                Thread.sleep(100);
                trail.add("b");
                return null;
            });

            return 2;
        });

        assertEquals(2, result);
        trail.add("done");
        assertIterableEquals(Arrays.asList("b", "a", "done"), trail.get());
    }
}
