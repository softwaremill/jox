package com.softwaremill.jox.otel.tracing;

import java.util.List;
import java.util.function.Consumer;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;

/**
 * Optional parameters of a span, reflecting the OpenTelemetry span builder, plus a {@code
 * beforeClose} callback.
 *
 * @param spanKind the kind of the span
 * @param attributes the attributes to set on the span
 * @param links the span contexts to link to
 * @param beforeClose callback executed right before closing the span, which receives the span and
 *     the failure of the covered code, if any. If it throws, the exception is added as suppressed
 *     to the one thrown by the covered code, or thrown if that code succeeded.
 */
public record SpanOptions(
        SpanKind spanKind,
        Attributes attributes,
        List<SpanContext> links,
        Consumer<SpanWithError> beforeClose) {

    private static final SpanOptions DEFAULT =
            new SpanOptions(SpanKind.INTERNAL, Attributes.empty(), List.of(), _ -> {});

    public static SpanOptions spanOptions() {
        return DEFAULT;
    }

    public SpanOptions withSpanKind(SpanKind spanKind) {
        return new SpanOptions(spanKind, attributes, links, beforeClose);
    }

    public SpanOptions withAttributes(Attributes attributes) {
        return new SpanOptions(spanKind, attributes, links, beforeClose);
    }

    public SpanOptions withLinks(List<SpanContext> links) {
        return new SpanOptions(spanKind, attributes, List.copyOf(links), beforeClose);
    }

    /**
     * @see SpanOptions#beforeClose()
     */
    public SpanOptions withBeforeClose(Consumer<SpanWithError> beforeClose) {
        return new SpanOptions(spanKind, attributes, links, beforeClose);
    }
}
