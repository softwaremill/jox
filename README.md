# Jox

[![Ideas, suggestions, problems, questions](https://img.shields.io/badge/Discourse-ask%20question-blue)](https://softwaremill.community/c/open-source/11)
[![CI](https://github.com/softwaremill/jox/workflows/CI/badge.svg)](https://github.com/softwaremill/jox/actions?query=workflow%3A%22CI%22)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.jox/core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.jox/core)
[![javadoc](https://javadoc.io/badge2/com.softwaremill.jox/core/javadoc.svg)](https://javadoc.io/doc/com.softwaremill.jox/core)

Modern concurrency for Java 21+ (backed by virtual threads, see [Project Loom](https://openjdk.org/projects/loom/)).
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

## Channels

### Dependency

Maven:

```xml

<dependency>
    <groupId>com.softwaremill.jox</groupId>
    <artifactId>channels</artifactId>
    <version>0.3.0</version>
</dependency>
```

Gradle:

```groovy
implementation 'com.softwaremill.jox:channels:0.3.0'
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

The test results for version 0.3.0, run on an M1 Max MacBook Pro, with Java 21.0.1, are as follows:

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
    <version>0.3.0</version>
</dependency>
```

Gradle:

```groovy
implementation 'com.softwaremill.jox:structured:0.3.0'
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
* once all forks complete, an `ExecutionException` is thrown by the `supervised` method
* the cause of the `ExecutionException` is the original exception
* any other exceptions (e.g. `InterruptedExceptions`) that have been thrown while ending the scope, are added as
  suppressed

Jox implements the "let it crash" model. When an error occurs, the entire scope ends, propagating the exception higher,
so that it can be properly handled. Moreover, no detail is lost: all exceptions are preserved, either as causes, or
suppressed exceptions.

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

## Streaming

### Dependency

Maven:

```xml

<dependency>
    <groupId>com.softwaremill.jox</groupId>
    <artifactId>channel-ops</artifactId>
    <version>0.3.0</version>
</dependency>
```

Gradle:

```groovy
implementation 'com.softwaremill.jox:channel-ops:0.3.0'
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
