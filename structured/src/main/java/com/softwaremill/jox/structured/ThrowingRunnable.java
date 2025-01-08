package com.softwaremill.jox.structured;

@FunctionalInterface
public interface ThrowingRunnable {
    void run() throws Exception;
}
