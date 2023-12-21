package com.softwaremill.jox;

public record ChannelError(Throwable cause) implements ChannelClosed {
    @Override
    public ChannelClosedException toException() {
        return new ChannelErrorException(cause);
    }
}
