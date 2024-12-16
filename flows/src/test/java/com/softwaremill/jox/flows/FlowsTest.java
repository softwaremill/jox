package com.softwaremill.jox.flows;

import com.softwaremill.jox.ChannelError;
import com.softwaremill.jox.Source;
import com.softwaremill.jox.structured.Fork;
import com.softwaremill.jox.structured.UnsupervisedScope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class FlowsTest {

    @Test
    void shouldBeEmpty() throws Exception {
        assertTrue(Flows.empty().runToList().isEmpty());
    }

    @Test
    @WithUnsupervisedScope
    void shouldCreateFlowFromFork(UnsupervisedScope scope) throws Exception {
        Fork<Integer> f = scope.forkUnsupervised(() -> 1);
        Flow<Integer> c = Flows.fromFork(f);
        assertEquals(List.of(1), c.runToList());
    }

    @Test
    void shouldCreateIteratingFlow() throws Exception {
        // given
        Flow<Integer> c = Flows.iterate(1, i -> i + 1);

        // when & then
        assertEquals(List.of(1, 2, 3), c.take(3).runToList());
    }

    @Test
    void shouldProduceRange() throws Exception {
        assertEquals(List.of(1, 2, 3, 4, 5), Flows.range(1, 5, 1).runToList());
        assertEquals(List.of(1, 3, 5), Flows.range(1, 5, 2).runToList());
        assertEquals(List.of(1, 4, 7, 10), Flows.range(1, 11, 3).runToList());
    }

    @Test
    @WithUnsupervisedScope
    public void shouldFailOnReceive(UnsupervisedScope scope) throws Exception {
        // when
        Flow<String> s = Flows.failed(new RuntimeException("boom"));

        // then
        Object received = s.runToChannel(scope).receiveOrClosed();
        assertInstanceOf(ChannelError.class, received);
        assertEquals("boom", ((ChannelError) received).cause().getMessage());
    }

    @Test
    void shouldReturnOriginalFutureFailureWhenFutureFails() {
        // given
        RuntimeException failure = new RuntimeException("future failed");

        // when & then
        assertThrows(RuntimeException.class,
                () -> Flows.fromCompletableFuture(CompletableFuture.failedFuture(failure)).runToList(),
                failure.getMessage());
    }

    @Test
    void shouldReturnFutureValue() throws Exception {
        // given
        CompletableFuture<Integer> future = CompletableFuture.completedFuture(1);

        // when
        List<Integer> result = Flows.fromCompletableFuture(future).runToList();

        // then
        assertEquals(List.of(1), result);
    }

    @Test
    @WithUnsupervisedScope
    void shouldReturnFuturesSourceValues(UnsupervisedScope scope) throws Exception {
        // given
        CompletableFuture<Source<Integer>> completableFuture = CompletableFuture
                .completedFuture(Flows.fromValues(1, 2).runToChannel(scope));

        // when
        List<Integer> result = Flows.fromFutureSource(completableFuture).runToList();

        // then
        assertEquals(List.of(1, 2), result);
    }

    @Test
    void shouldReturnOriginalFutureFailureWhenSourceFutureFails() {
        // given
        RuntimeException failure = new RuntimeException("future failed");

        // when & then
        assertThrows(RuntimeException.class,
                () -> Flows.fromFutureSource(CompletableFuture.failedFuture(failure)).runToList(),
                failure.getMessage());
    }

    @Test
    void shouldEvaluateElementBeforeEachSend() throws Exception {
        // given
        AtomicInteger i = new AtomicInteger(0);

        // when
        List<Integer> actual = Flows.repeatEval(i::incrementAndGet)
                .take(3)
                .runToList();

        // then
        assertEquals(List.of(1, 2, 3), actual);
    }

    @Test
    void shouldEvaluateElementBeforeEachSendAsLongAsDefined() throws Exception {
        // given
        AtomicInteger i = new AtomicInteger(0);
        Set<Integer> evaluated = new HashSet<>();

        // when
        List<Integer> result = Flows.repeatEvalWhileDefined(() -> {
                    int value = i.incrementAndGet();
                    evaluated.add(value);
                    return value < 5 ? Optional.of(value) : Optional.empty();
                })
                .runToList();

        // then
        assertEquals(List.of(1, 2, 3, 4), result);
        assertEquals(Set.of(1, 2, 3, 4, 5), evaluated);
    }

    @Test
    void shouldRepeatTheSameElement() throws Exception {
        // when
        List<Integer> result = Flows.repeat(2137)
                .take(3)
                .runToList();

        // then
        assertEquals(List.of(2137, 2137, 2137), result);
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    @WithUnsupervisedScope
    void shouldTickRegularly(UnsupervisedScope scope) throws InterruptedException {
        var c = Flows.tick(Duration.ofMillis(100), 1L)
                .runToChannel(scope);
        var start = System.currentTimeMillis();

        c.receive();
        assertTrue(System.currentTimeMillis() - start >= 0);
        assertTrue(System.currentTimeMillis() - start <= 50);

        c.receive();
        assertTrue(System.currentTimeMillis() - start >= 100);
        assertTrue(System.currentTimeMillis() - start <= 150);

        c.receive();
        assertTrue(System.currentTimeMillis() - start >= 200);
        assertTrue(System.currentTimeMillis() - start <= 250);
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    @WithUnsupervisedScope
    void shouldTickImmediatelyInCaseOfSlowConsumerAndThenResumeNormal(UnsupervisedScope scope) throws InterruptedException {
        var c = Flows.tick(Duration.ofMillis(100), 1L)
                .runToChannel(scope);
        var start = System.currentTimeMillis();

        Thread.sleep(200);
        c.receive();
        assertTrue(System.currentTimeMillis() - start >= 200);
        assertTrue(System.currentTimeMillis() - start <= 250);

        c.receive();
        assertTrue(System.currentTimeMillis() - start >= 200);
        assertTrue(System.currentTimeMillis() - start <= 250);
    }

    @Test
    void shouldTimeout() throws Exception {
        // given
        long start = System.currentTimeMillis();
        Flow<Integer> c = Flows.concat(
                Flows.timeout(Duration.ofMillis(100)),
                Flows.fromValues(1)
        );

        // when
        List<Integer> result = c.runToList();

        // then
        assertEquals(List.of(1), result);
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed >= 100L);
        assertTrue(elapsed <= 150L);
    }

    @Test
    void shouldConcatFlows() throws Exception {
        // given
        Flow<Integer> flow = Flows.concat(
                Flows.fromValues(1, 2, 3),
                Flows.fromValues(4, 5, 6)
        );

        // when
        List<Integer> result = flow.runToList();

        // then
        assertEquals(List.of(1, 2, 3, 4, 5, 6), result);
    }
}
