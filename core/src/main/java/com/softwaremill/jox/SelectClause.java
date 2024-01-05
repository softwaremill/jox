package com.softwaremill.jox;

import java.util.function.Supplier;

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

class DefaultClause<T> extends SelectClause<T> {
    private final Supplier<T> callback;

    public DefaultClause(Supplier<T> callback) {
        this.callback = callback;
    }

    @Override
    Channel<?> getChannel() {
        return null;
    }

    @Override
    Object register(SelectInstance select) {
        return DefaultClauseMarker.DEFAULT;
    }

    @Override
    T transformedRawValue(Object rawValue) {
        return callback.get();
    }
}

/**
 * Used as a result of {@link DefaultClause#register(SelectInstance)}, instead of null, to indicate that the select
 * clause has been selected during registration.
 */
enum DefaultClauseMarker {
    DEFAULT
}
