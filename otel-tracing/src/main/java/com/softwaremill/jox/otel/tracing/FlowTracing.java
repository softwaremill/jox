package com.softwaremill.jox.otel.tracing;

import static com.softwaremill.jox.otel.tracing.SpanOptions.spanOptions;

import io.opentelemetry.api.trace.Tracer;

import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.flows.Flows;

/** Helpers for creating OpenTelemetry spans around {@link Flow} elements. */
public final class FlowTracing {
    private FlowTracing() {
    }

    /**
     * Returns a flow, which for each element emitted from the given flow opens a new span. The span is made a
     * child of the current span, and becomes the current one until it ends, when the previously
     * current span is restored.
     *
     * <p>The span ends when the downstream stages are done processing the element, at the first
     * asynchronous boundary (e.g. {@link Flow#buffer()}), or if any of the following stages throws.
     *
     * <p>If a following stage throws, the span's status is set to {@code ERROR} and an appropriate
     * event is added to it.
     */
    public static <T> Flow<T> spanEach(Flow<T> flow, Tracer tracer, String spanName) {
        return spanEach(flow, tracer, spanName, spanOptions());
    }


    /**
     * @see #spanEach(Flow, Tracer, String)
     */
    public static <T> Flow<T> spanEach(
            Flow<T> flow, Tracer tracer, String spanName, SpanOptions options) {
        return Flows.usingEmit(
                emit ->
                        flow.runToEmit(
                                t ->
                                        Tracing.spanRun(
                                                tracer,
                                                spanName,
                                                options,
                                                () -> emit.apply(t))));
    }
}
