package com.softwaremill.jox.flows;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.reactivestreams.tck.TestEnvironment;
import org.reactivestreams.tck.flow.FlowPublisherVerification;

import com.softwaremill.jox.structured.Scope;
import com.softwaremill.jox.structured.Scopes;

public class FlowPublisherTckTest {

    private final AtomicReference<Scope> scope = new AtomicReference<>();
    private final FlowPublisherVerification<Integer> verification =
            new FlowPublisherVerification<>(new TestEnvironment()) {
                @Override
                public Publisher<Integer> createFlowPublisher(long l) {
                    Flow<Integer> flow = Flows.range(1, (int) l, 1);
                    return flow.toPublisher(scope.get());
                }

                @Override
                public Publisher<Integer> createFailedFlowPublisher() {
                    return Flows.<Integer>failed(new RuntimeException("boom"))
                            .toPublisher(scope.get());
                }
            };

    @Test
    void verifyTckScenarios() throws InterruptedException {
        List<Throwable> errors = new ArrayList<>();
        // We are invoking tests manually as we need to set separate supervised scope for each test
        for (Method method : verification.getClass().getMethods()) {
            if (method.getAnnotation(org.testng.annotations.Test.class) != null) {
                if (method.getName().startsWith("untested_")) {
                    continue;
                }
                Scopes.supervised(
                        s -> {
                            scope.set(s);
                            try {
                                method.invoke(verification);
                            } catch (InvocationTargetException e) {
                                handleInvocationTargetException(method, e);
                                errors.add(e.getCause());
                            }
                            return null;
                        });
                scope.set(null);
            }
        }
        assertTrue(errors.isEmpty(), "Test suite returned errors");
    }

    private static void handleInvocationTargetException(Method method, Throwable e) {
        Throwable cause = e.getCause();
        String errorMessage =
                String.format(
                        "Error in method %s:%n%s%n%s",
                        method.getName(), cause.getMessage(), getStackTrace(cause));
        System.err.println(errorMessage);
    }

    private static String getStackTrace(Throwable t) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : t.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
