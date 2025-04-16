# Structured concurrency

Programmer-friendly structured concurrency scopes, building upon the lower-level API available as a preview in Java 21,
[JEP 453](https://openjdk.org/jeps/453).

Requires the current LTS release of Java - JDK 21 (won't work with newer versions).

Javadocs: [https://javadoc.io](https://javadoc.io/doc/com.softwaremill.jox/structured).

## Dependency

Maven:

```xml

<dependency>
    <groupId>com.softwaremill.jox</groupId>
    <artifactId>structured</artifactId>
    <version>0.4.0</version>
</dependency>
```

Gradle:

```groovy
implementation 'com.softwaremill.jox:structured:0.4.0'
```

## Creating scopes and forking computations

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

## Error handling in scopes

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

public class Demo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
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

There are also 2 types of scopes:

* `supervised`: the default scope, which ends when all forks user forks complete successfully, or when there's any
  exception in supervised scopes
* `unsupervised`: a scope where only unsupervised forks can be started

## Running computations in parallel

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

## Racing computations

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

## Timing out a computation

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
