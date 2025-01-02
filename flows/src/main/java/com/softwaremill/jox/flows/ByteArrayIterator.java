package com.softwaremill.jox.flows;

import java.util.Iterator;
import java.util.NoSuchElementException;

class ByteArrayIterator implements Iterator<Byte> {
    private final byte[] array;
    private int position = 0;

    public ByteArrayIterator(byte[] array) {
        this.array = array;
    }

    @Override
    public boolean hasNext() {
        return position < array.length;
    }

    @Override
    public Byte next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return array[position++];
    }

    public int available() {
        return array.length - position;
    }

    public static ByteArrayIterator empty() {
        return new ByteArrayIterator(new byte[0]);
    }
}
