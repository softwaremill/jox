package com.softwaremill.jox.otel.tracing;

import java.util.Optional;

import io.opentelemetry.api.trace.Span;

/**
 * A span, together with the exception thrown by the code it covers, if any.
 *
 * @param span the span, e.g. to set attributes or add events before it is closed
 * @param exceptionOpt the exception thrown by the covered code, if any. Empty for exceptions which
 *     aren't failures, such as interruptions.
 */
public record SpanWithError(Span span, Optional<Throwable> exceptionOpt) {}
