package com.softwaremill.jox;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import static com.softwaremill.jox.Select.select;
import static com.softwaremill.jox.Select.selectOrClosed;
import static com.softwaremill.jox.TestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SelectReceiveTest {
    @Test
    void testSelectFromFirst_buffered_immediate() throws InterruptedException {
        // given
        Channel<String> ch1 = new Channel<>(1);
        Channel<String> ch2 = new Channel<>(1);
        ch1.send("v");

        // when
        String received = select(ch1.receiveClause(), ch2.receiveClause());

        // then
        assertEquals("v", received);
    }

    @Test
    void testSelectFromSecond_buffered_immediate() throws InterruptedException {
        // given
        Channel<String> ch1 = new Channel<>(1);
        Channel<String> ch2 = new Channel<>(1);
        ch2.send("v");

        // when
        String received = select(ch1.receiveClause(), ch2.receiveClause());

        // then
        assertEquals("v", received);
    }

    @Test
    void testSelectFromFirst_buffered_suspend() throws InterruptedException, ExecutionException {
        // given
        Channel<String> ch1 = new Channel<>(1);
        Channel<String> ch2 = new Channel<>(1);

        scoped(scope -> {
            // when
            var f = fork(scope, () -> select(ch1.receiveClause(), ch2.receiveClause()));
            forkVoid(scope, () -> {
                Thread.sleep(100); // making sure receive suspends
                ch1.send("v");
            });

            // then
            assertEquals("v", f.get());
        });
    }

    @Test
    void testSelectBiasedTowardsFirst_buffered() throws InterruptedException {
        // given
        Channel<String> ch1 = new Channel<>(1);
        Channel<String> ch2 = new Channel<>(1);
        ch1.send("v1");
        ch2.send("v2");

        // when
        String received = select(ch1.receiveClause(), ch2.receiveClause());

        // then
        assertEquals("v1", received);
    }

    @Test
    void testSelectBiasedTowardsFirst_whenDone_buffered() throws InterruptedException {
        // given
        Channel<String> ch1 = new Channel<>(1);
        Channel<String> ch2 = new Channel<>(1);
        ch1.send("x");
        ch2.done();

        // when
        Object received = selectOrClosed(ch1.receiveClause(), ch2.receiveClause());

        // then
        assertEquals("x", received);
    }

    @Test
    void testSelectFromReady_rendezvous_suspend() throws InterruptedException, ExecutionException {
        // given
        Channel<String> ch1 = new Channel<>();
        Channel<String> ch2 = new Channel<>();

        scoped(scope -> {
            forkVoid(scope, () -> {
                Thread.sleep(100); // making sure receive suspends
                ch2.send("v");
            });

            // when
            String received = select(ch1.receiveClause(), ch2.receiveClause());

            // then
            assertEquals("v", received);
        });
    }

    @Test
    void testSelectFromReady_rendezvous_immediate() throws InterruptedException, ExecutionException {
        // given
        Channel<String> ch1 = new Channel<>();
        Channel<String> ch2 = new Channel<>();

        scoped(scope -> {
            forkVoid(scope, () -> ch2.send("v"));

            // when
            Thread.sleep(100); // making sure send is ready
            String received = select(ch1.receiveClause(), ch2.receiveClause());

            // then
            assertEquals("v", received);
        });
    }

    @Test
    void testSelectWhenDone() throws InterruptedException {
        // given
        Channel<String> ch1 = new Channel<>(1);
        Channel<String> ch2 = new Channel<>(1);
        ch1.done();
        ch2.send("x");

        // when
        Object received = selectOrClosed(ch1.receiveClause(), ch2.receiveClause());

        // then
        assertEquals(new ChannelDone(ch1), received);
    }

    @TestWithCapacities
    void testReceiveMany(int capacity) throws InterruptedException, ExecutionException {
        // given
        int channelsCount = 5;
        int msgsCount = 10000;

        var channels = new ArrayList<Channel<String>>();
        for (int i = 0; i < channelsCount; i++) {
            channels.add(new Channel<>(capacity));
        }

        scoped(scope -> {
            for (int i = 0; i < channelsCount; i++) {
                var ch = channels.get(i);
                int finalI = i;
                forkVoid(scope, () -> {
                    for (int j = 0; j < msgsCount; j++) {
                        ch.send("ch" + finalI + "_" + j);
                    }
                });
            }

            // when
            var received = new HashSet<>();
            for (int i = 0; i < channelsCount * msgsCount; i++) {
                received.add(select(channels.stream().map(Channel::receiveClause).toArray(SelectClause[]::new)));
            }

            // then
            var expectedReceived = new HashSet<>();
            for (int i = 0; i < channelsCount; i++) {
                for (int j = 0; j < msgsCount; j++) {
                    expectedReceived.add("ch" + i + "_" + j);
                }
            }
            assertEquals(expectedReceived, received);
        });
    }

    @Test
    void testSelectWithTransformation() throws InterruptedException {
        // given
        Channel<String> ch1 = new Channel<>(1);
        Channel<String> ch2 = new Channel<>(1);
        ch1.send("v");

        // when
        String received = select(ch1.receiveClause(x -> x + x), ch2.receiveClause(x -> x + x));

        // then
        assertEquals("vv", received);
    }

    @Test
    void testBufferExpandedWhenSelecting() throws InterruptedException {
        // given
        Channel<String> ch = new Channel<>(2);

        // when
        ch.send("v1");
        ch.send("v2");
        String r1 = select(ch.receiveClause());
        String r2 = select(ch.receiveClause());

        ch.send("v3");
        ch.send("v4"); // none of the sends should block
        String r3 = select(ch.receiveClause());
        String r4 = select(ch.receiveClause());

        // then
        assertEquals("v1", r1);
        assertEquals("v2", r2);
        assertEquals("v3", r3);
        assertEquals("v4", r4);
    }

    @Test
    void testSelectFromNone() {
        assertThrows(IllegalArgumentException.class, Select::selectOrClosed);
    }

    @Test
    void testSelectFromNullableList() throws InterruptedException {
        // given
        Channel<String> ch1 = new Channel<>(1);
        Channel<String> ch2 = new Channel<>(1);
        ch1.send("v");

        // when
        assertThrows(IllegalArgumentException.class,
                () -> select(ch1.receiveClause(), null, ch2.receiveClause()));
    }

    @Test
    void testSelect_immediate_withError() throws InterruptedException {
        // given
        Channel<String> ch1 = new Channel<>(2);
        ch1.send("x");

        var e = new RuntimeException("boom!");
        Channel<String> ch2 = new Channel<>(2);
        ch2.error(e);

        // when
        var result = selectOrClosed(ch1.receiveClause(), ch2.receiveClause());

        // then
        assertEquals(new ChannelError(e, ch2), result);
    }
}
