package com.softwaremill.jox;

public interface ThrowingConsumer<T> {
    void accept(T t) throws Throwable;
}
