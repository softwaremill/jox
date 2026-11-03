package com.softwaremill.jox.otel.tracing;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;

/**
 * An opened {@link Span}, which is also the current one, together with the {@link Scope} that made
 * it current.
 *
 * <p>Calling {@link #close()} ends the span and closes the scope, restoring the previously current
 * span. Prefer try-with-resources, so that the span is always closed.
 *
 * <p>Example:
 *
 * <pre>{@code
 * try (var opened = Tracing.spanUnsafe(tracer, "my-span")) {
 *     foo();
 *     opened.span().setAttribute("key", "value");
 *     bar();
 * }
 * }</pre>
 */
public final class AutoCloseableSpan implements AutoCloseable {
    private final Span span;
    private final Scope openedScope;

    AutoCloseableSpan(Span span, Scope openedScope) {
        this.span = span;
        this.openedScope = openedScope;
    }

    /** The opened span, e.g. to set attributes or add events. */
    public Span span() {
        return span;
    }

    @Override
    public void close() {
        span.end();
        openedScope.close();
    }
}
