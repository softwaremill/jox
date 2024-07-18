package com.softwaremill.jox.structured;

/**
 * A functional interface, capturing a computation which runs using the {@link ScopedUnsupervised} capability to fork
 * computations.
 */
public interface ScopedUnsupervised<T> {
    T run(UnsupervisedScope scope) throws Exception;
}
