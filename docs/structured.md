# Structured concurrency

Programmer-friendly structured concurrency scopes, building upon the lower-level API available as a preview in Java 25,
[JEP 505](https://openjdk.org/jeps/505).

Requires Java 25 (current LTS).

Javadocs: [https://javadoc.io](https://javadoc.io/doc/com.softwaremill.jox/structured).

## Dependency

Maven:

```xml

<dependency>
    <groupId>com.softwaremill.jox</groupId>
    <artifactId>structured</artifactId>
    <version>0.4.1</version>
</dependency>
```

Gradle:

```groovy
implementation 'com.softwaremill.jox:structured:0.4.1'
```

## Creating scopes and forking computations

```java
import static com.softwaremill.jox.structured.Scopes.supervised;

void main(String[] args) throws InterruptedException {
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
    IO.println("result = " + result);
}
```

* the `supervised` scope will only complete once all forks started within complete as well
* in other words, it's guaranteed that no forks will remain running, after a `supervised` block completes
* `fork` starts a concurrently running computation, which can be joined in a blocking way. These computations are
  backed by virtual threads

## Error handling in scopes

```java
import static com.softwaremill.jox.structured.Scopes.supervised;

void main(String[] args) throws InterruptedException {
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
    IO.println("result = " + result);
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
`JoxScopeExecutionException#unwrapAndThrow`. If the wrapped exception is an instance of any of the passed classes, this
method unwraps original exception and throws it as checked exception; then the `throws` signature forces exception
handling. If the wrapped exception is not instance of any of the passed classes, **nothing happens**. All suppressed
exceptions from `JoxScopeExecutionException` are added as suppressed to the unwrapped one.

**Note** `throws` signature points to the closest super class of passed arguments.
Method does **not** rethrow `JoxScopeExecutionException` by default.
So it is advised to manually rethrow it after calling `unwrapAndThrow` method, e.g.:

```java
import com.softwaremill.jox.structured.JoxScopeExecutionException;
import com.softwaremill.jox.structured.Scopes;

void main(String[] args) throws InterruptedException {
    /* ... */
    try {
        Scopes.supervised(scope -> {
            throw new TestException("x");
        });
    } catch (JoxScopeExecutionException e) {
        e.unwrapAndThrow(OtherException.class, TestException.class, YetAnotherException.class);
        throw e;
    }
    /* ... */
}
```

## Other types of scopes & forks

There are 4 types of forks:

* `fork`: daemon fork, supervised; when the scope's body ends, such forks are interrupted
* `forkUser`: user fork, supervised; when the scope's body ends, the scope's method waits until such a fork completes
  normally
* `forkUnsupervised`: daemon fork, unsupervised; any thrown exceptions don't cause the scope to end, but instead can be
  discovered when the fork is `.join`ed
* `forkCancellable`: daemon fork, unsupervised, which can be manually cancelled (interrupted)

## Running computations in parallel

```java
import java.util.List;

import static com.softwaremill.jox.structured.Par.par;

void main(String[] args) throws InterruptedException {
    var result = par(List.of(() -> {
        Thread.sleep(500);
        return 5;
    }, () -> {
        Thread.sleep(1000);
        return 6;
    }));
    IO.println("result = " + result);
}
// result = [5, 6]
```

Uses `supervised` scopes underneath.

## Racing computations

```java
import static com.softwaremill.jox.structured.Race.race;

void main(String[] args) throws InterruptedException {
    var result = race(() -> {
        Thread.sleep(1000);
        return 10;
    }, () -> {
        Thread.sleep(500);
        return 5;
    });
    // result will be 5, the other computation will be interrupted on the Thread.sleep
    IO.println("result = " + result);
}
// result = 5
```

## Timing out a computation

```java
import java.util.concurrent.TimeoutException;

import static com.softwaremill.jox.structured.Race.timeout;

void main(String[] args) throws InterruptedException, TimeoutException {
    var result = timeout(1000, () -> {
        Thread.sleep(500);
        return 5;
    });
    IO.println("result = " + result);
}
// result = 5
```

## Comparing with Java's structured concurrency (JEP 505)

Java 21 and further releases include previews of a structured concurrency API. The latest version of the proposal is in
[JEP 505](https://openjdk.org/jeps/505). How does it compare with Jox's structured concurrency?

Let's examine a simple example of parallelizing two computations, first using JEP 505:

```java
Response handle() throws InterruptedException {
    try (var scope = StructuredTaskScope.open()) {
        Subtask<String> user = scope.fork(() -> findUser());
        Subtask<Integer> order = scope.fork(() -> fetchOrder());

        scope.join();

        return new Response(user.get(), order.get());
    }
}
```

and using Jox:

```java
Response handle() throws InterruptedException {
    return supervised(scope -> {
        Fork<String> user = scope.fork(() -> findUser());
        Fork<Integer> order = scope.fork(() -> fetchOrder());

        return new Response(user.join(), order.join());
    });
}
```

Both implement the same logic: run `findUser` and `fetchOrder` in parallel, and combine their results. Any failure
interrupts the other tasks, and when they terminate, propagates the exception.

How are they different?

In the JEP variant, an additional `scope.join()` call is needed, which is absent when using the Jox API.

On one hand, this forces structured concurrency usages using the JEP to have a fixed structure: fork - join - get
results.

The Jox variant can have arbitrary structure (mixed, multiple forks & joins), which is more flexible; and also has one
less opportunity for misuse (in the JEP, you might forget to call `scope.join()`).

On the other hand, the Jox variant starts an extra virtual thread (a "supervisor") and is less "direct": note that we
need to return the result of the `supervised` call, while the JEP simply uses try-with-resources and runs the main body
of the scope on the calling thread.
