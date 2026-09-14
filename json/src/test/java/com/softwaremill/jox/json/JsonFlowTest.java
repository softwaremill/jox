package com.softwaremill.jox.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.softwaremill.jox.flows.ByteChunk;
import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.flows.Flows;
import com.softwaremill.jox.structured.Par;

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
    void shouldParseNdjsonBomLineEndingsBlankLinesAndFinalUnterminatedRecord() throws Exception {
        // given
        var input =
                byteFlow(
                        """
                        \uFEFF
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
        assertCause(exception, JacksonException.class);
    }

    @Test
    void shouldApplyConfiguredNdjsonRecordLimit() throws Exception {
        // given
        var settings = new NdjsonReadSettings(3);
        var recordSpanningChunks =
                Flows.fromByteChunks(
                        ByteChunk.fromArray("12".getBytes(StandardCharsets.UTF_8)),
                        ByteChunk.fromArray("34".getBytes(StandardCharsets.UTF_8)));

        // when
        var atLimit =
                JsonFlow.parseNdjson(byteFlow("123\n456\n"), Integer.class, settings).runToList();
        var overLimit =
                assertThrows(
                        Exception.class,
                        () ->
                                JsonFlow.parseNdjson(byteFlow("1234\n"), Integer.class, settings)
                                        .runToList());
        var overLimitAcrossChunks =
                assertThrows(
                        Exception.class,
                        () ->
                                JsonFlow.parseNdjson(recordSpanningChunks, Integer.class, settings)
                                        .runToList());

        // then
        assertEquals(List.of(123, 456), atLimit);
        assertRecordLimitExceeded(overLimit, 3);
        assertRecordLimitExceeded(overLimitAcrossChunks, 3);
    }

    @Test
    void shouldValidateNdjsonRecordLimitSettings() {
        // when & then
        assertEquals(32 * 1024 * 1024, NdjsonReadSettings.defaults().maxRecordBytes());
        assertThrows(IllegalArgumentException.class, () -> new NdjsonReadSettings(0));
    }

    @Test
    void shouldCountBomAndCarriageReturnTowardNdjsonRecordLimit() throws Exception {
        // given
        var fourBytes = new NdjsonReadSettings(4);
        var threeBytes = new NdjsonReadSettings(3);
        var twoBytes = new NdjsonReadSettings(2);

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
        var settings = new NdjsonReadSettings(3);

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
        assertCause(
                ndjsonException,
                IllegalArgumentException.class,
                "Jox flows do not support null values");
        assertCause(
                arrayException,
                IllegalArgumentException.class,
                "Jox flows do not support null values");
        assertEquals(List.of(nullNode), ndjsonNodes);
        assertEquals(List.of(nullNode), arrayNodes);
    }

    @Test
    void shouldRejectMultipleValuesAndNonJsonWhitespaceOnOneLine() {
        // given
        var multipleValues = byteFlow("{\"name\":\"Ada\",\"age\":36} true\n");
        var nonJsonWhitespace = byteFlow("\u000b\n");

        // when
        var multipleValuesException =
                assertThrows(
                        Exception.class,
                        () -> JsonFlow.parseNdjson(multipleValues, Person.class).runToList());
        var nonJsonWhitespaceException =
                assertThrows(
                        Exception.class,
                        () -> JsonFlow.parseNdjson(nonJsonWhitespace, Person.class).runToList());

        // then
        assertCause(multipleValuesException, JacksonException.class);
        assertCause(nonJsonWhitespaceException, JacksonException.class);
    }

    @Test
    void shouldParseArraySurroundedByWhitespace() throws Exception {
        // given
        var input = byteFlow(" \n\t[ 1 ]\r\n ");

        // when & then
        assertEquals(List.of(1), JsonFlow.parseArray(input, Integer.class).runToList());
    }

    @Test
    void shouldRejectEmptyArrayInput() {
        // given
        var input = byteFlow("");

        // when
        var exception =
                assertThrows(
                        Exception.class,
                        () -> JsonFlow.parseArray(input, Integer.class).runToList());

        // then
        assertCause(exception, IllegalArgumentException.class, "Expected one top-level JSON array");
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
        assertCause(incompleteException, JacksonException.class);
        assertCause(
                wrongShape, IllegalArgumentException.class, "Expected one top-level JSON array");
        assertCause(
                trailing,
                IllegalArgumentException.class,
                "Unexpected content after the top-level JSON array");
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
    void shouldNotReadArrayInputBeforeRun() throws Exception {
        // given
        var emittedChunks = new AtomicInteger();
        var source =
                Flows.<ByteChunk>usingEmit(
                                emit -> {
                                    emittedChunks.incrementAndGet();
                                    emit.apply(
                                            ByteChunk.fromArray(
                                                    "[0,1]".getBytes(StandardCharsets.UTF_8)));
                                })
                        .toByteFlow();

        // when
        var parsed = JsonFlow.parseArray(source, Integer.class);

        // then
        assertEquals(0, emittedChunks.get());
        assertEquals(List.of(0, 1), parsed.runToList());
    }

    @Test
    void shouldEmitArrayElementsBeforeSourceEnds() throws Exception {
        // given
        var release = new CountDownLatch(1);
        var source =
                Flows.<ByteChunk>usingEmit(
                                emit -> {
                                    emit.apply(
                                            ByteChunk.fromArray(
                                                    "[1,2,".getBytes(StandardCharsets.UTF_8)));
                                    release.await();
                                })
                        .toByteFlow();

        // when
        List<Integer> result;
        try {
            result =
                    assertTimeoutPreemptively(
                            Duration.ofSeconds(2),
                            () -> JsonFlow.parseArray(source, Integer.class).take(2).runToList());
        } finally {
            release.countDown(); // unblocks the producer so the scope can close
        }

        // then
        assertEquals(List.of(1, 2), result);
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
        assertTrue(emittedRecords.get() < 100);
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
    void shouldTerminateEveryNdjsonValueWithNewlineUsingClassOverload() throws Exception {
        // given
        var values = Flows.fromValues(new Person("Ada", 36), new Person("Łukasz", 41));

        // when
        var chunks = JsonFlow.renderNdjson(values, Person.class).runToList();

        // then
        assertEquals(
                """
                {"name":"Ada","age":36}
                {"name":"Łukasz","age":41}
                """,
                render(Flows.fromByteChunks(chunks.toArray(ByteChunk[]::new))));
        assertEquals(
                List.of(1, 1), chunks.stream().map(chunk -> chunk.getArrays().size()).toList());
    }

    @Test
    void shouldAllowEscapedLineBreaksInNdjsonValues() throws Exception {
        // given
        var value = "first line\nsecond line\rthird line";

        // when
        var rendered = render(JsonFlow.renderNdjson(Flows.fromValues(value), String.class));

        // then
        assertEquals("\"first line\\nsecond line\\rthird line\"\n", rendered);
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
        assertCause(ndjsonException, IllegalArgumentException.class, expectedMessage);
        assertCause(arrayException, IllegalArgumentException.class, expectedMessage);
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
        assertCause(exception, IllegalArgumentException.class, "cannot be rendered as NDJSON");
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
        var arrayOutput = new ByteArrayOutputStream();

        // when
        var ndjsonException =
                assertThrows(
                        Exception.class,
                        () -> JsonFlow.renderNdjson(ndjsonValues, Person.class).runToList());
        var arrayException =
                assertThrows(
                        Exception.class,
                        () ->
                                JsonFlow.renderArray(arrayValues, Person.class)
                                        .runToOutputStream(arrayOutput));

        // then
        assertHasCause(ndjsonException, ndjsonFailure);
        assertHasCause(arrayException, arrayFailure);
        assertEquals(
                "[{\"name\":\"Ada\",\"age\":36}", arrayOutput.toString(StandardCharsets.UTF_8));
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
        var rendered = JsonFlow.renderNdjson(values, Integer.class);

        // then
        assertEquals(0, renderedValues.get());
        rendered.take(2).runToList();
        assertTrue(renderedValues.get() < 100);
    }

    @Test
    void shouldRenderTheSameNdjsonFlowConcurrently() throws Exception {
        // given
        var value = new PausedSerialization(new CyclicBarrier(2));
        var rendered = JsonFlow.renderNdjson(Flows.fromValues(value), PausedSerialization.class);

        // when
        var outputs =
                Par.par(List.<Callable<String>>of(() -> render(rendered), () -> render(rendered)));

        // then
        assertEquals(List.of("{\"value\":\"x\"}\n", "{\"value\":\"x\"}\n"), outputs);
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
    void shouldParseNdjsonFromFile() throws Exception {
        // given
        var path = tempDir.resolve("people.ndjson");
        Files.writeString(
                path,
                "{\"name\":\"Grace\",\"age\":37}\n{\"name\":\"Linus\",\"age\":28}",
                StandardCharsets.UTF_8);

        // when & then
        assertEquals(
                List.of(new Person("Grace", 37), new Person("Linus", 28)),
                JsonFlow.parseNdjson(Flows.fromFile(path, 3), Person.class).runToList());
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

    private static void assertCause(Throwable exception, Class<? extends Throwable> type) {
        assertCause(exception, type, "");
    }

    private static void assertCause(
            Throwable exception, Class<? extends Throwable> type, String messageFragment) {
        for (Throwable current = exception; current != null; current = current.getCause()) {
            if (type.isInstance(current)
                    && String.valueOf(current.getMessage()).contains(messageFragment)) {
                return;
            }
        }
        throw new AssertionError(
                "No "
                        + type.getSimpleName()
                        + " containing '"
                        + messageFragment
                        + "' in the cause chain",
                exception);
    }

    private static void assertRecordLimitExceeded(Throwable exception, int maximumBytes) {
        assertCause(
                exception,
                IllegalArgumentException.class,
                "NDJSON record exceeds the configured maximum of " + maximumBytes + " bytes");
    }

    private static void assertHasCause(Throwable exception, Throwable expected) {
        for (Throwable current = exception; current != null; current = current.getCause()) {
            if (current == expected) {
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

    /** Serialization waits until both concurrent runs are writing at the same time. */
    private static final class PausedSerialization {
        private final CyclicBarrier barrier;

        private PausedSerialization(CyclicBarrier barrier) {
            this.barrier = barrier;
        }

        public String getValue() {
            try {
                barrier.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return "x";
        }
    }

    private record Person(String name, int age) {}
}
