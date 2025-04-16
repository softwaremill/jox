# Jox

[![Ideas, suggestions, problems, questions](https://img.shields.io/badge/Discourse-ask%20question-blue)](https://softwaremill.community/c/open-source/11)
[![CI](https://github.com/softwaremill/jox/workflows/CI/badge.svg)](https://github.com/softwaremill/jox/actions?query=workflow%3A%22CI%22)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.jox/channels/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.jox/channels)
[![javadoc](https://javadoc.io/badge2/com.softwaremill.jox/channels/javadoc.svg)](https://javadoc.io/doc/com.softwaremill.jox/channels)

[Virtual-thread](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html) based safe concurrency & streaming
for Java 21.

Includes:

* Fast & scalable, completable channels, with Go-like `select`s
* Programmer-friendly structured concurrency
* Finite & infinite streaming using flows, with reactive streams compatibility, (blocking) I/O integration and a
  high-level, “functional” API

Find out more in the documentation available at [jox.softwaremill.com](https://jox.softwaremill.com/).

## A tour of Jox

Selectable [channels](https://jox.softwaremill.com/latest/channels.html):

```
var ch1 = Channel.<Integer>newBufferedDefaultChannel();
var ch2 = Channel.<Integer>newBufferedDefaultChannel();
var ch3 = Channel.<Integer>newBufferedDefaultChannel();

// send a value to two channels
ch2.send(29);
ch3.send(32);

var received = select(ch1.receiveClause(), ch2.receiveClause(), ch3.receiveClause());
```

A [flow](https://jox.softwaremill.com/latest/flows.html) with time-based & parallel processing:

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

[Sructured concurrency](https://jox.softwaremill.com/latest/structured.html) scope:

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

## Feedback

Is what we are looking for!

Let us know in the issues, or our [community forum](https://softwaremill.community/c/open-source/11).

## Project sponsor

We offer commercial development services. [Contact us](https://softwaremill.com) to learn more!

## Copyright

Copyright (C) 2023-2025 SoftwareMill [https://softwaremill.com](https://softwaremill.com).
