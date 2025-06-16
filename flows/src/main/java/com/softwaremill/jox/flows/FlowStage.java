package com.softwaremill.jox.flows;

/**
 * Contains the logic for running a single flow stage. As part of `run`s implementation, previous
 * flow stages might be run, either synchronously or asynchronously.
 */
public interface FlowStage<T> {
    void run(FlowEmit<T> emit) throws Exception;
}
