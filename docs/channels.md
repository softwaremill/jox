# Channels

Fast & scalable, completable channels, with Go-like `select`s. Inspired by the "Fast and Scalable Channels in Kotlin
Coroutines" [paper](https://arxiv.org/abs/2211.04986), and the
[Kotlin implementation](https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/common/src/channels/BufferedChannel.kt).

Requires Java 21+.

Javadocs: [https://javadoc.io](https://javadoc.io/doc/com.softwaremill.jox/channels).

## Dependency

Maven:

```xml

<dependency>
    <groupId>com.softwaremill.jox</groupId>
    <artifactId>channels</artifactId>
    <version>1.1.2</version>
</dependency>
```

Gradle:

```groovy
implementation 'com.softwaremill.jox:channels:1.1.2'
```

## Buffered channel

```java
import com.softwaremill.jox.Channel;

class Demo1 {
    public static void main(String[] args) throws InterruptedException {
        var ch = Channel.<Integer>newBufferedDefaultChannel();

        // send()-s won't block, as long as there's space in the buffer
        ch.send(1);
        System.out.println("Sent 1");
        ch.send(2);
        System.out.println("Sent 2");
        ch.send(3);
        System.out.println("Sent 3");

        System.out.println("Received: " + ch.receive());
        System.out.println("Received: " + ch.receive());
        System.out.println("Received: " + ch.receive());
        // the next receive() would block, since there are no more values in the channel
    }
}
```

## Rendezvous channel

```java
import com.softwaremill.jox.Channel;

class Demo2 {
    public static void main(String[] args) throws InterruptedException {
        // creates a rendezvous channel
        // (a sender & receiver must meet to pass a value: as if the buffer had size 0)
        var ch = Channel.<Integer>newRendezvousChannel();

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

Unlimited channels can be created with `Channel.newUnlimitedChannel()`. Such channels will never block on `send()`.

## Closing a channel

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
        var ch = Channel.<Integer>newBufferedChannel(3);

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

## Non-blocking operations

Non-blocking `trySend` and `tryReceive` methods are provided. They never block or suspend.

```java
var ch = Channel.<Integer>newBufferedChannel(1);

// returns true if sent, false if the buffer is full or no receiver is waiting
boolean s1 = ch.trySend(1);

// returns the value, or null if none is immediately available
Integer r1 = ch.tryReceive();
```

Both have `OrClosed` variants which return a `ChannelClosed` value instead of throwing an exception if the channel is closed.

Note: under contention, `trySend`/`tryReceive` may return `false`/`null` even when space or values are available. If you need guaranteed delivery, use `send()`/`receive()` instead.

## Selecting from multiple channels

The `select` method selects exactly one clause to complete. For example, you can receive a value from exactly one
channel:

```java
import com.softwaremill.jox.Channel;

import static com.softwaremill.jox.Select.select;

class Demo4 {
    public static void main(String[] args) throws InterruptedException {
        // creates a buffered channel (buffer of size 3)
        var ch1 = Channel.<Integer>newBufferedChannel(3);
        var ch2 = Channel.<Integer>newBufferedChannel(3);
        var ch3 = Channel.<Integer>newBufferedChannel(3);

        // send a value to two channels
        ch2.send(29);
        ch3.send(32);

        var received =
                select(ch1.receiveClause(), ch2.receiveClause(), ch3.receiveClause());

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
        var ch1 = Channel.<Integer>newBufferedChannel(1);
        var ch2 = Channel.<Integer>newBufferedChannel(1);

        ch1.send(12); // buffer is now full

        var sent = select(ch1.sendClause(13, () -> "1st"), ch2.sendClause(25, () -> "2nd"));

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
        var ch1 = Channel.<Integer>newBufferedChannel(3);
        var ch2 = Channel.<Integer>newBufferedChannel(3);

        var received = select(ch1.receiveClause(), ch2.receiveClause(), defaultClause(52));

        // prints: Received: 52
        System.out.println("Received: " + received);
    }
}
```

### Select with timeout

You can also select from multiple channels with a timeout using `selectWithin`. If none of the clauses can be completed
within the specified timeout, a `TimeoutException` is thrown:

```java
import com.softwaremill.jox.Channel;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static com.softwaremill.jox.Select.selectWithin;

class Demo7 {
    public static void main(String[] args) throws InterruptedException {
        var ch1 = Channel.<Integer>newBufferedChannel(3);
        var ch2 = Channel.<Integer>newBufferedChannel(3);

        try {
            // Wait up to 500 milliseconds for a value to be available
            var received = selectWithin(Duration.ofMillis(500), ch1.receiveClause(), ch2.receiveClause());
            System.out.println("Received: " + received);
        } catch (TimeoutException e) {
            // prints: Select timed out after 500 ms
            System.out.println("Select timed out after 500 ms");
        }
    }
}
```

Alternatively, you can use `selectOrClosedWithin` which returns a timeout value instead of throwing an exception:

```java
import com.softwaremill.jox.Channel;

import java.time.Duration;

import static com.softwaremill.jox.Select.selectOrClosedWithin;

class Demo8 {
    public static void main(String[] args) throws InterruptedException {
        var ch1 = Channel.<Integer>newBufferedChannel(3);
        var ch2 = Channel.<Integer>newBufferedChannel(3);

        var result = selectOrClosedWithin(Duration.ofMillis(500), "TIMEOUT",
                                          ch1.receiveClause(), ch2.receiveClause());

        if (result.equals("TIMEOUT")) {
            // prints: Select timed out
            System.out.println("Select timed out");
        } else {
            System.out.println("Received: " + result);
        }
    }
}
```

The timeout is implemented by creating a virtual thread that sends a timeout signal to an internal timeout channel after
the specified duration. It's guaranteed that this additional thread will be cleaned up before the select completes.

## Performance

The project includes benchmarks implemented using JMH - both for the `Channel`, as well as for some built-in Java
synchronisation primitives (queues), as well as the Kotlin channel implementation.

The test results for version 1.x, run on an M1 Max MacBook Pro, with Java 21.1.2, are as follows:

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
