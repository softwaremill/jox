package com.softwaremill.jox.simtime.agent;

import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_boolean;
import static java.lang.constant.ConstantDescs.CD_long;
import static java.lang.constant.ConstantDescs.CD_void;

import java.lang.classfile.Attributes;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassElement;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassHierarchyResolver;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.Label;
import java.lang.classfile.MethodModel;
import java.lang.classfile.MethodTransform;
import java.lang.classfile.Opcode;
import java.lang.classfile.attribute.StackMapTableAttribute;
import java.lang.classfile.constantpool.MethodRefEntry;
import java.lang.classfile.constantpool.PoolEntry;
import java.lang.classfile.instruction.ConstantInstruction;
import java.lang.classfile.instruction.InvokeDynamicInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.ReturnInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.softwaremill.jox.simtime.boot.Hooks;

/**
 * Rewrites clock reads, so that they go through the boot hooks:
 *
 * <ul>
 *   <li>in application classes: {@code System.nanoTime}, {@code System.currentTimeMillis} and
 *       {@code Thread.onSpinWait} calls and method references
 *   <li>in an allowlist of JDK classes, where a thread computes and checks its own deadlines:
 *       {@code System.nanoTime} and {@code System.currentTimeMillis} calls, and the timers of
 *       {@code StructuredTaskScope} timeouts
 *   <li>in {@code java.time.Clock}: the system clock's {@code Instant} and millis sources
 *   <li>in {@code VirtualThread}: the steps of the JDK's monitor unblocker, so that simulations can
 *       wait until it's done
 * </ul>
 *
 * Other JDK classes keep real time: they might compute deadlines on one thread and wait for them on
 * another.
 */
final class TimeTransformer implements ClassFileTransformer {
    /** Must not be rewritten, as they run during class loading, or are called by rewritten code. */
    private static final List<String> AGENT_PACKAGES =
            List.of("com/softwaremill/jox/simtime/agent/", "com/softwaremill/jox/simtime/boot/");

    private static final String CLOCK = "java/time/Clock";
    private static final String VIRTUAL_THREAD = "java/lang/VirtualThread";
    private static final String STRUCTURED_TASK_SCOPE =
            "java/util/concurrent/StructuredTaskScopeImpl";

    private static final List<String> JDK_ALLOWLIST =
            List.of(
                    VIRTUAL_THREAD,
                    "java/util/concurrent/locks/AbstractQueuedSynchronizer",
                    "java/util/concurrent/locks/AbstractQueuedLongSynchronizer",
                    "java/util/concurrent/locks/LockSupport",
                    "java/util/concurrent/locks/StampedLock",
                    "java/util/concurrent/FutureTask",
                    "java/util/concurrent/CompletableFuture",
                    "java/util/concurrent/ForkJoinTask",
                    "java/util/concurrent/LinkedTransferQueue",
                    "java/util/concurrent/SynchronousQueue",
                    "java/util/concurrent/Exchanger",
                    "java/util/concurrent/Phaser",
                    "java/util/concurrent/ThreadPerTaskExecutor",
                    STRUCTURED_TASK_SCOPE,
                    "jdk/internal/misc/ThreadFlock");

    /**
     * Build & test tooling, which doesn't run on simulated threads. Excluded to save class loading
     * time.
     */
    private static final List<String> APP_EXCLUDES =
            List.of(
                    "org/junit/",
                    "junit/",
                    "org/opentest4j/",
                    "org/apiguardian/",
                    "org/apache/maven/");

    /** JDK methods that must be rewritten for simulations to work, as {@code class.method}. */
    static final Set<String> REQUIRED_JDK_REWRITES =
            Set.of(
                    "java/lang/VirtualThread.parkNanos",
                    "java/lang/VirtualThread.sleepNanos",
                    "java/lang/VirtualThread.compareAndSetOnWaitingList",
                    "java/lang/VirtualThread.unblock",
                    "java/util/concurrent/locks/AbstractQueuedSynchronizer$ConditionObject.awaitNanos",
                    "java/util/concurrent/StructuredTaskScopeImpl.scheduleTimeout",
                    "java/time/Clock.currentInstant",
                    "java/time/Clock$SystemClock.millis");

    private static final ClassDesc HOOKS = ClassDesc.of(Hooks.CLASS_NAME);
    private static final MethodTypeDesc RETURNS_LONG = MethodTypeDesc.of(CD_long);
    private static final MethodTypeDesc RETURNS_VOID = MethodTypeDesc.of(CD_void);
    private static final MethodTypeDesc HOOK_SCHEDULE =
            MethodTypeDesc.of(
                    ClassDesc.of("java.util.concurrent.ScheduledFuture"),
                    ClassDesc.of("java.util.concurrent.ForkJoinPool"),
                    ClassDesc.of("java.lang.Runnable"),
                    CD_long,
                    ClassDesc.of("java.util.concurrent.TimeUnit"));
    private static final String FJP_SCHEDULE_DESCRIPTOR =
            HOOK_SCHEDULE.dropParameterTypes(0, 1).descriptorString();

    /**
     * Builds code without stack maps; {@link WithStackMaps} then re-adds the original ones, see
     * {@link #rewrite}.
     */
    private static final ClassFile WITHOUT_GENERATED_STACK_MAPS =
            ClassFile.of(ClassFile.StackMapsOption.DROP_STACK_MAPS);

    /** Generates stack maps; only used for JDK classes, for which class files are available. */
    private static final ClassFile GENERATING_STACK_MAPS =
            ClassFile.of(
                    ClassFile.ClassHierarchyResolverOption.of(
                            ClassHierarchyResolver.ofResourceParsing(
                                    ClassLoader.getSystemClassLoader())));

    private enum Kind {
        APP,
        JDK,
        CLOCK
    }

    /** A static method of {@link Hooks}, replacing a call. */
    private record Hook(String name, MethodTypeDesc type) {}

    /** Which calls are replaced by hooks. */
    private enum CallRules {
        APP,
        JDK,
        JDK_AND_SCHEDULE;

        /** The hook replacing the given call, or {@code null}. */
        Hook replacementFor(boolean isStatic, String owner, String name, String descriptor) {
            if (isStatic && owner.equals("java/lang/System") && descriptor.equals("()J")) {
                if (name.equals("nanoTime")) {
                    return new Hook(this == APP ? "appNanoTime" : "nanoTime", RETURNS_LONG);
                }
                if (name.equals("currentTimeMillis")) {
                    return new Hook(
                            this == APP ? "appCurrentTimeMillis" : "currentTimeMillis",
                            RETURNS_LONG);
                }
            }
            if (this == APP
                    && isStatic
                    && owner.equals("java/lang/Thread")
                    && name.equals("onSpinWait")
                    && descriptor.equals("()V")) {
                return new Hook("onSpinWait", RETURNS_VOID);
            }
            if (this == JDK_AND_SCHEDULE
                    && !isStatic
                    && owner.equals("java/util/concurrent/ForkJoinPool")
                    && name.equals("schedule")
                    && descriptor.equals(FJP_SCHEDULE_DESCRIPTOR)) {
                return new Hook("schedule", HOOK_SCHEDULE);
            }
            return null;
        }

        Hook replacementFor(InvokeInstruction i) {
            if (i.opcode() != Opcode.INVOKESTATIC && i.opcode() != Opcode.INVOKEVIRTUAL) {
                return null;
            }
            return replacementFor(
                    i.opcode() == Opcode.INVOKESTATIC,
                    i.owner().asInternalName(),
                    i.name().stringValue(),
                    i.type().stringValue());
        }

        /** The handle replacing the given constant (e.g. a method reference), or {@code null}. */
        DirectMethodHandleDesc replacementFor(ConstantDesc constant) {
            if (!(constant instanceof DirectMethodHandleDesc h)) return null;
            boolean isStatic = h.kind() == DirectMethodHandleDesc.Kind.STATIC;
            if (!isStatic && h.kind() != DirectMethodHandleDesc.Kind.VIRTUAL) return null;
            Hook hook =
                    replacementFor(
                            isStatic,
                            internalName(h.owner()),
                            h.methodName(),
                            h.lookupDescriptor());
            return hook == null
                    ? null
                    : MethodHandleDesc.ofMethod(
                            DirectMethodHandleDesc.Kind.STATIC, HOOKS, hook.name(), hook.type());
        }

        /** Whether a bootstrap argument (e.g. a method reference) is replaced. */
        boolean rewritesArgs(InvokeDynamicInstruction i) {
            for (ConstantDesc arg : i.bootstrapArgs()) {
                if (replacementFor(arg) != null) return true;
            }
            return false;
        }

        boolean rewritesAny(CodeModel code) {
            for (CodeElement e : code) {
                if (e instanceof InvokeInstruction i && replacementFor(i) != null) return true;
                if (e instanceof InvokeDynamicInstruction i && rewritesArgs(i)) return true;
                if (e instanceof ConstantInstruction.LoadConstantInstruction c
                        && replacementFor(c.constantValue()) != null) return true;
            }
            return false;
        }
    }

    /** Rewritten JDK methods, as {@code class.method}. */
    final Set<String> rewrittenJdkMethods = ConcurrentHashMap.newKeySet();

    private static String internalName(ClassDesc desc) {
        String descriptor = desc.descriptorString();
        return descriptor.substring(1, descriptor.length() - 1);
    }

    @Override
    public byte[] transform(
            Module module,
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer) {
        if (className == null) return null;
        Kind kind = kindOf(className, loader);
        if (kind == null) return null;
        try {
            return rewrite(kind, className, classfileBuffer);
        } catch (Throwable t) {
            String msg = "[jox simulated time] cannot rewrite " + className + ": " + t;
            System.err.println(msg);
            // fails simulations started later, also if the class is loaded after startup
            if (kind != Kind.APP) Hooks.reportAgentError(msg);
            return null;
        }
    }

    /**
     * Runs a transformation, so that the code used by the transformer is loaded & linked before
     * it's needed.
     */
    void prewarm() {
        byte[] bytes =
                ClassFile.of()
                        .build(
                                ClassDesc.of("com.softwaremill.jox.simtime.agent.Prewarm"),
                                cb ->
                                        cb.withMethodBody(
                                                "run",
                                                MethodTypeDesc.of(CD_Object, CD_boolean),
                                                ClassFile.ACC_STATIC,
                                                b -> {
                                                    // the branches push different types (String,
                                                    // Class), so the method has a stack map frame,
                                                    // which the rewrite has to carry over
                                                    Label other = b.newLabel();
                                                    Label end = b.newLabel();
                                                    b.invokestatic(
                                                                    ClassDesc.of(
                                                                            "java.lang.System"),
                                                                    "nanoTime",
                                                                    RETURNS_LONG)
                                                            .pop2()
                                                            .iload(0)
                                                            .ifeq(other)
                                                            .ldc("a")
                                                            .goto_(end)
                                                            .labelBinding(other)
                                                            .ldc(ClassDesc.of("java.lang.Thread"))
                                                            .labelBinding(end)
                                                            .areturn();
                                                }));
        if (rewrite(Kind.APP, "com/softwaremill/jox/simtime/agent/Prewarm", bytes) == null) {
            throw new IllegalStateException("prewarm transformation failed");
        }
    }

    static boolean isInScope(String className, ClassLoader loader) {
        return kindOf(className, loader) != null;
    }

    private static Kind kindOf(String className, ClassLoader loader) {
        for (String p : AGENT_PACKAGES) {
            if (className.startsWith(p)) return null;
        }
        for (String c : JDK_ALLOWLIST) {
            if (isClassOrNested(className, c)) return Kind.JDK;
        }
        if (isClassOrNested(className, CLOCK)) return Kind.CLOCK;
        if (loader == null || loader == ClassLoader.getPlatformClassLoader()) return null;
        if (className.startsWith("java/")
                || className.startsWith("jdk/")
                || className.startsWith("sun/")
                || className.startsWith("com/sun/")) return null;
        for (String e : APP_EXCLUDES) {
            if (className.startsWith(e)) return null;
        }
        return Kind.APP;
    }

    private static boolean isClassOrNested(String className, String outer) {
        return className.startsWith(outer)
                && (className.length() == outer.length()
                        || className.charAt(outer.length()) == '$');
    }

    /**
     * Returns the rewritten class, or {@code null} if nothing changed.
     *
     * <p>Generating stack maps requires the class hierarchy, which isn't always available for
     * application classes (e.g. when they are loaded by a custom class loader). Apart from {@code
     * Clock.currentInstant}, the rewrites don't add branch targets, so the original stack maps stay
     * valid.
     */
    private byte[] rewrite(Kind kind, String className, byte[] bytes) {
        ClassModel cm = ClassFile.of().parse(bytes);
        if (kind == Kind.APP && !referencesAppRewrittenMethod(cm)) return null;
        boolean generateStackMaps = className.equals(CLOCK);
        boolean[] changed = {false};
        ClassTransform ct =
                new ClassTransform() {
                    @Override
                    public void accept(ClassBuilder cb, ClassElement ce) {
                        if (ce instanceof MethodModel mm && mm.code().isPresent()) {
                            CodeModel code = mm.code().get();
                            CodeTransform t = codeTransformFor(kind, className, mm, code);
                            if (t != null) {
                                changed[0] = true;
                                if (kind != Kind.APP) {
                                    rewrittenJdkMethods.add(
                                            className + "." + mm.methodName().stringValue());
                                }
                                if (!generateStackMaps) {
                                    t =
                                            t.andThen(
                                                    new WithStackMaps(
                                                            code.findAttribute(
                                                                    Attributes.stackMapTable())));
                                }
                                cb.transformMethod(mm, MethodTransform.transformingCode(t));
                                return;
                            }
                        }
                        cb.with(ce);
                    }
                };
        byte[] result =
                (generateStackMaps ? GENERATING_STACK_MAPS : WITHOUT_GENERATED_STACK_MAPS)
                        .transformClass(cm, ct);
        return changed[0] ? result : null;
    }

    /** A quick check, avoiding parsing the code of most classes. */
    private static boolean referencesAppRewrittenMethod(ClassModel cm) {
        for (PoolEntry e : cm.constantPool()) {
            if (e instanceof MethodRefEntry m
                    && CallRules.APP.replacementFor(
                                    true,
                                    m.owner().asInternalName(),
                                    m.name().stringValue(),
                                    m.type().stringValue())
                            != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the transformation of the method's code, or {@code null} if it should stay unchanged.
     */
    private static CodeTransform codeTransformFor(
            Kind kind, String className, MethodModel mm, CodeModel code) {
        String method = mm.methodName().stringValue();
        String type = mm.methodType().stringValue();
        switch (kind) {
            case APP:
                return CallRules.APP.rewritesAny(code) ? new CallRewrite(CallRules.APP) : null;
            case CLOCK:
                if (className.equals(CLOCK) && method.equals("currentInstant")) {
                    return new PrependCurrentInstant();
                }
                return method.equals("millis") && CallRules.JDK.rewritesAny(code)
                        ? new CallRewrite(CallRules.JDK)
                        : null;
            case JDK:
                if (className.equals(VIRTUAL_THREAD)) {
                    if (method.equals("compareAndSetOnWaitingList") && type.equals("(ZZ)Z")) {
                        return new PrependOnWaitingListCas();
                    }
                    if (method.equals("unblock") && type.equals("()V")) {
                        return new AppendAfterUnblock();
                    }
                }
                CallRules rules =
                        className.equals(STRUCTURED_TASK_SCOPE)
                                ? CallRules.JDK_AND_SCHEDULE
                                : CallRules.JDK;
                return rules.rewritesAny(code) ? new CallRewrite(rules) : null;
            default:
                throw new IllegalArgumentException(kind.name());
        }
    }

    private record CallRewrite(CallRules rules) implements CodeTransform {
        @Override
        public void accept(CodeBuilder b, CodeElement e) {
            // no pattern switch: its bootstrap would run inside class loading
            Hook hook = e instanceof InvokeInstruction i ? rules.replacementFor(i) : null;
            DirectMethodHandleDesc handle =
                    e instanceof ConstantInstruction.LoadConstantInstruction c
                            ? rules.replacementFor(c.constantValue())
                            : null;
            if (hook != null) {
                b.invokestatic(HOOKS, hook.name(), hook.type());
            } else if (e instanceof InvokeDynamicInstruction i && rules.rewritesArgs(i)) {
                ConstantDesc[] args = i.bootstrapArgs().toArray(new ConstantDesc[0]);
                for (int k = 0; k < args.length; k++) {
                    DirectMethodHandleDesc replacement = rules.replacementFor(args[k]);
                    if (replacement != null) args[k] = replacement;
                }
                b.invokedynamic(
                        DynamicCallSiteDesc.of(
                                i.bootstrapMethod(), i.name().stringValue(), i.typeSymbol(), args));
            } else if (handle != null) {
                b.ldc(handle);
            } else {
                b.with(e);
            }
        }
    }

    /** Adds the original stack maps to transformed code, which is built without generating them. */
    private record WithStackMaps(Optional<StackMapTableAttribute> stackMaps)
            implements CodeTransform {
        @Override
        public void accept(CodeBuilder b, CodeElement e) {
            b.with(e);
        }

        @Override
        public void atEnd(CodeBuilder b) {
            stackMaps.ifPresent(b::with);
        }
    }

    /** {@code Instant i = Hooks.currentInstant(); if (i != null) return i;} */
    private static final class PrependCurrentInstant implements CodeTransform {
        @Override
        public void atStart(CodeBuilder b) {
            Label original = b.newLabel();
            b.invokestatic(
                            HOOKS,
                            "currentInstant",
                            MethodTypeDesc.of(ClassDesc.of("java.time.Instant")))
                    .dup()
                    .ifnull(original)
                    .areturn()
                    .labelBinding(original)
                    .pop();
        }

        @Override
        public void accept(CodeBuilder b, CodeElement e) {
            b.with(e);
        }
    }

    /** {@code Hooks.beforeOnWaitingListCas(newValue);} */
    private static final class PrependOnWaitingListCas implements CodeTransform {
        /**
         * The slot of {@code newValue} in {@code compareAndSetOnWaitingList(expected, newValue)}.
         */
        private static final int NEW_VALUE_SLOT = 2;

        @Override
        public void atStart(CodeBuilder b) {
            b.iload(NEW_VALUE_SLOT)
                    .invokestatic(
                            HOOKS,
                            "beforeOnWaitingListCas",
                            MethodTypeDesc.of(CD_void, CD_boolean));
        }

        @Override
        public void accept(CodeBuilder b, CodeElement e) {
            b.with(e);
        }
    }

    /** {@code Hooks.afterUnblock();} before each return. */
    private static final class AppendAfterUnblock implements CodeTransform {
        @Override
        public void accept(CodeBuilder b, CodeElement e) {
            if (e instanceof ReturnInstruction) b.invokestatic(HOOKS, "afterUnblock", RETURNS_VOID);
            b.with(e);
        }
    }
}
