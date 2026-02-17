package com.softwaremill.jox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A channel source, which can be used to receive values from the channel. See {@link Channel} for
 * more details.
 */
public interface Source<T> extends CloseableChannel {
    /**
     * Receive a value from the channel.
     *
     * @throws ChannelClosedException When the channel is closed.
     */
    T receive() throws InterruptedException;

    /**
     * Receive a value from the channel. Doesn't throw exceptions when the channel is closed, but
     * returns a value.
     *
     * @return Either a value of type {@code T}, or {@link ChannelClosed}, when the channel is
     *     closed.
     */
    Object receiveOrClosed() throws InterruptedException;

    /**
     * Attempt to receive a value from the channel if one is immediately available.
     *
     * <p>This method never blocks or suspends the calling thread. It completes in bounded time.
     * Safe to call from platform threads, including NIO event loop threads.
     *
     * <p>May spuriously return {@code null} under contention. Should not be used as a substitute
     * for {@link #receive()} in a spin loop.
     *
     * @return The received value, or {@code null} if no value is immediately available.
     * @throws ChannelClosedException When the channel is closed.
     */
    default T tryReceive() {
        Object r = tryReceiveOrClosed();
        if (r instanceof ChannelClosed c) throw c.toException();
        //noinspection unchecked
        return (T) r; // null means nothing available
    }

    /**
     * Attempt to receive a value from the channel if one is immediately available. Doesn't throw
     * exceptions when the channel is closed, but returns a value.
     *
     * <p>This method never blocks or suspends the calling thread. It completes in bounded time.
     * Safe to call from platform threads, including NIO event loop threads.
     *
     * <p>May spuriously return {@code null} under contention. Should not be used as a substitute
     * for {@link #receiveOrClosed()} in a spin loop.
     *
     * @return The received value of type {@code T}, {@link ChannelClosed} when the channel is
     *     closed, or {@code null} if no value is immediately available.
     */
    default Object tryReceiveOrClosed() {
        // Select-based fallback for binary compatibility
        Object received;
        try {
            received = Select.select(receiveClause(), Channel.DEFAULT_NOT_RECEIVED_CLAUSE);
        } catch (InterruptedException e) {
            throw new IllegalStateException(
                    "Interrupted during tryReceiveOrClosed, which should not be possible", e);
        }
        if (received == Channel.DEFAULT_NOT_RECEIVED_VALUE) {
            return null; // nothing available
        }
        return received;
    }

    /**
     * Create a clause which can be used in {@link Select#select(SelectClause[])}. The clause will
     * receive a value from the current channel.
     */
    SelectClause<T> receiveClause();

    /**
     * Create a clause which can be used in {@link Select#select(SelectClause[])}. The clause will
     * receive a value from the current channel, and transform it using the provided {@code
     * callback}.
     */
    <U> SelectClause<U> receiveClause(Function<T, U> callback);

    // draining operations

    /**
     * Invokes the given function for each received element. Blocks until the channel is done.
     *
     * @throws ChannelErrorException When there is an upstream error.
     */
    default void forEach(Consumer<T> c) throws InterruptedException {
        var repeat = true;
        while (repeat) {
            switch (receiveOrClosed()) {
                case ChannelDone cd -> repeat = false;
                case ChannelError ce -> throw ce.toException();
                case Object t -> c.accept((T) t);
            }
        }
    }

    /**
     * Accumulates all elements received from the channel into a list. Blocks until the channel is
     * done.
     *
     * @throws ChannelErrorException When there is an upstream error.
     */
    default List<T> toList() throws InterruptedException {
        var l = new ArrayList<T>();
        forEach(l::add);
        return l;
    }
}
