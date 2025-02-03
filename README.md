# Jox

[![Ideas, suggestions, problems, questions](https://img.shields.io/badge/Discourse-ask%20question-blue)](https://softwaremill.community/c/open-source/11)
[![CI](https://github.com/softwaremill/jox/workflows/CI/badge.svg)](https://github.com/softwaremill/jox/actions?query=workflow%3A%22CI%22)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.jox/channels/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.jox/channels)
[![javadoc](https://javadoc.io/badge2/com.softwaremill.jox/channels/javadoc.svg)](https://javadoc.io/doc/com.softwaremill.jox/channels)

Modern concurrency for Java 21 (backed by virtual threads, see [Project Loom](https://openjdk.org/projects/loom/)).
Requires JDK 21.
Includes:

* Fast and Scalable Channels in Java. Inspired by the "Fast and Scalable Channels in Kotlin Coroutines"
  [paper](https://arxiv.org/abs/2211.04986), and
  the [Kotlin implementation](https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/common/src/channels/BufferedChannel.kt).
* Programmer-friendly structured concurrency
* Blocking, synchronous, functional streaming operators

JavaDocs can be browsed
at [https://javadoc.io](https://www.javadoc.io/doc/com.softwaremill.jox/core/latest/com.softwaremill.jox/com/softwaremill/jox/package-summary.html).

Articles:

* [Announcing jox: Fast and Scalable Channels in Java](https://softwaremill.com/announcing-jox-fast-and-scalable-channels-in-java/)
* [Go-like selects using jox channels in Java](https://softwaremill.com/go-like-selects-using-jox-channels-in-java/)
* [Jox 0.1: virtual-thread friendly channels for Java](https://softwaremill.com/jox-0-1-virtual-thread-friendly-channels-for-java/)

Videos:

* [A 10-minute introduction to Jox](https://www.youtube.com/watch?v=Ss9b1HpPDt0)
* [Passing control information through channels](https://www.youtube.com/watch?v=VjiCzaiRro8)

For a Scala version, see the [Ox project](https://github.com/softwaremill/ox).

## Table of contents

* [Channels](#channels)
* [Structured concurrency](#structured-concurrency)
* [Streaming](#streaming)
* [Flows](#lazy-streaming---flows)

## Channels

### Dependency

Maven:

```xml

<dependency>
    <groupId>com.softwaremill.jox</groupId>
    <artifactId>channels</artifactId>
    <version>0.3.1</version>
</dependency>
```

Gradle:

```groovy
implementation 'com.softwaremill.jox:channels:0.3.1'
```

### Usage

#### Rendezvous channel

```java
import com.softwaremill.jox.Channel;

class Demo1 {
    public static void main(String[] args) throws InterruptedException {
        // creates a rendezvous channel
        // (a sender & receiver must meet to pass a value: as if the buffer had size 0)
        var ch = new Channel<Integer>();

        Thread.ofVirtual().start(() -> {
            try {
                // send() will block, until there's a matching receive()
                ch.send(1);
                System.out.println("Sent 1");
                ch.send(2);
                System.out.println("Sent 2");
                ch.send(3);
                System.out.println("Sent 3");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("Received: " + ch.receive());
        System.out.println("Received: " + ch.receive());
        System.out.println("Received: " + ch.receive());
    }
}
```

#### Buffered channel

```java
import com.softwaremill.jox.Channel;

class Demo2 {
    public static void main(String[] args) throws InterruptedException {
        // creates a buffered channel (buffer of size 3)
        var ch = new Channel<Integer>(3);

        // send()-s won't block
        ch.send(1);
        System.out.println("Sent 1");
        ch.send(2);
        System.out.println("Sent 2");
        ch.send(3);
        System.out.println("Sent 3");
        // the next send() would block

        System.out.println("Received: " + ch.receive());
        System.out.println("Received: " + ch.receive());
        System.out.println("Received: " + ch.receive());
        // same for the next receive() - it would block
    }
}
```

There is also a possibility to pass `Channel`'s buffer size via `ScopedValue`.
The channel must be created via `Channel.withScopedBufferSize()` to get the value.

If no value is in the scope, the default buffer size `Channel.DEFAULT_BUFFER_SIZE` is used

```java
import com.softwaremill.jox.Channel;

public class Demo {

    public static void main(String[] args) {
        ScopedValue.where(Channel.BUFFER_SIZE, 10)
                .run(() -> {
                    Channel.withScopedBufferSize(); // creates channel with buffer size = 10
                });
        Channel.withScopedBufferSize(); // no value in the scope, so default (16) buffer size is used
    }
}
```

Unlimited channels can be created with `Channel.newUnlimitedChannel()`. Such channels will never block on send().

#### Closing a channel

Channels can be closed, either because the source is `done` with sending values, or when there's an `error` while
the sink processes the received values.

`send()` and `receive()` will throw a `ChannelClosedException` when the channel is closed. Alternatively, you can
use the `sendOrClosed()` and `receiveOrClosed()` methods, which return either a `ChannelClosed` value (reason of
closure), or `null` / the received value.

Channels can also be inspected whether they are closed, using the `isClosedForReceive()` and `isClosedForSend()`.

```java
import com.softwaremill.jox.Channel;

class Demo3 {
    public static void main(String[] args) throws InterruptedException {
        // creates a buffered channel (buffer of size 3)
        var ch = new Channel<Integer>(3);

        // send()-s won't block
        ch.send(1);
        ch.done();

        // prints: Received: 1
        System.out.println("Received: " + ch.receiveOrClosed());
        // prints: Received: ChannelDone[]
        System.out.println("Received: " + ch.receiveOrClosed());
    }
}
```

#### Selecting from multiple channels

The `select` method selects exactly one clause to complete. For example, you can receive a value from exactly one
channel:

```java
import com.softwaremill.jox.Channel;

import static com.softwaremill.jox.Select.select;

class Demo4 {
    public static void main(String[] args) throws InterruptedException {
        // creates a buffered channel (buffer of size 3)
        var ch1 = new Channel<Integer>(3);
        var ch2 = new Channel<Integer>(3);
        var ch3 = new Channel<Integer>(3);

        // send a value to two channels
        ch2.send(29);
        ch3.send(32);

        var received = select(ch1.receiveClause(), ch2.receiveClause(), ch3.receiveClause());

        // prints: Received: 29
        System.out.println("Received: " + received);
        // ch3 still holds a value that can be received
    }
}
```

The received value can be optionally transformed by a provided function.

`select` is biased: if a couple of the clauses can be completed immediately, the one that appears first will be
selected.

Similarly, you can select from a send clause to complete. Apart from the `Channel.sendClause()` method, there's also a
variant which runs a callback, once the clause is selected:

```java
import com.softwaremill.jox.Channel;

import static com.softwaremill.jox.Select.select;

class Demo5 {
    public static void main(String[] args) throws InterruptedException {
        var ch1 = new Channel<Integer>(1);
        var ch2 = new Channel<Integer>(1);

        ch1.send(12); // buffer is now full

        var sent = select(ch1.sendClause(13, () -> "first"), ch2.sendClause(25, () -> "second"));

        // prints: Sent: second
        System.out.println("Sent: " + sent);
    }
}
```

Optionally, you can also provide a default clause, which will be selected if none of the other clauses can be completed
immediately:

```java
import com.softwaremill.jox.Channel;

import static com.softwaremill.jox.Select.defaultClause;
import static com.softwaremill.jox.Select.select;

class Demo6 {
    public static void main(String[] args) throws InterruptedException {
        var ch1 = new Channel<Integer>(3);
        var ch2 = new Channel<Integer>(3);

        var received = select(ch1.receiveClause(), ch2.receiveClause(), defaultClause(52));

        // prints: Received: 52
        System.out.println("Received: " + received);
    }
}
```

### Performance

The project includes benchmarks implemented using JMH - both for the `Channel`, as well as for some built-in Java
synchronisation primitives (queues), as well as the Kotlin channel implementation.

The test results for version 0.3.1, run on an M1 Max MacBook Pro, with Java 21.0.1, are as follows:

```
Benchmark                                                       (capacity)  (chainLength)  (parallelism)  Mode  Cnt     Score     Error  Units

// jox - multi channel

ChainedBenchmark.channelChain                                            0          10000            N/A  avgt   10   171.100 ±   3.122  ns/op
ChainedBenchmark.channelChain                                           16          10000            N/A  avgt   10    12.697 ±   0.340  ns/op
ChainedBenchmark.channelChain                                          100          10000            N/A  avgt   10     6.468 ±   0.565  ns/op

ParallelBenchmark.parallelChannels                                       0            N/A          10000  avgt   10   146.830 ±  10.941  ns/op
ParallelBenchmark.parallelChannels                                      16            N/A          10000  avgt   10    14.863 ±   2.556  ns/op
ParallelBenchmark.parallelChannels                                     100            N/A          10000  avgt   10     8.582 ±   0.523  ns/op

// kotlin - multi channel

ChainedKotlinBenchmark.channelChain_defaultDispatcher                    0          10000            N/A  avgt   20    74.912 ±   0.896  ns/op
ChainedKotlinBenchmark.channelChain_defaultDispatcher                   16          10000            N/A  avgt   20     6.958 ±   0.209  ns/op
ChainedKotlinBenchmark.channelChain_defaultDispatcher                  100          10000            N/A  avgt   20     4.917 ±   0.128  ns/op

ChainedKotlinBenchmark.channelChain_eventLoop                            0          10000            N/A  avgt   20    90.848 ±   1.633  ns/op
ChainedKotlinBenchmark.channelChain_eventLoop                           16          10000            N/A  avgt   20    30.055 ±   0.247  ns/op
ChainedKotlinBenchmark.channelChain_eventLoop                          100          10000            N/A  avgt   20    27.762 ±   0.201  ns/op

ParallelKotlinBenchmark.parallelChannels_defaultDispatcher               0            N/A          10000  avgt   20    74.002 ±   0.671  ns/op
ParallelKotlinBenchmark.parallelChannels_defaultDispatcher              16            N/A          10000  avgt   20    11.009 ±   0.223  ns/op
ParallelKotlinBenchmark.parallelChannels_defaultDispatcher             100            N/A          10000  avgt   20     4.145 ±   0.149  ns/op

// java built-in - multi queues

ChainedBenchmark.queueChain                                              0          10000            N/A  avgt   10    79.284 ±   5.376  ns/op
ChainedBenchmark.queueChain                                             16          10000            N/A  avgt   10     8.772 ±   0.152  ns/op
ChainedBenchmark.queueChain                                            100          10000            N/A  avgt   10     4.268 ±   0.231  ns/op

ParallelBenchmark.parallelQueues                                         0            N/A          10000  avgt   10    84.382 ±  20.473  ns/op
ParallelBenchmark.parallelQueues                                        16            N/A          10000  avgt   10    15.043 ±   2.096  ns/op
ParallelBenchmark.parallelQueues                                       100            N/A          10000  avgt   10     6.182 ±   0.685  ns/op

// jox - single channel

RendezvousBenchmark.channel                                            N/A            N/A            N/A  avgt   10   199.199 ±  11.493  ns/op

BufferedBenchmark.channel                                               16            N/A            N/A  avgt   10   201.319 ±  18.463  ns/op
BufferedBenchmark.channel                                              100            N/A            N/A  avgt   10   102.972 ±   9.247  ns/op

// kotlin - single channel

RendezvousKotlinBenchmark.channel_defaultDispatcher                    N/A            N/A            N/A  avgt   20   108.400 ±   1.227  ns/op

BufferedKotlinBenchmark.channel_defaultDispatcher                       16            N/A            N/A  avgt   20    35.717 ±   0.264  ns/op
BufferedKotlinBenchmark.channel_defaultDispatcher                      100            N/A            N/A  avgt   20    27.049 ±   0.060  ns/op

// jox - selects

SelectBenchmark.selectWithSingleClause                                 N/A            N/A            N/A  avgt   10   229.320 ±  23.705  ns/op
SelectBenchmark.selectWithTwoClauses                                   N/A            N/A            N/A  avgt   10   761.067 ±  30.963  ns/op

// kotlin - selects

SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher         N/A            N/A            N/A  avgt   20   171.426 ±   4.616  ns/op
SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher           N/A            N/A            N/A  avgt   20   228.280 ±  10.847  ns/op

// java built-in - single queue                                                                             

BufferedBenchmark.arrayBlockingQueue                                    16            N/A            N/A  avgt   10   264.974 ±  61.166  ns/op
BufferedBenchmark.arrayBlockingQueue                                   100            N/A            N/A  avgt   10   108.087 ±   4.545  ns/op

RendezvousBenchmark.exchanger                                          N/A            N/A            N/A  avgt   10    93.386 ±   1.421  ns/op
RendezvousBenchmark.synchronousQueue                                   N/A            N/A            N/A  avgt   10  1714.025 ± 671.140  ns/op

// multi queue/channel tests with a larger number of elements

Benchmark                                                   (capacity)  (parallelism)  Mode  Cnt  Score    Error  Units
ParallelBenchmark.parallelChannels                                  16          10000  avgt   10  14.155 ± 0.874  ns/op
ParallelBenchmark.parallelQueues                                    16          10000  avgt   20  16.053 ± 1.368  ns/op

ChainedBenchmark.channelChain                                       16          10000  avgt   10  13.972 ± 0.429  ns/op
ChainedBenchmark.queueChain                                         16          10000  avgt   20   9.556 ± 0.233  ns/op

ParallelKotlinBenchmark.parallelChannels_defaultDispatcher          16          10000  avgt   20   9.847 ± 1.012  ns/op

ChainedKotlinBenchmark.channelChain_defaultDispatcher               16          10000  avgt   20   6.039 ± 0.826  ns/op
```

## Structured concurrency

### Dependency

Maven:

```xml

<dependency>
    <groupId>com.softwaremill.jox</groupId>
    <artifactId>structured</artifactId>
    <version>0.3.1</version>
</dependency>
```

Gradle:

```groovy
implementation 'com.softwaremill.jox:structured:0.3.1'
```

### Usage

#### Creating scopes and forking computations

```java
import java.util.concurrent.ExecutionException;

import static com.softwaremill.jox.structured.Scopes.supervised;

public class Demo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var result = supervised(scope -> {
            var f1 = scope.fork(() -> {
                Thread.sleep(500);
                return 5;
            });
            var f2 = scope.fork(() -> {
                Thread.sleep(1000);
                return 6;
            });
            return f1.join() + f2.join();
        });
        System.out.println("result = " + result);
    }
}
```

* the `supervised` scope will only complete once any forks started within complete as well
* in other words, it's guaranteed that no forks will remain running, after a `supervised` block completes
* `fork` starts a concurrently running computation, which can be joined in a blocking way. These computatioins are
  backed by virtual threads

#### Error handling in scopes

```java
import java.util.concurrent.ExecutionException;

import static com.softwaremill.jox.structured.Scopes.supervised;

public class Demo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var result = supervised(scope -> {
            var f1 = scope.fork(() -> {
                Thread.sleep(1000);
                return 6;
            });
            var f2 = scope.<Integer>fork(() -> {
                Thread.sleep(500);
                throw new RuntimeException("I can’t count to 5!");
            });
            return f1.join() + f2.join();
        });
        System.out.println("result = " + result);
    }
}
```

* an exception thrown from the scope's body, or from any of the forks, causes the scope to end
* any forks that are still running are then interrupted
* once all forks complete, an `JoxScopeExecutionException` is thrown by the `supervised` method
* the cause of the `JoxScopeExecutionException` is the original exception
* any other exceptions (e.g. `InterruptedExceptions`) that have been thrown while ending the scope, are added as
  suppressed

Jox implements the "let it crash" model. When an error occurs, the entire scope ends, propagating the exception higher,
so that it can be properly handled. Moreover, no detail is lost: all exceptions are preserved, either as causes, or
suppressed exceptions.

As `JoxScopeExecutionException` is unchecked, we introduced utility method called
`JoxScopeExecutionException#unwrapAndThrow`.
If the wrapped exception is instance of any of passed classes, this method unwraps original exception and throws it as
checked exception, `throws` signature forces exception handling.
If the wrapped exception is not instance of any of passed classes, **nothing happens**.
All suppressed exceptions are rewritten from `JoxScopeExecutionException`

**Note** `throws` signature points to the closest super class of passed arguments.
Method does **not** rethrow `JoxScopeExecutionException` by default.
So it is advised to manually rethrow it after calling `unwrapAndThrow` method.

e.g.

```java
import com.softwaremill.jox.structured.JoxScopeExecutionException;
import com.softwaremill.jox.structured.Scopes;

...
try {
    Scopes.supervised(scope -> {
        throw new TestException("x");
    });
} catch (JoxScopeExecutionException e) {
    e.unwrapAndThrow(OtherException.class, TestException.class, YetAnotherException.class);
    throw e;
}
...
```

#### Other types of scopes & forks

There are 4 types of forks:

* `fork`: daemon fork, supervised; when the scope's body ends, such forks are interrupted
* `forkUser`: user fork, supervised; when the scope's body ends, the scope's method waits until such a fork completes
  normally
* `forkUnsupervised`: daemon fork, unsupervised; any thrown exceptions don't cause the scope to end, but instead can be
  discovered when the fork is `.join`ed
* `forkCancellable`: daemon fork, unsupervised, which can be manually cancelled (interrupted)

There are also 2 types of scopes:

* `supervised`: the default scope, which ends when all forks user forks complete successfully, or when there's any
  exception in supervised scopes
* `unsupervised`: a scope where only unsupervised forks can be started

#### Running computations in parallel

```java
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.softwaremill.jox.structured.Par.par;

public class Demo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var result = par(List.of(() -> {
            Thread.sleep(500);
            return 5;
        }, () -> {
            Thread.sleep(1000);
            return 6;
        }));
        System.out.println("result = " + result);
    }
}
// result = [5, 6]
```

Uses `supervised` scopes underneath.

#### Racing computations

```java
import java.util.concurrent.ExecutionException;

import static com.softwaremill.jox.structured.Race.race;

public class Demo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var result = race(() -> {
            Thread.sleep(1000);
            return 10;
        }, () -> {
            Thread.sleep(500);
            return 5;
        });
        // result will be 5, the other computation will be interrupted on the Thread.sleep
        System.out.println("result = " + result);
    }
}
// result = 5
```

#### Timing out a computation

```java
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.softwaremill.jox.structured.Race.timeout;

public class Demo {
    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        var result = timeout(1000, () -> {
            Thread.sleep(500);
            return 5;
        });
        System.out.println("result = " + result);
    }
}
// result = 5
```

## (Hot) Streaming

### Dependency

Maven:

```xml

<dependency>
    <groupId>com.softwaremill.jox</groupId>
    <artifactId>channel-ops</artifactId>
    <version>0.3.1</version>
</dependency>
```

Gradle:

```groovy
implementation 'com.softwaremill.jox:channel-ops:0.3.1'
```

### Usage

Using this module you can run operations on streams which require starting background threads. To do that,
you need to pass an active concurrency scope (started using `supervised`) to the `SourceOps` constructor.

Each method from `SourceOps` causes a new fork (virtual thread) to be started, which starts running its logic
immediately (producing elements / consuming and transforming elements from the given source). Thus, this is an
implementation of "hot streams".

#### Creating streams

Sources from iterables, or tick-sources, can be created by calling methods on `SourceOps`:

```java
import java.util.concurrent.ExecutionException;

import static com.softwaremill.jox.structured.Scopes.supervised;

public class Demo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        supervised(scope -> {
            new SourceOps(scope)
                    .tick(500, "tick")
                    .toSource()
                    .forEach(v -> System.out.println(v));
            return null; // unreachable, as `tick` produces infinitely many elements
        });
    }
}
```

A tick-source can also be used in the usual way, by calling `.receive` on it, or by using it in `select`'s clauses.

#### Transforming streams

Streams can be transformed by calling the appropriate methods on the object returned by
`SourceOps.forSource(scope, source)`.

`collect` combines the functionality of `map` and `filter`: elements are mapped, and when the mapping function returns
`null`, the element is skipped:

```java
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.softwaremill.jox.structured.Scopes.supervised;

public class Demo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var result = supervised(scope -> new SourceOps(scope)
                .fromIterable(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
                .collect(n -> {
                    if (n % 2 == 0) return null;
                    else return n * 10;
                })
                .toSource().toList());
        System.out.println("result = " + result);
    }
}
// result = [10, 30, 50, 70, 90]
```

## (Lazy) Streaming - Flows

### Dependency

Maven:

```xml

<dependency>
    <groupId>com.softwaremill.jox</groupId>
    <artifactId>flows</artifactId>
    <version>tbd</version>
</dependency>
```

Gradle:

```groovy
implementation 'com.softwaremill.jox:flows:tbd'
```

### Usage

A `Flow<T>` describes an asynchronous data transformation pipeline. When run, it emits elements of type `T`.

Flows are lazy, evaluation (and any effects) happen only when the flow is run. Flows might be finite or infinite; in the
latter case running a flow never ends normally; it might be interrupted, though. Finally, any exceptions that occur when
evaluating the flow's logic will be thrown when running the flow, after any cleanup logic completes.

### Creating Flows

There are number of methods in the `Flows` class which allows to create a `Flow`.

```java
import java.time.Duration;

import com.softwaremill.jox.flows.Flows;

public class Demo {

    public static void main(String[] args) {
        Flows.fromValues(1, 2, 3); // a finite flow
        Flows.tick(Duration.ofSeconds(1), "x"); // an infinite flow emitting "x" every second
        Flows.iterate(0, i -> i + 1); // an infinite flow iterating from 0
    }
}
```

Note that creating a flow as above doesn't emit any elements, or execute any of the flow's logic. Only when run, the
elements are emitted and any effects that are part of the flow's stages happen.

Flows can also be created using `Channel` `Source`s:

```java
import java.util.concurrent.ExecutionException;

import com.softwaremill.jox.Channel;
import com.softwaremill.jox.flows.Flows;
import com.softwaremill.jox.structured.Scopes;

public class Demo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Channel<Integer> ch = new Channel<>(16);
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
}
```

Finally, flows can be created by providing arbitrary element-emitting logic:

```java
import com.softwaremill.jox.flows.Flows;

public class Demo {

    public static void main(String[] args) {
        Flows.usingEmit(emit -> {
            emit.apply(21);
            for (int i = 0; i < 5; i++) {
                emit.apply(i);
            }
            emit.apply(37);
        });
    }
}
```

The `FlowEmit` instance is used to emit elements by the flow, that is process them further, as defined by the downstream
pipeline. This method only completes once the element is fully processed, and it might throw exceptions in case there's
a processing error.

As part of the callback, you can create `Scope`, fork background computations or run other flows asynchronously.
However, take care **not** to share the `FlowEmit` instance across threads. That is, instances of `FlowEmit` are
thread-unsafe and should only be used on the calling thread.
The lifetime of `FlowEmit` should not extend over the duration of the invocation of `usingEmit`.

Any asynchronous communication should be best done with `Channel`s. You can then manually forward any elements received
from a channel to `emit`, or use e.g. `FlowEmit.channelToEmit`.

### Transforming flows: basics

Multiple transformation stages can be added to a flow, each time returning a new `Flow` instance, describing the
extended pipeline. As before, no elements are emitted or transformed until the flow is run, as flows are lazy. There's a
number of pre-defined transformation stages:

```java
import java.util.Map;

import com.softwaremill.jox.flows.Flows;

public class Demo {

    public static void main(String[] args) {
        Flows.fromValues(1, 2, 3, 5, 6)
                .map(i -> i * 2)
                .filter(i -> i % 2 == 0)
                .take(3)
                .zip(Flows.repeat("a number"))
                .interleave(Flows.repeat(Map.entry(0, "also a number")), 1, false);
    }
}
```

You can also define arbitrary element-emitting logic, using each incoming element using `.mapUsingEmit`, similarly to
`Flows.usingEmit` above.

### Running flows

Flows have to be run, for any processing to happen. This can be done with one of the `.run...` methods. For example:

```java
import java.time.Duration;

import com.softwaremill.jox.flows.Flows;

public class Demo {

    public static void main(String[] args) throws Exception {
        Flows.fromValues(1, 2, 3).runToList(); // List(1, 2, 3)
        Flows.fromValues(1, 2, 3).runForeach(System.out::println);
        Flows.tick(Duration.ofSeconds(1), "x").runDrain(); // never finishes
    }
}
```

Running a flow is a blocking operation. Unless asynchronous boundaries are present (explicit or implicit, more on this
below), the entire processing happens on the calling thread. For example such a pipeline:

```java
import com.softwaremill.jox.flows.Flows;

public class Demo {

    public static void main(String[] args) throws Exception {
        Flows.fromValues(1, 2, 3, 5, 6)
                .map(i -> i * 2)
                .filter(i -> i % 2 == 0)
                .runToList();
    }
}
```

Processes the elements one-by-one on the thread that is invoking the run method.

### Transforming flows: concurrency

A number of flow transformations introduces asynchronous boundaries. For example,
`.mapPar(int parallelism, Function<T,U> mappingFunction)` describes a flow,
which runs the pipeline defined so far in the background, emitting elements to a `channel`. Another `fork` reads these
elements and runs up to `parallelism` invocations of `mappingFunction` concurrently. Mapped elements are then emitted by
the returned flow.

Behind the scenes, a new concurrency `Scope` is created along with a number of forks. In case of any exceptions,
everything is cleaned up before the flow propagates the exceptions. The `.mapPar` logic ensures that any exceptions from
the preceding pipeline are propagated through the channel.

Some other stages which introduce concurrency include `.merge`, `.interleave`, `.groupedWithin` and `I/O` stages. The
created channels serve as buffers between the pipeline stages, and their capacity is defined by the `ScopedValue`
`Channel.BUFFER_SIZE` in the scope, or default `Channel.DEFAULT_BUFFER_SIZE` is used.

Explicit asynchronous boundaries can be inserted using `.buffer()`. This might be useful if producing the next element
to emit, and consuming the previous should run concurrently; or if the processing times of the consumer varies, and the
producer should buffer up elements.

### Interoperability with channels

Flows can be created from channels, and run to channels. For example:

```java
import java.util.Arrays;

import com.softwaremill.jox.Channel;
import com.softwaremill.jox.Source;
import com.softwaremill.jox.flows.Flows;
import com.softwaremill.jox.structured.Scopes;

public class Demo {

    public static void main(String[] args) throws Exception {
        Source<String> ch = getSource(args); // provide a source
        Scopes.supervised(scope -> {
            Source<String> output = ScopedValue.getWhere(Channel.BUFFER_SIZE, 5, () -> Flows.fromSource(ch)
                    .mapConcat(v -> Arrays.asList(v.split(" ")))
                    .filter(v -> v.startsWith("example"))
                    .runToChannel(scope));
        });
    }
}
```

The method above needs to be run within a concurrency scope, as `.runToChannel()` creates a background fork which runs
the pipeline described by the flow, and emits its elements onto the returned channel.

### Text transformations and I/O operations

For smooth operations on `byte[]`, we've created a wrapper class `ByteChunk`. And for smooth type handling we created a
dedicated `ByteFlow`, a subtype of `Flow<ByteChunk>`.
To be able to utilize text and I/O operations, you need to create or transform into `ByteFlow`. It can be created via
`Flows.fromByteArray` or `Flows.fromByteChunk`.
`Flow` containing `byte[]` or `ByteChunk` can be transformed by using `toByteFlow()` method. Any other flow can be
transformed by using `toByteFlow()` with mapping function.

#### Text operations

* `encodeUtf8` encodes a `Flow<String>` into a `ByteFlow`
* `linesUtf8` decodes a `ByteFlow` into a `Flow<String>`. Assumes that the input represents text with line breaks. The
  `String` elements emitted by resulting `Flow<String>` represent text lines.
* `decodeStringUtf8` to decode a `ByteFlow` into a `Flow<String>`, without handling line breaks, just processing input
  bytes as UTF-8 characters, even if a multi-byte character is divided into two chunks.

#### I/O Operations

* `runToInputStream(UnsupervisedScope scope)` runs given flow asynchronously into returned `InputStream`
* `runToOutputStream(OutputStream outputStream)` runs given flow into provided `OutputStream`
* `runToFile(Path path)` runs given flow into file. If file does not exist, it's created.

It is also possible to create Flow from `inputStream` or `path` using `Flows` factory methods.

### Logging

Jox does not have any integrations with logging libraries, but it provides a simple way to log elements emitted by flows
using the `.tap` method:

```java
import com.softwaremill.jox.flows.Flows;

public class Demo {

    public static void main(String[] args) throws Exception {
        Flows.fromValues(1, 2, 3)
                .tap(n -> System.out.printf("Received: %d%n", n))
                .runToList();
    }
}
```

### Reactive streams interoperability

#### Flow -> Publisher

A `Flow` can be converted to a `java.util.concurrent.Flow.Publisher` using the `.toPublisher` method.

This needs to be run within an concurrency `Scope`, as upon subscribing, a fork is created to run the publishing
process. Hence, the scope should remain active as long as the publisher is used.

Internally, elements emitted by the flow are buffered, using a buffer of capacity given by the `Channel.BUFFER_SIZE` in
scope.

To obtain a `org.reactivestreams.Publisher` instance, you'll need to add the `reactive-streams` dependency and
use `org.reactivestreams.FlowAdapters`.

#### Publisher -> Flow

A `java.util.concurrent.Flow.Publisher` can be converted to a `Flow` using `Flow.fromPublisher`.

Internally, elements published to the subscription are buffered, using a buffer of capacity given by the
`Channel.BUFFER_SIZE` in scope. That's also how many elements will be at most requested from the publisher at a time.

To convert a `org.reactivestreams.Publisher` instance, you'll need the same dependency as above and use
`org.reactivestreams.FlowAdapters`.

## Feedback

Is what we are looking for!

Let us know in the issues, or our [community forum](https://softwaremill.community/c/open-source/11).

## Further work

There's some interesting features which we're planning to work on. Check out
the [open issues](https://github.com/softwaremill/jox/issues)!

## Project sponsor

We offer commercial development services. [Contact us](https://softwaremill.com) to learn more!

## Copyright

Copyright (C) 2023-2024 SoftwareMill [https://softwaremill.com](https://softwaremill.com).
