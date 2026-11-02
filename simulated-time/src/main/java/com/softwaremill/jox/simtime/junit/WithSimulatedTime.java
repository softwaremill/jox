package com.softwaremill.jox.simtime.junit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

import com.softwaremill.jox.simtime.SimulationOptions;

/**
 * Runs each test of the annotated class, or the annotated test method, using simulated time. Each
 * test, together with its {@code @BeforeEach} and {@code @AfterEach} methods, runs in one
 * simulation.
 *
 * @see SimulatedTimeExtension
 * @see SimulationOptions
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith(SimulatedTimeExtension.class)
public @interface WithSimulatedTime {
    /** See {@link SimulationOptions#watchdogTimeout()}. */
    long watchdogSeconds() default SimulationOptions.DEFAULT_WATCHDOG_SECONDS;

    /** See {@link SimulationOptions#maxVirtualTime()}. */
    long maxVirtualTimeDays() default SimulationOptions.DEFAULT_MAX_VIRTUAL_TIME_DAYS;
}
