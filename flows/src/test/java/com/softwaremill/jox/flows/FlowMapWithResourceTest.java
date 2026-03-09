package com.softwaremill.jox.flows;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class FlowMapWithResourceTest {

    @Test
    void shouldCreateUseAndCloseResource() throws Exception {
        // given
        List<String> lifecycle = new ArrayList<>();
        Flow<Integer> flow = Flows.fromValues(1, 2, 3);

        // when
        List<Integer> result =
                flow.mapWithResource(
                                () -> {
                                    lifecycle.add("create");
                                    return "resource";
                                },
                                r -> {
                                    lifecycle.add("close");
                                    return Optional.empty();
                                },
                                (r, t) -> {
                                    lifecycle.add("use-" + t);
                                    return t * 10;
                                })
                        .runToList();

        // then
        assertEquals(List.of(10, 20, 30), result);
        assertEquals(List.of("create", "use-1", "use-2", "use-3", "close"), lifecycle);
    }

    @Test
    void shouldEmitFinalElementFromClose() throws Exception {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2);

        // when
        List<Integer> result =
                flow.mapWithResource(() -> "resource", r -> Optional.of(99), (r, t) -> t * 10)
                        .runToList();

        // then
        assertEquals(List.of(10, 20, 99), result);
    }

    @Test
    void shouldCloseResourceOnError() throws Exception {
        // given
        List<String> lifecycle = new ArrayList<>();
        Flow<Integer> flow =
                Flows.fromValues(1, 2).concat(Flows.failed(new RuntimeException("boom")));

        // when & then
        assertThrows(
                Exception.class,
                () ->
                        flow.mapWithResource(
                                        () -> {
                                            lifecycle.add("create");
                                            return "resource";
                                        },
                                        r -> {
                                            lifecycle.add("close");
                                            return Optional.empty();
                                        },
                                        (r, t) -> t)
                                .runToList());

        // then — resource was still closed
        assertTrue(lifecycle.contains("close"));
    }

    @Test
    void shouldDropCloseReturnValueOnError() {
        // given
        Flow<Integer> flow =
                Flows.fromValues(1, 2, 3)
                        .map(
                                t -> {
                                    if (t == 3) throw new RuntimeException("boom");
                                    return t;
                                });

        // when & then
        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () ->
                                flow.mapWithResource(
                                                () -> "resource",
                                                r -> Optional.of(999),
                                                (r, t) -> t)
                                        .runToList());
        assertEquals("boom", thrown.getMessage());
    }

    @Test
    void shouldSuppressCloseExceptionWhenUpstreamFails() {
        // given
        RuntimeException upstreamError = new RuntimeException("upstream");
        Flow<Integer> flow = Flows.fromValues(1).concat(Flows.failed(upstreamError));

        // when & then
        Exception thrown =
                assertThrows(
                        Exception.class,
                        () ->
                                flow.mapWithResource(
                                                () -> "resource",
                                                r -> {
                                                    throw new RuntimeException("close");
                                                },
                                                (r, t) -> t)
                                        .runToList());

        // find the upstream exception in the cause chain
        Throwable upstream = thrown;
        while (upstream != null && upstream != upstreamError) {
            upstream = upstream.getCause();
        }
        assertNotNull(upstream, "upstream exception should be in the cause chain");
        assertEquals(1, upstream.getSuppressed().length);
        assertEquals("close", upstream.getSuppressed()[0].getMessage());
    }

    @Test
    void shouldThrowCloseExceptionWhenUpstreamSucceeds() {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2);

        // when & then
        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () ->
                                flow.mapWithResource(
                                                () -> "resource",
                                                r -> {
                                                    throw new RuntimeException("close failed");
                                                },
                                                (r, t) -> t)
                                        .runToList());
        assertEquals("close failed", thrown.getMessage());
    }

    @Test
    void shouldPropagateCreateException() {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2);

        // when & then
        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () ->
                                flow.mapWithResource(
                                                () -> {
                                                    throw new RuntimeException("create failed");
                                                },
                                                r -> Optional.empty(),
                                                (r, t) -> t)
                                        .runToList());
        assertEquals("create failed", thrown.getMessage());
    }

    @Test
    void shouldHandleEmptyFlow() throws Exception {
        // given
        List<String> lifecycle = new ArrayList<>();
        Flow<Integer> flow = Flows.empty();

        // when
        List<Integer> result =
                flow.mapWithResource(
                                () -> {
                                    lifecycle.add("create");
                                    return "resource";
                                },
                                r -> {
                                    lifecycle.add("close");
                                    return Optional.of(42);
                                },
                                (r, t) -> t)
                        .runToList();

        // then — final element is emitted even for empty flow
        assertEquals(List.of(42), result);
        assertEquals(List.of("create", "close"), lifecycle);
    }

    @Test
    void shouldPropagateErrorInMappingFunctionAndCloseResource() {
        // given
        List<String> lifecycle = new ArrayList<>();
        Flow<Integer> flow = Flows.fromValues(1, 2, 3);

        // when & then
        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () ->
                                flow.mapWithResource(
                                                () -> {
                                                    lifecycle.add("create");
                                                    return "resource";
                                                },
                                                r -> {
                                                    lifecycle.add("close");
                                                    return Optional.empty();
                                                },
                                                (r, t) -> {
                                                    if (t == 2)
                                                        throw new RuntimeException("map error");
                                                    return t;
                                                })
                                        .runToList());
        assertEquals("map error", thrown.getMessage());
        assertTrue(lifecycle.contains("close"));
    }

    // --- AutoCloseable variant ---

    @Test
    void closeable_shouldCreateUseAndCloseResource() throws Exception {
        // given
        List<String> lifecycle = new ArrayList<>();

        class TestResource implements AutoCloseable {
            TestResource() {
                lifecycle.add("create");
            }

            @Override
            public void close() {
                lifecycle.add("close");
            }
        }

        Flow<Integer> flow = Flows.fromValues(1, 2, 3);

        // when
        List<Integer> result =
                flow.mapWithCloseableResource(TestResource::new, (r, t) -> t * 10).runToList();

        // then
        assertEquals(List.of(10, 20, 30), result);
        assertEquals(List.of("create", "close"), lifecycle);
    }

    @Test
    void closeable_shouldCloseResourceWhenMappingFunctionErrors() {
        // given
        List<String> lifecycle = new ArrayList<>();

        class TestResource implements AutoCloseable {
            TestResource() {
                lifecycle.add("create");
            }

            @Override
            public void close() {
                lifecycle.add("close");
            }
        }

        Flow<Integer> flow = Flows.fromValues(1, 2, 3);

        // when & then
        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () ->
                                flow.mapWithCloseableResource(
                                                TestResource::new,
                                                (r, t) -> {
                                                    if (t == 2) throw new RuntimeException("boom");
                                                    return t;
                                                })
                                        .runToList());
        assertEquals("boom", thrown.getMessage());
        assertTrue(lifecycle.contains("close"));
    }
}
