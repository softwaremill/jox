package com.softwaremill.jox;

public sealed class ChannelClosedException extends RuntimeException permits ChannelDoneException, ChannelErrorException {
    public ChannelClosedException() {}

    public ChannelClosedException(Throwable cause) {
        super(cause);
    }
}

