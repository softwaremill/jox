package com.softwaremill.jox.flows;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ByteChunkTest {

    @Test
    void shouldCreateFromArray() {
        // given
        byte[] array = {1, 2, 3};

        // when
        ByteChunk chunk = ByteChunk.fromArray(array);

        // then
        assertArrayEquals(array, chunk.toArray());

        assertEquals(1, chunk.get(0));
        assertEquals(2, chunk.get(1));
        assertEquals(3, chunk.get(2));

        assertEquals(3, chunk.length());
    }

    @Test
    void shouldCreateEmpty() {
        ByteChunk chunk = ByteChunk.empty();
        assertEquals(0, chunk.length());
    }

    @Test
    void shouldThrowWhenIndexOutOfBounds() {
        // given
        byte[] array = {1, 2, 3};

        // when
        ByteChunk chunk = ByteChunk.fromArray(array);

        // then
        assertThrows(IndexOutOfBoundsException.class, () -> chunk.get(3));
    }

    @Test
    void shouldConvertToString() {
        // given
        byte[] array = "hello".getBytes(StandardCharsets.ISO_8859_1);

        // when
        ByteChunk chunk = ByteChunk.fromArray(array);

        // then
        assertEquals("hello", chunk.convertToString(StandardCharsets.ISO_8859_1));
    }

    @Test
    void shouldReturnWorkingIterator() {
        // given
        byte[] array = {1, 2, 3};
        ByteChunk chunk = ByteChunk.fromArray(array);
        int i = 0;

        // when & then
        for (Iterator<Byte> it = chunk.iterator(); it.hasNext(); ) {
            Byte b = it.next();
            assertEquals(array[i++], b);
        }
    }

    @Test
    void shouldTake() {
        // given
        byte[] array = {1, 2, 3};
        ByteChunk chunk = ByteChunk.fromArray(array);

        // when
        ByteChunk taken = chunk.take(2);

        // then
        assertArrayEquals(new byte[]{1, 2}, taken.toArray());
    }

    @Test
    void shouldDrop() {
        // given
        byte[] array = {1, 2, 3};
        ByteChunk chunk = ByteChunk.fromArray(array);

        // when
        ByteChunk dropped = chunk.drop(1);

        // then
        assertArrayEquals(new byte[]{2, 3}, dropped.toArray());
    }

    @Test
    void shouldSplitAt() {
        // given
        byte[] array = {1, 2, 3};
        ByteChunk chunk = ByteChunk.fromArray(array);

        // when
        Map.Entry<ByteChunk, ByteChunk> split = chunk.splitAt(1);

        // then
        assertArrayEquals(new byte[]{1}, split.getKey().toArray());
        assertArrayEquals(new byte[]{2, 3}, split.getValue().toArray());
    }

    @Test
    void shouldConcat() {
        // given
        byte[] array1 = {1, 2};
        byte[] array2 = {3, 4};
        ByteChunk chunk1 = ByteChunk.fromArray(array1);
        ByteChunk chunk2 = ByteChunk.fromArray(array2);

        // when
        ByteChunk concatenated = chunk2.concat(chunk1);

        // then
        assertArrayEquals(new byte[]{3, 4, 1, 2}, concatenated.toArray());
    }

    @Test
    void shouldReturnCorrectIndexWhenConditionIsFulfilled() {
        // given
        byte[] array = {1, 2, 3};
        ByteChunk chunk = ByteChunk.fromArray(array);

        // when & then
        assertEquals(1, chunk.indexWhere(b -> b == 2));
        assertEquals(-1, chunk.indexWhere(b -> b == 4));
    }

    @Test
    void shouldReturnMinusOneWhenConditionIsNotFulfilled() {
        // given
        byte[] array = {1, 2, 3};
        ByteChunk chunk = ByteChunk.fromArray(array);

        // when & then
        assertEquals(-1, chunk.indexWhere(b -> b == 4));
    }

    @Test
    void shouldReturnStartsWithCorrectly() {
        // given
        byte[] array1 = {1, 2, 3};
        byte[] array2 = {1, 2};
        ByteChunk chunk1 = ByteChunk.fromArray(array1);
        ByteChunk chunk2 = ByteChunk.fromArray(array2);

        // when & then
        assertTrue(chunk1.startsWith(chunk2));
        assertFalse(chunk2.startsWith(chunk1));
    }

    @Test
    void testEqualsAndHashCode() {
        // given
        byte[] array1 = {1, 2, 3};
        byte[] array2 = {1, 2, 3};
        ByteChunk chunk1 = ByteChunk.fromArray(array1);
        ByteChunk chunk2 = ByteChunk.fromArray(array2);

        // when & then
        assertEquals(chunk1, chunk2);
        assertEquals(chunk1.hashCode(), chunk2.hashCode());
    }
}