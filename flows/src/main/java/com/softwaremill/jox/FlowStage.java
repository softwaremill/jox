package com.softwaremill.jox;

public interface FlowStage<T> {
    void run(FlowEmit<T> emit) throws Throwable;
}
