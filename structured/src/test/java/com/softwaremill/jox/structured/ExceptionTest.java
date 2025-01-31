package com.softwaremill.jox.structured;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExceptionTest {
    static class CustomException extends RuntimeException {}

    static class CustomException2 extends RuntimeException {}

    static class CustomException3 extends RuntimeException {
        CustomException3(Exception e) {
            super(e);
        }
    }

    @Test
    void testUnsupervisedThrowsExceptionThrownByJoinedFork() throws InterruptedException {
        Trail trail = new Trail();
        try {
            Scopes.unsupervised(scope -> {
                scope.forkUnsupervised(() -> {
                    throw new CustomException();
                }).join();
                return null;
            });
        } catch (JoxScopeExecutionException e) {
            // the first EE, wraps CE, and is thrown by the join(); the second - wraps the first and is thrown by unsupervised
            trail.add(e.getCause().getCause().getClass().getSimpleName());
        }

        assertIterableEquals(List.of("CustomException"), trail.get());
    }

    @Test
    void testSupervisedThrowsExceptionThrownInScope() throws InterruptedException {
        Trail trail = new Trail();
        try {
            Scopes.supervised(scope -> {
                throw new CustomException();
            });
        } catch (JoxScopeExecutionException e) {
            trail.add(e.getCause().getClass().getSimpleName());
        }

        assertIterableEquals(List.of("CustomException"), trail.get());
    }

    @Test
    void testSupervisedThrowsExceptionThrownByFailingFork() throws InterruptedException {
        Trail trail = new Trail();
        try {
            Scopes.supervised(scope -> {
                scope.forkUser(() -> {
                    throw new CustomException();
                });
                return null;
            });
        } catch (JoxScopeExecutionException e) {
            trail.add(e.getCause().getClass().getSimpleName());
        }

        assertIterableEquals(List.of("CustomException"), trail.get());
    }

    @Test
    void testSupervisedInterruptsOtherForksWhenFailureAddSuppressedInterruptedExceptions() throws InterruptedException {
        Trail trail = new Trail();
        Semaphore s = new Semaphore(0);

        try {
            Scopes.supervised(scope -> {
                scope.forkUser(() -> {
                    s.acquire(); // will never complete
                    return null;
                });
                scope.forkUser(() -> {
                    s.acquire(); // will never complete
                    return null;
                });
                scope.forkUser(() -> {
                    Thread.sleep(100);
                    throw new CustomException();
                });
                return null;
            });
        } catch (JoxScopeExecutionException e) {
            trail.add(e.getCause().getClass().getSimpleName());
            addExceptionWithSuppressedTo(trail, e);
        }

        assertIterableEquals(List.of("CustomException", "JoxScopeExecutionException(suppressed=InterruptedException,InterruptedException)"), trail.get());
    }

    @Test
    void testSupervisedInterruptsOtherForksWhenFailureAddSuppressedCustomExceptions() throws InterruptedException {
        Trail trail = new Trail();
        Semaphore s = new Semaphore(0);

        try {
            Scopes.supervised(scope -> {
                scope.forkUser(() -> {
                    try {
                        s.acquire(); // will never complete
                    } finally {
                        throw new CustomException2();
                    }
                });
                scope.forkUser(() -> {
                    Thread.sleep(100);
                    throw new CustomException();
                });
                return null;
            });
        } catch (JoxScopeExecutionException e) {
            trail.add(e.getCause().getClass().getSimpleName());
            addExceptionWithSuppressedTo(trail, e);
        }

        assertIterableEquals(List.of("CustomException", "JoxScopeExecutionException(suppressed=CustomException2)"), trail.get());
    }

    @Test
    void testSupervisedDoesNotAddOriginalExceptionAsSuppressed() throws InterruptedException {
        Trail trail = new Trail();

        try {
            Scopes.supervised(scope -> {
                var f = scope.fork(() -> {
                    throw new CustomException();
                });
                f.join();
                return null;
            });
        } catch (JoxScopeExecutionException e) {
            addExceptionWithSuppressedTo(trail, e);
        }

        // either join() might throw the original exception (shouldn't be suppressed), or it might be interrupted before
        // throwing (should be suppressed then)
        List<String> expected1 = List.of("JoxScopeExecutionException(suppressed=)");
        List<String> expected2 = List.of("JoxScopeExecutionException(suppressed=InterruptedException)");

        assertTrue(trail.get().equals(expected1) || trail.get().equals(expected2));
    }

    @Test
    void testSupervisedAddsExceptionAsSuppressedEvenIfWrapsOriginalException() throws InterruptedException {
        Trail trail = new Trail();

        try {
            Scopes.supervised(scope -> {
                var f = scope.fork(() -> {
                    throw new CustomException();
                });
                try {
                    f.join();
                } catch (Exception e) {
                    throw new CustomException3(e);
                }
                return null;
            });
        } catch (JoxScopeExecutionException e) {
            trail.add(e.getCause().getClass().getSimpleName());
            addExceptionWithSuppressedTo(trail, e);
        }

        assertIterableEquals(List.of("CustomException", "JoxScopeExecutionException(suppressed=CustomException3)"), trail.get());
    }

    private void addExceptionWithSuppressedTo(Trail trail, Throwable e) {
        String[] suppressed = new String[e.getSuppressed().length];
        for (int i = 0; i < e.getSuppressed().length; i++) {
            suppressed[i] = e.getSuppressed()[i].getClass().getSimpleName();
        }
        trail.add(e.getClass().getSimpleName() + "(suppressed=" + String.join(",", suppressed) + ")");
    }
}
