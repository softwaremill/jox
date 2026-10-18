package com.softwaremill.jox.simtime.agent;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;

import com.softwaremill.jox.simtime.boot.Hooks;

/**
 * The Java agent, which enables simulated time. Add it to the test JVM using {@code
 * -javaagent:path/to/simulated-time.jar}.
 *
 * <p>Outside simulations, the JVM behaves as without the agent.
 */
public final class SimulatedTimeAgent {
    private static final String BOOT_JAR = "/META-INF/jox-simulated-time/boot.jar";

    private SimulatedTimeAgent() {}

    public static void premain(String args, Instrumentation inst) throws Exception {
        inst.appendToBootstrapClassLoaderSearch(new JarFile(extractBootJar().toFile()));
        Class<?> hooks = Class.forName(Hooks.CLASS_NAME, true, null);

        // rewritten java.base classes call the hooks; the runtime needs deep reflection on threads
        Module self = SimulatedTimeAgent.class.getModule();
        inst.redefineModule(
                Object.class.getModule(),
                Set.of(hooks.getModule()),
                Map.of(),
                Map.of("java.lang", Set.of(self), "java.util.concurrent", Set.of(self)),
                Set.of(),
                Map.of());

        // loaded now, so that they are rewritten (and verified) by the retransformation below
        for (String required : TimeTransformer.REQUIRED_JDK_REWRITES) {
            String className = required.substring(0, required.lastIndexOf('.'));
            Class.forName(className.replace('/', '.'), false, null);
        }

        TimeTransformer transformer = new TimeTransformer();
        transformer.prewarm();
        inst.addTransformer(transformer, true);
        retransformLoadedClasses(inst);
        verifyJdkRewrites(transformer);
    }

    private static Path extractBootJar() throws IOException {
        Path bootJar = Files.createTempFile("jox-simulated-time-boot", ".jar");
        bootJar.toFile().deleteOnExit();
        try (InputStream in = SimulatedTimeAgent.class.getResourceAsStream(BOOT_JAR)) {
            if (in == null)
                throw new IllegalStateException("missing " + BOOT_JAR + " in the agent jar");
            Files.copy(in, bootJar, StandardCopyOption.REPLACE_EXISTING);
        }
        return bootJar;
    }

    private static void retransformLoadedClasses(Instrumentation inst)
            throws UnmodifiableClassException {
        List<Class<?>> classes = new ArrayList<>();
        for (Class<?> c : inst.getAllLoadedClasses()) {
            if (c.isArray() || c.isPrimitive() || c.isHidden() || !inst.isModifiableClass(c))
                continue;
            if (TimeTransformer.isInScope(c.getName().replace('.', '/'), c.getClassLoader())) {
                classes.add(c);
            }
        }
        inst.retransformClasses(classes.toArray(Class[]::new));
    }

    /** Fails fast if the JDK's internals differ from what's expected. */
    private static void verifyJdkRewrites(TimeTransformer transformer) {
        Set<String> missing = new TreeSet<>(TimeTransformer.REQUIRED_JDK_REWRITES);
        missing.removeAll(transformer.rewrittenJdkMethods);
        if (!missing.isEmpty() || !Hooks.agentErrors().isEmpty()) {
            throw new IllegalStateException(
                    "jox simulated time is not supported on Java "
                            + Runtime.version()
                            + "; missing JDK rewrites: "
                            + missing
                            + "; errors: "
                            + Hooks.agentErrors());
        }
    }
}
