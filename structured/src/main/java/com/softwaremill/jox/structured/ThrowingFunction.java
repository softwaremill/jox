package com.softwaremill.jox.structured;

/** {@link java.util.function.Function} which can throw an exception. */
@FunctionalInterface
public interface ThrowingFunction<T, R> {
    R apply(T t) throws Exception;
}
