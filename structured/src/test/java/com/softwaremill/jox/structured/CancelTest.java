package com.softwaremill.jox.structured;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

import org.junit.jupiter.api.Test;

public class CancelTest {
    @Test
    void testCancelBlocksUntilForkCompletes() throws Exception {
        Trail trail = new Trail();
        Scopes.supervised(
                scope -> {
                    var f =
                            scope.forkCancellable(
                                    () -> {
                                        trail.add("started");
                                        try {
                                            Thread.sleep(500);
                                            trail.add("main done");
                                        } catch (InterruptedException e) {
                                            trail.add("interrupted");
                                            Thread.sleep(500);
                                            trail.add("interrupted done");
                                            throw e;
                                        }
                                        return null;
                                    });

                    Thread.sleep(100); // making sure the fork starts
                    try {
                        f.cancel();
                    } catch (ExecutionException e) {
                        // ignore
                    }
                    trail.add("cancel done");
                    Thread.sleep(1000);
                    return null;
                });
        assertIterableEquals(
                Arrays.asList("started", "interrupted", "interrupted done", "cancel done"),
                trail.get());
    }

    @Test
    void testCancelBlocksUntilForkCompletesStressTest() throws Exception {
        for (int i = 1; i <= 20; i++) {
            Trail trail = new Trail();
            Semaphore s = new Semaphore(0);
            int finalI = i;
            Scopes.supervised(
                    scope -> {
                        var f =
                                scope.forkCancellable(
                                        () -> {
                                            try {
                                                s.acquire();
                                                trail.add("main done");
                                            } catch (InterruptedException e) {
                                                trail.add("interrupted");
                                                Thread.sleep(100);
                                                trail.add("interrupted done");
                                            }
                                            return null;
                                        });

                        if (finalI % 2 == 0)
                            Thread.sleep(
                                    1); // interleave immediate cancels and after the fork starts
                        // (probably)
                        try {
                            f.cancel();
                        } catch (ExecutionException e) {
                            // ignore
                        }
                        s.release(1); // the acquire should be interrupted
                        trail.add("cancel done");
                        Thread.sleep(100);
                        return null;
                    });
            if (trail.get().size() == 1) {
                assertIterableEquals(
                        List.of("cancel done"), trail.get()); // the fork wasn't even started
            } else {
                assertIterableEquals(
                        Arrays.asList("interrupted", "interrupted done", "cancel done"),
                        trail.get());
            }
        }
    }

    @Test
    void testCancelNowReturnsImmediatelyAndWaitForForksWhenScopeCompletes() throws Exception {
        Trail trail = new Trail();
        Scopes.supervised(
                scope -> {
                    var f =
                            scope.forkCancellable(
                                    () -> {
                                        try {
                                            Thread.sleep(500);
                                            trail.add("main done");
                                        } catch (InterruptedException e) {
                                            Thread.sleep(500);
                                            trail.add("interrupted done");
                                        }
                                        return null;
                                    });

                    Thread.sleep(100); // making sure the fork starts
                    f.cancelNow();
                    trail.add("cancel done");
                    assertIterableEquals(List.of("cancel done"), trail.get());
                    return null;
                });
        assertIterableEquals(Arrays.asList("cancel done", "interrupted done"), trail.get());
    }

    @Test
    void testCancelNowFollowedByJoinEitherCatchesInterruptedExceptionWithWhichForkEnds() {
        assertThrows(
                JoxScopeExecutionException.class,
                () ->
                        Scopes.supervised(
                                scope -> {
                                    var f =
                                            scope.forkCancellable(
                                                    () -> {
                                                        Thread.sleep(200);
                                                        return null;
                                                    });
                                    Thread.sleep(100);
                                    f.cancelNow();
                                    return f.join();
                                }));
    }
}
