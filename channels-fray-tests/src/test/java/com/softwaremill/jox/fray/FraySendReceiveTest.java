package com.softwaremill.jox.fray;

import static com.softwaremill.jox.fray.Config.CHANNEL_SIZE;

import java.util.ArrayList;

import org.junit.jupiter.api.extension.ExtendWith;
import org.pastalab.fray.junit.junit5.FrayTestExtension;
import org.pastalab.fray.junit.junit5.annotations.ConcurrencyTest;

import com.softwaremill.jox.Channel;

@ExtendWith(FrayTestExtension.class)
public class FraySendReceiveTest {
    // send | receive

    @ConcurrencyTest
    public void sendReceiveTest() throws InterruptedException {
        Channel<Integer> ch = Channel.newBufferedChannel(CHANNEL_SIZE);

        Fork<Void> f1 = Fork.newNoResult(() -> ch.send(10));
        Fork<Integer> f2 = Fork.newWithResult(ch::receive);

        Fork.startAll(f1, f2);
        f1.join();

        assert (f2.join() == 10);
    }

    // send | send | receive | receive

    @ConcurrencyTest
    public void sendSendReceiveReceiveTest() throws InterruptedException {
        Channel<Integer> ch = Channel.newBufferedChannel(CHANNEL_SIZE);

        Fork<Void> f1 = Fork.newNoResult(() -> ch.send(10));
        Fork<Void> f2 = Fork.newNoResult(() -> ch.send(20));
        Fork<Integer> f3 = Fork.newWithResult(ch::receive);
        Fork<Integer> f4 = Fork.newWithResult(ch::receive);

        Fork.startAll(f1, f2, f3, f4);

        assert (f3.join() + f4.join() == 30);
    }

    // many sends | many receives

    @ConcurrencyTest
    public void multiSendMultipleReceiveTest() throws InterruptedException {
        Channel<Integer> ch = Channel.newBufferedChannel(CHANNEL_SIZE);

        // segment size is 32 by default, this covers more than 1 segment
        int concurrency = 40;

        var sendForks = new ArrayList<Fork<Void>>();
        var receiveForks = new ArrayList<Fork<Integer>>();

        for (int i = 0; i < concurrency; i++) {
            final var finalI = i;
            sendForks.add(Fork.newNoResult(() -> ch.send(finalI)));
            receiveForks.add(Fork.newWithResult(ch::receive));
        }

        Fork.startAll(sendForks.toArray(new Fork<?>[0]));
        Fork.startAll(receiveForks.toArray(new Fork<?>[0]));

        for (Fork<Void> sendFork : sendForks) {
            sendFork.join();
        }

        var result = 0;
        for (Fork<Integer> receiveFork : receiveForks) {
            result += receiveFork.join();
        }

        assert (result == concurrency * (concurrency - 1) / 2);
    }
}
