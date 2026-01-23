# Flows (streaming)

Finite & infinite streaming using flows, with reactive streams compatibility, (blocking) I/O integration, and a
high-level, "functional" API.

Requires Java 25 (current LTS).

Javadocs: [https://javadoc.io](https://javadoc.io/doc/com.softwaremill.jox/flows).

## Dependency

Maven:

```xml

<dependency>
    <groupId>com.softwaremill.jox</groupId>
    <artifactId>flows</artifactId>
    <version>0.5.2</version>
</dependency>
```

Gradle:

```groovy
implementation 'com.softwaremill.jox:flows:0.5.2'
```

## Usage

A `Flow<T>` describes an asynchronous data transformation pipeline. When run, it emits elements of type `T`.

Flows are lazy, evaluation (and any effects) happen only when the flow is run. Flows might be finite or infinite; in the
latter case running a flow never ends normally; it might be interrupted, though. Finally, any exceptions that occur when
evaluating the flow's logic will be thrown when running the flow, after any cleanup logic completes.

## Creating Flows

There are number of methods in the `Flows` class which allows to create a `Flow`.

```java
import java.time.Duration;

import com.softwaremill.jox.flows.Flows;

void main(String[] args) {
    Flows.fromValues(1, 2, 3); // a finite flow
    Flows.tick(Duration.ofSeconds(1), "x"); // an infinite flow emitting "x" every second
    Flows.iterate(0, i -> i + 1); // an infinite flow iterating from 0
}
```

Note that creating a flow as above doesn't emit any elements, or execute any of the flow's logic. Only when run, the
elements are emitted and any effects that are part of the flow's stages happen.

Flows can also be created using `Channel` `Source`s:

```java
import com.softwaremill.jox.Channel;
import com.softwaremill.jox.flows.Flows;
import com.softwaremill.jox.structured.Scopes;

void main() throws InterruptedException {
    Channel<Integer> ch = Channel.newBufferedDefaultChannel();
    Scopes.supervised(scope -> {
        scope.fork(() -> {
            ch.send(1);
            ch.send(15);
            ch.send(-2);
            ch.done();
            return null;
        });

        Flows.fromSource(ch); // TODO: transform the flow further & run
        return null;
    });
}
```

Finally, flows can be created by providing arbitrary element-emitting logic:

```java
import com.softwaremill.jox.flows.Flows;

void main() {
    Flows.usingEmit(emit -> {
        emit.apply(21);
        for (int i = 0; i < 5; i++) {
            emit.apply(i);
        }
        emit.apply(37);
    });
}
```

The `FlowEmit` instance is used to emit elements by the flow, that is process them further, as defined by the downstream
pipeline. This method only completes once the element is fully processed, and it might throw exceptions in case there's
a processing error.

As part of the callback, you can create a `Scope`, fork background computations or run other flows asynchronously.
However, take care **not** to share the `FlowEmit` instance across threads. That is, instances of `FlowEmit` are
thread-unsafe and should only be used on the calling thread. The lifetime of `FlowEmit` should not extend over the
duration of the invocation of `usingEmit`.

For **concurrent emissions** from multiple threads, use `usingChannel` instead:

```java
import com.softwaremill.jox.flows.Flows;
import static com.softwaremill.jox.structured.Scopes.supervised;

void main() throws Exception {
    Flows.usingChannel(sink -> {
        sink.send(1);

        supervised(scope -> {
            scope.forkUser(() -> {
                sink.send(2);
                return null;
            });
            scope.forkUser(() -> {
                sink.send(3);
                return null;
            });
            return null;
        });
    }).runToList(); // Returns [1, 2, 3] in non-deterministic order
}
```

If you have a `Channel` instance, you can also forward the channel to an emit using `Flow.usingEmti` and 
`FlowEmit.channelToEmit`. 

## Transforming flows: basics

Multiple transformation stages can be added to a flow, each time returning a new `Flow` instance, describing the
extended pipeline. As before, no elements are emitted or transformed until the flow is run, as flows are lazy. There's a
number of pre-defined transformation stages:

```java
import com.softwaremill.jox.flows.Flows;

void main() {
    Flows.fromValues(1, 2, 3, 5, 6)
            .map(i -> i * 2)
            .filter(i -> i % 2 == 0)
            .take(3)
            .zip(Flows.repeat("a number"))
            .interleave(Flows.repeat(Map.entry(0, "also a number")), 1, false);
}
```

You can also define arbitrary element-emitting logic, using each incoming element using `.mapUsingEmit`, similarly to
`Flows.usingEmit` above.

## Running flows

Flows have to be run, for any processing to happen. This can be done with one of the `.run...` methods. For example:

```java
import com.softwaremill.jox.flows.Flows;

void main() throws Exception {
    Flows.fromValues(1, 2, 3).runToList(); // List(1, 2, 3)
    Flows.fromValues(1, 2, 3).runForeach(System.out::println);
    Flows.tick(Duration.ofSeconds(1), "x").runDrain(); // never finishes
}
```

Running a flow is a blocking operation. Unless asynchronous boundaries are present (explicit or implicit, more on this
below), the entire processing happens on the calling thread. For example such a pipeline:

```java
import com.softwaremill.jox.flows.Flows;

void main() throws Exception {
    Flows.fromValues(1, 2, 3, 5, 6)
            .map(i -> i * 2)
            .filter(i -> i % 2 == 0)
            .runToList();
}
```

Processes the elements one-by-one on the thread that is invoking the run method.

## Transforming flows: concurrency

A number of flow transformations introduces asynchronous boundaries. For example,
`.mapPar(int parallelism, Function<T,U> mappingFunction)` describes a flow, which runs the pipeline defined so far in
the background, emitting elements to a `channel`. Another `fork` reads these elements and runs up to `parallelism`
invocations of `mappingFunction` concurrently. Mapped elements are then emitted by the returned flow.

Behind the scenes, a new concurrency `Scope` is created along with a number of forks. In case of any exceptions,
everything is cleaned up before the flow propagates the exceptions. The `.mapPar` logic ensures that any exceptions from
the preceding pipeline are propagated through the channel.

Some other stages which introduce concurrency include `.merge`, `.interleave`, `.groupedWithin` and `I/O` stages. The
created channels serve as buffers between the pipeline stages, and their capacity is defined by the `ScopedValue`
`Flow.CHANNEL_BUFFER_SIZE` in the scope, or default `Channel.DEFAULT_BUFFER_SIZE` is used.

Explicit asynchronous boundaries can be inserted using `.buffer()`. This might be useful if producing the next element
to emit, and consuming the previous should run concurrently; or if the processing times of the consumer varies, and the
producer should buffer up elements.

## Interoperability with channels

Flows can be created from channels, and run to channels. For example:

```java
import com.softwaremill.jox.Channel;
import com.softwaremill.jox.Source;
import com.softwaremill.jox.flows.Flows;
import com.softwaremill.jox.structured.Scopes;

void main(String[] args) throws Exception {
    Source<String> ch = getSource(args); // provide a source
    Scopes.supervised(scope -> {
        Source<String> output = ScopedValue.getWhere(Channel.BUFFER_SIZE, 5, () -> Flows.fromSource(ch)
                .mapConcat(v -> Arrays.asList(v.split(" ")))
                .filter(v -> v.startsWith("example"))
                .runToChannel(scope));
    });
}
```

The method above needs to be run within a concurrency scope, as `.runToChannel()` creates a background fork which runs
the pipeline described by the flow, and emits its elements onto the returned channel.

## Text transformations and I/O operations

For smooth operations on `byte[]`, we've created a wrapper class `ByteChunk`. And for smooth type handling we created a
dedicated `ByteFlow`, a subtype of `Flow<ByteChunk>`. To be able to utilize text and I/O operations, you need to create
or transform into `ByteFlow`. It can be created via `Flows.fromByteArray` or `Flows.fromByteChunk`. `Flow` containing
`byte[]` or `ByteChunk` can be transformed by using `toByteFlow()` method. Any other flow can be transformed by using
`toByteFlow()` with mapping function.

### Text operations

* `encodeUtf8` encodes a `Flow<String>` into a `ByteFlow`
* `linesUtf8` decodes a `ByteFlow` into a `Flow<String>`. Assumes that the input represents text with line breaks. The
  `String` elements emitted by resulting `Flow<String>` represent text lines.
* `decodeStringUtf8` to decode a `ByteFlow` into a `Flow<String>`, without handling line breaks, just processing input
  bytes as UTF-8 characters, even if a multi-byte character is divided into two chunks.

### I/O Operations

* `runToInputStream(Scope scope)` runs given flow asynchronously into returned `InputStream`
* `runToOutputStream(OutputStream outputStream)` runs given flow into provided `OutputStream`
* `runToFile(Path path)` runs given flow into file. If file does not exist, it's created.

It is also possible to create Flow from `inputStream` or `path` using `Flows` factory methods.

## Logging

Jox does not have any integrations with logging libraries, but it provides a simple way to log elements emitted by flows
using the `.tap` method:

```java
import com.softwaremill.jox.flows.Flows;

void main() throws Exception {
    Flows.fromValues(1, 2, 3)
            .tap(n -> System.out.printf("Received: %d%n", n))
            .runToList();
}
```

## Reactive streams interoperability

### Flow -> Publisher

A `Flow` can be converted to a `java.util.concurrent.Flow.Publisher` using the `.toPublisher` method.

This needs to be run within an concurrency `Scope`, as upon subscribing, a fork is created to run the publishing
process. Hence, the scope should remain active as long as the publisher is used.

Internally, elements emitted by the flow are buffered, using a buffer of capacity given by the `Channel.BUFFER_SIZE` in
scope.

To obtain a `org.reactivestreams.Publisher` instance, you'll need to add the `reactive-streams` dependency and
use `org.reactivestreams.FlowAdapters`.

### Publisher -> Flow

A `java.util.concurrent.Flow.Publisher` can be converted to a `Flow` using `Flow.fromPublisher`.

Internally, elements published to the subscription are buffered, using a buffer of capacity given by the
`Channel.BUFFER_SIZE` in scope. That's also how many elements will be at most requested from the publisher at a time.

To convert a `org.reactivestreams.Publisher` instance, you'll need the same dependency as above and use
`org.reactivestreams.FlowAdapters`.
