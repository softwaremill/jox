package com.softwaremill.jox;

/**
 * A clause to use as part of {@link Select#select(SelectClause[])}. Clauses can be created having a channel instance,
 * using {@link Channel#receiveClause()} and {@link Channel#sendClause(Object)}}.
 * <p>
 * A clause instance is immutable and can be reused in multiple `select` calls.
 */
public abstract class SelectClause<T> {
    abstract Channel<?> getChannel();

    /**
     * @return Either a {@link StoredSelectClause}, {@link ChannelClosed} when the channel is already closed, or the selected
     * value (not {@code null}).
     */
    abstract Object register(SelectInstance select);

    /**
     * Transforms the raw value with the transformation function provided when creating the clause.
     * <p>
     * Might throw any exceptions that the provided transformation function throws.
     */
    abstract T transformedRawValue(Object rawValue);
}
