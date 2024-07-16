package com.softwaremill.jox.structured;

import java.util.concurrent.CompletableFuture;

/**
 * A fork started using {@link Scope#forkCancellable} or {@link Scope#forkUnsupervised}, backed by a (virtual) thread.
 */
public interface UnsupervisedFork<T> extends Fork<T> {
}

class UnsupervisedForkUsingResult<T> extends ForkUsingResult<T> implements UnsupervisedFork<T> {
    UnsupervisedForkUsingResult(CompletableFuture<T> result) {
        super(result);
    }
}
