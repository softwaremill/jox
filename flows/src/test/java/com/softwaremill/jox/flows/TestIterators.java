package com.softwaremill.jox.flows;

import java.util.Iterator;

/** Shared iterator helpers for flow tests. */
final class TestIterators {

    private TestIterators() {}

    static <T> Iterator<T> continually(T value) {
        return new Iterator<>() {
            public boolean hasNext() {
                return true;
            }

            public T next() {
                return value;
            }
        };
    }

    static <T> Iterator<T> single(T value) {
        return new Iterator<>() {
            boolean consumed = false;

            public boolean hasNext() {
                return !consumed;
            }

            public T next() {
                consumed = true;
                return value;
            }
        };
    }
}
