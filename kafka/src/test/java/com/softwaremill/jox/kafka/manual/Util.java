package com.softwaremill.jox.kafka.manual;

import java.util.Random;
import java.util.concurrent.Callable;

public final class Util {

    private Util() {}

    public static <T> T timedAndLogged(String name, Callable<T> f) throws Exception {
        final var start = System.currentTimeMillis();
        final var result = f.call();
        final var took = System.currentTimeMillis() - start;
        System.out.println(name + " took " + took + "ms");
        return result;
    }

    public static String randomString() {
        final var leftLimit = 48;
        final var rightLimit = 122;
        final var targetStringLength = 100;
        final var random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
