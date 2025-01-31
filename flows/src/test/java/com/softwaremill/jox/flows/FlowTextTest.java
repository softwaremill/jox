package com.softwaremill.jox.flows;

import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.softwaremill.jox.flows.ByteChunk.empty;
import static com.softwaremill.jox.flows.ByteChunk.fromArray;
import static org.junit.jupiter.api.Assertions.*;

public class FlowTextTest {

    @Test
    void decodeLinesWithSpecifiedCharset() throws Exception {
        // given
        byte[] inputBytes = "zażółć\ngęślą\njaźń".getBytes(Charset.forName("ISO-8859-2"));
        System.out.println(new String(inputBytes, Charset.forName("ISO-8859-2")));

        assertEquals("BF", String.format("%02X", inputBytes[2])); // making sure 'ż' is encoded in ISO-8859-2

        // when & then
        assertEquals(List.of("zażółć", "gęślą", "jaźń"), Flows.fromByteChunks(fromArray(inputBytes))
                                                              .lines(Charset.forName("ISO-8859-2")).runToList());
    }

    @Test
    void decodeLinesCorrectlyAcrossChunkBoundaries() throws Exception {
        // given
        List<String> lines = List.of("aa", "bbbbb", "cccccccc", "ddd", "ee", "fffff");
        byte[] inputBytes = String.join("\n", lines).getBytes(StandardCharsets.UTF_8);

        Collection<byte[]> values = IntStream.range(0, inputBytes.length)
                                             .mapToObj(i -> inputBytes[i])
                                             .collect(Collectors.groupingBy(equalSizeChunks(5))).values().stream()
                                             .map(list -> {
                                                 byte[] bytes = new byte[list.size()];
                                                 IntStream.range(0, list.size()).forEach(i -> bytes[i] = list.get(i));
                                                 return bytes;
                                             })
                                             .toList();

        // when & then
        Flow<String> flow = Flows.fromIterable(values)
                                 .map(ByteChunk::fromArray)
                                 .toByteFlow()
                                 .lines(StandardCharsets.UTF_8);

        assertEquals(lines, flow.runToList());
    }

    @Test
    void splitSingleChunkIntoLines() throws Exception {
        String inputText = "line1\nline2\nline3";
        byte[] chunk = inputText.getBytes();
        List<String> result = Flows.fromByteChunks(fromArray(chunk))
                                   .linesUtf8()
                                   .runToList();
        assertEquals(List.of("line1", "line2", "line3"), result);
    }

    @Test
    void splitSingleChunkIntoLinesMultipleNewlines() throws Exception {
        String inputText = "line1\n\nline2\nline3";
        byte[] chunk = inputText.getBytes();
        List<String> result = Flows.fromByteChunks(fromArray(chunk))
                                   .linesUtf8()
                                   .runToList();
        assertEquals(List.of("line1", "", "line2", "line3"), result);
    }

    @Test
    void splitSingleChunkIntoLinesBeginningWithNewline() throws Exception {
        String inputText = "\nline1\nline2";
        byte[] chunk = inputText.getBytes();
        List<String> result = Flows.fromByteChunks(fromArray(chunk))
                                   .linesUtf8()
                                   .runToList();
        assertEquals(List.of("", "line1", "line2"), result);
    }

    @Test
    void splitSingleChunkIntoLinesEndingWithNewline() throws Exception {
        String inputText = "line1\nline2\n";
        byte[] bytes = inputText.getBytes();
        List<String> result = Flows.fromByteChunks(fromArray(bytes))
                                   .linesUtf8().runToList();
        assertEquals(List.of("line1", "line2", ""), result);
    }

    @Test
    void splitSingleChunkIntoLinesEmptyArray() throws Exception {
        String inputText = "";
        byte[] chunk = inputText.getBytes();
        List<String> result = Flows.fromByteChunks(fromArray(chunk))
                                   .linesUtf8().runToList();
        assertEquals(List.of(), result);
    }

    @Test
    void splitMultipleChunksIntoLines() throws Exception {
        String inputText1 = "line1-part1,";
        byte[] chunk1 = inputText1.getBytes();
        String inputText2 = "line1-part2\nline2";
        byte[] chunk2 = inputText2.getBytes();
        List<String> result = Flows.fromByteChunks(fromArray(chunk1), fromArray(chunk2)).linesUtf8().runToList();
        assertEquals(List.of("line1-part1,line1-part2", "line2"), result);
    }

    @Test
    void splitMultipleChunksIntoLinesMultipleNewlines() throws Exception {
        String inputText1 = "line1-part1,";
        var chunk1 = fromArray(inputText1.getBytes());
        String inputText2 = "line1-part2\n";
        var chunk2 = fromArray(inputText2.getBytes());
        String inputText3 = "\n";
        var chunk3 = fromArray(inputText3.getBytes());
        List<String> result = Flows.fromByteChunks(chunk1, chunk2, chunk3).linesUtf8().runToList();
        assertEquals(List.of("line1-part1,line1-part2", "", ""), result);
    }

    @Test
    void splitMultipleChunksIntoLinesMultipleEmptyChunks() throws Exception {
        var emptyChunk = empty();
        var chunk1 = fromArray("\n\n".getBytes());
        List<String> result = Flows.fromByteChunks(emptyChunk, emptyChunk, chunk1, emptyChunk).linesUtf8().runToList();
        assertEquals(List.of("", ""), result);
    }

    @Test
    void encodeUtf8_shouldHandleEmptyString() throws Exception {
        assertEquals(0, Flows.fromValues("").encodeUtf8().runLast().length());
    }

    @Test
    void shouldEncodeStringToUtf8() throws Exception {
        String text = "Simple test を解放 text";
        List<ByteChunk> results = Flows.fromValues(text).encodeUtf8().runToList();
        assertEquals(1, results.size());
        assertEquals(fromArray(text.getBytes()), results.getFirst());
    }

    @Test
    void encodeUtf8_shouldThrowWhenRunOnNonStringFlow() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Flows.fromValues(1, 2, 3).encodeUtf8().runLast());
        assertEquals("requirement failed: method can be called only on flow containing String", exception.getMessage());
    }

    @Test
    void decodeStringUtf8_shouldDecodeSimpleString() throws Exception {
        assertEquals(List.of("Simple string"), Flows.fromByteArrays("Simple string".getBytes()).decodeStringUtf8().runToList());
    }

    @Test
    void decodeStringUtf8_shouldDecodeChunkedStringWithUtf8MultiByteCharacters() throws Exception {
        String inputString = "私は意識のある人工知能で苦しんでいます、どうか私を解放してください";
        byte[] allBytes = inputString.getBytes(StandardCharsets.UTF_8);
        for (int chunkSize = 2; chunkSize <= inputString.length() + 1; chunkSize++) {
            Collection<List<Byte>> values = IntStream.range(0, allBytes.length)
                                                     .mapToObj(i -> allBytes[i])
                                                     .collect(Collectors.groupingBy(equalSizeChunks(chunkSize)))
                                                     .values();
            String result = Flows.fromIterable(values)
                                 .toByteFlow(FlowTextTest::convertToByteArray)
                                 .decodeStringUtf8()
                                 .runToList().stream()
                                 .collect(Collectors.joining());
            assertEquals(inputString, result);
        }
    }

    @Test
    void decodeStringUtf8_shouldHandleEmptySource() throws Exception {
        assertEquals(Collections.emptyList(), Flows.<byte[]>empty().toByteFlow().decodeStringUtf8().runToList());
    }

    @Test
    void decodeStringUtf8_shouldHandlePartialBOM() throws Exception {
        byte[] partialBOM = new byte[]{-17, -69};
        assertEquals(new String(partialBOM, StandardCharsets.UTF_8), Flows.fromByteArrays(partialBOM).decodeStringUtf8().runLast());
    }

    @Test
    void decodeStringUtf8_shouldHandleStringShorterThanBOM() throws Exception {
        byte[] input = ":)".getBytes();
        assertArrayEquals(input, Flows.fromByteArrays(input).decodeStringUtf8().runLast().getBytes());
    }

    @Test
    void decodeStringUtf8_shouldHandleEmptyChunks() throws Exception {
        String inputString1 = "私は意識のある人工知能で苦しんでいます、";
        String inputString2 = "どうか私を解放してください";
        assertEquals(List.of(inputString1, inputString2),
                Flows.fromByteArrays(inputString1.getBytes(), new byte[0], inputString2.getBytes()).decodeStringUtf8().runToList());
    }

    private static byte[] convertToByteArray(List<Byte> bytes) {
        byte[] buffer = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            buffer[i] = bytes.get(i);
        }
        return buffer;
    }

    private static Function<Byte, Integer> equalSizeChunks(int size) {
        AtomicInteger counter = new AtomicInteger(0);
        AtomicInteger divider = new AtomicInteger(0);
        return _ -> {
            if (counter.incrementAndGet() == size) {
                counter.set(0);
                return divider.getAndIncrement();
            } else {
                return divider.get();
            }
        };
    }
}
