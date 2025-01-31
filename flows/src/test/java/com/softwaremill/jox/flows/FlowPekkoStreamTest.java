package com.softwaremill.jox.flows;

import com.softwaremill.jox.structured.Scopes;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.stream.javadsl.AsPublisher;
import org.apache.pekko.stream.javadsl.Sink;
import org.apache.pekko.stream.javadsl.Source;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Publisher;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FlowPekkoStreamTest {

    private ActorSystem system;

    @BeforeEach
    void setUp() {
        system = ActorSystem.create("test");
    }

    @AfterEach
    void cleanUp() {
        system.terminate();
    }

    @Test
    void test() throws ExecutionException, InterruptedException {
        Scopes.supervised(scope -> {
            var flow = Flows.fromIterable(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
                            .map(i -> i * 2)
                            .filter(i -> i % 3 == 0);
            var result = Source
                    .fromPublisher(FlowAdapters.toPublisher(flow.toPublisher(scope)))
                    .map(i -> i * 2)
                    .runWith(Sink.seq(), system)
                    .toCompletableFuture()
                    .get();

            assertEquals(List.of(12, 24, 36), result);
            return null;
        });
    }

    @Test
    public void testSimpleFlow() throws InterruptedException {
        Scopes.supervised(scope -> {
            var flow = Flows.fromIterable(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
                            .map(i -> i * 2)
                            .filter(i -> i % 3 == 0);
            var result = Source
                    .fromPublisher(FlowAdapters.toPublisher(flow.toPublisher(scope)))
                    .map(i -> i * 2)
                    .runWith(Sink.seq(), system)
                    .toCompletableFuture()
                    .get();

            assertEquals(List.of(12, 24, 36), result);
            return null;
        });
    }

    @Test
    public void testConcurrentFlow() throws InterruptedException {
        Scopes.supervised(scope -> {
            var flow = Flows.tick(Duration.ofMillis(100), "x")
                            .merge(Flows.tick(Duration.ofMillis(200), "y"), false, false)
                            .take(5);
            var result = Source
                    .fromPublisher(FlowAdapters.toPublisher(flow.toPublisher(scope)))
                    .map(s -> s + s)
                    .runWith(Sink.seq(), system)
                    .toCompletableFuture()
                    .get();

            result = result.stream().sorted().toList();
            assertEquals(List.of("xx", "xx", "xx", "yy", "yy"), result);
            return null;
        });
    }

    @Test
    public void testFlowFromSimplePublisher() throws Exception {
        Publisher<Integer> publisher = Source
                .fromIterator(() -> List.of(1, 2, 3).iterator())
                .map(i -> i * 2)
                .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), system);

        var result = Flows.fromPublisher(FlowAdapters.toFlowPublisher(publisher))
                          .map(i -> i * 10)
                          .runToList();

        assertEquals(List.of(20, 40, 60), result);
    }

    @Test
    public void testFlowFromConcurrentPublisher() throws Exception {
        Publisher<String> publisher = Source
                .tick(Duration.ZERO, Duration.ofMillis(100), "x")
                .merge(Source.tick(Duration.ZERO, Duration.ofMillis(200), "y"))
                .take(5)
                .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), system);

        var result = Flows.fromPublisher(FlowAdapters.toFlowPublisher(publisher))
                          .map(s -> s + s)
                          .runToList();

        result.sort(String::compareTo);
        assertEquals(List.of("xx", "xx", "xx", "yy", "yy"), result);
    }
}
