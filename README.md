# jox

Fast and Scalable Channels in Java. Designed to be used with Java 21+ and virtual threads,
see [Project Loom](https://openjdk.org/projects/loom/) (although the `core` module can be used with Java 17+).

Inspired by the "Fast and Scalable Channels in Kotlin Coroutines" [paper](https://arxiv.org/abs/2211.04986), and
the [Kotlin implementation](https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/common/src/channels/BufferedChannel.kt).

Articles:

* [Announcing jox: Fast and Scalable Channels in Java](https://softwaremill.com/announcing-jox-fast-and-scalable-channels-in-java/)
* [Go-like selects using jox channels in Java](https://softwaremill.com/go-like-selects-using-jox-channels-in-java/)

## Dependencies

Maven:

```xml

<dependency>
    <groupId>com.softwaremill.jox</groupId>
    <artifactId>core</artifactId>
    <version>0.0.7</version>
</dependency>
```

Gradle:

```groovy
implementation 'com.softwaremill.jox:core:0.0.7'
```

SBT:

```scala
libraryDependencies += "com.softwaremill.jox" % "core" % "0.0.7"
```

## Usage

### Rendezvous channel

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

Unlimited channels can be created with `Channel.newUnlimitedChannel()`. Such channels will never block on send().

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

### Selecting from "done" channels

Receive clauses, for which channels are "done", will be skipped, and `select` with restart (as long as there are any
clauses left). This is motivated by the fact that a "done" channel is not in an error state, but signals that there are
no more values; while there might be more values available from other clauses.

Optionally, clauses created with `Channel.receiveOrDoneClause`, will cause `select` to throw/ return when the associated
channel is done, bypassing the behavior described above.

## Performance

The project includes benchmarks implemented using JMH - both for the `Channel`, as well as for some built-in Java
synchronisation primitives (queues), as well as the Kotlin channel implementation.

The test results for version 0.0.7, run on an M1 Max MacBook Pro, with Java 21.0.1, are as follows:

```
Benchmark                                                      (capacity)  (chainLength)  Mode  Cnt     Score     Error  Units

// jox - single channel
BufferedBenchmark.channel                                               1            N/A  avgt   20   210.366 ±  18.979  ns/op
BufferedBenchmark.channel                                              10            N/A  avgt   20   148.691 ±  25.368  ns/op
BufferedBenchmark.channel                                             100            N/A  avgt   20   149.499 ±  22.495  ns/op

RendezvousBenchmark.channel                                           N/A            N/A  avgt   20   187.940 ±   8.783  ns/op

// kotlin - single channel
BufferedKotlinBenchmark.channel_defaultDispatcher                       1            N/A  avgt   30    85.027 ±  0.709  ns/op
BufferedKotlinBenchmark.channel_defaultDispatcher                      10            N/A  avgt   30    40.095 ±  0.452  ns/op
BufferedKotlinBenchmark.channel_defaultDispatcher                     100            N/A  avgt   30    26.879 ±  0.063  ns/op

RendezvousKotlinBenchmark.channel_defaultDispatcher                   N/A            N/A  avgt   30   116.664 ± 10.099  ns/op

// jox - selects
SelectBenchmark.selectWithSingleClause                                N/A            N/A  avgt   20   353.074 ±  27.860  ns/op
SelectBenchmark.selectWithTwoClauses                                  N/A            N/A  avgt   20   651.050 ±  31.037  ns/op

// kotlin - selects
SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher        N/A            N/A  avgt   30   169.823 ±   1.250  ns/op
SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher          N/A            N/A  avgt   30   227.413 ±   2.659  ns/op

// java built-in - single queue
BufferedBenchmark.arrayBlockingQueue                                    1            N/A  avgt   20  2447.455 ± 427.354  ns/op
BufferedBenchmark.arrayBlockingQueue                                   10            N/A  avgt   20   546.227 ±  96.690  ns/op
BufferedBenchmark.arrayBlockingQueue                                  100            N/A  avgt   20   125.287 ±   4.387  ns/op

RendezvousBenchmark.exchanger                                         N/A            N/A  avgt   20   106.114 ±  20.360  ns/op
RendezvousBenchmark.synchronousQueue                                  N/A            N/A  avgt   20   869.988 ± 101.291  ns/op

// jox - multi channel
ChainedBenchmark.channelChain                                           0            100  avgt   20   225.370 ±   4.693  ns/op
ChainedBenchmark.channelChain                                           0           1000  avgt   20   173.997 ±   4.160  ns/op
ChainedBenchmark.channelChain                                           0          10000  avgt   20   160.097 ±   4.520  ns/op
ChainedBenchmark.channelChain                                         100            100  avgt   20     8.377 ±   0.133  ns/op
ChainedBenchmark.channelChain                                         100           1000  avgt   20     6.147 ±   0.054  ns/op
ChainedBenchmark.channelChain                                         100          10000  avgt   20     7.942 ±   0.447  ns/op
                                                                                                                
// kotlin - multi channel                                                                                       
ChainedKotlinBenchmark.channelChain_defaultDispatcher                   0            100  avgt   30    96.106 ±   1.247  ns/op
ChainedKotlinBenchmark.channelChain_defaultDispatcher                   0           1000  avgt   30    74.858 ±   0.810  ns/op
ChainedKotlinBenchmark.channelChain_defaultDispatcher                   0          10000  avgt   30    72.894 ±   0.787  ns/op
ChainedKotlinBenchmark.channelChain_defaultDispatcher                 100            100  avgt   30     5.164 ±   0.104  ns/op
ChainedKotlinBenchmark.channelChain_defaultDispatcher                 100           1000  avgt   30     4.157 ±   0.029  ns/op
ChainedKotlinBenchmark.channelChain_defaultDispatcher                 100          10000  avgt   30     4.965 ±   0.043  ns/op
ChainedKotlinBenchmark.channelChain_eventLoop                           0            100  avgt   30    70.484 ±   0.431  ns/op
ChainedKotlinBenchmark.channelChain_eventLoop                           0           1000  avgt   30    98.400 ±   1.003  ns/op
ChainedKotlinBenchmark.channelChain_eventLoop                           0          10000  avgt   30    92.579 ±   1.650  ns/op
ChainedKotlinBenchmark.channelChain_eventLoop                         100            100  avgt   30    27.052 ±   0.121  ns/op
ChainedKotlinBenchmark.channelChain_eventLoop                         100           1000  avgt   30    25.982 ±   0.111  ns/op
ChainedKotlinBenchmark.channelChain_eventLoop                         100          10000  avgt   30    27.276 ±   0.316  ns/op
                                                                                                                
// java built-in - multi queues                                                                                 
ChainedBenchmark.queueChain                                             0            100  avgt   20   186.677 ±   2.564  ns/op
ChainedBenchmark.queueChain                                             0           1000  avgt   20   108.954 ±  13.825  ns/op
ChainedBenchmark.queueChain                                             0          10000  avgt   20   101.643 ±  10.526  ns/op
ChainedBenchmark.queueChain                                           100            100  avgt   20     7.933 ±   0.546  ns/op
ChainedBenchmark.queueChain                                           100           1000  avgt   20     5.281 ±   0.261  ns/op
ChainedBenchmark.queueChain                                           100          10000  avgt   20     5.798 ±   0.058  ns/op
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
