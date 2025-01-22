package com.softwaremill.jox.structured;

public class JoxScopeExecutionException extends RuntimeException {

    public JoxScopeExecutionException(String message) {
        super(message);
    }

    public JoxScopeExecutionException(Throwable cause) {
        super(cause);
    }

    /**
     * If the cause of this {@link JoxScopeExecutionException} is an instance of any of the given exceptions, it is thrown.
     * Method signature points to the closest super class of the passed exception classes.
     * <p>
     * <b>Be careful</b> if the cause is not any of given arguments, nothing happens, so it is advised to rethrow this exception
     * after calling this method.
     * <p>
     * e.g.
     * <pre>{@code
     * try {
     *     Scopes.supervised(scope -> {
     *         throw new TestException("x");
     *     });
     * } catch (JoxScopeExecutionException e) {
     *     e.unwrapAndThrow(OtherException.class, TestException.class, YetAnotherException.class);
     *     throw e;
     * }</pre>
     */
    @SafeVarargs
    public final <T extends Throwable> void unwrapAndThrow(Class<? extends T>... exceptionsToRethrow) throws T {
        Throwable cause = getCause();
        for (Class<? extends T> exceptionToRethrow : exceptionsToRethrow) {
            if (exceptionToRethrow.isInstance(cause)) {
                // rewrite suppressed exceptions
                for (var suppressed : getSuppressed()) {
                    cause.addSuppressed(suppressed);
                }
                throw (T) exceptionToRethrow.cast(cause);
            }
        }
    }
}
