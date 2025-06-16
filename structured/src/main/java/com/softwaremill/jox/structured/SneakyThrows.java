package com.softwaremill.jox.structured;

class SneakyThrows {
    /** Allows to bypass compiler errors about checked exceptions. */
    static <E extends Exception> void sneakyThrows(ThrowingRunnable f) throws E {
        try {
            f.run();
        } catch (Exception e) {
            // noinspection unchecked
            throw (E) e;
        }
    }
}
