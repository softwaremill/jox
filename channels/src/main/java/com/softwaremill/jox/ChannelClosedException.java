package com.softwaremill.jox;

/**
 * Thrown by {@link Channel#send(Object)} and {@link Channel#receive()} when the channel is closed.
 */
public sealed class ChannelClosedException extends RuntimeException
        permits ChannelDoneException, ChannelErrorException {
    public ChannelClosedException() {}

    public ChannelClosedException(Throwable cause) {
        super(cause);
    }
}
