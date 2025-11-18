package com.softwaremill.jox;

import java.util.function.Supplier;

/**
 * A channel sink, which can be used to send values to the channel. See {@link Channel} for more
 * details.
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
     * Send a value to the channel. Doesn't throw exceptions when the channel is closed but returns
     * a value.
     *
     * @param value The value to send. Not {@code null}.
     * @return Either {@code null}, or {@link ChannelClosed}, when the channel is closed.
     */
    Object sendOrClosed(T value) throws InterruptedException;

    /**
     * Attempt to send a value to the channel if there's a waiting receiver, or space in the buffer.
     *
     * @param value The value to send. Not {@code null}.
     * @return {@code true} if the value was sent, {@code false} otherwise.
     * @throws ChannelClosedException When the channel is closed.
     */
    default boolean trySend(T value) {
        Object sent;
        try {
            sent = Select.select(sendClause(value), Channel.DEFAULT_NOT_SENT_CLAUSE);
        } catch (InterruptedException e) {
            throw new IllegalStateException(
                    "Interrupted during trySend, which should not be possible", e);
        }
        return sent != Channel.DEFAULT_NOT_SENT_VALUE;
    }

    /**
     * Attempt to send a value to one of the given channels if in any of them there's a waiting
     * receiver, or space in the buffer.
     *
     * @param value The value to send. Not {@code null}.
     * @return {@code true} if the value was sent, {@code false} otherwise.
     * @throws ChannelClosedException When the channel is closed.
     */
    @SafeVarargs
    static <T> boolean trySend(T value, Sink<T>... toOneOfChannels) {
        if (toOneOfChannels == null || toOneOfChannels.length == 0) return false;

        var selectCauses = new SelectClause[toOneOfChannels.length + 1];
        for (int i = 0; i < toOneOfChannels.length; i++) {
            selectCauses[i] = toOneOfChannels[i].sendClause(value);
        }
        selectCauses[toOneOfChannels.length] = Channel.DEFAULT_NOT_SENT_CLAUSE;

        Object sent;
        try {
            sent = Select.select(selectCauses);
        } catch (InterruptedException e) {
            throw new IllegalStateException(
                    "Interrupted during trySend, which should not be possible", e);
        }
        return sent != Channel.DEFAULT_NOT_SENT_VALUE;
    }

    //

    /**
     * Create a clause which can be used in {@link Select#select(SelectClause[])}. The clause will
     * send the given value to the current channel, and return {@code null} as the clause's result.
     */
    SelectClause<Void> sendClause(T value);

    /**
     * Create a clause which can be used in {@link Select#select(SelectClause[])}. The clause will
     * send the given value to the current channel and return the value of the provided callback as
     * the clause's result.
     */
    <U> SelectClause<U> sendClause(T value, Supplier<U> callback);
}
