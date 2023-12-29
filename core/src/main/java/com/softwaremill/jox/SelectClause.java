package com.softwaremill.jox;

/**
 * A clause to use as part of {@link Select#select(SelectClause[])}. Clauses can be created having a channel instance,
 * using {@link Channel#receiveClause()}.
 */
public abstract class SelectClause<T> {
    /**
     * The value might be set from different threads, but there's always a memory barrier between write & read, due to
     * synchronization on {@link SelectInstance}'s {@code state} field.
     */
    protected Object rawValue;

    abstract Channel<?> getChannel();

    /**
     * @return Either a {@link StoredSelect}, {@link ChannelClosed} when the channel is already closed, or the selected value.
     */
    abstract Object register(SelectInstance select);

    /**
     * Should be called only after {@link #setRawValue(Object)}. Transforms the raw value with the transformation function
     * provided when creating the clause.
     */
    abstract T transformedRawValue();

    void setRawValue(Object rawValue) {
        this.rawValue = rawValue;
    }
}
