package com.softwaremill.jox.structured;

/**
 * A functional interface, capturing a computation which runs using the {@link Scope} capability to fork computations.
 */
public interface Scoped<T> {
    T run(Scope scope) throws Exception;
}
