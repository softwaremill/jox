package com.softwaremill.jox.flows;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

import org.junit.jupiter.api.Test;

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
        assertArrayEquals(new byte[] {1, 2}, taken.toArray());
    }

    @Test
    void shouldDrop() {
        // given
        byte[] array = {1, 2, 3};
        ByteChunk chunk = ByteChunk.fromArray(array);

        // when
        ByteChunk dropped = chunk.drop(1);

        // then
        assertArrayEquals(new byte[] {2, 3}, dropped.toArray());
    }

    @Test
    void shouldSplitAt() {
        // given
        byte[] array = {1, 2, 3};
        ByteChunk chunk = ByteChunk.fromArray(array);

        // when
        Map.Entry<ByteChunk, ByteChunk> split = chunk.splitAt(1);

        // then
        assertArrayEquals(new byte[] {1}, split.getKey().toArray());
        assertArrayEquals(new byte[] {2, 3}, split.getValue().toArray());
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
        assertArrayEquals(new byte[] {3, 4, 1, 2}, concatenated.toArray());
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
    }

    @Test
    void shouldConcatenateMultipleChunks() {
        // given
        ByteChunk chunk1 = ByteChunk.fromArray(new byte[] {1, 2});
        ByteChunk chunk2 = ByteChunk.fromArray(new byte[] {3, 4});
        ByteChunk chunk3 = ByteChunk.fromArray(new byte[] {5, 6});

        // when
        ByteChunk result = chunk1.concat(chunk2).concat(chunk3);

        // then
        assertArrayEquals(new byte[] {1, 2, 3, 4, 5, 6}, result.toArray());
        assertEquals(3, result.getArrays().size()); // Should preserve array structure
    }

    @Test
    void shouldHandleEmptyChunkConcatenation() {
        // given
        ByteChunk empty = ByteChunk.empty();
        ByteChunk nonEmpty = ByteChunk.fromArray(new byte[] {1, 2, 3});

        // when & then
        assertSame(nonEmpty, empty.concat(nonEmpty));
        assertSame(nonEmpty, nonEmpty.concat(empty));
        assertEquals(0, empty.concat(empty).length());
    }

    @Test
    void shouldTakeFromMultipleArrays() {
        // given
        ByteChunk chunk1 = ByteChunk.fromArray(new byte[] {1, 2, 3});
        ByteChunk chunk2 = ByteChunk.fromArray(new byte[] {4, 5, 6});
        ByteChunk concatenated = chunk1.concat(chunk2);

        // when
        ByteChunk taken = concatenated.take(4);

        // then
        assertArrayEquals(new byte[] {1, 2, 3, 4}, taken.toArray());
        // Should reuse first array completely and create partial second array
        assertEquals(2, taken.getArrays().size());
    }

    @Test
    void shouldTakeZeroElements() {
        // given
        ByteChunk chunk = ByteChunk.fromArray(new byte[] {1, 2, 3});

        // when
        ByteChunk taken = chunk.take(0);

        // then
        assertEquals(0, taken.length());
        assertTrue(taken.getArrays().isEmpty());
    }

    @Test
    void shouldTakeMoreThanAvailable() {
        // given
        ByteChunk chunk = ByteChunk.fromArray(new byte[] {1, 2, 3});

        // when
        ByteChunk taken = chunk.take(10);

        // then
        assertSame(chunk, taken);
    }

    @Test
    void shouldDropFromMultipleArrays() {
        // given
        ByteChunk chunk1 = ByteChunk.fromArray(new byte[] {1, 2, 3});
        ByteChunk chunk2 = ByteChunk.fromArray(new byte[] {4, 5, 6});
        ByteChunk concatenated = chunk1.concat(chunk2);

        // when
        ByteChunk dropped = concatenated.drop(2);

        // then
        assertArrayEquals(new byte[] {3, 4, 5, 6}, dropped.toArray());
    }

    @Test
    void shouldDropEntireFirstArray() {
        // given
        ByteChunk chunk1 = ByteChunk.fromArray(new byte[] {1, 2, 3});
        ByteChunk chunk2 = ByteChunk.fromArray(new byte[] {4, 5, 6});
        ByteChunk concatenated = chunk1.concat(chunk2);

        // when
        ByteChunk dropped = concatenated.drop(3);

        // then
        assertArrayEquals(new byte[] {4, 5, 6}, dropped.toArray());
        // Should reuse the second array completely
        assertEquals(1, dropped.getArrays().size());
        assertSame(chunk2.getArrays().get(0), dropped.getArrays().get(0));
    }

    @Test
    void shouldDropZeroElements() {
        // given
        ByteChunk chunk = ByteChunk.fromArray(new byte[] {1, 2, 3});

        // when
        ByteChunk dropped = chunk.drop(0);

        // then
        assertSame(chunk, dropped);
    }

    @Test
    void shouldDropMoreThanAvailable() {
        // given
        ByteChunk chunk = ByteChunk.fromArray(new byte[] {1, 2, 3});

        // when
        ByteChunk dropped = chunk.drop(10);

        // then
        assertEquals(0, dropped.length());
    }

    @Test
    void shouldSplitAtMultipleArrayBoundary() {
        // given
        ByteChunk chunk1 = ByteChunk.fromArray(new byte[] {1, 2, 3});
        ByteChunk chunk2 = ByteChunk.fromArray(new byte[] {4, 5, 6});
        ByteChunk concatenated = chunk1.concat(chunk2);

        // when
        Map.Entry<ByteChunk, ByteChunk> split = concatenated.splitAt(3);

        // then
        assertArrayEquals(new byte[] {1, 2, 3}, split.getKey().toArray());
        assertArrayEquals(new byte[] {4, 5, 6}, split.getValue().toArray());
    }

    @Test
    void shouldHandleIndexWhereAcrossArrays() {
        // given
        ByteChunk chunk1 = ByteChunk.fromArray(new byte[] {1, 2, 3});
        ByteChunk chunk2 = ByteChunk.fromArray(new byte[] {4, 5, 6});
        ByteChunk concatenated = chunk1.concat(chunk2);

        // when & then
        assertEquals(0, concatenated.indexWhere(b -> b == 1));
        assertEquals(2, concatenated.indexWhere(b -> b == 3));
        assertEquals(3, concatenated.indexWhere(b -> b == 4)); // First element of second array
        assertEquals(5, concatenated.indexWhere(b -> b == 6)); // Last element
        assertEquals(-1, concatenated.indexWhere(b -> b == 10)); // Not found
    }

    @Test
    void shouldHandleIteratorAcrossArrays() {
        // given
        ByteChunk chunk1 = ByteChunk.fromArray(new byte[] {1, 2});
        ByteChunk chunk2 = ByteChunk.fromArray(new byte[] {3, 4, 5});
        ByteChunk concatenated = chunk1.concat(chunk2);

        // when
        Iterator<Byte> iterator = concatenated.iterator();
        byte[] result = new byte[5];
        int i = 0;
        while (iterator.hasNext()) {
            result[i++] = iterator.next();
        }

        // then
        assertArrayEquals(new byte[] {1, 2, 3, 4, 5}, result);
    }

    @Test
    void shouldHandleStartsWithAcrossArrays() {
        // given
        ByteChunk chunk1 = ByteChunk.fromArray(new byte[] {1, 2, 3});
        ByteChunk chunk2 = ByteChunk.fromArray(new byte[] {4, 5, 6});
        ByteChunk concatenated = chunk1.concat(chunk2);

        ByteChunk prefix1 = ByteChunk.fromArray(new byte[] {1, 2});
        ByteChunk prefix2 = ByteChunk.fromArray(new byte[] {1, 2, 3, 4});
        ByteChunk notPrefix = ByteChunk.fromArray(new byte[] {2, 3});

        // when & then
        assertTrue(concatenated.startsWith(prefix1));
        assertTrue(concatenated.startsWith(prefix2));
        assertFalse(concatenated.startsWith(notPrefix));
    }

    @Test
    void shouldHandleEqualsWithDifferentArrayStructures() {
        // given
        ByteChunk singleArray = ByteChunk.fromArray(new byte[] {1, 2, 3, 4, 5});

        ByteChunk chunk1 = ByteChunk.fromArray(new byte[] {1, 2});
        ByteChunk chunk2 = ByteChunk.fromArray(new byte[] {3, 4, 5});
        ByteChunk multiArray = chunk1.concat(chunk2);

        // when & then
        assertEquals(singleArray, multiArray);
    }

    @Test
    void shouldHandleConvertToStringAcrossArrays() {
        // given
        ByteChunk chunk1 = ByteChunk.fromArray("Hello ".getBytes(StandardCharsets.UTF_8));
        ByteChunk chunk2 = ByteChunk.fromArray("World".getBytes(StandardCharsets.UTF_8));
        ByteChunk concatenated = chunk1.concat(chunk2);

        // when
        String result = concatenated.convertToString(StandardCharsets.UTF_8);

        // then
        assertEquals("Hello World", result);
    }

    @Test
    void shouldHandleGetAcrossArrays() {
        // given
        ByteChunk chunk1 = ByteChunk.fromArray(new byte[] {1, 2, 3});
        ByteChunk chunk2 = ByteChunk.fromArray(new byte[] {4, 5, 6});
        ByteChunk concatenated = chunk1.concat(chunk2);

        // when & then
        assertEquals(1, concatenated.get(0));
        assertEquals(3, concatenated.get(2));
        assertEquals(4, concatenated.get(3)); // First element of second array
        assertEquals(6, concatenated.get(5)); // Last element

        assertThrows(IndexOutOfBoundsException.class, () -> concatenated.get(6));
        assertThrows(IndexOutOfBoundsException.class, () -> concatenated.get(-1));
    }

    @Test
    void shouldHandleLargeConcatenationChains() {
        // given
        ByteChunk result = ByteChunk.empty();

        // when - create a chain of 10 small chunks
        for (int i = 0; i < 10; i++) {
            ByteChunk small = ByteChunk.fromArray(new byte[] {(byte) i});
            result = result.concat(small);
        }

        // then
        assertEquals(10, result.length());
        assertEquals(10, result.getArrays().size()); // Should preserve array structure

        for (int i = 0; i < 10; i++) {
            assertEquals((byte) i, result.get(i));
        }
    }
}
