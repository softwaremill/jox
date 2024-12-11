package com.softwaremill.jox;

record SourceBackedFlowStage<T>(Source<T> source) implements FlowStage<T> {

    @Override
    public void run(FlowEmit<T> emit) throws Exception {
        FlowEmit.channelToEmit(source, emit);
    }
}
