package com.softwaremill.jox.flows;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * A simple wrapper class for byte array which purpose is to simply operations and processing.
 * ByteChunk is immutable.
 */
public class ByteChunk {

    private final byte[] array;

    public ByteChunk(byte[] array) {
        this.array = array;
    }

    public static ByteChunk fromArray(byte[] array) {
        return new ByteChunk(array);
    }

    public static ByteChunk fromArray(Byte[] array) {
        byte[] bytes = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            bytes[i] = array[i];
        }
        return new ByteChunk(bytes);
    }

    public static ByteChunk empty() {
        return new ByteChunk(new byte[0]);
    }

    /**
     * @param idx index of the element
     * @return element positioned at the given index
     */
    public byte get(int idx) {
        if (idx < 0 || idx >= array.length) {
            throw new IndexOutOfBoundsException("Index %d out of bounds for array of length %d".formatted(idx, array.length));
        }
        return array[idx];
    }

    /**
     * @return Chunk converted to String using given charset
     */
    public String convertToString(Charset charset) {
        return new String(toArray(), charset);
    }

    /**
     * @return Iterator over the elements of the Chunk
     */
    public Iterator<Byte> iterator() {
        return IntStream.range(0, array.length)
                .mapToObj(i -> array[i])
                .iterator();
    }

    /**
     * @return number of elements in the Chunk
     */
    public int length() {
        return array.length;
    }

    /**
     * Takes n elements from the beginning of the Chunk and returns copy of the result
     */
    public ByteChunk take(int n) {
        return new ByteChunk(Arrays.copyOf(array, n));
    }

    /**
     * Drops n elements from the beginning of the array and returns copy of the result
     */
    public ByteChunk drop(int n) {
        return new ByteChunk(Arrays.copyOfRange(array, n, array.length));
    }

    /**
     * @param idx index at which to split the Chunk
     * @return Pair of Chunks, first containing elements from 0 to idx (exclusive), second containing elements from idx to the end
     */
    public Map.Entry<ByteChunk, ByteChunk> splitAt(int idx) {
        return Map.entry(take(idx), drop(idx));
    }

    /**
     * @param other Chunk to concatenate with this
     * @return new Chunk containing elements of this and other
     */
    public ByteChunk concat(ByteChunk other) {
        return ByteChunk.fromArray(concatArrays(toArray(), other.toArray()));
    }

    /**
     * @param condition function that returns true for element that should be found
     * @return index of the first element that satisfies the condition, or -1 if no such element is found
     */
    public int indexWhere(Function<Byte, Boolean> condition) {
        Iterator<Byte> iterator = iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Byte item = iterator.next();
            if (condition.apply(item)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * @return copy of the Chunk as an array
     */
    public byte[] toArray() {
        return Arrays.copyOf(array, array.length);
    }

    /**
     * Checks if this starts with chunk b
     */
    public boolean startsWith(ByteChunk other) {
        return Arrays.equals(this.take(other.length()).toArray(), other.toArray());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(array);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ByteChunk that = (ByteChunk) obj;
        return Arrays.equals(array, that.array);
    }

    private static byte[] concatArrays(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
