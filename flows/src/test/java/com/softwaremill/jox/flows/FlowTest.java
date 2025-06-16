package com.softwaremill.jox.flows;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.softwaremill.jox.ChannelError;
import com.softwaremill.jox.Source;
import com.softwaremill.jox.structured.JoxScopeExecutionException;
import com.softwaremill.jox.structured.Scopes;

class FlowTest {

    @Test
    void shouldRunForEach() throws Throwable {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3);
        List<Integer> results = new ArrayList<>();

        // when
        flow.runForeach(results::add);

        // then
        assertEquals(List.of(1, 2, 3), results);
    }

    @Test
    void shouldRunToList() throws Throwable {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3);

        // when
        List<Integer> results = flow.runToList();

        // then
        assertEquals(List.of(1, 2, 3), results);
    }

    @Test
    void shouldThrowExceptionForFailedFlow() {
        assertThrows(
                IllegalStateException.class,
                () -> {
                    Flows.failed(new IllegalStateException())
                            .runFold(0, (acc, n) -> Integer.valueOf(acc.toString() + n));
                });
    }

    @Test
    void shouldThrowExceptionThrownInFunctionF() {
        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () -> {
                            Flows.fromValues(1)
                                    .runFold(
                                            0,
                                            (_, _) -> {
                                                throw new RuntimeException(
                                                        "Function `f` is broken");
                                            });
                        });
        assertEquals("Function `f` is broken", thrown.getMessage());
    }

    @Test
    void shouldReturnZeroValueFromFoldOnEmptySource() throws Exception {
        assertEquals(0, Flows.empty().runFold(0, (acc, n) -> Integer.valueOf(acc.toString() + n)));
    }

    @Test
    void shouldReturnFoldOnNonEmptyFold() throws Exception {
        assertEquals(3, Flows.fromValues(1, 2).runFold(0, Integer::sum));
    }

    @Test
    void shouldTakeFromSimpleFlow() throws Exception {
        Flow<Integer> f = Flows.fromValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<Integer> result = f.take(5).runToList();
        assertEquals(List.of(1, 2, 3, 4, 5), result);
    }

    @Test
    void shouldTakeFromAsyncFlow() throws Exception {
        Flow<Integer> f = Flows.fromValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).buffer(16);
        List<Integer> result = f.take(5).runToList();
        assertEquals(List.of(1, 2, 3, 4, 5), result);
    }

    @Test
    void shouldTakeAllIfFlowEndsSooner() throws Exception {
        Flow<Integer> f = Flows.fromValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<Integer> result = f.take(50).runToList();
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), result);
    }

    @Test
    void shouldWorkWithASingleAsyncBoundary() throws Throwable {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3, 4, 5).buffer(3);

        // when
        List<Integer> integers = flow.runToList();

        // then
        assertEquals(List.of(1, 2, 3, 4, 5), integers);
    }

    @Test
    void shouldWorkWithMultipleAsyncBoundaries() throws Throwable {
        // given
        Flow<Integer> flow =
                Flows.fromValues(1, 2, 3, 4, 5)
                        .buffer(3)
                        .map(i -> i * 2)
                        .buffer(2)
                        .map(i -> i + 1)
                        .buffer(5);

        // when
        List<Integer> integers = flow.runToList();

        // then
        assertEquals(List.of(3, 5, 7, 9, 11), integers);
    }

    @Test
    void shouldPropagateErrorsWhenUsingBuffer() {
        var exception =
                assertThrows(
                        JoxScopeExecutionException.class,
                        () -> {
                            Flows.fromValues(1, 2, 3)
                                    .map(
                                            _ -> {
                                                throw new IllegalStateException();
                                            })
                                    .buffer(5)
                                    .runToList();
                        });
        assertInstanceOf(IllegalStateException.class, exception.getCause().getCause());
    }

    @Test
    void shouldFilter() throws Throwable {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3, 4, 5);
        List<Integer> results = new ArrayList<>();

        // when
        flow.filter(i -> i % 2 == 0).runForeach(results::add);

        // then
        assertEquals(List.of(2, 4), results);
    }

    @Test
    void shouldTap() throws Throwable {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3);
        List<Integer> results = new ArrayList<>();

        // when
        flow.tap(results::add).map(i -> i * 2).runForeach(_ -> {});

        // then
        assertEquals(List.of(1, 2, 3), results);
    }

    @Test
    void shouldNotIntersperseWithInjectOnlyOverEmptySource() throws Exception {
        // given
        Flow<String> f = Flows.empty();

        // when & then
        assertEquals(List.of(), f.intersperse(", ").runToList());
    }

    @Test
    void shouldNotIntersperseWithInjectOnlyOverSourceWithOneElement() throws Exception {
        // given
        Flow<String> f = Flows.fromValues("foo");

        // when & then
        assertEquals(List.of("foo"), f.intersperse(", ").runToList());
    }

    @Test
    void shouldIntersperseWithInjectOnlyOverSourceWithMultipleElements() throws Exception {
        // given
        Flow<String> f = Flows.fromValues("foo", "bar");

        // when & then
        assertEquals(List.of("foo", ", ", "bar"), f.intersperse(", ").runToList());
    }

    @Test
    void shouldIntersperseWithStartInjectAndEndOverEmptySource() throws Exception {
        // given
        Flow<String> f = Flows.empty();

        // when & then
        assertEquals(List.of("[", "]"), f.intersperse("[", ", ", "]").runToList());
    }

    @Test
    void shouldIntersperseWithStartInjectAndEndOverSourceWithOneElement() throws Exception {
        // given
        Flow<String> f = Flows.fromValues("foo");

        // when & then
        assertEquals(List.of("[", "foo", "]"), f.intersperse("[", ", ", "]").runToList());
    }

    @Test
    void shouldIntersperseWithStartInjectAndEndOverSourceWithMultipleElements() throws Exception {
        // given
        Flow<String> f = Flows.fromValues("foo", "bar");

        // when & then
        assertEquals(
                List.of("[", "foo", ", ", "bar", "]"), f.intersperse("[", ", ", "]").runToList());
    }

    @Test
    void shouldNotTakeFromEmptyFlow() throws Exception {
        // given
        Flow<Integer> flow = Flows.empty();
        flow = flow.takeWhile(x -> x < 3, false);

        // when & then
        assertEquals(List.of(), flow.runToList());
    }

    @Test
    void shouldTakeAsLongAsPredicateIsSatisfied() throws Exception {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3).takeWhile(x -> x < 3, false);

        // when & then
        assertEquals(List.of(1, 2), flow.runToList());
    }

    @Test
    void shouldTakeFailedElementIfIncludeFirstFailingTrue() throws Exception {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3, 4).takeWhile(x -> x < 3, true);

        // when & then
        assertEquals(List.of(1, 2, 3), flow.runToList());
    }

    @Test
    void shouldWorkIfAllElementsMatchPredicate() throws Exception {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3).takeWhile(x -> x < 5, false);

        // when & then
        assertEquals(List.of(1, 2, 3), flow.runToList());
    }

    @Test
    void shouldNotTakeIfPredicateFailsForFirstOrMoreElements() throws Exception {
        // given
        Flow<Integer> flow = Flows.fromValues(3, 2, 1).takeWhile(x -> x < 3, false);

        // when & then
        assertEquals(List.of(), flow.runToList());
    }

    @Test
    void shouldTakeWhileFromAsyncFlow() throws Exception {
        // given
        Flow<Integer> flow = Flows.fromValues(3, 2, 1).buffer().takeWhile(x -> x > 2, false);

        // when & then
        assertEquals(List.of(3), flow.runToList());
    }

    @Test
    void shouldNotThrottleEmptySource() throws Exception {
        // given
        Flow<Integer> s = Flows.empty();

        // when
        long startTime = System.currentTimeMillis();
        List<Integer> result = s.throttle(1, Duration.ofSeconds(1)).runToList();
        long executionTime = System.currentTimeMillis() - startTime;

        // then
        assertEquals(Collections.emptyList(), result);
        assertTrue(executionTime < 200);
    }

    @Test
    void shouldThrottleToSpecifiedElementsPerTimeUnits() throws Exception {
        // given
        Flow<Integer> s = Flows.fromValues(1, 2);

        // when
        long startTime = System.currentTimeMillis();
        List<Integer> result = s.throttle(1, Duration.ofMillis(50)).runToList();
        long executionTime = System.currentTimeMillis() - startTime;

        // then
        assertEquals(Arrays.asList(1, 2), result);
        assertTrue(executionTime >= 100 && executionTime <= 150);
    }

    @Test
    void shouldFailToThrottleWhenElementsLessThanOrEqualToZero() {
        // given
        Flow<Integer> s = Flows.empty();

        // when
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> s.throttle(-1, Duration.ofMillis(50)));

        // then
        assertEquals("requirement failed: elements must be > 0", exception.getMessage());
    }

    @Test
    void shouldFailToThrottleWhenPerLowerThan1ms() {
        // given
        Flow<Integer> s = Flows.empty();

        // when
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class, () -> s.throttle(1, Duration.ofNanos(50)));

        // then
        assertEquals("requirement failed: per time must be >= 1 ms", exception.getMessage());
    }

    @Test
    void shouldNotEvaluateSubsequentFlowsIfThereIsAFailure() {
        // given
        AtomicBoolean evaluated = new AtomicBoolean(false);
        Flow<Integer> f =
                Flows.<Integer>failed(new IllegalStateException())
                        .concat(
                                Flows.usingEmit(
                                        emit -> {
                                            evaluated.set(true);
                                            emit.apply(1);
                                        }));

        // when & then
        assertThrows(IllegalStateException.class, f::runToList);
        assertFalse(evaluated.get());
    }

    @Test
    void shouldConcatFlows() throws Exception {
        // given
        Flow<Integer> f1 = Flows.fromValues(1, 2, 3);
        Flow<Integer> f2 = Flows.fromValues(4, 5, 6);

        // when
        Flow<Integer> f = f1.concat(f2);

        // then
        assertEquals(List.of(1, 2, 3, 4, 5, 6), f.runToList());
    }

    @Test
    void shouldPrependFlows() throws Exception {
        // given
        Flow<Integer> f1 = Flows.fromValues(1, 2, 3);
        Flow<Integer> f2 = Flows.fromValues(4, 5, 6);

        // when
        Flow<Integer> f = f1.prepend(f2);

        // then
        assertEquals(List.of(4, 5, 6, 1, 2, 3), f.runToList());
    }

    @Test
    void shouldNotDropFromEmptyFlow() throws Exception {
        // given
        Flow<Integer> s = Flows.empty();

        // when & then
        assertEquals(List.of(), s.drop(1).runToList());
    }

    @Test
    void shouldDropElementsFromSource() throws Exception {
        // given
        Flow<Integer> s = Flows.fromValues(1, 2, 3);

        // when & then
        assertEquals(List.of(3), s.drop(2).runToList());
    }

    @Test
    void shouldReturnEmptySourceWhenMoreElementsThanSourceLengthWasDropped() throws Exception {
        // given
        Flow<Integer> s = Flows.fromValues(1, 2);

        // when & then
        assertEquals(List.of(), s.drop(3).runToList());
    }

    @Test
    void shouldNotDropWhenNIsZero() throws Exception {
        // given
        Flow<Integer> s = Flows.fromValues(1, 2, 3);

        // when & then
        assertEquals(List.of(1, 2, 3), s.drop(0).runToList());
    }

    @Test
    void shouldMergeTwoSimpleFlows() throws Exception {
        // given
        var c1 = Flows.fromValues(1, 2, 3);
        var c2 = Flows.fromValues(4, 5, 6);

        // when
        List<Integer> result = c1.merge(c2, false, false).runToList().stream().sorted().toList();

        // then
        assertEquals(List.of(1, 2, 3, 4, 5, 6), result);
    }

    @Test
    void shouldMergeTwoAsyncFlows() throws Exception {
        // given
        var c1 = Flows.fromValues(1, 2, 3).buffer(10);
        var c2 = Flows.fromValues(4, 5, 6).buffer(10);

        // when
        List<Integer> result = c1.merge(c2, false, false).runToList().stream().sorted().toList();

        // then
        assertEquals(List.of(1, 2, 3, 4, 5, 6), result);
    }

    @Test
    void shouldMergeWithATickFlow() throws Exception {
        // given
        var c1 = Flows.tick(Duration.ofMillis(100), 0).take(3);
        var c2 = Flows.fromValues(0, 1, 2, 3);

        // when
        var r = c1.merge(c2, false, false).runToList();

        // then
        assertThat(new HashSet<>(r), contains(0, 1, 2, 3));
        assertEquals(List.of(0, 0), r.subList(r.size() - 2, r.size()));
    }

    @Test
    void shouldPropagateErrorFromTheLeft() {
        // given
        var c1 = Flows.fromValues(1, 2, 3).concat(Flows.failed(new IllegalStateException()));
        var c2 = Flows.fromValues(4, 5, 6);

        var s = c1.merge(c2, false, false);

        // when
        var exception = assertThrows(JoxScopeExecutionException.class, s::runToList);

        // then
        assertInstanceOf(IllegalStateException.class, exception.getCause().getCause());
    }

    @Test
    void shouldPropagateErrorFromTheRight() {
        // given
        var c1 = Flows.fromValues(1, 2, 3);
        var c2 = Flows.fromValues(4, 5, 6).concat(Flows.failed(new IllegalStateException()));

        var s = c1.merge(c2, false, false);

        // when
        var exception = assertThrows(JoxScopeExecutionException.class, s::runToList);

        // then
        assertInstanceOf(IllegalStateException.class, exception.getCause().getCause());
    }

    @Test
    void shouldMergeTwoFlowsEmittingAllElementsFromTheLeftWhenRightCompletes() throws Exception {
        // given
        var c1 = Flows.fromValues(1, 2, 3, 4).throttle(1, Duration.ofMillis(100));
        var c2 = Flows.fromValues(5, 6).throttle(1, Duration.ofMillis(100));

        // when
        List<Integer> result = c1.merge(c2, false, false).runToList().stream().sorted().toList();

        // then
        assertEquals(List.of(1, 2, 3, 4, 5, 6), result);
    }

    @Test
    void shouldMergeTwoFlowsEmittingAllElementsFromTheRightWhenLeftCompletes() throws Exception {
        // given
        var c1 = Flows.fromValues(1, 2).throttle(1, Duration.ofMillis(100));
        var c2 = Flows.fromValues(3, 4, 5, 6).throttle(1, Duration.ofMillis(100));

        // when
        List<Integer> result = c1.merge(c2, false, false).runToList().stream().sorted().toList();

        // then
        assertEquals(List.of(1, 2, 3, 4, 5, 6), result);
    }

    @Test
    void shouldMergeTwoFlowsCompletingTheResultingFlowWhenTheLeftFlowCompletes() throws Exception {
        // given
        var c1 = Flows.fromValues(1, 2).throttle(1, Duration.ofMillis(100));
        var c2 = Flows.fromValues(3, 4, 5, 6).throttle(1, Duration.ofMillis(100));

        // when
        var result = c1.merge(c2, true, false).runToList().stream().sorted().toList();

        // then
        assertTrue(result.equals(List.of(1, 2, 3, 4)) || result.equals(List.of(1, 2, 3)));
    }

    @Test
    void shouldMergeTwoFlowsCompletingTheResultingFlowWhenTheRightFlowCompletes() throws Exception {
        // given
        var c1 = Flows.fromValues(1, 2, 3, 4).throttle(1, Duration.ofMillis(100));
        var c2 = Flows.fromValues(5, 6).throttle(1, Duration.ofMillis(100));

        // when
        var result = c1.merge(c2, false, true).runToList().stream().sorted().toList();

        // then
        assertTrue(new HashSet<>(result).containsAll(List.of(5, 6)));
        assertTrue(result.size() > 2);
    }

    @Test
    void shouldEmitElementsOnlyFromOriginalSourceWhenNotEmpty() throws Exception {
        // given
        Flow<Integer> flow = Flows.fromValues(1).orElse(Flows.fromValues(2, 3));

        // when & then
        assertEquals(List.of(1), flow.runToList());
    }

    @Test
    void shouldEmitElementsOnlyFromAlternativeSourceWhenOriginalSourceCreatedEmpty()
            throws Exception {
        // given
        Flow<Integer> flow = Flows.<Integer>empty().orElse(Flows.fromValues(2, 3));

        // when & then
        assertEquals(List.of(2, 3), flow.runToList());
    }

    @Test
    void shouldEmitElementsOnlyFromAlternativeSourceWhenOriginalSourceEmpty() throws Exception {
        // given
        Flow<Integer> flow = Flows.<Integer>fromValues().orElse(Flows.fromValues(2, 3));

        // when & then
        assertEquals(List.of(2, 3), flow.runToList());
    }

    @Test
    void shouldReturnFailedSourceWhenOriginalSourceFailed() throws Exception {
        // given
        RuntimeException failure = new RuntimeException();
        Flow<Integer> flow = Flows.<Integer>failed(failure).orElse(Flows.fromValues(2, 3));

        // when & then
        Scopes.unsupervised(
                scope -> {
                    Source<Integer> source = flow.runToChannel(scope);
                    assertEquals(failure, ((ChannelError) source.receiveOrClosed()).cause());
                    return null;
                });
    }

    @Test
    void scan_shouldScanEmptyFlow() throws Exception {
        // given
        Flow<Integer> flow = Flows.empty();

        // when & then
        Flow<Integer> scannedFlow = flow.scan(0, Integer::sum);
        assertEquals(List.of(0), scannedFlow.runToList());
    }

    @Test
    void scan_shouldScanFlowOfSummedInt() throws Exception {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Flow<Integer> scannedFlow = flow.scan(0, Integer::sum);

        // when & then
        assertEquals(List.of(0, 1, 3, 6, 10, 15, 21, 28, 36, 45, 55), scannedFlow.runToList());
    }

    @Test
    void scan_shouldScanFlowOfMultipliedInt() throws Exception {
        // given
        Flow<Integer> flow = Flows.fromValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // when & then
        Flow<Integer> scannedFlow = flow.scan(1, (acc, el) -> acc * el);
        assertEquals(
                List.of(1, 1, 2, 6, 24, 120, 720, 5040, 40320, 362880, 3628800),
                scannedFlow.runToList());
    }

    @Test
    void scan_shouldScanFlowOfConcatenatedString() throws Exception {
        // given
        Flow<String> flow = Flows.fromValues("f", "l", "o", "w");
        Flow<String> scannedFlow = flow.scan("my", (acc, el) -> acc + el);

        // when & then
        assertEquals(List.of("my", "myf", "myfl", "myflo", "myflow"), scannedFlow.runToList());
    }

    @Test
    void debounceBy_shouldNotDebounceIfAppliedOnEmptyFlow() throws Exception {
        // given
        Flow<Integer> c = Flows.empty();

        // when & then
        Flow<Integer> s = c.debounceBy(x -> x * 2);
        assertEquals(List.of(), s.runToList());
    }

    @Test
    void debounceBy_shouldNotDebounceIfAppliedOnFlowContainingOnlyDistinctFValue()
            throws Exception {
        // given
        Flow<Integer> c = Flows.fromValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // when & then
        Flow<Integer> s = c.debounceBy(x -> x * 2);
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), s.runToList());
    }

    @Test
    void debounceBy_shouldDebounceIfAppliedOnFlowContainingRepeatingFValue() throws Exception {
        // given
        Flow<Integer> c = Flows.fromValues(1, 1, 2, 3, 4, 4, 5);
        Flow<Integer> s = c.debounceBy(x -> x * 2);

        // when & then
        assertEquals(List.of(1, 2, 3, 4, 5), s.runToList());
    }

    @Test
    void debounceBy_shouldDebounceSubsequentOddPrimeNumbers() throws Exception {
        // given
        Flow<Integer> c = Flows.fromValues(1, 1, 1, 2, 4, 3, 7, 4, 5);

        // when & then
        Flow<Integer> s = c.debounceBy(x -> x % 2 == 0);
        assertEquals(List.of(1, 2, 3, 4, 5), s.runToList());
    }

    @Test
    void debounce_shouldNotDebounceIfAppliedOnEmptyFlow() throws Exception {
        // given
        Flow<Integer> c = Flows.empty();

        // when & then
        Flow<Integer> s = c.debounce();
        assertEquals(List.of(), s.runToList());
    }

    @Test
    void debounce_shouldNotDebounceIfAppliedOnFlowContainingOnlyDistinctValues() throws Exception {
        // given
        Flow<Integer> c = Flows.fromValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // when & then
        Flow<Integer> s = c.debounce();
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), s.runToList());
    }

    @Test
    void debounce_shouldDebounceIfAppliedOnFlowContainingOnlyRepeatingValues() throws Exception {
        // given
        Flow<Integer> c = Flows.fromValues(1, 1, 1, 1, 1);

        // when & then
        Flow<Integer> s = c.debounce();
        assertEquals(List.of(1), s.runToList());
    }

    @Test
    void debounce_shouldDebounceIfAppliedOnFlowContainingRepeatingElements() throws Exception {
        // given
        Flow<Integer> c = Flows.fromValues(1, 1, 2, 3, 4, 4, 5);

        // when & then
        Flow<Integer> s = c.debounce();
        assertEquals(List.of(1, 2, 3, 4, 5), s.runToList());
    }

    @Test
    void sample_shouldNotSampleAnythingFromEmptyFlow() throws Exception {
        // given
        Flow<Integer> c = Flows.empty();

        // when & then
        Flow<Integer> s = c.sample(5);
        assertEquals(Collections.emptyList(), s.runToList());
    }

    @Test
    void sample_shouldNotSampleAnythingWhenNIsZero() throws Exception {
        // given
        Flow<Integer> c = Flows.fromValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // when & then
        Flow<Integer> s = c.sample(0);
        assertEquals(Collections.emptyList(), s.runToList());
    }

    @Test
    void sample_shouldSampleEveryElementWhenNIsOne() throws Exception {
        // given
        Flow<Integer> c = Flows.fromValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        int n = 1;

        // when & then
        Flow<Integer> s = c.sample(n);
        assertEquals(IntStream.rangeClosed(n, 10).boxed().toList(), s.runToList());
    }

    @Test
    void sample_shouldSampleEveryNthElement() throws Exception {
        // given
        Flow<Integer> c = Flows.fromValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        int n = 3;

        // when & then
        Flow<Integer> s = c.sample(n);
        assertEquals(List.of(3, 6, 9), s.runToList());
    }

    @Test
    void shouldCollectOverSource() throws Exception {
        // given
        Flow<Integer> c = Flows.fromValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // when & then
        Flow<Integer> s =
                c.collect(
                        i -> {
                            if (i % 2 == 0) {
                                return Optional.of(i * 10);
                            }
                            return Optional.empty();
                        });
        List<Integer> result = s.runToList();
        assertEquals(List.of(20, 40, 60, 80, 100), result);
    }
}
