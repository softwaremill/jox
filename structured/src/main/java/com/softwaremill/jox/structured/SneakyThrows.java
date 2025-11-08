package com.softwaremill.jox.structured;

/** Allows bypassing compiler errors about checked exceptions. */
class SneakyThrows {
    static AssertionError sneakyThrow(Throwable checked) /*throws Throwable*/ {
        throw sneakyThrow0(checked);
    }

    @SuppressWarnings({"unchecked", "TypeParameterUnusedInFormals"})
    private static <E extends Throwable> E sneakyThrow0(Throwable throwable) throws E {
        throw (E) throwable;
    }
}
