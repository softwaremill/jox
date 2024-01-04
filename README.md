# jox

Fast and Scalable Channels in Java.

Designed to be used with [Project Loom](https://openjdk.org/projects/loom/).

Inspired by the "Fast and Scalable Channels in Kotlin Coroutines" [paper](https://arxiv.org/abs/2211.04986), and
the [Kotlin implementation](https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/common/src/channels/BufferedChannel.kt).

Articles:

* [Announcing jox: Fast and Scalable Channels in Java](https://softwaremill.com/announcing-jox-fast-and-scalable-channels-in-java/).

## Dependencies

Maven:

```xml

<dependency>
    <groupId>com.softwaremill.jox</groupId>
    <artifactId>core</artifactId>
    <version>0.0.3</version>
</dependency>
```

Gradle:

```groovy
implementation 'com.softwaremill.jox:core:0.0.3'
```

SBT:

```scala
libraryDependencies += "com.softwaremill.jox" % "core" % "0.0.3"
```

## Usage

### Rendezvous channel

```java
import com.softwaremill.jox.Channel;

class Demo1 {
    public static void main(String[] args) throws InterruptedException {
        // creates a rendezvous channel
        // (buffer of size 0 - a sender & receiver must meet to pass a value)
        var ch = new Channel<Integer>(0);

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

### Buffered channel

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

### Closing a channel

Channels can be closed, either because the source is `done` with sending values, or when there's an `error` while
the sink processes the received values.

`send()` and `receive()` will throw a `ChannelClosedException` when the channel is closed. Alternatively, you can
use the `sendSafe()` and `receiveSafe()` methods, which return either a `ChannelClosed` value (reason of closure),
or `null` / the received value.

Channels can also be inspected whether they are closed, using the `isClosed()`, `isDone()` and `isError()` methods.

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
        System.out.println("Received: " + ch.receiveSafe());
        // prints: Received: ChannelDone[]
        System.out.println("Received: " + ch.receiveSafe());
    }
}
```

### Selecting from multiple channels

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

## Performance

The project includes benchmarks implemented using JMH - both for the `Channel`, as well as for some built-in Java
synchronisation primitives (queues), as well as the Kotlin channel implementation.

The test results for version 0.0.1, run on an M1 Max MacBook Pro, with Java 21.0.1, are as follows:

```
Benchmark                                                          (capacity)  Mode  Cnt     Score     Error  Units

// jox
RendezvousBenchmark.channel                                               N/A  avgt   30   176.499 ±  14.964  ns/op
RendezvousBenchmark.channel:receiveFromChannel                            N/A  avgt   30   176.499 ±  14.964  ns/op
RendezvousBenchmark.channel:sendToChannel                                 N/A  avgt   30   176.499 ±  14.964  ns/op
RendezvousBenchmark.channel_iterative                                     N/A  avgt   30   209.041 ±  30.397  ns/op

BufferedBenchmark.channel                                                   1  avgt   30   177.547 ±  14.626  ns/op
BufferedBenchmark.channel:receiveFromChannel                                1  avgt   30   177.547 ±  14.626  ns/op
BufferedBenchmark.channel:sendToChannel                                     1  avgt   30   177.547 ±  14.626  ns/op
BufferedBenchmark.channel                                                  10  avgt   30   135.838 ±  14.578  ns/op
BufferedBenchmark.channel:receiveFromChannel                               10  avgt   30   135.838 ±  14.578  ns/op
BufferedBenchmark.channel:sendToChannel                                    10  avgt   30   135.838 ±  14.578  ns/op
BufferedBenchmark.channel                                                 100  avgt   30    92.837 ±  13.936  ns/op
BufferedBenchmark.channel:receiveFromChannel                              100  avgt   30    92.837 ±  13.936  ns/op
BufferedBenchmark.channel:sendToChannel                                   100  avgt   30    92.836 ±  13.935  ns/op
BufferedBenchmark.channel_iterative                                         1  avgt   30   185.138 ±  14.382  ns/op
BufferedBenchmark.channel_iterative                                        10  avgt   30   126.594 ±  12.089  ns/op
BufferedBenchmark.channel_iterative                                       100  avgt   30    83.534 ±   6.540  ns/op

// java
RendezvousBenchmark.exchanger                                             N/A  avgt   30   177.630 ± 152.388  ns/op
RendezvousBenchmark.exchanger:exchange1                                   N/A  avgt   30   177.630 ± 152.388  ns/op
RendezvousBenchmark.exchanger:exchange2                                   N/A  avgt   30   177.630 ± 152.388  ns/op
RendezvousBenchmark.synchronous_queue                                     N/A  avgt   30   978.826 ± 188.831  ns/op
RendezvousBenchmark.synchronous_queue:putToSynchronousQueue               N/A  avgt   30   978.826 ± 188.830  ns/op
RendezvousBenchmark.synchronous_queue:takeFromSynchronousQueue            N/A  avgt   30   978.825 ± 188.832  ns/op

BufferedBenchmark.array_blocking_queue                                      1  avgt   30  2266.799 ± 231.198  ns/op
BufferedBenchmark.array_blocking_queue:putToArrayBlockingQueue              1  avgt   30  2266.798 ± 231.197  ns/op
BufferedBenchmark.array_blocking_queue:takeFromArrayBlockingQueue           1  avgt   30  2266.799 ± 231.199  ns/op
BufferedBenchmark.array_blocking_queue                                     10  avgt   30   450.796 ±  93.496  ns/op
BufferedBenchmark.array_blocking_queue:putToArrayBlockingQueue             10  avgt   30   450.795 ±  93.495  ns/op
BufferedBenchmark.array_blocking_queue:takeFromArrayBlockingQueue          10  avgt   30   450.797 ±  93.497  ns/op
BufferedBenchmark.array_blocking_queue                                    100  avgt   30   147.962 ±   9.743  ns/op
BufferedBenchmark.array_blocking_queue:putToArrayBlockingQueue            100  avgt   30   147.962 ±   9.743  ns/op
BufferedBenchmark.array_blocking_queue:takeFromArrayBlockingQueue         100  avgt   30   147.962 ±   9.743  ns/op

// kotlin
RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher               N/A  avgt   30  108.338  ±   0.538  ns/op

BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher                   1  avgt   30   86.614  ±   0.784  ns/op
BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher                  10  avgt   30   40.153  ±   0.221  ns/op
BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher                 100  avgt   30   26.764  ±   0.022  ns/op
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
