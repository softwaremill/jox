package com.softwaremill.jox;

import java.util.function.Function;

/**
 * A channel source, which can be used to receive values from the channel. See {@link Channel} for more details.
 */
public interface Source<T> extends CloseableChannel {
    /**
     * Receive a value from the channel.
     *
     * @throws ChannelClosedException When the channel is closed.
     */
    T receive() throws InterruptedException;

    /**
     * Receive a value from the channel. Doesn't throw exceptions when the channel is closed, but returns a value.
     *
     * @return Either a value of type {@code T}, or {@link ChannelClosed}, when the channel is closed.
     */
    Object receiveSafe() throws InterruptedException;

    /**
     * Create a clause which can be used in {@link Select#select(SelectClause[])}. The clause will receive a value from
     * the current channel.
     */
    SelectClause<T> receiveClause();

    /**
     * Create a clause which can be used in {@link Select#select(SelectClause[])}. The clause will receive a value from
     * the current channel, and transform it using the provided {@code callback}.
     */
    <U> SelectClause<U> receiveClause(Function<T, U> callback);
}
