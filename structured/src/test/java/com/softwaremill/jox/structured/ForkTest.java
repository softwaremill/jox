package com.softwaremill.jox.structured;

import static com.softwaremill.jox.structured.Scopes.supervised;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class ForkTest {
    @Test
    void testRunTwoForksConcurrently() throws Exception {
        var trail = new Trail();
        supervised(
                scope -> {
                    var f1 =
                            scope.forkUnsupervised(
                                    () -> {
                                        Thread.sleep(500);
                                        trail.add("f1 complete");
                                        return 5;
                                    });
                    var f2 =
                            scope.forkUnsupervised(
                                    () -> {
                                        Thread.sleep(1000);
                                        trail.add("f2 complete");
                                        return 6;
                                    });
                    trail.add("main mid");
                    var result = f1.join() + f2.join();
                    trail.add("result = " + result);
                    return null;
                });

        assertIterableEquals(
                Arrays.asList("main mid", "f1 complete", "f2 complete", "result = 11"),
                trail.get());
    }

    @Test
    void testNestedForks() throws Exception {
        Trail trail = new Trail();
        Scopes.supervised(
                scope -> {
                    var f1 =
                            scope.forkUnsupervised(
                                    () -> {
                                        var f2 =
                                                scope.forkUnsupervised(
                                                        () -> {
                                                            try {
                                                                return 6;
                                                            } finally {
                                                                trail.add("f2 complete");
                                                            }
                                                        });

                                        try {
                                            return 5 + f2.join();
                                        } finally {
                                            trail.add("f1 complete");
                                        }
                                    });

                    trail.add("result = " + f1.join());
                    return null;
                });

        assertIterableEquals(
                Arrays.asList("f2 complete", "f1 complete", "result = 11"), trail.get());
    }

    @Test
    void testInterruptChildForksWhenParentsComplete() throws Exception {
        Trail trail = new Trail();
        Scopes.supervised(
                scope -> {
                    var f1 =
                            scope.forkUnsupervised(
                                    () -> {
                                        scope.forkUnsupervised(
                                                () -> {
                                                    try {
                                                        Thread.sleep(1000);
                                                        trail.add("f2 complete");
                                                        return 6;
                                                    } catch (InterruptedException e) {
                                                        trail.add("f2 interrupted");
                                                        throw e;
                                                    }
                                                });

                                        Thread.sleep(500);
                                        trail.add("f1 complete");
                                        return 5;
                                    });

                    trail.add("main mid");
                    trail.add("result = " + f1.join());
                    return null;
                });

        assertIterableEquals(
                Arrays.asList("main mid", "f1 complete", "result = 5", "f2 interrupted"),
                trail.get());
    }
}
