# JSON flows

Lazy, backpressured parsing and rendering of newline-delimited JSON (NDJSON) and top-level JSON arrays using Jox
`Flow` and `ByteFlow`.

Requires Java 25.

## Dependency

Maven:

```xml
<dependency>
    <groupId>com.softwaremill.jox</groupId>
    <artifactId>json</artifactId>
    <version>0.1.0</version>
</dependency>
```

Gradle:

```groovy
implementation 'com.softwaremill.jox:json:0.1.0'
```

Gradle (Kotlin DSL):

```kotlin
implementation("com.softwaremill.jox:json:0.1.0")
```

## API

`JsonFlow` provides four transformations:

* `parseNdjson(ByteFlow, ...)` parses UTF-8 NDJSON into a `Flow<T>`.
* `parseArray(ByteFlow, ...)` parses one top-level JSON array into a `Flow<T>` of its elements.
* `renderNdjson(Flow<T>, ...)` renders values as a UTF-8 NDJSON `ByteFlow`.
* `renderArray(Flow<T>, ...)` renders values as one UTF-8 JSON array `ByteFlow`.

Each method is lazy: parsing, rendering and I/O start only when the returned flow is run. Values are processed one at
a time, preserving the backpressure, failure propagation and cancellation behavior of the underlying Jox flow.

Parsing rejects an NDJSON record or array element that Jackson deserializes as Java `null`, and rendering rejects raw
Java `null` elements, as Jox flows do not support them. To retain JSON `null` values, use Jackson's tree model, where
they are represented by non-null null nodes.

Each operation has overloads accepting a `Class<T>`, a Jackson `TypeReference<T>`, or a configured Jackson
`ObjectReader`/`ObjectWriter`. The `Class<T>` and `TypeReference<T>` overloads use the module's default `ObjectMapper`.

## NDJSON

NDJSON parsing accepts LF and CRLF line endings, ignores empty and whitespace-only lines, and accepts a final record
without a line ending. Every non-blank line must contain exactly one JSON value; malformed JSON or trailing content on
a record fails the flow when it is run. Input must be valid UTF-8. One UTF-8 byte-order mark is accepted at the very
beginning of the stream.

An incomplete record is buffered across source chunks until its LF delimiter, or until end-of-input for the final
unterminated record. The default maximum encoded record size is 32 MiB, excluding the LF delimiter. An initial BOM and
the CR in a CRLF line ending count toward the limit. Use
`JsonReadSettings.defaults().maxNdjsonRecordBytes(...)` to choose another positive byte limit and pass the resulting
settings as the final argument to `parseNdjson`.

```java
import java.nio.charset.StandardCharsets;

import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.flows.Flows;
import com.softwaremill.jox.json.JsonFlow;

record Event(long id, String message) {}

void main() throws Exception {
    var input = """
            {"id":1,"message":"created"}
            {"id":2,"message":"updated"}
            """;

    Flow<Event> events = JsonFlow.parseNdjson(
            Flows.fromByteArrays(input.getBytes(StandardCharsets.UTF_8)),
            Event.class);

    events.filter(event -> event.id() > 1)
            .runForeach(System.out::println);
}
```

NDJSON rendering writes one JSON value followed by an LF byte. The final value also has a terminating LF. A writer
whose output contains raw CR or LF bytes is rejected, as such output would break NDJSON record boundaries. In
particular, do not use a pretty-printing writer for NDJSON.

```java
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import com.softwaremill.jox.flows.Flows;
import com.softwaremill.jox.json.JsonFlow;

record Event(long id, String message) {}

void main() throws Exception {
    var events = Flows.fromValues(
            new Event(1, "created"),
            new Event(2, "updated"));

    var output = new ByteArrayOutputStream();
    JsonFlow.renderNdjson(events, Event.class).runToOutputStream(output);

    System.out.print(output.toString(StandardCharsets.UTF_8));
}
```

## JSON arrays

Array parsing requires exactly one complete top-level array and incrementally emits its elements while parsing. A
different top-level JSON value, an incomplete array, malformed input, or JSON content after the array fails the flow.
An empty array produces an empty flow. Elements can be emitted before the source ends, but successful completion waits
for end-of-input so that trailing content can be rejected. As array parsing uses an internal supervised scope, failures
are wrapped in `JoxScopeExecutionException`.

```java
import java.nio.charset.StandardCharsets;

import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.flows.Flows;
import com.softwaremill.jox.json.JsonFlow;

record Event(long id, String message) {}

void main() throws Exception {
    var input = """
            [
              {"id":1,"message":"created"},
              {"id":2,"message":"updated"}
            ]
            """;

    Flow<Event> events = JsonFlow.parseArray(
            Flows.fromByteArrays(input.getBytes(StandardCharsets.UTF_8)),
            Event.class);

    events.runForeach(System.out::println);
}
```

Array rendering writes `[` and `]` around comma-separated values. Elements are serialized one at a time, and an empty
input flow produces `[]`. If serialization or the input flow fails after output starts, already-written bytes can
contain an incomplete array; callers should discard failed output or write transactionally when this matters.

```java
import java.nio.file.Path;

import com.softwaremill.jox.flows.Flows;
import com.softwaremill.jox.json.JsonFlow;

record Event(long id, String message) {}

void main() throws Exception {
    var events = Flows.fromValues(
            new Event(1, "created"),
            new Event(2, "updated"));

    JsonFlow.renderArray(events, Event.class)
            .runToFile(Path.of("events.json"));
}
```

For generic element types, use a Jackson `TypeReference`:

```java
import java.util.List;

import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.flows.Flow.ByteFlow;
import com.softwaremill.jox.json.JsonFlow;

import tools.jackson.core.type.TypeReference;

record Event(long id, String message) {}

Flow<List<Event>> parseBatches(ByteFlow input) {
    return JsonFlow.parseArray(input, new TypeReference<List<Event>>() {});
}
```

## Jackson configuration

For custom Jackson modules, naming strategies, date handling, polymorphism, tree-model values, or other mapper
features, configure an `ObjectMapper` and derive an `ObjectReader` or `ObjectWriter`. The reader or writer determines
the type and Jackson behavior for each NDJSON record or array element.

The parsing mode controls trailing-token validation: NDJSON enables `FAIL_ON_TRAILING_TOKENS` so that every record
contains exactly one value, while array parsing disables it when reading individual elements. These settings override
the supplied reader's value for that feature. The generic result type of an `ObjectReader` overload is inferred by Java
and cannot be checked against the reader's configured type, so callers must keep them consistent.

```java
import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.flows.Flow.ByteFlow;
import com.softwaremill.jox.json.JsonFlow;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

record Event(long id, String message) {}

Flow<Event> parseEvents(ByteFlow input) {
    var mapper = new ObjectMapper();
    var reader = mapper.readerFor(Event.class)
            .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    return JsonFlow.parseNdjson(input, reader);
}
```

The same configured mapper can create a writer using `mapper.writerFor(Event.class)`, which can be passed to
`renderNdjson` or `renderArray`.

## Composing with Jox flows and I/O

The results are ordinary Jox `Flow` and `ByteFlow` values. Parsed values can use transformations such as `map`,
`filter`, `mapPar`, `buffer` and error recovery. Rendered bytes can be written using existing `ByteFlow` operations.
Likewise, JSON input can come from any `ByteFlow`, including files, `InputStream`s and in-memory byte chunks.

For example, this pipeline reads NDJSON from a file, applies regular flow transformations, and streams one JSON array
to another file:

```java
import java.nio.file.Path;

import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.flows.Flows;
import com.softwaremill.jox.json.JsonFlow;

record Event(long id, boolean accepted) {}

void main() throws Exception {
    Flow<Event> accepted = JsonFlow.parseNdjson(
                    Flows.fromFile(Path.of("events.ndjson")),
                    Event.class)
            .filter(Event::accepted)
            .map(event -> new Event(event.id(), true));

    JsonFlow.renderArray(accepted, Event.class)
            .runToFile(Path.of("accepted.json"));
}
```

Use `Flows.fromInputStream(...)` for stream input, and `runToOutputStream(...)` for stream output. These operations
retain their normal Jox resource ownership: the input or output stream is closed when the flow finishes or fails.
