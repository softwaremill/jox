package com.softwaremill.jox.flows;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ByteFlowTest {

    @Test
    void shouldThrowWhenMappingToByteFlowWhenFlowDoesNotContainByteChunksNorByteArrays() {
        // given
        var flow = Flows.fromValues(1, 2, 3);

        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> flow.toByteFlow());

        // then
        assertEquals("requirement failed: ByteFlow can only be created from ByteChunk or byte[]", exception.getMessage());
    }

    @Test
    void shouldMapToByteFlowUsingByteArrayMappingFunction() throws Exception {
        // given
        var flow = Flows.fromValues(1, 2, 3);

        // when
        Flow.ByteFlow byteFlow = flow.toByteFlow((Flow.ByteArrayMapper<Integer>) integer -> new byte[]{integer.byteValue()});

        // then
        assertEquals(List.of(
                        ByteChunk.fromArray(new byte[]{1}),
                        ByteChunk.fromArray(new byte[]{2}),
                        ByteChunk.fromArray(new byte[]{3})),
                byteFlow.runToList());
    }

    @Test
    void shouldMapToByteFlowUsingByteChunkMappingFunction() throws Exception {
        // given
        var flow = Flows.fromValues(1, 2, 3);

        // when
        Flow.ByteFlow byteFlow = flow.toByteFlow((Flow.ByteChunkMapper<Integer>) integer -> ByteChunk.fromArray(new byte[]{integer.byteValue()}));

        // then
        assertEquals(List.of(
                        ByteChunk.fromArray(new byte[]{1}),
                        ByteChunk.fromArray(new byte[]{2}),
                        ByteChunk.fromArray(new byte[]{3})),
                byteFlow.runToList());
    }

    @Test
    void shouldCreateByteFlowFromByteArrays() throws Exception {
        Flow.ByteFlow byteFlow = Flows.fromByteArrays("MjE".getBytes(StandardCharsets.UTF_8), "zNw==".getBytes(StandardCharsets.UTF_8));

        assertEquals(List.of("MjEzNw=="), byteFlow.linesUtf8().runToList());
    }

    @Test
    void shouldCreateByteFlowFromByteChunks() throws Exception {
        Flow.ByteFlow byteFlow = Flows.fromByteChunks(
                ByteChunk.fromArray("MjE".getBytes(StandardCharsets.UTF_8)),
                ByteChunk.fromArray("zNw==".getBytes(StandardCharsets.UTF_8))
        );

        assertEquals(List.of("MjEzNw=="), byteFlow.linesUtf8().runToList());
    }
}
