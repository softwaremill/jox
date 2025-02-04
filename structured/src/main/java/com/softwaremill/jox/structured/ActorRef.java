package com.softwaremill.jox.structured;

import com.softwaremill.jox.Channel;
import com.softwaremill.jox.Sink;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static com.softwaremill.jox.structured.Scopes.unsupervised;

public class ActorRef<T> {

    private final Sink<ThrowingConsumer<T>> c;

    public ActorRef(Sink<ThrowingConsumer<T>> c) {
        this.c = c;
    }

    /**
     * Send an invocation to the actor and await for the result.
     * <p>
     * The `f` function should be an invocation of a method on `T` and should not directly or indirectly return the `T` value, as this might
     * expose the actor's internal mutable state to other threads.
     * <p>
     * Any non-fatal exceptions thrown by `f` will be propagated to the caller and the actor will continue processing other invocations.
     * Fatal exceptions will be propagated to the actor's enclosing scope, and the actor will close.
     */
    public <U> U ask(ThrowingFunction<T, U> f) throws Exception {
        CompletableFuture<U> cf = new CompletableFuture<>();
        c.send(t -> {
            try {
                cf.complete(f.apply(t));
            } catch (Throwable e) {
                if (e instanceof RuntimeException) {
                    cf.completeExceptionally(e);
                } else {
                    cf.completeExceptionally(e);
                    throw e;
                }
            }
        });
        try {
            return cf.get();
        } catch (ExecutionException e) {
            throw (Exception) e.getCause();
        }
    }

    /**
     * Send an invocation to the actor that should be processed in the background (fire-and-forget). Might block until there's enough space
     * in the actor's mailbox (incoming channel).
     * <p>
     * Any exceptions thrown by `f` will be propagated to the actor's enclosing scope, and the actor will close.
     */
    public void tell(ThrowingConsumer<T> f) throws InterruptedException {
        c.send(f);
    }

    /**
     * The same as {@link ActorRef#create(Scope, Object, Consumer)} but with empty close action.
     */
    public static <T> ActorRef<T> create(Scope scope, T logic) {
        return create(scope, logic, null);
    }

    /**
     * Creates a new actor ref, that is a fork in the current concurrency scope, which protects a mutable resource (`logic`) and executes
     * invocations on it serially, one after another. It is guaranteed that `logic` will be accessed by at most one thread at a time. The
     * methods of `logic: T` define the actor's interface (the messages that can be "sent to the actor").
     * <p>
     * Invocations can be scheduled using the returned `ActorRef`. When an invocation is an `ActorRef.ask`, any non-fatal exceptions are
     * propagated to the caller, and the actor continues. Fatal exceptions, or exceptions that occur during `ActorRef.tell` invocations,
     * cause the actor's channel to be closed with an error, and are propagated to the enclosing scope.
     * <p>
     * The actor's mailbox (incoming channel) will have a capacity of {@link Channel#DEFAULT_BUFFER_SIZE}.
     */
    public static <T> ActorRef<T> create(Scope scope, T logic, Consumer<T> close) {
        Channel<ThrowingConsumer<T>> c = Channel.newBufferedDefaultChannel();
        ActorRef<T> ref = new ActorRef<>(c);
        scope.fork(() -> {
            try {
                while (true) {
                    ThrowingConsumer<T> m = c.receive();
                    try {
                        m.accept(logic);
                    } catch (Throwable t) {
                        c.error(t);
                        throw t;
                    }
                }
            } finally {
                if (close != null) {
                    uninterruptible(() -> {
                        close.accept(logic);
                        return null;
                    });
                }
            }
        });
        return ref;
    }

    private static void uninterruptible(Callable<Void> f) throws ExecutionException, InterruptedException {
        unsupervised(scope -> {
            Fork<Void> t = scope.forkUnsupervised(f);

            ThrowingRunnable joinDespiteInterrupted = () -> {
                while (true) {
                    try {
                        t.join();
                        break;
                    } catch (InterruptedException e) {
                        // Continue the loop to retry joining
                    }
                }
            };
            joinDespiteInterrupted.run();
            return null;
        });
    }
}
