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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.softwaremill.jox.flows.ByteChunk;
import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.flows.Flows;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class JsonFlowTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final IllegalStateException DESERIALIZATION_FAILURE =
            new IllegalStateException("deserialization failed");
    private static final IllegalStateException SERIALIZATION_FAILURE =
            new IllegalStateException("serialization failed");

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
        // given
        var ndjsonInput = byteFlow("");
        var arrayInput = byteFlow("[]");

        // when
        var ndjson = JsonFlow.parseNdjson(ndjsonInput, Person.class).runToList();
        var array = JsonFlow.parseArray(arrayInput, Person.class).runToList();

        // then
        assertEquals(List.of(), ndjson);
        assertEquals(List.of(), array);
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
    void shouldParseNdjsonWithSplitUtf8Bom() throws Exception {
        // given
        var input =
                Flows.fromByteChunks(
                        ByteChunk.fromArray(new byte[] {(byte) 0xef}),
                        ByteChunk.fromArray(new byte[] {(byte) 0xbb}),
                        ByteChunk.fromArray(new byte[] {(byte) 0xbf, '"', 'o', 'k', '"', '\n'}));

        // when & then
        assertEquals(List.of("ok"), JsonFlow.parseNdjson(input, String.class).runToList());
    }

    @Test
    void shouldRejectMalformedNdjsonUtf8() {
        // given
        var input = Flows.fromByteArrays(new byte[] {'"', (byte) 0xc3, '(', '"', '\n'});

        // when
        var exception =
                assertThrows(
                        Exception.class,
                        () -> JsonFlow.parseNdjson(input, String.class).runToList());

        // then
        assertCauseTypeAndMessage(
                exception, IllegalArgumentException.class, "NDJSON input contains malformed UTF-8");
    }

    @Test
    void shouldApplyConfiguredNdjsonRecordLimitToEveryReaderOverload() throws Exception {
        // given
        var settings = JsonReadSettings.defaults().maxNdjsonRecordBytes(3);
        TypeReference<Integer> type = new TypeReference<>() {};
        var reader = MAPPER.readerFor(Integer.class);

        // when
        var usingClass =
                JsonFlow.parseNdjson(byteFlow("123\n456\n"), Integer.class, settings).runToList();
        var usingTypeReference =
                JsonFlow.parseNdjson(byteFlow("123\n456\n"), type, settings).runToList();
        var usingReader =
                JsonFlow.<Integer>parseNdjson(byteFlow("123\n456\n"), reader, settings).runToList();
        var classException =
                assertThrows(
                        Exception.class,
                        () ->
                                JsonFlow.parseNdjson(byteFlow("1234\n"), Integer.class, settings)
                                        .runToList());
        var typeReferenceException =
                assertThrows(
                        Exception.class,
                        () -> JsonFlow.parseNdjson(byteFlow("1234\n"), type, settings).runToList());
        var readerException =
                assertThrows(
                        Exception.class,
                        () ->
                                JsonFlow.<Integer>parseNdjson(byteFlow("1234\n"), reader, settings)
                                        .runToList());

        // then
        assertEquals(List.of(123, 456), usingClass);
        assertEquals(List.of(123, 456), usingTypeReference);
        assertEquals(List.of(123, 456), usingReader);
        assertRecordLimitExceeded(classException, 3);
        assertRecordLimitExceeded(typeReferenceException, 3);
        assertRecordLimitExceeded(readerException, 3);
    }

    @Test
    void shouldValidateNdjsonRecordLimitSettings() {
        // when & then
        assertEquals(32 * 1024 * 1024, JsonReadSettings.defaults().maxNdjsonRecordBytes());
        assertThrows(IllegalArgumentException.class, () -> new JsonReadSettings(0));
    }

    @Test
    void shouldCountBomAndCarriageReturnTowardNdjsonRecordLimit() throws Exception {
        // given
        var fourBytes = JsonReadSettings.defaults().maxNdjsonRecordBytes(4);
        var threeBytes = JsonReadSettings.defaults().maxNdjsonRecordBytes(3);
        var twoBytes = JsonReadSettings.defaults().maxNdjsonRecordBytes(2);

        // when
        var bomAtLimit =
                JsonFlow.parseNdjson(byteFlow("\uFEFF1\n"), Integer.class, fourBytes).runToList();
        var bomOverLimit =
                assertThrows(
                        Exception.class,
                        () ->
                                JsonFlow.parseNdjson(
                                                byteFlow("\uFEFF1\n"), Integer.class, threeBytes)
                                        .runToList());
        var crAtLimit =
                JsonFlow.parseNdjson(byteFlow("12\r\n"), Integer.class, threeBytes).runToList();
        var crOverLimit =
                assertThrows(
                        Exception.class,
                        () ->
                                JsonFlow.parseNdjson(byteFlow("12\r\n"), Integer.class, twoBytes)
                                        .runToList());

        // then
        assertEquals(List.of(1), bomAtLimit);
        assertRecordLimitExceeded(bomOverLimit, 3);
        assertEquals(List.of(12), crAtLimit);
        assertRecordLimitExceeded(crOverLimit, 2);
    }

    @Test
    void shouldEmitValidNdjsonRecordsBeforeLaterRecordInSameChunkFails() {
        // given
        var emitted = new ArrayList<Integer>();
        var settings = JsonReadSettings.defaults().maxNdjsonRecordBytes(3);

        // when
        var exception =
                assertThrows(
                        Exception.class,
                        () ->
                                JsonFlow.parseNdjson(byteFlow("1\n1234\n"), Integer.class, settings)
                                        .runForeach(emitted::add));

        // then
        assertEquals(List.of(1), emitted);
        assertRecordLimitExceeded(exception, 3);
    }

    @Test
    void shouldPreserveNdjsonRecordsAcrossEmptyChunks() throws Exception {
        // given
        var input =
                Flows.fromByteChunks(
                        ByteChunk.fromArray("12".getBytes(StandardCharsets.UTF_8)),
                        ByteChunk.empty(),
                        ByteChunk.fromArray("3\n".getBytes(StandardCharsets.UTF_8)));

        // when & then
        assertEquals(List.of(123), JsonFlow.parseNdjson(input, Integer.class).runToList());
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
    void shouldRejectDeserializedNullValuesButAllowJsonNullNodes() throws Exception {
        // given
        var nullNode = MAPPER.readTree("null");

        // when
        var ndjsonException =
                assertThrows(
                        Exception.class,
                        () -> JsonFlow.parseNdjson(byteFlow("null\n"), String.class).runToList());
        var arrayException =
                assertThrows(
                        Exception.class,
                        () -> JsonFlow.parseArray(byteFlow("[null]"), String.class).runToList());
        var ndjsonNodes = JsonFlow.parseNdjson(byteFlow("null\n"), JsonNode.class).runToList();
        var arrayNodes = JsonFlow.parseArray(byteFlow("[null]"), JsonNode.class).runToList();

        // then
        assertCauseMessage(ndjsonException, "Jox flows do not support null values");
        assertCauseMessage(arrayException, "Jox flows do not support null values");
        assertEquals(List.of(nullNode), ndjsonNodes);
        assertEquals(List.of(nullNode), arrayNodes);
    }

    @Test
    void shouldRejectMalformedNdjsonAndMultipleValuesOnOneLine() {
        // given
        var malformed = byteFlow("{\"name\":}\n");
        var multipleValues = byteFlow("{\"name\":\"Ada\",\"age\":36} true\n");

        // when
        var malformedException =
                assertThrows(
                        Exception.class,
                        () -> JsonFlow.parseNdjson(malformed, Person.class).runToList());
        var multipleValuesException =
                assertThrows(
                        Exception.class,
                        () -> JsonFlow.parseNdjson(multipleValues, Person.class).runToList());

        // then
        assertCauseType(malformedException, JacksonException.class);
        assertCauseType(multipleValuesException, JacksonException.class);
    }

    @Test
    void shouldHandleArrayWhitespaceAndRejectMissingInputAndTrailingCommas() throws Exception {
        // given
        var inputWithWhitespace = byteFlow(" \n\t[ 1 ]\r\n ");
        var emptyInput = byteFlow("");
        var whitespaceOnlyInput = byteFlow(" \r\n\t");
        var trailingCommaInput = byteFlow("[1,]");

        // when
        var result = JsonFlow.parseArray(inputWithWhitespace, Integer.class).runToList();
        var empty =
                assertThrows(
                        Exception.class,
                        () -> JsonFlow.parseArray(emptyInput, Integer.class).runToList());
        var whitespaceOnly =
                assertThrows(
                        Exception.class,
                        () -> JsonFlow.parseArray(whitespaceOnlyInput, Integer.class).runToList());

        // then
        assertEquals(List.of(1), result);
        assertCauseMessage(empty, "Expected one top-level JSON array");
        assertCauseMessage(whitespaceOnly, "Expected one top-level JSON array");
        var trailingComma =
                assertThrows(
                        Exception.class,
                        () -> JsonFlow.parseArray(trailingCommaInput, Integer.class).runToList());
        assertCauseType(trailingComma, JacksonException.class);
    }

    @Test
    void shouldRejectMalformedArrayWrongTopLevelShapeAndTrailingContent() {
        // given
        var incomplete = byteFlow("[1");
        var wrongShapeInput = byteFlow("{\"name\":\"Ada\"}");
        var trailingInput = byteFlow("[] true");

        // when
        var incompleteException =
                assertThrows(
                        Exception.class,
                        () -> JsonFlow.parseArray(incomplete, Integer.class).runToList());
        var wrongShape =
                assertThrows(
                        Exception.class,
                        () -> JsonFlow.parseArray(wrongShapeInput, Person.class).runToList());
        var trailing =
                assertThrows(
                        Exception.class,
                        () -> JsonFlow.parseArray(trailingInput, Person.class).runToList());

        // then
        assertCauseType(incompleteException, JacksonException.class);
        assertCauseMessage(wrongShape, "Expected one top-level JSON array");
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
    void shouldCancelAndCloseArrayInputAfterDownstreamFailure() {
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
        var readBytes = new AtomicInteger();
        var closed = new AtomicBoolean();
        var inputStream =
                new ByteArrayInputStream(bytes) {
                    @Override
                    public synchronized int read(byte[] target, int offset, int length) {
                        int read = super.read(target, offset, length);
                        if (read > 0) {
                            readBytes.addAndGet(read);
                        }
                        return read;
                    }

                    @Override
                    public void close() {
                        closed.set(true);
                    }
                };
        var downstreamFailure = new IllegalStateException("downstream failed");

        // when
        var exception =
                assertThrows(
                        Exception.class,
                        () ->
                                JsonFlow.parseArray(
                                                Flows.fromInputStream(inputStream, 1),
                                                Integer.class)
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
        assertTrue(closed.get());
        assertTrue(readBytes.get() < bytes.length);
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
        var ndjson = JsonFlow.parseNdjson(oneByteChunks("\uFEFF1\n2"), Integer.class);
        var array = JsonFlow.parseArray(byteFlow("[1,2]"), Integer.class);

        // when & then
        assertEquals(List.of(1), ndjson.take(1).runToList());
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
    void shouldAllowEscapedLineBreaksInNdjsonValues() throws Exception {
        // given
        var value = "first line\nsecond line\rthird line";

        // when
        var rendered = render(JsonFlow.renderNdjson(Flows.fromValues(value), String.class));

        // then
        assertEquals("\"first line\\nsecond line\\rthird line\"\n", rendered);
        assertEquals(
                List.of(value), JsonFlow.parseNdjson(byteFlow(rendered), String.class).runToList());
    }

    @Test
    void shouldRenderEmptyFlows() throws Exception {
        // given
        Flow<Person> empty = Flows.empty();

        // when
        var ndjson = render(JsonFlow.renderNdjson(empty, Person.class));
        var array = render(JsonFlow.renderArray(empty, Person.class));

        // then
        assertEquals("", ndjson);
        assertEquals("[]", array);
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
        var values =
                Flows.fromValues(
                        MAPPER.readTree("{\"n\":1}"),
                        MAPPER.readTree("[true,null]"),
                        MAPPER.readTree("null"));

        // when & then
        assertEquals(
                "{\"n\":1}\n[true,null]\nnull\n", render(JsonFlow.renderNdjson(values, writer)));
        assertEquals(
                "[{\"n\":1},[true,null],null]",
                render(
                        JsonFlow.renderArray(
                                Flows.fromValues(
                                        MAPPER.readTree("{\"n\":1}"),
                                        MAPPER.readTree("[true,null]"),
                                        MAPPER.readTree("null")),
                                writer)));
    }

    @Test
    void shouldRejectRawNullValuesWhenRendering() {
        // given
        var values = Flows.<String>usingEmit(emit -> emit.apply(null));

        // when
        var ndjsonException =
                assertThrows(
                        Exception.class,
                        () -> JsonFlow.renderNdjson(values, String.class).runToList());
        var arrayException =
                assertThrows(
                        Exception.class,
                        () -> JsonFlow.renderArray(values, String.class).runToList());

        // then
        var expectedMessage =
                "Java null cannot be rendered because Jox flows do not support null values";
        assertCauseTypeAndMessage(ndjsonException, IllegalArgumentException.class, expectedMessage);
        assertCauseTypeAndMessage(arrayException, IllegalArgumentException.class, expectedMessage);
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
    void shouldAllowPrettyPrintedArrayElements() throws Exception {
        // given
        var person = new Person("Ada", 36);
        var prettyWriter = MAPPER.writerFor(Person.class).withDefaultPrettyPrinter();

        // when
        var rendered = render(JsonFlow.renderArray(Flows.fromValues(person), prettyWriter));

        // then
        assertTrue(rendered.contains("\n"));
        assertEquals(
                List.of(person), JsonFlow.parseArray(byteFlow(rendered), Person.class).runToList());
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
    void shouldPropagateJacksonReaderAndWriterFailures() {
        // given
        var ndjsonInput = byteFlow("{\"value\":\"x\"}\n");
        var arrayInput = byteFlow("[{\"value\":\"x\"}]");
        var failingValue = new FailingSerialization();

        // when
        var ndjsonReaderException =
                assertThrows(
                        Exception.class,
                        () ->
                                JsonFlow.parseNdjson(ndjsonInput, FailingDeserialization.class)
                                        .runToList());
        var arrayReaderException =
                assertThrows(
                        Exception.class,
                        () ->
                                JsonFlow.parseArray(arrayInput, FailingDeserialization.class)
                                        .runToList());
        var ndjsonWriterException =
                assertThrows(
                        Exception.class,
                        () ->
                                JsonFlow.renderNdjson(
                                                Flows.fromValues(failingValue),
                                                FailingSerialization.class)
                                        .runToList());
        var arrayWriterException =
                assertThrows(
                        Exception.class,
                        () ->
                                JsonFlow.renderArray(
                                                Flows.fromValues(failingValue),
                                                FailingSerialization.class)
                                        .runToList());

        // then
        assertHasCause(ndjsonReaderException, DESERIALIZATION_FAILURE);
        assertHasCause(arrayReaderException, DESERIALIZATION_FAILURE);
        assertHasCause(ndjsonWriterException, SERIALIZATION_FAILURE);
        assertHasCause(arrayWriterException, SERIALIZATION_FAILURE);
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
        assertEquals("[0", chunksToString(rendered.take(2).runToList()));
        assertEquals(1, renderedValues.get());
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

    private static void assertCauseMessage(Throwable exception, String expectedFragment) {
        for (Throwable current = exception; current != null; current = current.getCause()) {
            if (current.getMessage() != null && current.getMessage().contains(expectedFragment)) {
                return;
            }
        }
        throw new AssertionError(
                "No exception in the cause chain contained: " + expectedFragment, exception);
    }

    private static void assertCauseType(
            Throwable exception, Class<? extends Throwable> expectedType) {
        for (Throwable current = exception; current != null; current = current.getCause()) {
            if (expectedType.isInstance(current)) {
                return;
            }
        }
        throw new AssertionError(
                "No exception in the cause chain had type: " + expectedType.getName(), exception);
    }

    private static void assertCauseTypeAndMessage(
            Throwable exception, Class<? extends Throwable> expectedType, String expectedMessage) {
        for (Throwable current = exception; current != null; current = current.getCause()) {
            if (expectedType.isInstance(current) && expectedMessage.equals(current.getMessage())) {
                return;
            }
        }
        throw new AssertionError(
                "No exception in the cause chain had type "
                        + expectedType.getName()
                        + " and message: "
                        + expectedMessage,
                exception);
    }

    private static void assertRecordLimitExceeded(Throwable exception, int maximumBytes) {
        assertCauseTypeAndMessage(
                exception,
                IllegalArgumentException.class,
                "NDJSON record exceeds the configured maximum of " + maximumBytes + " bytes");
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

    private record FailingDeserialization(String value) {
        private FailingDeserialization {
            throw DESERIALIZATION_FAILURE;
        }
    }

    private static final class FailingSerialization {
        public String getValue() {
            throw SERIALIZATION_FAILURE;
        }
    }

    private record Person(String name, int age) {}
}
