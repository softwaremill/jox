# Jox

[Virtual-thread](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html) based safe concurrency & streaming
for Java 21. Open-source, Apache2 licensed.

Jox contains three main modules:

* Fast & scalable, completable [channels](channels.md), with Go-like `select`s
* Programmer-friendly [structured concurrency](structured.md)
* Finite & infinite streaming using [flows](flows.md), with reactive streams compatibility, (blocking) I/O integration
  and a high-level, "functional" API

Source code is [avaiable on GitHub](https://github.com/softwaremill/jox).

## A tour of Jox

Selectable [channels](channels.md):

```
var ch1 = Channel.<Integer>newBufferedDefaultChannel();
var ch2 = Channel.<Integer>newBufferedDefaultChannel();
var ch3 = Channel.<Integer>newBufferedDefaultChannel();

// send a value to two channels
ch2.send(29);
ch3.send(32);

var received = select(ch1.receiveClause(), ch2.receiveClause(), ch3.receiveClause());
```

A [flow](flows.md) with time-based & parallel processing:

```
var nats =
  Flows.unfold(0, i -> Optional.of(Map.entry(i+1, i+1)));
 
Flows.range(1, 100, 1)
  .throttle(1, Duration.ofSeconds(1))
  .mapPar(4, i -> {
    Thread.sleep(5000);
    var j = i*3;
    return j+1;
  })
  .filter(i -> i % 2 == 0)
  .zip(nats)
  .runForeach(System.out::println);
```

[Sructured concurrency](structured.md) scope:

```
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
```

## Sponsors

Development and maintenance of Jox is sponsored by [SoftwareMill](https://softwaremill.com), a software development and
consulting company. We help clients scale their business through software. Our areas of expertise include performant
backends, distributed systems, integrating data pipelines and ML/AI "science as a service".

[![](https://files.softwaremill.com/logo/logo.png "SoftwareMill")](https://softwaremill.com)

## Commercial Support

We offer commercial support for Jox and related technologies, as well as development services.
[Contact us](https://softwaremill.com/contact/) to learn more about our offer!

## Other materials

Articles:

* [Announcing jox: Fast and Scalable Channels in Java](https://softwaremill.com/announcing-jox-fast-and-scalable-channels-in-java/)
* [Go-like selects using jox channels in Java](https://softwaremill.com/go-like-selects-using-jox-channels-in-java/)
* [Jox 0.1: virtual-thread friendly channels for Java](https://softwaremill.com/jox-0-1-virtual-thread-friendly-channels-for-java/)
* [Programmer-friendly structured concurrency for Java](https://softwaremill.com/programmer-friendly-structured-concurrency-for-java/)
* [Java data processing using modern concurrent programming](https://softwaremill.com/java-data-processing-using-modern-concurrent-programming/)
* [Flows - simple Java asynchronous data processing in action](https://softwaremill.com/flows-simple-java-asynchronous-data-processing-in-action/)

Videos:

* [A 10-minute introduction to Jox Channels](https://www.youtube.com/watch?v=Ss9b1HpPDt0)
* [Passing control information through channels](https://www.youtube.com/watch?v=VjiCzaiRro8)

For a Scala version, see the [Ox project](https://github.com/softwaremill/ox).

## Table of contents

```{eval-rst}

.. toctree::
   :maxdepth: 2
   :caption: Jox
   
   channels
   flows
   structured
   contributing
