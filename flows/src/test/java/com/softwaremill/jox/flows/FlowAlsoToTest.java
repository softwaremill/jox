package com.softwaremill.jox.flows;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import com.softwaremill.jox.Channel;
import com.softwaremill.jox.ChannelClosedException;
import com.softwaremill.jox.ChannelDone;
import com.softwaremill.jox.ChannelError;
import com.softwaremill.jox.structured.Scopes;
import org.junit.jupiter.api.Test;

public class FlowAlsoToTest {

    @Test
    void alsoTo_shouldSendToBothSinks() throws Exception {
        // given
        var c = new Channel<Integer>(10);

        // when
        List<Integer> result = Flows.fromValues(1, 2, 3)
                .alsoTo(c)
                .runToList();

        // then
        assertEquals(List.of(1, 2, 3), result);
        assertEquals(List.of(1, 2, 3), c.toList());
    }

    @Test
    void alsoTo_shouldSendToBothSinksAndNotHangWhenOtherSinkIsRendezvousChannel() throws Exception {
        Scopes.supervised(scope -> {
            // given
            var c = new Channel<Integer>();
            var f = scope.fork(c::toList);

            // when
            List<Integer> result = Flows.fromValues(1, 2, 3, 4, 5)
                    .alsoTo(c)
                    .runToList();

            // then
            assertEquals(List.of(1, 2, 3, 4, 5), result);
            assertEquals(List.of(1, 2, 3, 4, 5), f.join());
            return null;
        });
    }

    @Test
    void alsoTo_shouldCloseMainFlowWhenOtherCloses() throws Exception {
        Scopes.supervised(scope -> {
            // given
            var c = new Channel<Integer>();
            scope.fork(() -> {
                var list = List.of(c.receiveOrClosed(), c.receiveOrClosed(), c.receiveOrClosed());
                c.doneOrClosed();
                // a send() from the main thread might be waiting - we need to consume that, and only then the main thread
                // will discover that the channel is closed
                c.receiveOrClosed();
                return list;
            });
            Flow<Integer> flow = Flows.iterate(1, i -> i + 1)
                    .take(100)
                    .alsoTo(c);

            // when & then
            assertThrows(ChannelClosedException.class, flow::runToList);
            return null;
        });
    }

    @Test
    void alsoTo_shouldCloseMainFlowWithErrorWhenOtherErrors() throws Exception {
        Scopes.supervised(scope -> {
            // given
            var c = new Channel<Integer>(1);
            var f = scope.fork(() -> {
                c.receiveOrClosed();
                c.receiveOrClosed();
                c.receiveOrClosed();
                c.errorOrClosed(new IllegalStateException());
                return null;
            });

            Flow<Integer> flow = Flows.iterate(1, i -> i + 1)
                    .take(100)
                    .alsoTo(c);

            // when & then
            assertThrows(ChannelClosedException.class, flow::runToList);
            f.join();
            return null;
        });
    }

    @Test
    void alsoTo_shouldCloseOtherChannelWithErrorWhenMainErrors() throws Exception {
        Scopes.supervised(scope -> {
            // given
            var other = new Channel<Integer>(0);
            var forkOther = scope.forkUnsupervised(other::toList);

            Flow<Integer> flow = Flows.iterate(1, i -> i + 1)
                    .take(100)
                    .concat(Flows.failed(new IllegalStateException()))
                    .alsoTo(other);

            // when & then
            assertThrows(IllegalStateException.class, flow::runToList);
            assertThrows(ExecutionException.class, forkOther::join);
            return null;
        });
    }

    @Test
    void alsoToTap_shouldSendToBothSinksWhenOtherIsFaster() throws Exception {
        // given
        var other = new Channel<Integer>(10);
        Flow<Integer> flow = Flows
                .fromValues(1, 2, 3)
                .alsoToTap(other)
                .tap(_ -> Thread.sleep(50));

        // when & then
        assertEquals(List.of(1, 2, 3), flow.runToList());
        assertEquals(List.of(1, 2, 3), other.toList());
    }

    @Test
    void alsoTapTo_shouldSendToBothSinksWhenOtherIsSlower() throws Exception {
        Scopes.supervised(scope -> {
            // given
            var other = new Channel<Integer>();
            var slowConsumerFork = scope.fork(() -> {
                var consumed = new LinkedList<>();
                while (true) {
                    Thread.sleep(100);
                    var result = other.receiveOrClosed();
                    if (result instanceof ChannelDone || result instanceof ChannelError) {
                        break;
                    } else {
                        consumed.add(result);
                    }
                }
                return consumed;
            });
            var main = new Channel<Integer>();
            scope.fork(() -> {
                for (int i = 1; i <= 20; i++) {
                    main.send(i);
                    Thread.sleep(10);
                }
                main.done();
                return null;
            });

            // when
            List<Integer> result = Flows.fromSource(main).alsoToTap(other).runToList();

            // then
            assertEquals(IntStream.rangeClosed(1, 20).boxed().toList(), result);
            assertThat(slowConsumerFork.join(), hasSize(lessThan(10)));
            return null;
        });
    }

    @Test
    void alsoTapTo_shouldNotFailTheFlowWhenTheOtherSinkFails() throws Exception {
        Scopes.supervised(scope -> {
            // given
            var other = new Channel<Integer>();
            var f = scope.fork(() -> {
                var v = other.receiveOrClosed();
                other.error(new RuntimeException("boom!"));
                return v;
            });

            // when
            List<Integer> result = Flows
                    .iterate(1, i -> i + 1)
                    .take(10)
                    .tap(_ -> Thread.sleep(10))
                    .alsoToTap(other)
                    .runToList();

            // then
            assertEquals(IntStream.rangeClosed(1, 10).boxed().toList(), result);
            assertEquals(1, f.join());
            return null;
        });
    }

    @Test
    void alsoTapTo_shouldNotCloseTheFlowWhenTheOtherSinkCloses() throws Exception {
        Scopes.supervised(scope -> {
            // given
            var other = new Channel<Integer>();
            var f = scope.fork(() -> {
                var v = other.receiveOrClosed();
                other.done();
                return v;
            });

            // when
            List<Integer> result = Flows
                    .iterate(1, i -> i + 1)
                    .take(10)
                    .tap(_ -> Thread.sleep(10))
                    .alsoToTap(other)
                    .runToList();

            // then
            assertEquals(IntStream.rangeClosed(1, 10).boxed().toList(), result);
            assertEquals(1, f.join());
            return null;
        });
    }
}
