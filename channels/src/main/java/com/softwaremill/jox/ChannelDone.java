package com.softwaremill.jox;

public record ChannelDone() implements ChannelClosed {
    @Override
    public ChannelClosedException toException() {
        return new ChannelDoneException();
    }
}
