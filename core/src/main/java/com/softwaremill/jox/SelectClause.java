package com.softwaremill.jox;

/**
 * A clause to use as part of {@link Select#select(SelectClause[])}. Clauses can be created having a channel instance,
 * using {@link Channel#receiveClause()} and {@link Channel#sendClause(Object)}}.
 */
public abstract class SelectClause<T> {
    /**
     * Either the value to send, or the received value.
     * <p>
     * For a receive clause, the value might be set from different threads, but there's always a memory barrier between
     * write & read, due to synchronization on {@link SelectInstance}'s {@code state} field.
     */
    private Object payload;

    SelectClause(Object payload) {
        this.payload = payload;
    }

    abstract Channel<?> getChannel();

    /**
     * @return Either a {@link StoredSelect}, {@link ChannelClosed} when the channel is already closed, or the selected
     * value (not {@code null}).
     */
    abstract Object register(SelectInstance select);

    /**
     * Transforms the raw value with the transformation function provided when creating the clause.
     * For a receive clause, should be called only after {@link #setPayload(Object)}.
     * <p>
     * Might throw any exceptions that the provided transformation function throws.
     */
    abstract T transformedRawValue();

    Object getPayload() {return payload;}

    void setPayload(Object payload) {
        this.payload = payload;
    }
}
