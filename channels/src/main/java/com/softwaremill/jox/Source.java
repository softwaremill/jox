package com.softwaremill.jox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

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
    Object receiveOrClosed() throws InterruptedException;

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
     * Accumulates all elements received from the channel into a list. Blocks until the channel is done.
     *
     * @throws ChannelErrorException When there is an upstream error.
     */
    default List<T> toList() throws InterruptedException {
        var l = new ArrayList<T>();
        forEach(l::add);
        return l;
    }
}
