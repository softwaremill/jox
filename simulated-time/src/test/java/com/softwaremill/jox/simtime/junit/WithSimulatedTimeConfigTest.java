package com.softwaremill.jox.simtime.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import java.time.Duration;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;

import com.softwaremill.jox.simtime.SimulatedTimeException.RunawayTimeException;
import com.softwaremill.jox.simtime.SimulatedTimeException.WatchdogException;

/** Runs sample test classes, which are only executed through the test kit. */
class WithSimulatedTimeConfigTest {
    @WithSimulatedTime(watchdogSeconds = 1)
    static class WatchdogSample {
        static class SleepsInStaticInitializer {
            static {
                try {
                    Thread.sleep(Duration.ofHours(1));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            static void touch() {}
        }

        @Test
        void blocksTheCarrier() {
            SleepsInStaticInitializer.touch();
        }
    }

    @Test
    void classAnnotationConfiguresTheWatchdog() {
        var failure =
                assertInstanceOf(WatchdogException.class, singleFailure(WatchdogSample.class));
        assertTrue(failure.getMessage().contains("PT1S"), failure.getMessage());
    }

    @WithSimulatedTime(maxVirtualTimeDays = 365)
    static class RunawaySample {
        @Test
        @WithSimulatedTime(maxVirtualTimeDays = 1)
        void sleepsTooLong() throws InterruptedException {
            Thread.sleep(Duration.ofDays(2));
        }
    }

    @Test
    void methodAnnotationOverridesClassAnnotation() {
        assertInstanceOf(RunawayTimeException.class, singleFailure(RunawaySample.class));
    }

    @WithSimulatedTime(maxVirtualTimeDays = 1)
    static class OuterSample {
        @Nested
        class NestedSample {
            @Test
            void sleepsTooLong() throws InterruptedException {
                Thread.sleep(Duration.ofDays(2));
            }
        }
    }

    @Test
    void nestedClassesUseTheConfigurationOfTheEnclosingClass() {
        assertInstanceOf(RunawayTimeException.class, singleFailure(OuterSample.class));
    }

    @WithSimulatedTime(maxVirtualTimeDays = Long.MAX_VALUE)
    static class TooLongSample {
        @Test
        void test() {}
    }

    @Test
    void tooLargeValuesAreRejected() {
        var failure =
                assertInstanceOf(
                        IllegalArgumentException.class, singleFailure(TooLongSample.class));
        assertTrue(failure.getMessage().contains("maxVirtualTime"), failure.getMessage());
    }

    @WithSimulatedTime
    static class TestFactorySample {
        @TestFactory
        Stream<DynamicTest> dynamicTests() {
            return Stream.of(DynamicTest.dynamicTest("sleeps", () -> Thread.sleep(1)));
        }
    }

    @Test
    void dynamicTestsAreRejected() {
        Events containers =
                EngineTestKit.engine("junit-jupiter")
                        .selectors(selectClass(TestFactorySample.class))
                        .execute()
                        .containerEvents();
        Throwable failure = firstFailure(containers);
        assertTrue(failure.getMessage().contains("@TestFactory"), failure.getMessage());
    }

    private static Throwable singleFailure(Class<?> testClass) {
        Events events =
                EngineTestKit.engine("junit-jupiter")
                        .selectors(selectClass(testClass))
                        .execute()
                        .testEvents();
        assertEquals(1, events.failed().count());
        return firstFailure(events);
    }

    private static Throwable firstFailure(Events events) {
        return events.failed().stream()
                .findFirst()
                .orElseThrow()
                .getRequiredPayload(TestExecutionResult.class)
                .getThrowable()
                .orElseThrow();
    }
}
