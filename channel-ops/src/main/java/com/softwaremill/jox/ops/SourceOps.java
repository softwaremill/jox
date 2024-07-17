package com.softwaremill.jox.ops;

import com.softwaremill.jox.*;
import com.softwaremill.jox.structured.Scope;

import java.util.Iterator;
import java.util.function.Function;

public class SourceOps {
    private final Scope scope;
    private final int defaultCapacity;

    public SourceOps(Scope scope) {
        this(scope, 16);
    }

    public SourceOps(Scope scope, int defaultCapacity) {
        this.scope = scope;
        this.defaultCapacity = defaultCapacity;
    }

    public static <T> ForSource<T> forSource(Scope scope, Source<T> s) {
        var sourceOps = new SourceOps(scope);
        return sourceOps.new ForSource<>(s);
    }

    public class ForSource<T> {
        private final Source<T> source;

        ForSource(Source<T> source) {
            this.source = source;
        }

        public Source<T> toSource() {
            return source;
        }

        /**
         * Applies the given mapping function {@code f} to each element received from this source, and sends the
         * results to the returned channel. If {@code f} returns {@code null}, the value will be skipped.
         * <p>
         * Errors from this channel are propagated to the returned channel. Any exceptions that occur when invoking
         * {@code f} are propagated as errors to the returned channel as well.
         * <p>
         * For a lazily-evaluated version, see {@link Channel#collectAsView(Function)}.
         *
         * @param f The mapping function.
         * @return Ops on a source, onto which results of the mapping function will be sent.
         */
        public <U> ForSource<U> collect(Function<T, U> f) {
            var c2 = new Channel<U>(defaultCapacity);
            scope.fork(() -> {
                var repeat = true;
                while (repeat) {
                    switch (source.receiveOrClosed()) {
                        case ChannelDone cd -> {
                            c2.doneOrClosed();
                            repeat = false;
                        }
                        case ChannelError ce -> {
                            c2.errorOrClosed(ce.cause());
                            repeat = false;
                        }
                        case Object t -> {
                            try {
                                var u = f.apply((T) t);
                                if (u != null) {
                                    repeat = !(c2.sendOrClosed(u) instanceof ChannelClosed);
                                } // else skip & continue
                            } catch (Exception e) {
                                c2.errorOrClosed(e);
                            }
                        }
                    }
                }
                return null;
            });
            return new ForSource<U>(c2);
        }
    }

    //

    public <T> ForSource<T> fromIterator(Iterator<T> i) {
        var c = new Channel<T>(defaultCapacity);
        scope.fork(() -> {
            try {
                while (i.hasNext()) {
                    c.sendOrClosed(i.next());
                }
                c.doneOrClosed();
            } catch (Exception e) {
                c.errorOrClosed(e);
            }
            return null;
        });
        return new ForSource<T>(c);
    }

    public <T> ForSource<T> fromIterable(Iterable<T> i) {
        return fromIterator(i.iterator());
    }

    /**
     * Creates a rendezvous channel (without a buffer, regardless of the default capacity), to which the given value is
     * sent repeatedly, at least {@code intervalMillis}ms apart between each two elements. The first value is sent
     * immediately.
     * <p>
     * The interval is measured between the subsequent invocations of the {@code send(value)} method. Hence, if there's
     * a slow consumer, the next tick can be sent right after the previous one is received (if it was received later
     * than the inter-tick interval duration). However, ticks don't accumulate, e.g. when the consumer is so slow that
     * multiple intervals pass between {@code send} invocations.
     * <p>
     * Must be run within a scope, since a child fork is created which sends the ticks, and waits until the next tick
     * can be sent.
     *
     * @param intervalMillis The temporal spacing between subsequent ticks.
     * @param tickValue      The value to send to the channel on every tick.
     * @return Ops on a source to which the tick values are sent.
     */
    public <T> ForSource<T> tick(long intervalMillis, T tickValue) {
        var c = new Channel<T>();
        scope.fork(() -> {
            while (true) {
                var start = System.nanoTime();
                c.sendOrClosed(tickValue);
                var end = System.nanoTime();
                var sleep = intervalMillis * 1_000_000 - (end - start);
                if (sleep > 0) Thread.sleep(sleep / 1_000_000, (int) sleep % 1_000_000);
            }
        });
        return new ForSource<T>(c);
    }
}
