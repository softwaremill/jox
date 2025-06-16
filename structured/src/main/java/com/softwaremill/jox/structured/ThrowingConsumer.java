package com.softwaremill.jox.structured;

/** {@link java.util.function.Consumer} which can throw an exception. */
@FunctionalInterface
public interface ThrowingConsumer<T> {
    void accept(T t) throws Exception;
}
