package com.softwaremill.jox.simtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

class ClockRewriteTest {
    static final long DAY = Duration.ofDays(1).toNanos();

    @Test
    void methodReferencesToClocksUseVirtualTime() throws Exception {
        SimulatedTime.run(
                () -> {
                    LongSupplier nanoTime = System::nanoTime;
                    LongSupplier millis = System::currentTimeMillis;
                    Thread.sleep(Duration.ofDays(1));
                    assertEquals(SimulatedTime.NANO_TIME_ORIGIN + DAY, nanoTime.getAsLong());
                    assertEquals(
                            SimulatedTime.START.plus(Duration.ofDays(1)).toEpochMilli(),
                            millis.getAsLong());
                    return null;
                });
    }

    /** Without yielding, the spinning thread would keep the only carrier thread forever. */
    @Test
    void spinWaitingYieldsToOtherThreads() throws Exception {
        SimulatedTime.run(
                FailureModesTest.SHORT_WATCHDOG,
                () -> {
                    AtomicBoolean flag = new AtomicBoolean();
                    Thread setter = Thread.ofVirtual().start(() -> flag.set(true));
                    while (!flag.get()) Thread.onSpinWait();
                    setter.join();
                    return null;
                });
    }

    /** Merges types at a branch, which requires class hierarchy information to verify. */
    public static class ReadsClock implements Supplier<Long> {
        static class A {}

        static class B extends A {}

        static class C extends A {}

        @Override
        public Long get() {
            A x = System.nanoTime() >= 0 ? new B() : new C();
            return x.hashCode() == 0 ? 0L : System.nanoTime();
        }
    }

    /** Defines {@link ReadsClock} again, without exposing class files as resources. */
    static class NoResourcesClassLoader extends ClassLoader {
        NoResourcesClassLoader() {
            super(ClassLoader.getPlatformClassLoader());
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (!name.startsWith(ReadsClock.class.getName()))
                throw new ClassNotFoundException(name);
            String resource = name.replace('.', '/') + ".class";
            try (InputStream in =
                    ClockRewriteTest.class.getClassLoader().getResourceAsStream(resource)) {
                byte[] bytes = in.readAllBytes();
                return defineClass(name, bytes, 0, bytes.length);
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }
        }
    }

    @Test
    void classesWithoutResourcesUseVirtualTime() throws Exception {
        @SuppressWarnings("unchecked")
        Supplier<Long> readsClock =
                (Supplier<Long>)
                        new NoResourcesClassLoader()
                                .loadClass(ReadsClock.class.getName())
                                .getDeclaredConstructor()
                                .newInstance();
        long t =
                SimulatedTime.run(
                        () -> {
                            Thread.sleep(Duration.ofDays(1));
                            return readsClock.get();
                        });
        assertEquals(SimulatedTime.NANO_TIME_ORIGIN + DAY, t);
    }
}
