package com.softwaremill.jox;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import static com.softwaremill.jox.Select.select;
import static com.softwaremill.jox.Select.selectSafe;
import static com.softwaremill.jox.TestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SelectSendTest {
    @Test
    public void testSelectToFirst_buffered_immediate() throws InterruptedException {
        // given
        Channel<String> ch1 = new Channel<>(1);
        Channel<String> ch2 = new Channel<>(1);

        // when
        select(ch1.sendClause("v1"), ch2.sendClause("v2"));

        // then
        assertEquals("v1", select(ch1.receiveClause(), ch2.receiveClause()));
    }

    @Test
    public void testSelectToSecond_buffered_immediate() throws InterruptedException {
        // given
        Channel<String> ch1 = new Channel<>(1);
        Channel<String> ch2 = new Channel<>(1);
        ch1.send("v0"); // filling in the buffer

        // when
        select(ch1.sendClause("v1"), ch2.sendClause("v2"));

        // then
        assertEquals("v0", select(ch1.receiveClause(), ch2.receiveClause()));
        assertEquals("v2", select(ch1.receiveClause(), ch2.receiveClause()));
    }

    @Test
    public void testSelectBiasedTowardsToFirst_rendezvous_immediate() throws InterruptedException, ExecutionException {
        // given
        Channel<String> ch1 = new Channel<>();
        Channel<String> ch2 = new Channel<>();

        scoped(scope -> {
            // when
            forkVoid(scope, () -> {
                Thread.sleep(100); // making sure receives suspend
                select(ch1.sendClause("v1"), ch2.sendClause("v2"));
            });

            // then
            var received = select(ch1.receiveClause(), ch2.receiveClause());
            assertEquals("v1", received);
        });
    }

    @Test
    public void testSelect_rendezvous_suspend() throws InterruptedException, ExecutionException {
        // given
        Channel<String> ch1 = new Channel<>();
        Channel<String> ch2 = new Channel<>();

        scoped(scope -> {
            // when
            var f = fork(scope, () -> select(ch1.sendClause("v1", () -> "1"), ch2.sendClause("v2", () -> "2")));
            Thread.sleep(100); // making sure send suspends

            // then
            var received = select(ch1.receiveClause(), ch2.receiveClause());
            if (f.get().equals("1")) {
                assertEquals("v1", received);
            } else {
                assertEquals("v2", received);
            }
        });
    }

    @Test
    public void testSelectBiasedTowardsFirst_buffered() throws InterruptedException {
        // given
        Channel<String> ch1 = new Channel<>(1);
        Channel<String> ch2 = new Channel<>(1);

        // when
        select(ch1.sendClause("v1"), ch2.sendClause("v2"));

        // then
        var result = select(ch1.receiveClause(), ch2.receiveClause());
        assertEquals("v1", result);
    }

    @Test
    public void testSelectWhenDone() throws InterruptedException {
        // given
        Channel<String> ch1 = new Channel<>();
        Channel<String> ch2 = new Channel<>();
        ch2.done();

        // when
        Object received = selectSafe(ch1.sendClause("v1"), ch2.sendClause("v2"));

        // then
        assertEquals(new ChannelDone(), received);
    }

    @TestWithCapacities
    public void testSendMany(int capacity) throws InterruptedException, ExecutionException {
        // given
        int channelsCount = 5;
        int msgsCount = 10000;

        for (int k = 0; k < 200; k++) {
            var channels = new ArrayList<Channel<String>>();
            for (int i = 0; i < channelsCount; i++) {
                channels.add(new Channel<>(capacity));
            }

            try {
                scoped(scope -> {
                    var received = ConcurrentHashMap.newKeySet();
                    forkVoid(scope, () -> {
                        for (int i = 0; i < channelsCount * msgsCount; i++) {
                            received.add(select(channels.stream().map(Channel::receiveClause).toArray(SelectClause[]::new)));
                        }
                    });

                    // when
                    for (int i = 0; i < channelsCount * msgsCount; i++) {
                        int finalI = i;
                        select(channels.stream().map(ch -> ch.sendClause("v_" + finalI)).toArray(SelectClause[]::new));
                    }

                    // then
                    var expectedReceived = new HashSet<>();
                    for (int i = 0; i < channelsCount * msgsCount; i++) {
                        expectedReceived.add("v_" + i);
                    }

                    var notReceived = new HashSet<>(expectedReceived);
                    notReceived.removeAll(received);
                    assertEquals(Collections.emptySet(), notReceived);

                    var extraReceived = new HashSet<>(received);
                    extraReceived.removeAll(expectedReceived);
                    assertEquals(Collections.emptySet(), extraReceived);
                });
            } catch (Exception e) {
                System.out.println("Channels:");
                for (var ch : channels) {
                    System.out.println(ch);
                }
                throw e;
            }
        }
    }

    @Test
    public void testSelectWithTransformation() throws InterruptedException {
        // given
        Channel<String> ch1 = new Channel<>(1);
        Channel<String> ch2 = new Channel<>(1);

        // when
        String sent = select(ch1.sendClause("v1", () -> "1"), ch2.sendClause("v2", () -> "2"));

        // then
        assertEquals("1", sent);
    }

    @Test
    public void testBufferExpandedWhenSelecting() throws InterruptedException {
        // given
        Channel<String> ch = new Channel<>(2);

        // when
        select(ch.sendClause("v1"));
        select(ch.sendClause("v2"));
        String r1 = ch.receive();
        String r2 = ch.receive();

        select(ch.sendClause("v3"));
        select(ch.sendClause("v4")); // none of the sends should block
        String r3 = ch.receive();
        String r4 = ch.receive();

        // then
        assertEquals("v1", r1);
        assertEquals("v2", r2);
        assertEquals("v3", r3);
        assertEquals("v4", r4);
    }
}
