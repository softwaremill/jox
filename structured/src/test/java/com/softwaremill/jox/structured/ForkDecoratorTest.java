package com.softwaremill.jox.structured;

import static com.softwaremill.jox.structured.Scopes.supervised;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.Test;

public class ForkDecoratorTest {

    /** Carries the value bound when a fork is started into the fork's body. */
    private static class PropagatingDecorator implements Decorator {
        @Override
        public <T> Callable<T> decorate(Callable<T> f) {
            var captured = VALUE.isBound() ? VALUE.get() : null;
            return () ->
                    captured == null ? f.call() : ScopedValue.where(VALUE, captured).call(f::call);
        }
    }

    private static final ScopedValue<String> VALUE = ScopedValue.newInstance();

    private static String valueInFork() {
        return VALUE.isBound() ? VALUE.get() : null;
    }

    @Test
    void shouldNotSeeAValueBoundInTheScopeBodyWithoutADecorator() throws Exception {
        var seen =
                supervised(
                        scope ->
                                ScopedValue.where(VALUE, "bound-in-body")
                                        .call(
                                                () ->
                                                        scope.fork(ForkDecoratorTest::valueInFork)
                                                                .join()));

        // forks are started by the thread running `supervised`, which is outside the binding
        assertNull(seen);
    }

    @Test
    void shouldSeeAValueBoundInTheScopeBodyWithADecorator() throws Exception {
        var seen =
                ScopedValue.where(Scope.FORK_DECORATOR, new PropagatingDecorator())
                        .call(
                                () ->
                                        supervised(
                                                scope ->
                                                        ScopedValue.where(VALUE, "bound-in-body")
                                                                .call(
                                                                        () ->
                                                                                scope.fork(
                                                                                                ForkDecoratorTest
                                                                                                        ::valueInFork)
                                                                                        .join())));

        assertEquals("bound-in-body", seen);
    }

    @Test
    void shouldDecorateEachForkWithTheValueBoundWhenItWasStarted() throws Exception {
        var seen =
                ScopedValue.where(Scope.FORK_DECORATOR, new PropagatingDecorator())
                        .call(
                                () ->
                                        supervised(
                                                scope -> {
                                                    List<String> results =
                                                            new CopyOnWriteArrayList<>();
                                                    for (var v : List.of("first", "second")) {
                                                        ScopedValue.where(VALUE, v)
                                                                .call(
                                                                        () ->
                                                                                results.add(
                                                                                        scope.fork(
                                                                                                        ForkDecoratorTest
                                                                                                                ::valueInFork)
                                                                                                .join()));
                                                    }
                                                    return results;
                                                }));

        // each fork carries the value bound at its own start, not one value for the whole scope
        assertEquals(List.of("first", "second"), seen);
    }

    @Test
    void shouldDecorateForksOfNestedScopes() throws Exception {
        var seen =
                ScopedValue.where(Scope.FORK_DECORATOR, new PropagatingDecorator())
                        .call(
                                () ->
                                        supervised(
                                                outer ->
                                                        ScopedValue.where(VALUE, "bound-in-body")
                                                                .call(
                                                                        () ->
                                                                                outer.forkUser(
                                                                                                () ->
                                                                                                        supervised(
                                                                                                                inner ->
                                                                                                                        inner.forkUser(
                                                                                                                                        ForkDecoratorTest
                                                                                                                                                ::valueInFork)
                                                                                                                                .join()))
                                                                                        .join())));

        assertEquals("bound-in-body", seen);
    }

    @Test
    void shouldDecorateAllForkVariants() throws Exception {
        var decorated = new CopyOnWriteArrayList<String>();
        Decorator counting =
                new Decorator() {
                    @Override
                    public <T> Callable<T> decorate(Callable<T> f) {
                        decorated.add("decorated");
                        return f;
                    }
                };

        ScopedValue.where(Scope.FORK_DECORATOR, counting)
                .call(
                        () ->
                                supervised(
                                        scope -> {
                                            scope.fork(() -> 1).join();
                                            scope.forkUser(() -> 1).join();
                                            scope.forkUnsupervised(() -> 1).join();
                                            scope.forkCancellable(() -> 1).join();
                                            return null;
                                        }));

        // the scope's main body fork, plus the four above, plus the body of the nested scope
        // which forkCancellable starts internally
        assertEquals(6, decorated.size());
    }
}
