package com.softwaremill.jox.otel.tracing;

import static com.softwaremill.jox.otel.tracing.SpanOptions.spanOptions;

import java.nio.channels.ClosedByInterruptException;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;

import com.softwaremill.jox.structured.ThrowingRunnable;

/** Helpers for creating OpenTelemetry spans around code which uses Jox's concurrency operators. */
public final class Tracing {
    private Tracing() {}

    /**
     * Returns the current span. This is not unsafe per se, but the caller is responsible for using
     * the span properly, e.g. not ending a span that is managed elsewhere.
     */
    public static Span currentSpanUnsafe() {
        return Span.fromContext(Context.current());
    }

    /**
     * Opens a new {@link Span} as a child of the current span. The new span becomes the current
     * one.
     *
     * <p>Unsafe, because the caller is responsible for closing the returned {@link
     * AutoCloseableSpan}, which ends the span and restores the previously current one.
     * Try-with-resources guarantees that.
     *
     * <p>For the context to be propagated to forks, the {@code PropagatingForkDecorator} from the
     * {@code otel-context} module is required: it has to be set as the fork decorator used by
     * concurrency scopes, using the {@code Scope#FORK_DECORATOR} scoped value.
     *
     * @return an {@link AutoCloseableSpan}, which closes the opened span
     */
    public static AutoCloseableSpan spanUnsafe(Tracer tracer, String spanName) {
        return spanUnsafe(tracer, spanName, spanOptions());
    }

    /**
     * @see #spanUnsafe(Tracer, String)
     */
    public static AutoCloseableSpan spanUnsafe(
            Tracer tracer, String spanName, SpanOptions options) {
        var previousContext = Context.current();
        SpanBuilder builder =
                tracer.spanBuilder(spanName)
                        .setSpanKind(options.spanKind())
                        .setAllAttributes(options.attributes())
                        .setParent(previousContext);
        for (var link : options.links()) {
            builder = builder.addLink(link);
        }
        var started = builder.startSpan();
        var scope = previousContext.with(started).makeCurrent();
        return new AutoCloseableSpan(started, scope);
    }

    /**
     * Evaluates {@code t} within a new span, which is made a child of the current span, and becomes
     * the current one. The span is ended once {@code t} completes, and the previously current span
     * is restored.
     *
     * <p>If {@code t} throws, the exception is recorded on the span as an event, and the span's
     * status is set to {@code ERROR}. Interruptions are not failures: they end the span normally.
     */
    public static <T> T span(Tracer tracer, String spanName, Callable<T> t) throws Exception {
        return span(tracer, spanName, spanOptions(), t);
    }

    /**
     * @see #span(Tracer, String, Callable)
     */
    public static <T> T span(Tracer tracer, String spanName, SpanOptions options, Callable<T> t)
            throws Exception {
        var finalizer = spanUnsafe(tracer, spanName, options);
        Throwable thrown = null;
        T result = null;
        Optional<Throwable> actionableThrown = Optional.empty();
        try {
            try {
                result = t.call();
            } catch (Throwable e) {
                thrown = e;
                if (shouldRecordDefect(e)) {
                    actionableThrown = Optional.of(e);
                    finalizer.span().recordException(e);
                    finalizer.span().setStatus(StatusCode.ERROR, e.getMessage());
                }
            }

            try {
                options.beforeClose().accept(new SpanWithError(finalizer.span(), actionableThrown));
            } catch (Throwable e) {
                if (thrown != null) {
                    thrown.addSuppressed(e);
                } else {
                    throw e;
                }
            }
        } finally {
            finalizer.close();
        }

        if (thrown != null) {
            throw sneakyThrow(thrown);
        }
        return result;
    }

    /** As {@link #span(Tracer, String, Callable)}, for code which doesn't return a value. */
    public static void spanRun(Tracer tracer, String spanName, ThrowingRunnable t)
            throws Exception {
        spanRun(tracer, spanName, spanOptions(), t);
    }

    /**
     * @see #spanRun(Tracer, String, ThrowingRunnable)
     */
    public static void spanRun(
            Tracer tracer, String spanName, SpanOptions options, ThrowingRunnable t)
            throws Exception {
        span(
                tracer,
                spanName,
                options,
                () -> {
                    t.run();
                    return null;
                });
    }

    /**
     * Interruptions aren't failures of the covered code, so they end the span normally. Exceptions
     * which only wrap the actual cause are unwrapped first.
     */
    private static boolean shouldRecordDefect(Throwable e) {
        while (true) {
            if (e instanceof InterruptedException || e instanceof ClosedByInterruptException) {
                return false;
            }
            if (e instanceof ExecutionException && e.getCause() != null) {
                e = e.getCause();
                continue;
            }
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> E sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }
}
