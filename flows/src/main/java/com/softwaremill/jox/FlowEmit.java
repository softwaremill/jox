package com.softwaremill.jox;

public interface FlowEmit<T> {
    void apply(T t) throws Throwable;

    static <T> void channelToEmit(Source<T> source, FlowEmit<T> emit) throws Throwable {
        boolean shouldRun = true;
        while (shouldRun) {
            Object t = null;
            try {
                t = source.receiveOrClosed();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            shouldRun = !switch (t) {
                case ChannelDone done -> false;
                case ChannelError error -> throw error.toException();
                default -> {
                    //noinspection unchecked
                    emit.apply((T) t);
                    yield true;
                }
            };
        }
    }
}
