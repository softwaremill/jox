package com.softwaremill.jox.fray;

import static com.softwaremill.jox.Select.select;
import static com.softwaremill.jox.fray.Config.CHANNEL_SIZE;

import java.util.ArrayList;

import org.junit.jupiter.api.extension.ExtendWith;
import org.pastalab.fray.junit.junit5.FrayTestExtension;
import org.pastalab.fray.junit.junit5.annotations.ConcurrencyTest;

import com.softwaremill.jox.Channel;
import com.softwaremill.jox.SelectClause;

@ExtendWith(FrayTestExtension.class)
public class FraySelectTest {
    // select(send) | select(receive)

    @ConcurrencyTest
    public void sendReceiveWithSelectTest() throws InterruptedException {
        Channel<Integer> ch = Channel.newBufferedChannel(CHANNEL_SIZE);

        Fork<Void> f1 = Fork.newNoResult(() -> select(ch.sendClause(10)));
        Fork<Integer> f2 = Fork.newWithResult(() -> select(ch.receiveClause()));

        Fork.startAll(f1, f2);
        f1.join();

        assert (f2.join() == 10);
    }

    // send | send | select(receive, receive)

    @ConcurrencyTest
    public void sendSendSelectTest() throws InterruptedException {
        Channel<Integer> ch1 = Channel.newBufferedChannel(CHANNEL_SIZE);
        Channel<Integer> ch2 = Channel.newBufferedChannel(CHANNEL_SIZE);

        Fork<Void> f1 = Fork.newNoResult(() -> select(ch1.sendClause(10)));
        Fork<Void> f2 = Fork.newNoResult(() -> select(ch2.sendClause(20)));
        Fork<Integer> f3 =
                Fork.newWithResult(() -> select(ch1.receiveClause(), ch2.receiveClause()));

        Fork.startAll(f1, f2, f3);

        int joined = f3.join();
        if (joined == 10) {
            assert (ch2.receive() == 20);
        } else if (joined == 20) {
            assert (ch1.receive() == 10);
        } else {
            assert false;
        }

        f1.join();
        f2.join();
    }

    // many sends | many select(many receives)

    @ConcurrencyTest
    public void multiSendMultipleSelectReceiveTest() throws InterruptedException {
        // segment size is 32, this covers more than 1 segment
        int concurrency = 40;

        var channels = new ArrayList<Channel<Integer>>();
        for (int i = 0; i < concurrency; i++) {
            channels.add(Channel.newBufferedChannel(CHANNEL_SIZE));
        }

        var sendForks = new ArrayList<Fork<Void>>();
        var receiveForks = new ArrayList<Fork<Integer>>();

        for (int i = 0; i < concurrency; i++) {
            final var finalI = i;
            sendForks.add(Fork.newNoResult(() -> channels.get(finalI).send(finalI)));
            receiveForks.add(
                    Fork.newWithResult(
                            () -> {
                                var clauses = new SelectClause<?>[concurrency];
                                for (int j = 0; j < concurrency; j++) {
                                    clauses[j] = channels.get(j).receiveClause();
                                }
                                return (Integer) select(clauses);
                            }));
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
