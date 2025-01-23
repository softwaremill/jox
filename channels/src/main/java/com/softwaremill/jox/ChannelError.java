package com.softwaremill.jox;

public record ChannelError(Throwable cause, Channel<?> channel) implements ChannelClosed {
    @Override
    public ChannelClosedException toException() {
        return new ChannelErrorException(cause);
    }
}
