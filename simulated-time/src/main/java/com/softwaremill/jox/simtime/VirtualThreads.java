package com.softwaremill.jox.simtime;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinTask;

/**
 * Access to JDK-internal virtual thread state. Requires {@code java.lang} to be opened by the
 * agent.
 */
final class VirtualThreads {
    private VirtualThreads() {}

    private static final VarHandle SCHEDULER;
    private static final VarHandle ON_WAITING_LIST;
    private static final Constructor<?> BUILDER;

    static {
        try {
            Class<?> vt = Class.forName("java.lang.VirtualThread");
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(vt, MethodHandles.lookup());
            SCHEDULER = lookup.findVarHandle(vt, "scheduler", Executor.class);
            ON_WAITING_LIST = lookup.findVarHandle(vt, "onWaitingList", boolean.class);
            BUILDER =
                    Class.forName("java.lang.ThreadBuilders$VirtualThreadBuilder")
                            .getDeclaredConstructor(Executor.class);
            BUILDER.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * A builder of virtual threads run by the given scheduler. Their child virtual threads inherit
     * it.
     */
    static Thread.Builder.OfVirtual builder(Executor scheduler) {
        try {
            return (Thread.Builder.OfVirtual) BUILDER.newInstance(scheduler);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    static Executor schedulerOf(Thread virtualThread) {
        return (Executor) SCHEDULER.get(virtualThread);
    }

    /** Whether the JDK will soon make the thread, blocked on a monitor, runnable again. */
    static boolean isOnWaitingList(Thread virtualThread) {
        return (boolean) ON_WAITING_LIST.getVolatile(virtualThread);
    }

    /**
     * The virtual thread, whose continuation is run by a task submitted to the scheduler, or {@code
     * null} if it can't be determined.
     */
    static Thread threadOf(Object task) {
        try {
            Object continuation = task;
            if (task instanceof ForkJoinTask<?>) {
                Field adapted = ADAPTED_RUNNABLE.get(task.getClass());
                continuation = adapted == null ? null : adapted.get(task);
                if (continuation == null) return null;
            }
            Field thread = CAPTURED_THREAD.get(continuation.getClass());
            return thread == null ? null : (Thread) thread.get(continuation);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /** The continuation task is a method reference bound to the virtual thread. */
    private static final ClassValue<Field> CAPTURED_THREAD =
            new ClassValue<>() {
                @Override
                protected Field computeValue(Class<?> c) {
                    for (Field f : c.getDeclaredFields()) {
                        if (Thread.class.isAssignableFrom(f.getType())) {
                            f.setAccessible(true);
                            return f;
                        }
                    }
                    return null;
                }
            };

    /**
     * Since Java 26, the continuation task is wrapped using {@code ForkJoinTask.adapt(Runnable)}.
     */
    private static final ClassValue<Field> ADAPTED_RUNNABLE =
            new ClassValue<>() {
                @Override
                protected Field computeValue(Class<?> c) {
                    for (Class<?> k = c; k != null && k != Object.class; k = k.getSuperclass()) {
                        for (Field f : k.getDeclaredFields()) {
                            if (f.getType() == Runnable.class) {
                                f.setAccessible(true);
                                return f;
                            }
                        }
                    }
                    return null;
                }
            };
}
