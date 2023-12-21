package com.softwaremill.jox;

/**
 * Returned by {@link Channel#sendSafe(Object)} and {@link Channel#receiveSafe()} when the channel is closed.
 */
public sealed interface ChannelClosed permits ChannelDone, ChannelError {
    ChannelClosedException toException();
}
