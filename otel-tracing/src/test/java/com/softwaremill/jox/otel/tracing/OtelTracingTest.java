package com.softwaremill.jox.otel.tracing;

import static com.softwaremill.jox.otel.tracing.FlowTracing.spanEach;
import static com.softwaremill.jox.otel.tracing.SpanOptions.spanOptions;
import static com.softwaremill.jox.otel.tracing.Tracing.currentSpanUnsafe;
import static com.softwaremill.jox.otel.tracing.Tracing.span;
import static com.softwaremill.jox.otel.tracing.Tracing.spanRun;
import static com.softwaremill.jox.otel.tracing.Tracing.spanUnsafe;
import static com.softwaremill.jox.structured.Scopes.supervised;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.flows.Flows;
import com.softwaremill.jox.otel.context.PropagatingForkDecorator;
import com.softwaremill.jox.structured.Scope;

public class OtelTracingTest {
    private static final InMemorySpanExporter exporter = InMemorySpanExporter.create();

    private static final SdkTracerProvider tracerProvider =
            SdkTracerProvider.builder()
                    .setResource(
                            Resource.create(
                                    Attributes.of(
                                            AttributeKey.stringKey("service.name"),
                                            "otel-tracing-test")))
                    .addSpanProcessor(SimpleSpanProcessor.create(exporter))
                    .build();

    private static final Tracer tracer =
            OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build().getTracer("test");

    @AfterAll
    static void afterAll() {
        tracerProvider.shutdown().join(10, TimeUnit.SECONDS);
    }

    private static final String LEVEL0 = "Level0";
    private static final String LEVEL1 = "Level1";
    private static final String LEVEL1_FIRST = "Level1_First";
    private static final String LEVEL1_FIRST_CHILD = "Level1_First_Child";
    private static final String LEVEL1_STAGE = "Level1_Stage";

    /** Isolates the spans of a single test, which share the exporter, by a random name prefix. */
    private static class Fixture {
        final String prefix = "[" + UUID.randomUUID() + "]";
        final Flow<Integer> flow = Flows.fromValues(1, 2, 3);

        String name(String level) {
            return prefix + "-" + level;
        }

        List<SpanData> testSpans() {
            return exporter.getFinishedSpanItems().stream()
                    .filter(s -> s.getName().startsWith(prefix))
                    .toList();
        }

        List<SpanData> spansOf(String level) {
            return testSpans().stream().filter(s -> s.getName().endsWith(level)).toList();
        }

        void assertNesting() {
            var level0 = spansOf(LEVEL0);
            var level1First = spansOf(LEVEL1_FIRST);
            var level1 = Stream.concat(spansOf(LEVEL1).stream(), level1First.stream()).toList();
            var level1FirstChildren = spansOf(LEVEL1_FIRST_CHILD);

            assertFalse(level0.isEmpty(), "expected Level0 spans to exist for nesting check");
            assertFalse(level1.isEmpty(), "expected Level1 spans to exist for nesting check");
            for (var s : level1) {
                assertTrue(
                        level0.stream().anyMatch(p -> p.getSpanId().equals(s.getParentSpanId())),
                        "Level1 span " + s.getName() + " has no Level0 parent");
            }
            if (!level1FirstChildren.isEmpty()) {
                assertFalse(level1First.isEmpty());
                for (var s : level1FirstChildren) {
                    assertTrue(
                            level1First.stream()
                                    .anyMatch(p -> p.getSpanId().equals(s.getParentSpanId())),
                            "Level1_First_Child span " + s.getName() + " has no parent");
                }
            }
        }
    }

    // region spanEach

    @Test
    void shouldNotOpenASpanUntilTheFlowIsRun() {
        var f = new Fixture();
        spanEach(f.flow, tracer, f.name(LEVEL0));
        assertTrue(f.testSpans().isEmpty());
    }

    @Test
    void shouldRecordExceptionAndSetErrorStatusWhenTheFlowFails() {
        var f = new Fixture();
        var parent = spanUnsafe(tracer, f.name(LEVEL0));

        var options =
                spanOptions()
                        .withBeforeClose(
                                s ->
                                        s.exceptionOpt()
                                                .ifPresent(e -> s.span().addEvent(e.toString())));

        assertThrows(
                Exception.class,
                () ->
                        spanEach(f.flow, tracer, f.name(LEVEL1), options)
                                .map(
                                        x -> {
                                            if (x == 2) throw new Exception("boom");
                                            return x;
                                        })
                                .runToList());

        parent.close();

        assertEquals(2, f.spansOf(LEVEL1).size());
        var failed =
                f.spansOf(LEVEL1).stream()
                        .filter(s -> s.getStatus().getStatusCode() == StatusCode.ERROR)
                        .findFirst()
                        .orElseThrow();
        assertEquals("boom", failed.getStatus().getDescription());
        var event =
                failed.getEvents().stream()
                        .filter(e -> e.getName().equals("exception"))
                        .findFirst()
                        .orElseThrow();
        assertEquals(
                "boom", event.getAttributes().get(AttributeKey.stringKey("exception.message")));
        // custom beforeClose event: e.toString() contains the exception message
        assertTrue(
                failed.getEvents().stream()
                        .anyMatch(
                                e ->
                                        !e.getName().equals("exception")
                                                && e.getName().contains("boom")));
        f.assertNesting();
    }

    @Test
    void shouldOpenASpanPerElementAroundTheStagesPlacedAfterIt() throws Exception {
        var f = new Fixture();
        var parent = spanUnsafe(tracer, f.name(LEVEL0));

        spanEach(f.flow, tracer, f.name(LEVEL1_FIRST))
                .map(x -> span(tracer, f.name(LEVEL1_FIRST_CHILD), () -> x))
                .runToList();

        parent.close();

        assertEquals(3, f.spansOf(LEVEL1_FIRST).size());
        assertEquals(3, f.spansOf(LEVEL1_FIRST_CHILD).size());
        f.assertNesting();
    }

    @Test
    void shouldNotCoverTheStagesPlacedBeforeIt() throws Exception {
        var f = new Fixture();
        var parent = spanUnsafe(tracer, f.name(LEVEL0));

        spanEach(
                        f.flow.map(x -> span(tracer, f.name(LEVEL1), () -> x)),
                        tracer,
                        f.name(LEVEL1_FIRST))
                .runToList();

        parent.close();

        var level0Id = f.spansOf(LEVEL0).getFirst().getSpanId();
        assertEquals(3, f.spansOf(LEVEL1).size());
        assertEquals(3, f.spansOf(LEVEL1_FIRST).size());
        // Level1 spans were opened before spanEach, so they're children of Level0
        assertTrue(f.spansOf(LEVEL1).stream().allMatch(s -> s.getParentSpanId().equals(level0Id)));
        f.assertNesting();
    }

    @Test
    void shouldCoverTheStagesPlacedAfterItAlsoOutsideTheFlatMapItIsUsedIn() throws Exception {
        var f = new Fixture();
        var parent = spanUnsafe(tracer, f.name(LEVEL0));

        Flows.fromValues(1)
                .flatMap(x -> spanEach(Flows.fromValues(x), tracer, f.name(LEVEL1)))
                // outside the flatMap, still covered by the spanEach span
                .map(y -> span(tracer, f.name(LEVEL1_FIRST), () -> y))
                .runToList();

        parent.close();

        assertEquals(1, f.spansOf(LEVEL1).size());
        assertEquals(1, f.spansOf(LEVEL1_FIRST).size());
        var spanEachSpanId = f.spansOf(LEVEL1).getFirst().getSpanId();
        assertEquals(spanEachSpanId, f.spansOf(LEVEL1_FIRST).getFirst().getParentSpanId());
    }

    @Test
    void shouldEndTheSpanAtABufferPlacedAfterIt() throws Exception {
        var f = new Fixture();
        var parent = spanUnsafe(tracer, f.name(LEVEL0));

        ScopedValue.where(Scope.FORK_DECORATOR, PropagatingForkDecorator.INSTANCE)
                .call(
                        () ->
                                spanEach(f.flow, tracer, f.name(LEVEL1))
                                        .buffer()
                                        // after the buffer: not a child of the spanEach span
                                        .map(x -> span(tracer, f.name(LEVEL1_STAGE), () -> x))
                                        .runToList());

        parent.close();

        var level0Id = f.spansOf(LEVEL0).getFirst().getSpanId();
        assertEquals(3, f.spansOf(LEVEL1).size());
        assertEquals(3, f.spansOf(LEVEL1_STAGE).size());
        assertTrue(
                f.spansOf(LEVEL1_STAGE).stream()
                        .allMatch(s -> s.getParentSpanId().equals(level0Id)));
    }

    @Test
    void shouldHaveAllElementSpansAsChildrenOfTheEnclosingSpanWhenRunConcurrently()
            throws Exception {
        var f = new Fixture();
        var parent = spanUnsafe(tracer, f.name(LEVEL0));

        ScopedValue.where(Scope.FORK_DECORATOR, PropagatingForkDecorator.INSTANCE)
                .call(
                        () ->
                                supervised(
                                        scope -> {
                                            for (int i = 0; i < 5; i++) {
                                                scope.fork(
                                                        () ->
                                                                spanEach(
                                                                                f.flow,
                                                                                tracer,
                                                                                f.name(LEVEL1))
                                                                        .runToList());
                                            }
                                            return null;
                                        }));

        parent.close();

        assertEquals(15, f.spansOf(LEVEL1).size());
        f.assertNesting();
    }

    @Test
    void shouldPassSpanKindAndAttributesToSpanEach() throws Exception {
        var f = new Fixture();
        var parent = spanUnsafe(tracer, f.name(LEVEL0));
        var attr = Attributes.of(AttributeKey.stringKey("k"), "v");

        spanEach(
                        f.flow,
                        tracer,
                        f.name(LEVEL1),
                        spanOptions().withSpanKind(SpanKind.CONSUMER).withAttributes(attr))
                .runToList();

        parent.close();

        var spans = f.spansOf(LEVEL1);
        assertEquals(3, spans.size());
        assertTrue(spans.stream().allMatch(s -> s.getKind() == SpanKind.CONSUMER));
        assertTrue(
                spans.stream()
                        .allMatch(
                                s ->
                                        "v"
                                                .equals(
                                                        s.getAttributes()
                                                                .get(
                                                                        AttributeKey.stringKey(
                                                                                "k")))));
    }

    // endregion

    // region context propagation

    @Test
    void shouldPropagateTheContextToForksWithTheDecorator() throws Exception {
        var f = new Fixture();
        var parent = spanUnsafe(tracer, f.name(LEVEL0));

        ScopedValue.where(Scope.FORK_DECORATOR, PropagatingForkDecorator.INSTANCE)
                .call(
                        () ->
                                supervised(
                                        scope ->
                                                scope.fork(
                                                                () ->
                                                                        span(
                                                                                tracer,
                                                                                f.name(LEVEL1),
                                                                                () -> 1))
                                                        .join()));

        parent.close();

        assertEquals(1, f.spansOf(LEVEL1).size());
        f.assertNesting();
    }

    @Test
    void shouldPropagateASpanOpenedInsideTheScopeToForksStartedUnderIt() throws Exception {
        var f = new Fixture();

        // a long-lived scope, with a span opened per unit of work *inside* it
        ScopedValue.where(Scope.FORK_DECORATOR, PropagatingForkDecorator.INSTANCE)
                .call(
                        () ->
                                supervised(
                                        scope -> {
                                            var perItem = spanUnsafe(tracer, f.name(LEVEL0));
                                            scope.forkUser(
                                                            () ->
                                                                    span(
                                                                            tracer,
                                                                            f.name(LEVEL1),
                                                                            () -> 1))
                                                    .join();
                                            perItem.close();
                                            return null;
                                        }));

        assertEquals(1, f.spansOf(LEVEL0).size());
        assertEquals(1, f.spansOf(LEVEL1).size());
        // the forked span must be a child of the span that was current when fork() was called
        assertEquals(
                f.spansOf(LEVEL0).getFirst().getSpanId(),
                f.spansOf(LEVEL1).getFirst().getParentSpanId());
    }

    @Test
    void shouldPropagateTheContextThroughNestedScopesAndForks() throws Exception {
        var f = new Fixture();

        ScopedValue.where(Scope.FORK_DECORATOR, PropagatingForkDecorator.INSTANCE)
                .call(
                        () ->
                                supervised(
                                        outer -> {
                                            var lvl1 = spanUnsafe(tracer, f.name(LEVEL0));
                                            outer.forkUser(
                                                            () -> {
                                                                var lvl2 =
                                                                        spanUnsafe(
                                                                                tracer,
                                                                                f.name(LEVEL1));
                                                                // a nested scope, started from
                                                                // within a fork
                                                                supervised(
                                                                        inner ->
                                                                                inner.forkUser(
                                                                                                () ->
                                                                                                        span(
                                                                                                                tracer,
                                                                                                                f
                                                                                                                        .name(
                                                                                                                                LEVEL1_FIRST_CHILD),
                                                                                                                () ->
                                                                                                                        1))
                                                                                        .join());
                                                                lvl2.close();
                                                                return null;
                                                            })
                                                    .join();
                                            lvl1.close();
                                            return null;
                                        }));

        assertEquals(1, f.spansOf(LEVEL0).size());
        assertEquals(1, f.spansOf(LEVEL1).size());
        assertEquals(1, f.spansOf(LEVEL1_FIRST_CHILD).size());
        // the chain nests at every hop: scope body -> fork -> nested scope's fork
        assertEquals(
                f.spansOf(LEVEL0).getFirst().getSpanId(),
                f.spansOf(LEVEL1).getFirst().getParentSpanId());
        assertEquals(
                f.spansOf(LEVEL1).getFirst().getSpanId(),
                f.spansOf(LEVEL1_FIRST_CHILD).getFirst().getParentSpanId());
    }

    @Test
    void shouldNotPropagateTheContextToForksWithoutTheDecorator() throws Exception {
        var f = new Fixture();
        var parent = spanUnsafe(tracer, f.name(LEVEL0));

        supervised(scope -> scope.fork(() -> span(tracer, f.name(LEVEL1), () -> 1)).join());

        parent.close();

        var level0Id = f.spansOf(LEVEL0).getFirst().getSpanId();
        assertEquals(1, f.spansOf(LEVEL1).size());
        // without the decorator, the fork's span is not a child of Level0
        assertNotEquals(level0Id, f.spansOf(LEVEL1).getFirst().getParentSpanId());
    }

    // endregion

    // region span

    @Test
    void shouldReturnTheResultAndNotMarkTheSpanAsFailedWhenTheCodeSucceeds() throws Exception {
        var f = new Fixture();
        var result = span(tracer, f.name(LEVEL0), () -> 1);

        assertEquals(1, result);
        var s = f.spansOf(LEVEL0).getFirst();
        assertEquals(StatusCode.UNSET, s.getStatus().getStatusCode());
        assertTrue(s.getEvents().isEmpty());
    }

    @Test
    void shouldRecordAThrownExceptionAndPassItToBeforeClose() {
        var f = new Fixture();
        var seen = new AtomicReference<Optional<Throwable>>();

        var thrown =
                assertThrows(
                        Exception.class,
                        () ->
                                span(
                                        tracer,
                                        f.name(LEVEL0),
                                        spanOptions()
                                                .withBeforeClose(s -> seen.set(s.exceptionOpt())),
                                        () -> {
                                            throw new Exception("boom");
                                        }));

        assertEquals("boom", thrown.getMessage());
        assertEquals("boom", seen.get().orElseThrow().getMessage());
        var s = f.spansOf(LEVEL0).getFirst();
        assertEquals(StatusCode.ERROR, s.getStatus().getStatusCode());
        assertEquals("boom", s.getStatus().getDescription());
    }

    @Test
    void shouldLeaveTheStatusDescriptionEmptyWhenTheExceptionHasNoMessage() {
        var f = new Fixture();

        assertThrows(
                Exception.class,
                () ->
                        spanRun(
                                tracer,
                                f.name(LEVEL0),
                                () -> {
                                    throw new RuntimeException();
                                }));

        var s = f.spansOf(LEVEL0).getFirst();
        assertEquals(StatusCode.ERROR, s.getStatus().getStatusCode());
        // not the literal string "null"
        assertEquals("", s.getStatus().getDescription());
    }

    @Test
    void shouldThrowFromBeforeCloseAndCloseTheSpanWhenTheCodeSucceeds() {
        var f = new Fixture();

        var thrown =
                assertThrows(
                        Exception.class,
                        () ->
                                span(
                                        tracer,
                                        f.name(LEVEL0),
                                        spanOptions()
                                                .withBeforeClose(
                                                        s -> {
                                                            throw new RuntimeException("cb");
                                                        }),
                                        () -> 1));

        assertEquals("cb", thrown.getMessage());
        assertEquals(1, f.spansOf(LEVEL0).size());
        assertEquals(StatusCode.UNSET, f.spansOf(LEVEL0).getFirst().getStatus().getStatusCode());
    }

    @Test
    void shouldAddTheExceptionOfBeforeCloseAsSuppressedToTheThrownOne() {
        var f = new Fixture();
        var cause = new Exception("cause");

        var thrown =
                assertThrows(
                        Exception.class,
                        () ->
                                span(
                                        tracer,
                                        f.name(LEVEL0),
                                        spanOptions()
                                                .withBeforeClose(
                                                        s -> {
                                                            throw new RuntimeException(
                                                                    "from beforeClose");
                                                        }),
                                        () -> {
                                            throw cause;
                                        }));

        assertSame(cause, thrown);
        assertTrue(
                Arrays.stream(thrown.getSuppressed())
                        .anyMatch(e -> "from beforeClose".equals(e.getMessage())));
        assertEquals(1, f.spansOf(LEVEL0).size());
    }

    @Test
    void shouldAddTheExceptionOfBeforeCloseAsSuppressedToAnInterruption() {
        var f = new Fixture();
        var interruption = new InterruptedException();

        var thrown =
                assertThrows(
                        InterruptedException.class,
                        () ->
                                spanRun(
                                        tracer,
                                        f.name(LEVEL0),
                                        spanOptions()
                                                .withBeforeClose(
                                                        s -> {
                                                            throw new RuntimeException(
                                                                    "from beforeClose");
                                                        }),
                                        () -> {
                                            throw interruption;
                                        }));

        // the interruption isn't recorded, but the callback's exception is still suppressed onto it
        assertSame(interruption, thrown);
        assertTrue(
                Arrays.stream(thrown.getSuppressed())
                        .anyMatch(e -> "from beforeClose".equals(e.getMessage())));
        assertEquals(1, f.spansOf(LEVEL0).size());
        assertEquals(StatusCode.UNSET, f.spansOf(LEVEL0).getFirst().getStatus().getStatusCode());
    }

    @Test
    void shouldPassSpanKindAndAttributesToTheSpanBuilder() throws Exception {
        var f = new Fixture();
        var attr = Attributes.of(AttributeKey.stringKey("k"), "v");

        spanRun(
                tracer,
                f.name(LEVEL0),
                spanOptions().withSpanKind(SpanKind.PRODUCER).withAttributes(attr),
                () -> {});

        var s = f.spansOf(LEVEL0).getFirst();
        assertEquals(SpanKind.PRODUCER, s.getKind());
        assertEquals("v", s.getAttributes().get(AttributeKey.stringKey("k")));
    }

    @Test
    void shouldPassLinksToTheSpanBuilderInOrder() throws Exception {
        var f = new Fixture();
        var first = spanUnsafe(tracer, f.name("LinkA"));
        first.close();
        var second = spanUnsafe(tracer, f.name("LinkB"));
        second.close();

        spanRun(
                tracer,
                f.name(LEVEL0),
                spanOptions()
                        .withLinks(
                                List.of(
                                        first.span().getSpanContext(),
                                        second.span().getSpanContext())),
                () -> {});

        var links = f.spansOf(LEVEL0).getFirst().getLinks();
        assertEquals(2, links.size());
        assertEquals(
                first.span().getSpanContext().getSpanId(),
                links.get(0).getSpanContext().getSpanId());
        assertEquals(
                second.span().getSpanContext().getSpanId(),
                links.get(1).getSpanContext().getSpanId());
    }

    @Test
    void shouldRestoreThePreviousSpanAsCurrentAfterSpanReturns() throws Exception {
        var f = new Fixture();
        var outer = spanUnsafe(tracer, f.name(LEVEL0));
        spanRun(tracer, f.name(LEVEL1), () -> {});
        assertSame(outer.span(), currentSpanUnsafe());
        outer.close();
    }

    @Test
    void shouldRestoreThePreviousSpanAsCurrentAfterSpanThrows() {
        var f = new Fixture();
        var outer = spanUnsafe(tracer, f.name(LEVEL0));
        assertThrows(
                Exception.class,
                () ->
                        spanRun(
                                tracer,
                                f.name(LEVEL1),
                                () -> {
                                    throw new Exception("boom");
                                }));
        assertSame(outer.span(), currentSpanUnsafe());
        outer.close();
    }

    // endregion

    // region interruptions and wrapped exceptions

    @Test
    void shouldNotTreatAnInterruptionAsAFailure() {
        var f = new Fixture();
        var seen = new AtomicReference<Optional<Throwable>>();

        assertThrows(
                InterruptedException.class,
                () ->
                        spanRun(
                                tracer,
                                f.name(LEVEL0),
                                spanOptions().withBeforeClose(s -> seen.set(s.exceptionOpt())),
                                () -> {
                                    throw new InterruptedException();
                                }));

        assertTrue(seen.get().isEmpty());
        assertEquals(StatusCode.UNSET, f.spansOf(LEVEL0).getFirst().getStatus().getStatusCode());
    }

    @Test
    void shouldTreatAnInterruptionDuringIoAsNotAFailure() {
        var f = new Fixture();
        var seen = new AtomicReference<Optional<Throwable>>();

        assertThrows(
                ClosedByInterruptException.class,
                () ->
                        spanRun(
                                tracer,
                                f.name(LEVEL0),
                                spanOptions().withBeforeClose(s -> seen.set(s.exceptionOpt())),
                                () -> {
                                    throw new ClosedByInterruptException();
                                }));

        assertTrue(seen.get().isEmpty());
        assertEquals(StatusCode.UNSET, f.spansOf(LEVEL0).getFirst().getStatus().getStatusCode());
    }

    @Test
    void shouldRecordTheClosingOfAChannelByAnotherThreadAsAFailure() {
        var f = new Fixture();

        assertThrows(
                AsynchronousCloseException.class,
                () ->
                        spanRun(
                                tracer,
                                f.name(LEVEL0),
                                () -> {
                                    throw new AsynchronousCloseException();
                                }));

        assertEquals(StatusCode.ERROR, f.spansOf(LEVEL0).getFirst().getStatus().getStatusCode());
    }

    @Test
    void shouldTreatAnExecutionExceptionWrappingAnInterruptionAsNotAFailure() {
        var f = new Fixture();
        var seen = new AtomicReference<Optional<Throwable>>();

        assertThrows(
                ExecutionException.class,
                () ->
                        spanRun(
                                tracer,
                                f.name(LEVEL0),
                                spanOptions().withBeforeClose(s -> seen.set(s.exceptionOpt())),
                                () -> {
                                    throw new ExecutionException(new InterruptedException());
                                }));

        assertTrue(seen.get().isEmpty());
        assertEquals(StatusCode.UNSET, f.spansOf(LEVEL0).getFirst().getStatus().getStatusCode());
    }

    @Test
    void shouldTreatAnExecutionExceptionWrappingAFailureAsAFailure() {
        var f = new Fixture();

        assertThrows(
                ExecutionException.class,
                () ->
                        spanRun(
                                tracer,
                                f.name(LEVEL0),
                                () -> {
                                    throw new ExecutionException(new Exception("boom"));
                                }));

        assertEquals(StatusCode.ERROR, f.spansOf(LEVEL0).getFirst().getStatus().getStatusCode());
    }

    @Test
    void shouldRecordFatalErrorsAsFailures() {
        var f = new Fixture();

        assertThrows(
                LinkageError.class,
                () ->
                        spanRun(
                                tracer,
                                f.name(LEVEL0),
                                () -> {
                                    throw new LinkageError("broken");
                                }));

        assertEquals(StatusCode.ERROR, f.spansOf(LEVEL0).getFirst().getStatus().getStatusCode());
    }

    @Test
    void shouldEndSpanWhenTheEnclosingForkIsCancelled() throws Exception {
        var f = new Fixture();
        var semaphore = new Semaphore(0);

        ScopedValue.where(Scope.FORK_DECORATOR, PropagatingForkDecorator.INSTANCE)
                .call(
                        () ->
                                supervised(
                                        scope -> {
                                            var fork =
                                                    scope.forkCancellable(
                                                            () ->
                                                                    spanEach(
                                                                                    f.flow,
                                                                                    tracer,
                                                                                    f.name(LEVEL1))
                                                                            .tap(
                                                                                    x -> {
                                                                                        if (x
                                                                                                == 1) {
                                                                                            semaphore
                                                                                                    .release();
                                                                                        } else {
                                                                                            Thread
                                                                                                    .sleep(
                                                                                                            Duration
                                                                                                                    .ofSeconds(
                                                                                                                            100));
                                                                                        }
                                                                                    })
                                                                            .runToList());
                                            semaphore.acquire();
                                            fork.cancelNow();
                                            return null;
                                        }));

        // the first element's span was started; interruption is not a failure
        assertFalse(f.spansOf(LEVEL1).isEmpty());
        assertTrue(
                f.spansOf(LEVEL1).stream()
                        .allMatch(s -> s.getStatus().getStatusCode() == StatusCode.UNSET));
    }

    // endregion
}
