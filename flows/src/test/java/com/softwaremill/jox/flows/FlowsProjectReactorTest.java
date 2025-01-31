package com.softwaremill.jox.flows;

import com.softwaremill.jox.structured.Scopes;
import org.junit.jupiter.api.Test;
import org.reactivestreams.FlowAdapters;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FlowsProjectReactorTest {

    @Test
    void simpleFlowShouldEmitElementsToBeProcessedByFlux() throws InterruptedException {
        Scopes.supervised(scope -> {
            // given
            var flow = Flows.range(1, 4, 1);

            // when
            var result = Flux.from(FlowAdapters.toPublisher(flow.toPublisher(scope)))
                             .map(i -> i * 2)
                             .collectList()
                             .block();

            // then
            assertEquals(List.of(2, 4, 6, 8), result);
            return null;
        });
    }

    @Test
    void concurrentFlowShouldEmitElementsToBeProcessedByFlux() throws InterruptedException {
        Scopes.supervised(scope -> {
            // given
            var flow = Flows.tick(Duration.ofMillis(100), "x")
                            .merge(Flows.tick(Duration.ofMillis(200), "y"), false, false)
                            .take(5);

            // when
            var result = Flux.from(FlowAdapters.toPublisher(flow.toPublisher(scope)))
                             .map(s -> s + s)
                             .collectList()
                             .block();

            // then
            result.sort(String::compareTo);
            assertEquals(List.of("xx", "xx", "xx", "yy", "yy"), result);
            return null;
        });
    }

    @Test
    void shouldCreateFlowFromASimplePublisher() throws Exception {
        // given
        Flux<Integer> map = Flux.fromStream(IntStream.rangeClosed(1, 4).boxed())
                                .map(i -> i * 2);

        // when
        List<Integer> result = Flows.fromPublisher(FlowAdapters.toFlowPublisher(map))
                                    .runToList();

        // then
        assertEquals(List.of(2, 4, 6, 8), result);
    }

    @Test
    void shouldCreateFlowFromAConcurrentPublisher() throws Exception {
        // given
        Flux<String> flux = Flux.interval(Duration.ofMillis(100))
                                .map(_ -> "x")
                                .mergeWith(Flux.interval(Duration.ofMillis(150))
                                               .map(_ -> "y"))
                                .take(5);

        // when
        List<String> result = Flows.fromPublisher(FlowAdapters.toFlowPublisher(flux))
                                   .map(s -> s + s)
                                   .runToList();

        // then
        result.sort(String::compareTo);
        assertEquals(List.of("xx", "xx", "xx", "yy", "yy"), result);
    }
}
