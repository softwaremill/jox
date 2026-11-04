package com.softwaremill.jox.otel.context;

import java.util.concurrent.Callable;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

import com.softwaremill.jox.structured.Decorator;

/**
 * A {@link Decorator} which propagates the OpenTelemetry {@link Context} to wrapped callable.
 *
 * <p>Intended to be assigned to {@code
 * com.softwaremill.jox.structured.Scope#FORK_DECORATOR}:
 *
 * <pre>{@code
 * ScopedValue.where(Scope.FORK_DECORATOR, PropagatingForkDecorator.INSTANCE)
 *     .call(() -> supervised(scope -> { ... }));
 * }</pre>
 */
public enum PropagatingForkDecorator implements Decorator {
    INSTANCE;

    @Override
    public <T> Callable<T> decorate(Callable<T> f) {
        // captured on the thread which starts the fork, where the context is still current
        Context forkingContext = Context.current();
        return () -> {
            try (Scope ignored = forkingContext.makeCurrent()) {
                return f.call();
            }
        };
    }
}
