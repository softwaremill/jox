package com.softwaremill.jox;

import java.util.concurrent.ExecutionException;

public interface FlowEmit<T> {
    void apply(T t) throws Exception;

    static <T> void channelToEmit(Source<T> source, FlowEmit<T> emit) throws Exception {
        boolean shouldRun = true;
        while (shouldRun) {
            Object t = source.receiveOrClosed();
            shouldRun = switch (t) {
                case ChannelDone done -> false;
                case ChannelError error -> throw error.toException();
                default -> {
                    try {
                        //noinspection unchecked
                        emit.apply((T) t);
                    } catch (Throwable e) {
                        throw new ExecutionException(e);
                    }
                    yield true;
                }
            };
        }
    }
}
