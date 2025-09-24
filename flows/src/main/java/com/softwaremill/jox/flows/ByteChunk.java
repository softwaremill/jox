package com.softwaremill.jox.flows;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A simple wrapper class for byte arrays which purpose is to simplify operations and processing.
 * ByteChunk is immutable and optimized for efficient concatenation using multi-array backing.
 */
public class ByteChunk {

    private final List<byte[]> arrays;
    private final int totalLength;

    /** Creates a ByteChunk from multiple byte arrays. */
    private ByteChunk(List<byte[]> arrays) {
        this.arrays = arrays;
        this.totalLength = arrays.stream().mapToInt(arr -> arr.length).sum();
    }

    /** Creates a ByteChunk from a single byte array. */
    public static ByteChunk fromArray(byte[] array) {
        var l = new ArrayList<byte[]>();
        l.add(array);
        return new ByteChunk(l);
    }

    /** Creates an empty ByteChunk. */
    public static ByteChunk empty() {
        return new ByteChunk(new ArrayList<>());
    }

    /**
     * @param idx index of the element
     * @return element positioned at the given index
     */
    public byte get(int idx) {
        if (idx < 0 || idx >= totalLength) {
            throw new IndexOutOfBoundsException(
                    "Index %d out of bounds for array of length %d".formatted(idx, totalLength));
        }

        int currentIndex = idx;
        for (byte[] array : arrays) {
            if (currentIndex < array.length) {
                return array[currentIndex];
            }
            currentIndex -= array.length;
        }

        // This should never happen due to the bounds check above
        throw new IndexOutOfBoundsException(
                "Index %d out of bounds for array of length %d".formatted(idx, totalLength));
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
        return new MultiArrayIterator(arrays);
    }

    /**
     * @return number of elements in the Chunk
     */
    public int length() {
        return totalLength;
    }

    /** Takes n elements from the beginning of the Chunk and returns copy of the result. */
    public ByteChunk take(int n) {
        if (n <= 0) {
            return empty();
        }
        if (n >= totalLength) {
            return this;
        }

        // Find which arrays to include and where to stop
        List<byte[]> newArrays = new ArrayList<>();
        int remaining = n;

        for (byte[] array : arrays) {
            if (remaining <= 0) break;

            if (remaining >= array.length) {
                // Take the entire array
                newArrays.add(array);
                remaining -= array.length;
            } else {
                // Take partial array
                byte[] partialArray = Arrays.copyOf(array, remaining);
                newArrays.add(partialArray);
                remaining = 0;
            }
        }

        return new ByteChunk(newArrays);
    }

    /** Drops n elements from the beginning of the array and returns copy of the result. */
    public ByteChunk drop(int n) {
        if (n <= 0) {
            return this;
        }
        if (n >= totalLength) {
            return empty();
        }

        // Find which arrays to keep and where to start
        int remaining = n;
        int startArrayIndex = 0;
        int startOffset = 0;

        for (int i = 0; i < arrays.size(); i++) {
            byte[] array = arrays.get(i);
            if (remaining < array.length) {
                startArrayIndex = i;
                startOffset = remaining;
                break;
            }
            remaining -= array.length;
        }

        // If we're dropping from a single array and it's a complete prefix
        if (arrays.size() == 1) {
            byte[] array = arrays.get(0);
            List<byte[]> newArrays = new ArrayList<>();
            newArrays.add(Arrays.copyOfRange(array, n, array.length));
            return new ByteChunk(newArrays);
        }

        // Create new array list starting from the appropriate position
        List<byte[]> newArrays = new java.util.ArrayList<>();

        // Add the partial first array if needed
        if (startOffset > 0) {
            byte[] firstArray = arrays.get(startArrayIndex);
            if (startOffset < firstArray.length) {
                byte[] partialArray =
                        Arrays.copyOfRange(firstArray, startOffset, firstArray.length);
                newArrays.add(partialArray);
            }
            startArrayIndex++;
        }

        // Add remaining complete arrays
        for (int i = startArrayIndex; i < arrays.size(); i++) {
            newArrays.add(arrays.get(i));
        }

        return newArrays.isEmpty() ? empty() : new ByteChunk(newArrays);
    }

    /**
     * @param idx index at which to split the Chunk
     * @return Pair of Chunks, first containing elements from 0 to idx (exclusive), second
     *     containing elements from idx to the end
     */
    public Map.Entry<ByteChunk, ByteChunk> splitAt(int idx) {
        return Map.entry(take(idx), drop(idx));
    }

    /**
     * @param other Chunk to concatenate with this
     * @return new Chunk containing elements of this and other
     */
    public ByteChunk concat(ByteChunk other) {
        if (this.totalLength == 0) {
            return other;
        }
        if (other.totalLength == 0) {
            return this;
        }

        // Combine the array lists for O(1) concatenation
        List<byte[]> combinedArrays =
                new java.util.ArrayList<>(this.arrays.size() + other.arrays.size());
        combinedArrays.addAll(this.arrays);
        combinedArrays.addAll(other.arrays);

        return new ByteChunk(combinedArrays);
    }

    /**
     * @param condition function that returns true for element that should be found
     * @return index of the first element that satisfies the condition, or -1 if no such element is
     *     found
     */
    public int indexWhere(Function<Byte, Boolean> condition) {
        int currentIndex = 0;

        for (byte[] array : arrays) {
            for (int i = 0; i < array.length; i++) {
                if (condition.apply(array[i])) {
                    return currentIndex + i;
                }
            }
            currentIndex += array.length;
        }

        return -1;
    }

    /**
     * @return copy of the Chunk as an array
     *     <p>This method creates a single contiguous array from the multi-array backing. For
     *     performance-critical code that needs to iterate over bytes, consider using the iterator()
     *     method or accessing arrays directly via getArrays().
     */
    public byte[] toArray() {
        if (totalLength == 0) {
            return new byte[0];
        }

        // Optimization: if we have only one array, return a copy directly
        if (arrays.size() == 1) {
            byte[] singleArray = arrays.get(0);
            return Arrays.copyOf(singleArray, singleArray.length);
        }

        // For multiple arrays, concatenate them
        byte[] result = new byte[totalLength];
        int offset = 0;

        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }

        return result;
    }

    /** Checks if this starts with chunk b */
    public boolean startsWith(ByteChunk other) {
        return Arrays.equals(this.take(other.length()).toArray(), other.toArray());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ByteChunk that = (ByteChunk) obj;

        // Quick length check
        if (this.totalLength != that.totalLength) {
            return false;
        }

        // Fallback to byte-by-byte comparison for different structures
        return Arrays.equals(this.toArray(), that.toArray());
    }

    /**
     * Returns the backing arrays for optimized operations that can work directly with multiple
     * arrays without flattening.
     *
     * @return immutable list of backing byte arrays
     */
    public List<byte[]> getArrays() {
        return arrays;
    }
}
