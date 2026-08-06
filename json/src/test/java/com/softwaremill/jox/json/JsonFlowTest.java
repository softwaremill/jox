package com.softwaremill.jox.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.softwaremill.jox.flows.ByteChunk;
import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.flows.Flows;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class JsonFlowTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @TempDir Path tempDir;

    @Test
    void shouldParseNdjsonLineEndingsBlankLinesAndFinalUnterminatedRecord() throws Exception {
        // given
        var input =
                byteFlow(
                        """

                        {"name":"Ada","age":36}\r
                           \t
                        {"name":"Łukasz","age":41}\
                        """);

        // when
        var result = JsonFlow.parseNdjson(input, Person.class).runToList();

        // then
        assertEquals(List.of(new Person("Ada", 36), new Person("Łukasz", 41)), result);
    }

    @Test
    void shouldParseEmptyNdjsonAndEmptyArray() throws Exception {
        assertEquals(List.of(), JsonFlow.parseNdjson(byteFlow(""), Person.class).runToList());
        assertEquals(List.of(), JsonFlow.parseArray(byteFlow("[]"), Person.class).runToList());
    }

    @Test
    void shouldParseNdjsonAcrossEveryByteBoundaryIncludingUtf8() throws Exception {
        // given
        var input = oneByteChunks("{\"name\":\"Zażółć 🦊\",\"age\":7}\n");

        // when & then
        assertEquals(
                List.of(new Person("Zażółć 🦊", 7)),
                JsonFlow.parseNdjson(input, Person.class).runToList());
    }

    @Test
    void shouldParseArrayAcrossEveryByteBoundaryIncludingUtf8() throws Exception {
        // given
        var input =
                oneByteChunks(
                        """
                        [{"name":"東京","age":10},{"name":"Málaga 🌊","age":20}]
                        """);

        // when & then
        assertEquals(
                List.of(new Person("東京", 10), new Person("Málaga 🌊", 20)),
                JsonFlow.parseArray(input, Person.class).runToList());
    }

    @Test
    void shouldParseGenericTypesUsingTypeReferenceOverloads() throws Exception {
        // given
        TypeReference<List<Person>> type = new TypeReference<>() {};

        // when
        var ndjson =
                JsonFlow.parseNdjson(
                                byteFlow(
                                        """
                                        [{"name":"Ada","age":36}]
                                        [{"name":"Grace","age":37},{"name":"Linus","age":28}]
                                        """),
                                type)
                        .runToList();
        var array =
                JsonFlow.parseArray(
                                byteFlow(
                                        """
                                        [[{"name":"Ada","age":36}],[{"name":"Grace","age":37}]]
                                        """),
                                type)
                        .runToList();

        // then
        assertEquals(
                List.of(
                        List.of(new Person("Ada", 36)),
                        List.of(new Person("Grace", 37), new Person("Linus", 28))),
                ndjson);
        assertEquals(
                List.of(List.of(new Person("Ada", 36)), List.of(new Person("Grace", 37))), array);
    }

    @Test
    void shouldParseJsonNodesUsingConfiguredReaderOverloads() throws Exception {
        // given
        var reader = MAPPER.readerFor(JsonNode.class);

        // when
        List<JsonNode> ndjson =
                JsonFlow.<JsonNode>parseNdjson(byteFlow("{\"n\":1}\n[true,null]\n"), reader)
                        .runToList();
        List<JsonNode> array =
                JsonFlow.<JsonNode>parseArray(byteFlow("[{\"n\":1},[true,null]]"), reader)
                        .runToList();

        // then
        assertEquals(List.of(MAPPER.readTree("{\"n\":1}"), MAPPER.readTree("[true,null]")), ndjson);
        assertEquals(ndjson, array);
    }

    @Test
    void shouldRejectMalformedNdjsonAndMultipleValuesOnOneLine() {
        assertFails(
                () -> JsonFlow.parseNdjson(byteFlow("{\"name\":}\n"), Person.class).runToList());
        assertFails(
                () ->
                        JsonFlow.parseNdjson(
                                        byteFlow("{\"name\":\"Ada\",\"age\":36} true\n"),
                                        Person.class)
                                .runToList());
    }

    @Test
    void shouldRejectMalformedArrayWrongTopLevelShapeAndTrailingContent() {
        assertFails(
                () ->
                        JsonFlow.parseArray(byteFlow("[{\"name\":\"Ada\"}"), Person.class)
                                .runToList());

        var wrongShape =
                assertThrows(
                        Exception.class,
                        () ->
                                JsonFlow.parseArray(byteFlow("{\"name\":\"Ada\"}"), Person.class)
                                        .runToList());
        assertCauseMessage(wrongShape, "Expected one top-level JSON array");

        var trailing =
                assertThrows(
                        Exception.class,
                        () -> JsonFlow.parseArray(byteFlow("[] true"), Person.class).runToList());
        assertCauseMessage(trailing, "Unexpected content after the top-level JSON array");
    }

    @Test
    void shouldPropagateParsingUpstreamErrors() {
        // given
        var ndjsonFailure = new IllegalStateException("ndjson upstream failed");
        var arrayFailure = new IllegalStateException("array upstream failed");
        var ndjson =
                Flows.concat(
                                Flows.fromByteArrays(
                                        "{\"name\":\"Ada\",\"age\":36}\n"
                                                .getBytes(StandardCharsets.UTF_8)),
                                Flows.<ByteChunk>failed(ndjsonFailure))
                        .toByteFlow();
        var array =
                Flows.concat(
                                Flows.fromByteArrays(
                                        "[{\"name\":\"Ada\",\"age\":36}"
                                                .getBytes(StandardCharsets.UTF_8)),
                                Flows.<ByteChunk>failed(arrayFailure))
                        .toByteFlow();

        // when
        var ndjsonException =
                assertThrows(
                        Exception.class,
                        () -> JsonFlow.parseNdjson(ndjson, Person.class).runToList());
        var arrayException =
                assertThrows(
                        Exception.class,
                        () -> JsonFlow.parseArray(array, Person.class).runToList());

        // then
        assertHasCause(ndjsonException, ndjsonFailure);
        assertHasCause(arrayException, arrayFailure);
    }

    @Test
    void shouldBeLazyAndStopArrayUpstreamAfterDownstreamTakesInitialElements() throws Exception {
        // given
        var input = new StringBuilder("[");
        for (int i = 0; i < 20_000; i++) {
            if (i > 0) {
                input.append(',');
            }
            input.append(i);
        }
        input.append(']');
        var bytes = input.toString().getBytes(StandardCharsets.UTF_8);
        var emittedBytes = new AtomicInteger();
        var source =
                Flows.<ByteChunk>usingEmit(
                                emit -> {
                                    for (byte value : bytes) {
                                        emittedBytes.incrementAndGet();
                                        emit.apply(ByteChunk.fromArray(new byte[] {value}));
                                    }
                                })
                        .toByteFlow();

        // when
        var parsed = JsonFlow.parseArray(source, Integer.class);

        // then
        assertEquals(0, emittedBytes.get());
        assertEquals(List.of(0, 1), parsed.take(2).runToList());
        assertTrue(emittedBytes.get() < bytes.length);
    }

    @Test
    void shouldStopNdjsonUpstreamAndPropagateDownstreamFailure() throws Exception {
        // given
        var emittedRecords = new AtomicInteger();
        var source =
                Flows.<ByteChunk>usingEmit(
                                emit -> {
                                    for (int i = 0; i < 100; i++) {
                                        emittedRecords.incrementAndGet();
                                        emit.apply(
                                                ByteChunk.fromArray(
                                                        ("%d\n".formatted(i))
                                                                .getBytes(StandardCharsets.UTF_8)));
                                    }
                                })
                        .toByteFlow();
        var downstreamFailure = new IllegalStateException("downstream failed");

        // when
        var exception =
                assertThrows(
                        Exception.class,
                        () ->
                                JsonFlow.parseNdjson(source, Integer.class)
                                        .map(
                                                value -> {
                                                    if (value == 1) {
                                                        throw downstreamFailure;
                                                    }
                                                    return value;
                                                })
                                        .runToList());

        // then
        assertHasCause(exception, downstreamFailure);
        assertEquals(2, emittedRecords.get());
    }

    @Test
    void shouldRunParsingFlowsRepeatedly() throws Exception {
        // given
        var ndjson = JsonFlow.parseNdjson(byteFlow("1\n2\n"), Integer.class);
        var array = JsonFlow.parseArray(byteFlow("[1,2]"), Integer.class);

        // when & then
        assertEquals(List.of(1, 2), ndjson.runToList());
        assertEquals(List.of(1, 2), ndjson.runToList());
        assertEquals(List.of(1, 2), array.runToList());
        assertEquals(List.of(1, 2), array.runToList());
    }

    @Test
    void shouldRenderNdjsonWithMandatoryNewlinesUsingClassOverload() throws Exception {
        // given
        var values = Flows.fromValues(new Person("Ada", 36), new Person("Łukasz", 41));

        // when
        var result = render(JsonFlow.renderNdjson(values, Person.class));

        // then
        assertEquals(
                """
                {"name":"Ada","age":36}
                {"name":"Łukasz","age":41}
                """,
                result);
    }

    @Test
    void shouldRenderEmptyFlows() throws Exception {
        assertEquals("", render(JsonFlow.renderNdjson(Flows.empty(), Person.class)));
        assertEquals("[]", render(JsonFlow.renderArray(Flows.empty(), Person.class)));
    }

    @Test
    void shouldRenderGenericTypesUsingTypeReferenceOverloads() throws Exception {
        // given
        TypeReference<List<Integer>> type = new TypeReference<>() {};
        Flow<List<Integer>> values = Flows.fromValues(List.of(1, 2), List.of(3));

        // when & then
        assertEquals("[1,2]\n[3]\n", render(JsonFlow.renderNdjson(values, type)));
        assertEquals(
                "[[1,2],[3]]",
                render(JsonFlow.renderArray(Flows.fromValues(List.of(1, 2), List.of(3)), type)));
    }

    @Test
    void shouldRenderJsonNodesUsingConfiguredWriterOverloads() throws Exception {
        // given
        var writer = MAPPER.writerFor(JsonNode.class);
        var values = Flows.fromValues(MAPPER.readTree("{\"n\":1}"), MAPPER.readTree("[true,null]"));

        // when & then
        assertEquals("{\"n\":1}\n[true,null]\n", render(JsonFlow.renderNdjson(values, writer)));
        assertEquals(
                "[{\"n\":1},[true,null]]",
                render(
                        JsonFlow.renderArray(
                                Flows.fromValues(
                                        MAPPER.readTree("{\"n\":1}"),
                                        MAPPER.readTree("[true,null]")),
                                writer)));
    }

    @Test
    void shouldRejectRawLineBreaksProducedByNdjsonWriter() {
        // given
        var prettyWriter = MAPPER.writerFor(Person.class).withDefaultPrettyPrinter();

        // when
        var exception =
                assertThrows(
                        Exception.class,
                        () ->
                                JsonFlow.renderNdjson(
                                                Flows.fromValues(new Person("Ada", 36)),
                                                prettyWriter)
                                        .runToList());

        // then
        assertCauseMessage(exception, "cannot be rendered as NDJSON");
    }

    @Test
    void shouldPropagateRenderingUpstreamErrors() {
        // given
        var ndjsonFailure = new IllegalStateException("ndjson values failed");
        var arrayFailure = new IllegalStateException("array values failed");
        var ndjsonValues =
                Flows.concat(
                        Flows.fromValues(new Person("Ada", 36)),
                        Flows.<Person>failed(ndjsonFailure));
        var arrayValues =
                Flows.concat(
                        Flows.fromValues(new Person("Ada", 36)),
                        Flows.<Person>failed(arrayFailure));

        // when
        var ndjsonException =
                assertThrows(
                        Exception.class,
                        () -> JsonFlow.renderNdjson(ndjsonValues, Person.class).runToList());
        var arrayException =
                assertThrows(
                        Exception.class,
                        () -> JsonFlow.renderArray(arrayValues, Person.class).runToList());

        // then
        assertHasCause(ndjsonException, ndjsonFailure);
        assertHasCause(arrayException, arrayFailure);
    }

    @Test
    void shouldRenderLazilyAndStopAfterDownstreamTakesInitialChunks() throws Exception {
        // given
        var renderedValues = new AtomicInteger();
        var values =
                Flows.<Integer>usingEmit(
                        emit -> {
                            for (int i = 0; i < 100; i++) {
                                renderedValues.incrementAndGet();
                                emit.apply(i);
                            }
                        });

        // when
        var rendered = JsonFlow.renderArray(values, Integer.class);

        // then
        assertEquals(0, renderedValues.get());
        assertEquals("[0,1", chunksToString(rendered.take(3).runToList()));
        assertEquals(2, renderedValues.get());
    }

    @Test
    void shouldRunRenderingFlowsRepeatedly() throws Exception {
        // given
        var ndjson = JsonFlow.renderNdjson(Flows.fromValues(1, 2), Integer.class);
        var array = JsonFlow.renderArray(Flows.fromValues(1, 2), Integer.class);

        // when & then
        assertEquals("1\n2\n", render(ndjson));
        assertEquals("1\n2\n", render(ndjson));
        assertEquals("[1,2]", render(array));
        assertEquals("[1,2]", render(array));
    }

    @Test
    void shouldRoundTripNdjsonAndArrays() throws Exception {
        // given
        var people =
                List.of(
                        new Person("Zażółć 🦊", 7),
                        new Person("東京", 10),
                        new Person("Málaga 🌊", 20));

        // when
        var ndjson =
                JsonFlow.parseNdjson(
                                JsonFlow.renderNdjson(Flows.fromIterable(people), Person.class),
                                Person.class)
                        .runToList();
        var array =
                JsonFlow.parseArray(
                                JsonFlow.renderArray(Flows.fromIterable(people), Person.class),
                                Person.class)
                        .runToList();

        // then
        assertEquals(people, ndjson);
        assertEquals(people, array);
    }

    @Test
    void shouldUseInputStreamFileAndRenderedOutputIntegrations() throws Exception {
        // given
        var stream =
                new ByteArrayInputStream(
                        "[{\"name\":\"Ada\",\"age\":36}]".getBytes(StandardCharsets.UTF_8));
        var path = tempDir.resolve("people.ndjson");
        Files.writeString(
                path,
                "{\"name\":\"Grace\",\"age\":37}\n{\"name\":\"Linus\",\"age\":28}",
                StandardCharsets.UTF_8);

        // when
        var fromInputStream =
                JsonFlow.parseArray(Flows.fromInputStream(stream, 1), Person.class).runToList();
        var fromFile = JsonFlow.parseNdjson(Flows.fromFile(path, 3), Person.class).runToList();
        var output =
                render(JsonFlow.renderArray(Flows.fromValues(new Person("Ada", 36)), Person.class));

        // then
        assertEquals(List.of(new Person("Ada", 36)), fromInputStream);
        assertEquals(List.of(new Person("Grace", 37), new Person("Linus", 28)), fromFile);
        assertEquals("[{\"name\":\"Ada\",\"age\":36}]", output);
    }

    private static Flow.ByteFlow byteFlow(String value) {
        return Flows.fromByteArrays(value.getBytes(StandardCharsets.UTF_8));
    }

    private static Flow.ByteFlow oneByteChunks(String value) {
        var bytes = value.getBytes(StandardCharsets.UTF_8);
        var chunks = new ByteChunk[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            chunks[i] = ByteChunk.fromArray(new byte[] {bytes[i]});
        }
        return Flows.fromByteChunks(chunks);
    }

    private static String render(Flow.ByteFlow flow) throws Exception {
        var output = new ByteArrayOutputStream();
        flow.runToOutputStream(output);
        return output.toString(StandardCharsets.UTF_8);
    }

    private static String chunksToString(List<ByteChunk> chunks) {
        var output = new ByteArrayOutputStream();
        for (var chunk : chunks) {
            for (var array : chunk.getArrays()) {
                output.writeBytes(array);
            }
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    private static void assertFails(ThrowingRunnable action) {
        assertThrows(Exception.class, action::run);
    }

    private static void assertCauseMessage(Throwable exception, String expectedFragment) {
        for (Throwable current = exception; current != null; current = current.getCause()) {
            if (current.getMessage() != null && current.getMessage().contains(expectedFragment)) {
                return;
            }
        }
        throw new AssertionError(
                "No exception in the cause chain contained: " + expectedFragment, exception);
    }

    private static void assertHasCause(Throwable exception, Throwable expected) {
        for (Throwable current = exception; current != null; current = current.getCause()) {
            if (current == expected) {
                assertSame(expected, current);
                return;
            }
        }
        throw new AssertionError(
                "Expected exception was not present in the cause chain", exception);
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private record Person(String name, int age) {}
}
