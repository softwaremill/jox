package com.softwaremill.jox;

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
    Object receiveSafe() throws InterruptedException;

    /**
     * Create a clause which can be used in {@link Select#select(SelectClause[])}. The clause will receive a value from
     * the current channel.
     * <p>
     * If the source is/becomes done, {@link Select#select(SelectClause[])} will restart with channels that are not done yet.
     */
    SelectClause<T> receiveClause();

    /**
     * Create a clause which can be used in {@link Select#select(SelectClause[])}. The clause will receive a value from
     * the current channel, and transform it using the provided {@code callback}.
     * <p>
     * If the source is/becomes done, {@link Select#select(SelectClause[])} will restart with channels that are not done yet.
     */
    <U> SelectClause<U> receiveClause(Function<T, U> callback);

    /**
     * Create a clause which can be used in {@link Select#select(SelectClause[])}. The clause will receive a value from
     * the current channel.
     * <p>
     * If the source is/becomes done, {@link Select#select(SelectClause[])} will stop and throw {@link ChannelDoneException}
     * or return a {@link ChannelDone} value (in the {@code safe} variant).
     */
    SelectClause<T> receiveOrDoneClause();

    /**
     * Create a clause which can be used in {@link Select#select(SelectClause[])}. The clause will receive a value from
     * the current channel, and transform it using the provided {@code callback}.
     * <p>
     * If the source is/becomes done, {@link Select#select(SelectClause[])} will stop and throw {@link ChannelDoneException}
     * or return a {@link ChannelDone} value (in the {@code safe} variant).
     */
    <U> SelectClause<U> receiveOrDoneClause(Function<T, U> callback);

    //

    /**
     * Creates a view of this source, where the results of {@link #receive()} will be transformed using the given function {@code f}.
     * If {@code f} returns {@code null}, the value will be skipped, and another value will be received.
     * <p>
     * The same logic applies to receive clauses created using this source, which can be used in {@link Select#select(SelectClause[])}.
     *
     * @param f The mapping / filtering function. If the function returns {@code null}, the value will be skipped.
     * @return A source which is a view of this source, with the mapping / filtering function applied.
     */
    default <V> Source<V> collectAsView(Function<T, V> f) {
        return new CollectSource<>(this, f);
    }

    /**
     * Creates a view of this source, where the results of {@link #receive()} will be filtered using the given predicate {@code p}.
     * <p>
     * The same logic applies to receive clauses created using this source, which can be used in {@link Select#select(SelectClause[])}.
     *
     * @param p The predicate to use for filtering.
     * @return A source which is a view of this source, with the filtering function applied.
     */
    default Source<T> filterAsView(Predicate<T> p) {
        return new CollectSource<>(this, t -> p.test(t) ? t : null);
    }
}
