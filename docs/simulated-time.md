# Simulated time (testing)

Run time-dependent code in tests, without waiting in real time. Inside a simulation, sleeps, timeouts and ticks
complete immediately, while the clocks observed by the code advance exactly by the requested durations. This works
both for jox (`Flows.tick`, `Flow.throttle`, `Race.timeout`, `selectWithin`, ...) and for plain Java code
(`Thread.sleep`, timed waits of `java.util.concurrent` and monitors, `System.nanoTime`, `Instant.now`, ...).

Requires Java 25 (current LTS).

Javadocs: [https://javadoc.io](https://javadoc.io/doc/com.softwaremill.jox/simulated-time).

## Dependency

The jar is both a test dependency and a Java agent; add it as both. It must be on the class path, not the module path.
It doesn't bring jox or JUnit transitively: add the jox modules that you use, and JUnit (for the `@WithSimulatedTime`
extension). As jox `structured` and `flows` use Java 25 preview features, compiling and running tests that use them
requires `--enable-preview`.

Maven:

```xml

<dependency>
    <groupId>com.softwaremill.jox</groupId>
    <artifactId>simulated-time</artifactId>
    <version>0.6.0</version>
    <scope>test</scope>
</dependency>
```

To add the agent, use the `maven-dependency-plugin` to resolve the jar's path, and pass it to surefire, together with
`--enable-preview`. If you already set an `argLine`, add these options to it. In modular projects, also set
`<useModulePath>false</useModulePath>`.

```xml

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <compilerArgs>
            <arg>--enable-preview</arg>
        </compilerArgs>
    </configuration>
</plugin>
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>properties</goal>
            </goals>
        </execution>
    </executions>
</plugin>
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>--enable-preview -javaagent:${com.softwaremill.jox:simulated-time:jar}</argLine>
    </configuration>
</plugin>
```

Gradle:

```groovy
configurations {
    simulatedTimeAgent { transitive = false }
}

dependencies {
    testImplementation 'com.softwaremill.jox:simulated-time:0.6.0'
    simulatedTimeAgent 'com.softwaremill.jox:simulated-time:0.6.0'
}

tasks.withType(JavaCompile).configureEach {
    options.compilerArgs.add('--enable-preview')
}

tasks.named('test') {
    jvmArgs '--enable-preview'
    jvmArgumentProviders.add({ ["-javaagent:${configurations.simulatedTimeAgent.singleFile}"] } as CommandLineArgumentProvider)
}
```

The agent rewrites clock reads in the classes of the test JVM; outside simulations, they still return real time.
Starting the JVM with the agent prints a (harmless) warning about class data sharing being limited.

## Usage

```java
import static com.softwaremill.jox.structured.Scopes.supervised;

import com.softwaremill.jox.flows.Flows;
import com.softwaremill.jox.simtime.SimulatedTime;

var ticks = SimulatedTime.run(() -> {
    long start = System.nanoTime();
    return Flows.tick(Duration.ofHours(1), "x")
            .map(_ -> Duration.ofNanos(System.nanoTime() - start))
            .take(3)
            .runToList();
});
// ticks == [PT0S, PT1H, PT2H], computed in a few milliseconds

SimulatedTime.run(() -> supervised(scope -> {
    var f = scope.fork(() -> {
        Thread.sleep(Duration.ofDays(1));
        return Instant.now();
    });
    return f.join(); // SimulatedTime.START plus 1 day
}));
```

* `SimulatedTime.run` runs the given code in a new virtual thread, and blocks until it completes, returning its result
  or throwing its exception
* the simulation includes that thread, and all virtual threads started by threads of the simulation (including forks
  of jox scopes, and of `StructuredTaskScope`s)
* when all threads of the simulation are blocked, virtual time advances to the nearest deadline (of a sleep, or a timed
  wait), and the threads waiting for it are woken up
* in the simulation, `Instant.now()` starts at `SimulatedTime.START` (2026-01-01T00:00:00Z), and `System.nanoTime()`
  at the arbitrary `SimulatedTime.NANO_TIME_ORIGIN`: measure elapsed time as differences of `nanoTime` values
* separate simulations can run in parallel, each with its own virtual time
* `SimulationOptions` configure the watchdog and the maximum virtual time, see below
* `Simulation` runs multiple bodies of code in sequence, sharing virtual time and threads

When a simulation can't continue, it fails with a `SimulatedTimeException` (including the stack traces of its threads):

* `DeadlockException`: all threads are blocked, there are no pending timers, and no thread has been woken up from
  outside the simulation for 500ms
* `WatchdogException`: virtual time didn't advance for `watchdogTimeout` (30 seconds by default) of real time, while
  some thread was running: e.g. the carrier thread is blocked (see the limitations below), or a loop polls the clock
  or calls `Thread.onSpinWait()`. A loop which does neither can't be stopped: the watchdog then prints the stack traces
  to `System.err`, and the simulation keeps running
* `RunawayTimeException`: virtual time would exceed `maxVirtualTime` (365 days by default), e.g. in an infinite loop of
  sleeps

## JUnit

Annotate a test class or method with `@WithSimulatedTime`, to run each test (including `@RepeatedTest`s and
`@ParameterizedTest`s) using simulated time:

```java
import com.softwaremill.jox.simtime.junit.WithSimulatedTime;

@WithSimulatedTime
class TickTest {
    long start;

    @BeforeEach
    void setUp() throws InterruptedException {
        start = System.nanoTime();
        Thread.sleep(Duration.ofMinutes(10));
    }

    @Test
    void tick() {
        var ticks = Flows.tick(Duration.ofHours(1), "x")
                .map(_ -> Duration.ofNanos(System.nanoTime() - start))
                .take(2)
                .runToList();
        assertEquals(List.of(Duration.ofMinutes(10), Duration.ofMinutes(70)), ticks);
    }
}
```

* a test runs in one simulation, together with its `@BeforeEach` and `@AfterEach` methods: virtual time and threads
  started in `@BeforeEach` carry over
* `@WithSimulatedTime(watchdogSeconds = ..., maxVirtualTimeDays = ...)` configures the simulation; a method's
  annotation takes precedence over the class's
* test class constructors, field initializers and `@BeforeAll` / `@AfterAll` methods run outside the simulation, using
  real time
* `@Nested` test classes use the configuration of the enclosing class
* `@TestFactory` methods (dynamic tests) are not supported, and fail
* after the watchdog fires, the simulation can't run anymore: the test's `@AfterEach` methods are skipped (and fail with
  the same exception)
* JUnit's `@Timeout` measures real time, and might only fail the test after the simulation ends; to detect hanging
  tests, rely on the watchdog

## How it works

A simulation runs its virtual threads one at a time, on the thread that called `SimulatedTime.run`. Hence, in a
simulation, `Thread.onSpinWait()` in application code yields the thread, as spinning can't make progress. See
[ADR 2](https://github.com/softwaremill/jox/blob/main/adr/0002-simulated-time-via-custom-scheduler-and-agent.md) for
the design.

## Limitations

* only virtual threads of the simulation use virtual time. Platform threads, virtual threads started from them, the
  common pool and other executors use real time, and the simulation doesn't wait for them, e.g. for
  `CompletableFuture.orTimeout`, `CompletableFuture.delayedExecutor`, a `ScheduledExecutorService`, or I/O: once the
  threads of the simulation are blocked, time advances to the next timer anyway
* Awaitility polls on a separate platform thread by default; use `await().pollInSameThread()`
* virtual time is exact, but the interleaving of threads is not deterministic
* blocking inside a class initializer, or in native code, blocks the simulation's only carrier thread in real time; the
  watchdog reports this
* clock reads in JDK classes keep real time, except for `java.time.Clock` and the allowlisted classes implementing
  timed waits (e.g. `new java.util.Date()` returns real time)
* threads of the simulation, which are still alive when `SimulatedTime.run` returns, are not run anymore
* the agent relies on JDK internals; it's tested on Java 25, and fails at startup if the internals don't match
