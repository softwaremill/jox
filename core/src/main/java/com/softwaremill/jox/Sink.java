package com.softwaremill.jox;

import java.util.function.Supplier;

/**
 * A channel sink, which can be used to send values to the channel. See {@link Channel} for more details.
 */
public interface Sink<T> extends CloseableChannel {
    /**
     * Send a value to the channel.
     *
     * @param value The value to send. Not {@code null}.
     * @throws ChannelClosedException When the channel is closed.
     */
    void send(T value) throws InterruptedException;

    /**
     * Send a value to the channel. Doesn't throw exceptions when the channel is closed, but returns a value.
     *
     * @param value The value to send. Not {@code null}.
     * @return Either {@code null}, or {@link ChannelClosed}, when the channel is closed.
     */
    Object sendOrClosed(T value) throws InterruptedException;

    //

    /**
     * Create a clause which can be used in {@link Select#select(SelectClause[])}. The clause will send the given value
     * to the current channel, and return {@code null} as the clause's result.
     */
    SelectClause<Void> sendClause(T value);

    /**
     * Create a clause which can be used in {@link Select#select(SelectClause[])}. The clause will send the given value
     * to the current channel, and return the value of the provided callback as the clause's result.
     */
    <U> SelectClause<U> sendClause(T value, Supplier<U> callback);
}
