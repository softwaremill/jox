package com.softwaremill.jox.otel.context;

import java.util.concurrent.Callable;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

import com.softwaremill.jox.structured.ForkDecorator;

/**
 * A {@link ForkDecorator} which propagates the OpenTelemetry {@link Context} to forks: the context
 * which is current when a fork is started becomes current in the fork's body.
 *
 * <p>Stateless, hence a single instance - the context is captured each time a fork is decorated.
 *
 * <p>Should be set as the decorator used by concurrency scopes, through the {@code
 * com.softwaremill.jox.structured.Scope#FORK_DECORATOR} scoped value:
 *
 * <pre>{@code
 * ScopedValue.where(Scope.FORK_DECORATOR, PropagatingForkDecorator.INSTANCE)
 *     .call(() -> supervised(scope -> { ... }));
 * }</pre>
 */
public enum PropagatingForkDecorator implements ForkDecorator {
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
