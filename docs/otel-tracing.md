# OpenTelemetry tracing

Dependency:

```xml
<dependency>
    <groupId>com.softwaremill.jox</groupId>
    <artifactId>otel-tracing</artifactId>
    <version>0.6.0</version>
</dependency>
```

The module depends on `otel-context`, so it doesn't need to be added separately. An OpenTelemetry SDK
and exporter have to be configured by the application, as usual.

Jox provides helpers for creating [OpenTelemetry](https://opentelemetry.io) spans around code that uses Jox's
concurrency operators, including [flows](flows.md). An opened span becomes a child of the current span, and becomes the
current span itself, until it's closed.

Spans are created using an OpenTelemetry `Tracer`, passed as the first argument:

```java
import static com.softwaremill.jox.otel.tracing.Tracing.span;

int someTask() throws InterruptedException {
    Thread.sleep(Duration.ofMillis(200));
    return 21;
}

int anotherTask(int in) throws InterruptedException {
    Thread.sleep(Duration.ofMillis(500));
    return 42;
}

int measuredComputations(Tracer tracer) throws Exception {
    return span(tracer, "outerSpan", () -> {
        var result1 = someTask();
        return span(tracer, "innerSpan", () -> anotherTask(result1));
    });
}
```

For code which doesn't return a value, use `spanRun`.

## Propagating the context

The current span is kept in the OpenTelemetry `Context`, which by default isn't propagated to forks created in
[`supervised`](structured.md) scopes. Forks are started by the thread which runs the enclosing `supervised`, not by the
thread which calls `fork`, so values inherited when a thread is created - such as the OpenTelemetry context - come from
that thread, and not from the one which started the fork.

Jox tracing therefore should be used with the `PropagatingForkDecorator` from the `otel-context` module, set as the fork
decorator used by concurrency scopes through the `Scope.FORK_DECORATOR` scoped value. It captures the context when
`fork` is called, and restores it around the fork's body:

```java
import static com.softwaremill.jox.structured.Scopes.supervised;

import com.softwaremill.jox.otel.context.PropagatingForkDecorator;
import com.softwaremill.jox.structured.Scope;

ScopedValue.where(Scope.FORK_DECORATOR, PropagatingForkDecorator.INSTANCE)
    .call(() -> supervised(scope -> {
        // spans created in forks are children of the span active when the fork was created
        return null;
    }));
```

Without it, spans created in forks won't be children of the span active when the fork was created. This also applies to
flow stages which run asynchronously, e.g. after a `buffer()`.

The binding covers everything called within it, at any depth, including scopes created by code which knows nothing about
it. Hence it's usually set once, at the entry point:

```java
public static void main(String[] args) throws Exception {
    ScopedValue.where(Scope.FORK_DECORATOR, PropagatingForkDecorator.INSTANCE)
        .call(() -> runApp());
}
```

Code which runs outside the binding - e.g. on threads which aren't created within it - won't propagate the context.

## Spans around flows

[Flows](flows.md) can open a span for each element. The span is a child of the current span, and if the code
within it throws, the exception is recorded on the span, and its status is set to `ERROR`.

### spanEach

`spanEach` covers the stages placed after it, up to the first asynchronous boundary, e.g. `buffer`:

```java
import static com.softwaremill.jox.otel.tracing.FlowTracing.spanEach;

List<Integer> downstream(Tracer tracer, Flow<Integer> flow) throws Exception {
    return spanEach(flow, tracer, "element")
        .map(x -> x + 1) // covered by the span
        .buffer()        // the span ends here
        .map(x -> x * 2) // not covered
        .runToList();
}
```

The span covers also the stages placed after a `flatMap` that the span was created in, as it passes the elements on from
within the span:

```java
List<Integer> inFlatMap(Tracer tracer, Flow<Integer> flow) throws Exception {
    return flow
        .flatMap(x -> spanEach(
            Flows.fromValues(x, x * 10).tap(v -> Thread.sleep(Duration.ofMillis(100))), // not covered
            tracer, "element")
            .map(v -> v + 1)) // covered by the span
        .map(v -> v * 2)      // also covered
        .runToList();
}
```

### Optional parameters

The operators accept a `SpanOptions` instance, which reflects the OpenTelemetry span builder: `spanKind`, `attributes`
and `links`.

It also carries a `beforeClose` callback. Called right before the span is closed, it receives the span and, if any
exception was thrown by the code covered by the span, that exception as `exceptionOpt`. It can be used to modify the
span before it's closed.

```java
import static com.softwaremill.jox.otel.tracing.SpanOptions.spanOptions;

List<Integer> withCallback(Tracer tracer, Flow<Integer> flow) throws Exception {
    return spanEach(flow, tracer, "process", spanOptions()
            .withBeforeClose(s -> {
                s.exceptionOpt().ifPresent(e ->
                    s.span().setAttribute("error.type", e.getClass().getSimpleName()));
                s.span().addEvent("done");
            }))
        .map(x -> x + 1)
        .runToList();
}
```

## Spans around code

`span` evaluates the given code within a span, which is ended once the code completes. If it throws, the exception is
recorded on the span: its status is set to `ERROR`, and an `exception` event is added. Interruptions are not failures,
so they end the span normally; the same applies to an `ExecutionException` wrapping an interruption.

The exception is re-thrown as is, after the span is ended.

## Manual spans

`spanUnsafe` opens a span, which has to be closed by the caller. Closing the returned `AutoCloseableSpan` ends the span
and restores the previously current span. It's unsafe, as forgetting to close it leaves the span open and the current
span unrestored; use it with try-with-resources, so that closing is guaranteed. `currentSpanUnsafe` returns the current
span, e.g. to add attributes:

```java
import static com.softwaremill.jox.otel.tracing.Tracing.currentSpanUnsafe;
import static com.softwaremill.jox.otel.tracing.Tracing.spanUnsafe;

void manual(Tracer tracer) {
    try (var opened = spanUnsafe(tracer, "manual-span")) {
        opened.span().setAttribute("key", "value");
        currentSpanUnsafe().addEvent("working");
    }
}
```
