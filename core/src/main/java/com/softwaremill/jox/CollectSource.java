package com.softwaremill.jox;

import java.util.function.Function;

public class CollectSource<V, T> implements Source<T> {
    private final Source<V> original;

    private final Function<V, T> f;

    /**
     * A view on the {@code original} source, which transforms the received values using the provided function {@code f}.
     * If {@code f} returns {@code null}, the value will be skipped, and another value will be received.
     * <p>
     * The same logic applies to receive clauses created using this source, which can be used in {@link Select#select(SelectClause[])}.
     *
     * @param original The original source, from which values are received.
     * @param f        The mapping / filtering function. If the function returns {@code null}, the value will be skipped.
     */
    public CollectSource(Source<V> original, Function<V, T> f) {
        this.original = original;
        this.f = f;
    }

    @Override
    public T receive() throws InterruptedException {
        while (true) {
            var r = original.receive();
            var t = f.apply(r);
            // a null indicates that the value should not be collected (skipped)
            if (t != null) {
                return t;
            }
        }
    }

    @Override
    public Object receiveSafe() throws InterruptedException {
        while (true) {
            var r = original.receiveSafe();
            if (r instanceof ChannelClosed c) {
                return c;
            } else {
                //noinspection unchecked
                var t = f.apply((V) r);
                // a null indicates that the value should not be collected (skipped)
                if (t != null) {
                    return t;
                }
            }
        }
    }

    @Override
    public SelectClause<T> receiveClause() {
        return original.receiveClause(v -> {
            var t = f.apply(v);
            if (t != null) {
                return t;
            } else {
                // `null` is a valid return value from a select clause (default for send clauses and allowed for callbacks)
                // that's why we need a marker to indicate that the value should not be collected, when the result of `f`
                // is `null`. This is then handled in `Select`.
                //noinspection unchecked
                return (T) RestartSelectMarker.RESTART;
            }
        });
    }

    @Override
    public <U> SelectClause<U> receiveClause(Function<T, U> callback) {
        return original.receiveClause(v -> {
            var t = f.apply(v);
            if (t != null) {
                return callback.apply(t);
            } else {
                //noinspection unchecked
                return (U) RestartSelectMarker.RESTART;
            }
        });
    }

    // delegates for closeable channel

    @Override
    public void done() {
        original.done();
    }

    @Override
    public Object doneSafe() {
        return original.doneSafe();
    }

    @Override
    public void error(Throwable reason) {
        original.error(reason);
    }

    @Override
    public Object errorSafe(Throwable reason) {
        return original.errorSafe(reason);
    }

    @Override
    public ChannelClosed closedForSend() {
        return original.closedForSend();
    }

    @Override
    public ChannelClosed closedForReceive() {
        return original.closedForReceive();
    }
}
