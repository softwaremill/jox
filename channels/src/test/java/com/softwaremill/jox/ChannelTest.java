package com.softwaremill.jox;

import java.util.HashSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.softwaremill.jox.TestUtil.forkVoid;
import static com.softwaremill.jox.TestUtil.scoped;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Channel tests which are run for various capacities.
 */
public class ChannelTest {
    @TestWithCapacities
    void testSendReceiveInManyForks(int capacity) throws ExecutionException, InterruptedException {
        // given
        Channel<Integer> channel = capacity == 0 ? Channel.newRendezvousChannel() : Channel.newBufferedChannel(capacity);
        var fs = new HashSet<Future<Void>>();
        var s = new ConcurrentSkipListSet<Integer>();

        // when
        scoped(scope -> {
            for (int i = 1; i <= 1000; i++) {
                int ii = i;
                forkVoid(scope, () -> channel.send(ii));
            }
            for (int i = 1; i <= 1000; i++) {
                fs.add(forkVoid(scope, () -> s.add(channel.receive())));
            }
            for (Future<Void> f : fs) {
                f.get();
            }

            // then
            assertEquals(1000, s.size());
        });
    }

    @TestWithCapacities
    void testSendReceiveManyElementsInTwoForks(int capacity) throws ExecutionException, InterruptedException {
        // given
        Channel<Integer> channel = capacity == 0 ? Channel.newRendezvousChannel() : Channel.newBufferedChannel(capacity);
        var s = new ConcurrentSkipListSet<Integer>();

        // when
        scoped(scope -> {
            forkVoid(scope, () -> {
                for (int i = 1; i <= 1000; i++) {
                    channel.send(i);
                }
            });
            forkVoid(scope, () -> {
                for (int i = 1; i <= 1000; i++) {
                    s.add(channel.receive());
                }
            }).get();

            // then
            assertEquals(1000, s.size());
        });
    }
}


