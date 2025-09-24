package com.softwaremill.jox.flows;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An iterator that efficiently iterates across multiple byte arrays without copying. This enables
 * O(1) concatenation of ByteChunks by avoiding array copying.
 */
class MultiArrayIterator implements Iterator<Byte> {
    private final List<byte[]> arrays;
    private int currentArrayIndex = 0;
    private int currentPosition = 0;
    private final int totalLength;

    public MultiArrayIterator(List<byte[]> arrays) {
        this.arrays = arrays;
        this.totalLength = arrays.stream().mapToInt(arr -> arr.length).sum();
    }

    @Override
    public boolean hasNext() {
        return currentArrayIndex < arrays.size()
                && (currentArrayIndex < arrays.size() - 1
                        || currentPosition < arrays.get(currentArrayIndex).length);
    }

    @Override
    public Byte next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        // Move to next non-empty array if current is exhausted
        while (currentArrayIndex < arrays.size()
                && currentPosition >= arrays.get(currentArrayIndex).length) {
            currentArrayIndex++;
            currentPosition = 0;
        }

        if (currentArrayIndex >= arrays.size()) {
            throw new NoSuchElementException();
        }

        return arrays.get(currentArrayIndex)[currentPosition++];
    }

    /**
     * Returns the number of bytes available for reading without blocking. This is used by
     * InputStream implementations.
     */
    public int available() {
        int remaining = 0;
        for (int i = currentArrayIndex; i < arrays.size(); i++) {
            byte[] array = arrays.get(i);
            if (i == currentArrayIndex) {
                remaining += array.length - currentPosition;
            } else {
                remaining += array.length;
            }
        }
        return remaining;
    }

    /** Returns the total length of all arrays combined. */
    public int totalLength() {
        return totalLength;
    }

    /** Creates an empty MultiArrayIterator. */
    public static MultiArrayIterator empty() {
        return new MultiArrayIterator(List.of());
    }
}
