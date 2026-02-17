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
     * <p>This method never blocks or suspends the calling thread. It completes in bounded time.
     * Safe to call from platform threads, including NIO event loop threads.
     *
     * <p>May return {@code false} even when space is available, due to contention with concurrent
     * operations. Should not be used as a substitute for {@link #send(Object)} in a spin loop.
     *
     * @param value The value to send. Not {@code null}.
     * @return {@code true} if the value was sent, {@code false} otherwise.
     * @throws ChannelClosedException When the channel is closed.
     */
    default boolean trySend(T value) {
        Object r = trySendOrClosed(value);
        if (r instanceof ChannelClosed c) throw c.toException();
        return r == null; // null = sent, sentinel = not sent
    }

    /**
     * Attempt to send a value to the channel if there's a waiting receiver, or space in the buffer.
     * Doesn't throw exceptions when the channel is closed but returns a value.
     *
     * <p>This method never blocks or suspends the calling thread. It completes in bounded time.
     * Safe to call from platform threads, including NIO event loop threads.
     *
     * <p>May fail even when space is available, due to contention with concurrent operations.
     * Should not be used as a substitute for {@link #sendOrClosed(Object)} in a spin loop.
     *
     * @param value The value to send. Not {@code null}.
     * @return Either {@code null} when the value was sent successfully, {@link ChannelClosed} when
     *     the channel is closed, or a sentinel value indicating the value was not sent (check using
     *     {@code result == null} for success, {@code result instanceof ChannelClosed} for closed).
     */
    default Object trySendOrClosed(T value) {
        // Select-based fallback for binary compatibility
        Object sent;
        try {
            sent = Select.select(sendClause(value), Channel.DEFAULT_NOT_SENT_CLAUSE);
        } catch (InterruptedException e) {
            throw new IllegalStateException(
                    "Interrupted during trySendOrClosed, which should not be possible", e);
        }
        if (sent == Channel.DEFAULT_NOT_SENT_VALUE) {
            return Channel.TRY_SEND_NOT_SENT;
        }
        return null; // sent successfully
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
