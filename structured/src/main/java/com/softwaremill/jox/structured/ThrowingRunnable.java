package com.softwaremill.jox.structured;

/** {@link java.lang.Runnable} which can throw an exception. */
@FunctionalInterface
public interface ThrowingRunnable {
    void run() throws Exception;
}
