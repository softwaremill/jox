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

public class SelectTest {
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
        String received = select(ch1.receiveClauseMap(x -> x + x), ch2.receiveClauseMap(x -> x + x));

        // then
        assertEquals("vv", received);
    }
}
