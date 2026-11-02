# 2. Simulated Time via a Custom Virtual Thread Scheduler and a Java Agent

Date: 2026-10-13

## Status

Accepted

## Context

Tests of time-dependent code (ticks, timeouts, throttling) either wait in real time, or need code changes to inject a
clock. We want unchanged jox and JDK code (`Thread.sleep`, timed `java.util.concurrent` waits) to run on virtual time.

Intercepting each sleep / wait call site doesn't scale: there are many timed waits in the JDK, which also differ between
versions. Detecting when to advance time by counting blocked threads is racy.

## Decision

The `simulated-time` module runs a simulation's virtual threads on a custom scheduler (a `ForkJoinPool` subclass), one
at a time, on a single carrier. The JDK hands it the timers of all timed waits (`ForkJoinPool.schedule`), so time
advances exactly when the run queue is empty, and no thread is about to be unblocked after waiting for a monitor. A Java
agent (using the ClassFile API) rewrites clock reads in application classes and in an allowlist of JDK classes, and
hooks into the JDK's monitor unblocker, to detect pending unblocks.

## Consequences

- Unchanged code gets exact virtual time; the agent is required, and depends on JDK internals (checked at startup).
- Threads outside the simulation, and their timers, keep real time.
- Interleavings are not deterministic; for that, see the Fray tests.
