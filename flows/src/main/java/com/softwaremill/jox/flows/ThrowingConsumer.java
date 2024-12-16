package com.softwaremill.jox.flows;

public interface ThrowingConsumer<T> {
    void accept(T t) throws Exception;
}
