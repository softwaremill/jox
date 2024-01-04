package com.softwaremill.jox;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import static com.softwaremill.jox.Select.select;
import static com.softwaremill.jox.Select.selectSafe;
import static com.softwaremill.jox.TestUtil.forkVoid;
import static com.softwaremill.jox.TestUtil.scoped;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SelectReceiveTest {
    @Test
    public void testSelectFromFirstBuffered() throws InterruptedException {
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
    public void testSelectFromSecondBuffered() throws InterruptedException {
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
    public void testSelectBiasedTowardsFirstBuffered() throws InterruptedException {
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
    public void testSelectFromReadyRendezvous() throws InterruptedException, ExecutionException {
        // given
        Channel<String> ch1 = new Channel<>();
        Channel<String> ch2 = new Channel<>();

        scoped(scope -> {
            forkVoid(scope, () -> ch2.send("v"));

            // when
            String received = select(ch1.receiveClause(), ch2.receiveClause());

            // then
            assertEquals("v", received);
        });
    }

    @Test
    public void testSelectWhenDone() throws InterruptedException {
        // given
        Channel<String> ch1 = new Channel<>(1);
        Channel<String> ch2 = new Channel<>(1);
        ch2.done();

        // when
        Object received = selectSafe(ch1.receiveClause(), ch2.receiveClause());

        // then
        assertEquals(new ChannelDone(), received);
    }

    @TestWithCapacities
    public void testReceiveMany(int capacity) throws InterruptedException, ExecutionException {
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
    public void testSelectWithTransformation() throws InterruptedException {
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
    public void testBufferExpandedWhenSelecting() throws InterruptedException {
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
}
