package com.softwaremill.jox.structured;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
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

        var exception = assertThrows(JoxScopeExecutionException.class, () -> {
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

        assertEquals("x", exception.getCause().getMessage());
        trail.add("done");
        assertIterableEquals(Arrays.asList("b", "done"), trail.get());
    }

    @Test
    void testSupervisedInterruptsMainBodyOnceForkEndsWithException() {
        Trail trail = new Trail();

        var exception = assertThrows(JoxScopeExecutionException.class, () -> {
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

        assertEquals("x", exception.getCause().getMessage());
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

    @Test
    void shouldBeAbleToRethrowOriginalException() {
        class TestException extends Exception {
            public TestException(String message) {
                super(message);
            }
        }

        assertThrows(TestException.class, () -> {
            try {
                Scopes.supervised(scope -> {
                    throw new TestException("x");
                });
            } catch (JoxScopeExecutionException e) {
                e.unwrapAndThrow(TestException.class);
                throw e;
            }
        });
    }

    @Test
    void shouldThrowNothingWhenExceptionIsNotPassed() {
        class TestException extends Exception {
            public TestException(String message) {
                super(message);
            }
        }

        assertDoesNotThrow(() -> {
            try {
                Scopes.supervised(scope -> {
                    throw new RuntimeException("x"); // different exception
                });
            } catch (JoxScopeExecutionException e) {
                e.unwrapAndThrow(TestException.class); // we expect test exception
                // no rethrow of e
            }
        });
    }

    @Test
    void shouldThrowJoxScopeExecutionExceptionWhenExceptionIsNotPassedAndRethrowIsUsed() {
        class TestException extends Exception {
            public TestException(String message) {
                super(message);
            }
        }

        assertThrows(JoxScopeExecutionException.class, () -> {
            try {
                Scopes.supervised(scope -> {
                    throw new RuntimeException("x"); // different exception
                });
            } catch (JoxScopeExecutionException e) {
                e.unwrapAndThrow(TestException.class); // we expect test exception
                throw e; // e is rethrown
            }
        });
    }

    @Test
    void shouldPassSuppressedExceptions() {
        class TestException extends Exception {
            public TestException(String message) {
                super(message);
            }
        }

        TestException testException = assertThrows(TestException.class, () -> {
            try {
                Scopes.supervised(scope -> {
                    scope.fork(() -> {
                        throw new TestException("y");
                    });
                    throw new TestException("x"); // different exception
                });
            } catch (JoxScopeExecutionException e) {
                e.unwrapAndThrow(TestException.class); // we expect test exception
                throw e; // e is rethrown
            }
        });

        assertThat(Arrays.asList(testException.getSuppressed()), hasSize(1));
        assertInstanceOf(TestException.class, testException.getSuppressed()[0]);
    }
}
