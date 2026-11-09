package com.softwaremill.jox.simtime.junit;

import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.commons.support.AnnotationSupport;

import com.softwaremill.jox.simtime.Simulation;
import com.softwaremill.jox.simtime.SimulationOptions;

/**
 * Runs each test, together with its {@code @BeforeEach} and {@code @AfterEach} methods, in one
 * {@link Simulation}. Usually registered using {@link WithSimulatedTime}, which also configures the
 * simulation.
 *
 * <p>Test, test template ({@code @RepeatedTest}, {@code @ParameterizedTest}) and lifecycle methods
 * run in virtual threads. {@code @TestFactory} methods are rejected. JUnit's {@code @Timeout}
 * measures real time. Test class constructors, field initializers and {@code @BeforeAll} /
 * {@code @AfterAll} methods run outside the simulation, using real time.
 */
public final class SimulatedTimeExtension implements InvocationInterceptor {
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(SimulatedTimeExtension.class);

    @Override
    public void interceptBeforeEachMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext)
            throws Throwable {
        runSimulated(invocation, extensionContext);
    }

    @Override
    public void interceptTestMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext)
            throws Throwable {
        runSimulated(invocation, extensionContext);
    }

    @Override
    public void interceptTestTemplateMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext)
            throws Throwable {
        runSimulated(invocation, extensionContext);
    }

    @Override
    public void interceptAfterEachMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext)
            throws Throwable {
        runSimulated(invocation, extensionContext);
    }

    /** Dynamic tests would run outside the simulation, using real time. */
    @Override
    public <T> T interceptTestFactoryMethod(
            Invocation<T> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) {
        invocation.skip();
        throw new ExtensionConfigurationException(
                "@TestFactory (dynamic tests) isn't supported with simulated time: "
                        + extensionContext.getDisplayName());
    }

    private static void runSimulated(Invocation<Void> invocation, ExtensionContext context)
            throws Exception {
        Simulation simulation =
                context.getStore(NAMESPACE)
                        .computeIfAbsent(
                                Simulation.class,
                                _ -> new Simulation(options(context)),
                                Simulation.class);
        simulation.run(
                () -> {
                    try {
                        invocation.proceed();
                    } catch (Exception | Error e) {
                        throw e;
                    } catch (Throwable t) {
                        throw new UndeclaredThrowableException(t);
                    }
                    return null;
                });
    }

    private static SimulationOptions options(ExtensionContext context) {
        return findAnnotation(context)
                .map(
                        a ->
                                new SimulationOptions(
                                        Duration.ofSeconds(a.watchdogSeconds()),
                                        maxVirtualTime(a.maxVirtualTimeDays())))
                .orElse(SimulationOptions.DEFAULT);
    }

    private static Duration maxVirtualTime(long days) {
        try {
            return Duration.ofDays(days);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("maxVirtualTime is too large: " + days + " days", e);
        }
    }

    private static Optional<WithSimulatedTime> findAnnotation(ExtensionContext context) {
        return AnnotationSupport.findAnnotation(context.getTestMethod(), WithSimulatedTime.class)
                .or(
                        () ->
                                AnnotationSupport.findAnnotation(
                                        context.getRequiredTestClass(),
                                        WithSimulatedTime.class,
                                        context.getEnclosingTestClasses()));
    }
}
