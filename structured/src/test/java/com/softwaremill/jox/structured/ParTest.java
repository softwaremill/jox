package com.softwaremill.jox.structured;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.softwaremill.jox.structured.Par.par;
import static com.softwaremill.jox.structured.Par.parLimit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class ParTest {
    @Test
    void testParRunsComputationsInParallel() throws Exception {
        Trail trail = new Trail();
        var result = par(List.of(() -> {
            Thread.sleep(200);
            trail.add("a");
            return 1;
        }, () -> {
            Thread.sleep(100);
            trail.add("b");
            return 2;
        }));

        trail.add("done");

        assertIterableEquals(List.of(1, 2), result);
        assertIterableEquals(Arrays.asList("b", "a", "done"), trail.get());
    }

    @Test
    void testParInterruptsOtherComputationsIfOneFails() throws InterruptedException {
        Trail trail = new Trail();
        try {
            par(List.of(() -> {
                Thread.sleep(200);
                trail.add("par 1 done");
                return null;
            }, () -> {
                Thread.sleep(100);
                trail.add("exception");
                throw new Exception("boom");
            }));
        } catch (ExecutionException e) {
            if (e.getCause().getMessage().equals("boom")) {
                trail.add("catch");
            }
        }

        // Checking if the forks aren't left running
        Thread.sleep(300);
        trail.add("all done");

        assertIterableEquals(Arrays.asList("exception", "catch", "all done"), trail.get());
    }

    @Test
    void testParLimitRunsUpToGivenNumberOfComputationsInParallel() throws Exception {
        AtomicInteger running = new AtomicInteger(0);
        AtomicInteger max = new AtomicInteger(0);
        var result = parLimit(2, IntStream.rangeClosed(1, 9).<Callable<Integer>>mapToObj(i -> () -> {
            int current = running.incrementAndGet();
            max.updateAndGet(m -> Math.max(current, m));
            Thread.sleep(100);
            running.decrementAndGet();
            return i * 2;
        }).toList());

        assertIterableEquals(List.of(2, 4, 6, 8, 10, 12, 14, 16, 18), result);
        assertEquals(2, max.get());
    }

    @Test
    void testParLimitInterruptsOtherComputationsIfOneFails() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        Trail trail = new Trail();
        try {
            parLimit(2, IntStream.rangeClosed(1, 5).<Callable<Void>>mapToObj(i -> () -> {
                if (counter.incrementAndGet() == 4) {
                    Thread.sleep(10);
                    trail.add("exception");
                    throw new Exception("boom");
                } else {
                    Thread.sleep(200);
                    trail.add("x");
                    return null;
                }
            }).toList());
        } catch (ExecutionException e) {
            if (e.getCause().getMessage().equals("boom")) {
                trail.add("catch");
            }
        }

        Thread.sleep(300);
        trail.add("all done");

        assertIterableEquals(Arrays.asList("x", "x", "exception", "catch", "all done"), trail.get());
    }
}
