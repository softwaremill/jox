package com.softwaremill.jox;

/**
 * Returned by {@link Channel#sendOrClosed(Object)} and {@link Channel#receiveOrClosed()} when the channel is closed.
 */
public sealed interface ChannelClosed permits ChannelDone, ChannelError {
    ChannelClosedException toException();
}
