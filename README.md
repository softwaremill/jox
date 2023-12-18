# Jox

Fast and scalable channels for Java.

Designed to be used with [Project Loom](https://openjdk.org/projects/loom/).

Inspired by the "Fast and Scalable Channels in Kotlin Coroutines" [paper](https://arxiv.org/abs/2211.04986), and
the [Kotlin implementation](https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/common/src/channels/BufferedChannel.kt).

## Dependencies

Maven dependency:

```xml

<dependency>
    <groupId>com.softwaremill.jox</groupId>
    <artifactId>core</artifactId>
    <version>0.0.1</version>
</dependency>
```

Gradle:

```groovy
implementation 'com.softwaremill.jox:core:0.0.1'
```

SBT:

```scala
libraryDependencies += "com.softwaremill.jox" % "core" % "0.0.1"
```

## Usage

### Rendezvous channel

```java
import com.softwaremill.jox.Channel;

class Demo1 {
    public static void main(String[] args) {
        // creates a rendezvous channel (buffer of size 0 - a sender & receiver must meet to pass a value)
        var ch = new Channel<Integer>(0);

        new Thread.ofVirtual().start(() -> {
            // send() will block, until there's a matching receive()
            ch.send(1);
            System.out.println("Sent 1");
            ch.send(2);
            System.out.println("Sent 2");
            ch.send(3);
            System.out.println("Sent 3");
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
    public static void main(String[] args) {
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
    public static void main(String[] args) {
        // creates a buffered channel (buffer of size 3)
        var ch = new Channel<Integer>(3);

        // send()-s won't block
        ch.send(1);
        ch.done();

        // prints: Received: 1
        System.out.println("Received: " + ch.receiveSafe());
        // prints: Received: ChannelClosed.ChannelDone
        System.out.println("Received: " + ch.receiveSafe());
    }
}
```

## Performance

The project includes benchmarks implemented using JMH - both for the `Channel`, as well as for some built-in Java
synchronisation primitives (queues), as well as the Kotlin channel implementation.

The test results for version 0.0.1, run on an M1 Max MacBook Pro, with Java 21.0.1, are as follows:

```

```

## Feedback

Is what we are looking for!

Let us know in the issues, or our [community forum](https://softwaremill.community/c/open-source/11).

## Further work

Comparing to the Kotlin implementation, there's a number of features missing. Most notably, we plan to work on
Go-like `select`s next! 
