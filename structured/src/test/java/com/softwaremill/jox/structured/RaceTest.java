package com.softwaremill.jox.structured;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static com.softwaremill.jox.structured.Race.race;
import static com.softwaremill.jox.structured.Race.timeout;
import static org.junit.jupiter.api.Assertions.*;

public class RaceTest {
    @Test
    void testTimeoutShortCircuitsLongComputation() throws Exception {
        Trail trail = new Trail();
        try {
            timeout(1000, () -> {
                Thread.sleep(2000);
                trail.add("no timeout");
                return null;
            });
        } catch (TimeoutException e) {
            trail.add("timeout");
        }

        trail.add("done");
        Thread.sleep(2000);

        assertIterableEquals(Arrays.asList("timeout", "done"), trail.get());
    }

    @Test
    void testTimeoutDoesNotInterruptShortComputation() throws Exception {
        Trail trail = new Trail();
        try {
            var r = timeout(1000, () -> {
                Thread.sleep(500);
                trail.add("no timeout");
                return 5;
            });
            assertEquals(5, r);
            trail.add("asserted");
        } catch (TimeoutException e) {
            trail.add("timeout");
        }

        trail.add("done");
        Thread.sleep(2000);

        assertIterableEquals(Arrays.asList("no timeout", "asserted", "done"), trail.get());
    }

    @Test
    void testRaceRacesSlowerAndFasterComputation() throws Exception {
        Trail trail = new Trail();
        long start = System.currentTimeMillis();
        race(() -> {
            Thread.sleep(1000);
            trail.add("slow");
            return null;
        }, () -> {
            Thread.sleep(500);
            trail.add("fast");
            return null;
        });
        long end = System.currentTimeMillis();

        Thread.sleep(1000);
        assertIterableEquals(List.of("fast"), trail.get());
        assertTrue(end - start < 1000);
    }

    @Test
    void testRaceRacesFasterAndSlowerComputation() throws Exception {
        Trail trail = new Trail();
        long start = System.currentTimeMillis();
        race(() -> {
            Thread.sleep(500);
            trail.add("fast");
            return null;
        }, () -> {
            Thread.sleep(1000);
            trail.add("slow");
            return null;
        });
        long end = System.currentTimeMillis();

        Thread.sleep(1000);
        assertIterableEquals(List.of("fast"), trail.get());
        assertTrue(end - start < 1000);
    }

    @Test
    void testRaceReturnsFirstSuccessfulComputationToComplete() throws Exception {
        Trail trail = new Trail();
        long start = System.currentTimeMillis();
        race(() -> {
            Thread.sleep(200);
            trail.add("error");
            throw new RuntimeException("boom!");
        }, () -> {
            Thread.sleep(500);
            trail.add("slow");
            return null;
        }, () -> {
            Thread.sleep(1000);
            trail.add("very slow");
            return null;
        });
        long end = System.currentTimeMillis();

        Thread.sleep(1000);
        assertIterableEquals(Arrays.asList("error", "slow"), trail.get());
        assertTrue(end - start < 1000);
    }

    @Test
    void testRaceShouldAddOtherExceptionsAsSuppressed() {
        var exception = assertThrows(JoxScopeExecutionException.class, () -> {
            race(() -> {
                throw new RuntimeException("boom1!");
            }, () -> {
                Thread.sleep(200);
                throw new RuntimeException("boom2!");
            }, () -> {
                Thread.sleep(200);
                throw new RuntimeException("boom3!");
            });
        });

        assertEquals("boom1!", exception.getCause().getMessage());
        assertEquals(Set.of("boom2!", "boom3!"), Arrays.stream(exception.getSuppressed()).map(Throwable::getMessage).collect(Collectors.toSet()));
    }
}
