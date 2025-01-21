package com.softwaremill.jox;

public record ChannelDone(Channel<?> channel) implements ChannelClosed {
    @Override
    public ChannelClosedException toException() {
        return new ChannelDoneException();
    }
}
