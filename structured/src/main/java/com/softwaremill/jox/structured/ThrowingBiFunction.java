package com.softwaremill.jox.structured;

/** {@link java.util.function.BiFunction} which can throw an exception. */
@FunctionalInterface
public interface ThrowingBiFunction<T, U, R> {
    R apply(T t, U u) throws Exception;
}
