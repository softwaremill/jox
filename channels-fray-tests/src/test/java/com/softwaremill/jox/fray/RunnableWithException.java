package com.softwaremill.jox.fray;

@FunctionalInterface
interface RunnableWithException {
    void run() throws Exception;
}
