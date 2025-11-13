package com.softwaremill.jox.fray;

import static com.softwaremill.jox.Select.select;
import static com.softwaremill.jox.fray.Config.CHANNEL_SIZE;

import java.util.ArrayList;

import org.junit.jupiter.api.extension.ExtendWith;
import org.pastalab.fray.junit.junit5.FrayTestExtension;
import org.pastalab.fray.junit.junit5.annotations.ConcurrencyTest;

import com.softwaremill.jox.Channel;
import com.softwaremill.jox.ChannelDone;

@ExtendWith(FrayTestExtension.class)
public class FrayInterruptTest {
    // send <- interrupt | receive

    @ConcurrencyTest
    public void interruptSendReceive() throws InterruptedException {
        Channel<Integer> ch = Channel.newBufferedChannel(CHANNEL_SIZE);

        var f1 =
                Fork.newNoResult(
                        () -> {
                            try {
                                ch.send(10);
                            } catch (InterruptedException e) {
                                ch.send(11);
                            } finally {
                                ch.done();
                            }
                        });

        var f2 = Fork.newNoResult(f1::interrupt);

        var f3 =
                Fork.newNoResult(
                        () -> {
                            var r = ch.receive();
                            assert (r == 10 || r == 11);
                            // only one value should be sent
                            assert (ch.receiveOrClosed() instanceof ChannelDone);
                        });

        Fork.startAll(f1, f2, f3);
        Fork.joinAll(f1, f2, f3);
    }

    // send | receive <- interrupt

    @ConcurrencyTest
    public void sendInterruptReceive() throws InterruptedException {
        Channel<Integer> ch = Channel.newBufferedChannel(CHANNEL_SIZE);

        var f1 =
                Fork.newNoResult(
                        () -> {
                            ch.send(10);
                            ch.done();
                        });

        var f2 =
                Fork.newNoResult(
                        () -> {
                            try {
                                assert (ch.receive() == 10);

                                try {
                                    // only one value should be sent
                                    assert (ch.receiveOrClosed() instanceof ChannelDone);
                                } catch (InterruptedException e) {
                                    assert (ch.receiveOrClosed() instanceof ChannelDone);
                                }
                            } catch (InterruptedException e) {
                                // the value should be still "receivable"
                                assert (ch.receive() == 10);

                                // only one value should be sent
                                assert (ch.receiveOrClosed() instanceof ChannelDone);
                            }
                        });

        var f3 = Fork.newNoResult(f2::interrupt);

        Fork.startAll(f1, f2, f3);
        Fork.joinAll(f1, f2, f3);
    }

    // select(send) <- interrupt | receive

    @ConcurrencyTest
    public void interruptSelectSendReceive() throws InterruptedException {
        Channel<Integer> ch = Channel.newBufferedChannel(CHANNEL_SIZE);

        var f1 =
                Fork.newNoResult(
                        () -> {
                            try {
                                select(ch.sendClause(10));
                            } catch (InterruptedException e) {
                                select(ch.sendClause(11));
                            } finally {
                                ch.done();
                            }
                        });

        var f2 = Fork.newNoResult(f1::interrupt);

        var f3 =
                Fork.newNoResult(
                        () -> {
                            var r = select(ch.receiveClause());
                            assert (r == 10 || r == 11);
                            // only one value should be sent
                            assert (ch.receiveOrClosed() instanceof ChannelDone);
                        });

        Fork.startAll(f1, f2, f3);
        Fork.joinAll(f1, f2, f3);
    }

    // multiple sends <- interrupt | receive

    @ConcurrencyTest
    public void interruptMultiSendReceive() throws InterruptedException {
        int concurrency = 10;
        Channel<Integer> ch = Channel.newBufferedChannel(CHANNEL_SIZE);

        var sendForks = new ArrayList<Fork<Void>>();
        var interruptForks = new ArrayList<Fork<Void>>();

        for (int i = 0; i < concurrency; i++) {
            final var finalI = i;
            var sendFork =
                    Fork.newNoResult(
                            () -> {
                                try {
                                    select(ch.sendClause(finalI));
                                } catch (InterruptedException e) {
                                    select(ch.sendClause(finalI));
                                }
                            });
            sendForks.add(sendFork);

            interruptForks.add(Fork.newNoResult(sendFork::interrupt));
        }

        var receiveFork =
                Fork.newWithResult(
                        () -> {
                            var r = 0;
                            for (int i = 0; i < concurrency; i++) {
                                r += ch.receive();
                            }
                            return r;
                        });

        Fork.startAll(sendForks.toArray(new Fork<?>[0]));
        Fork.startAll(interruptForks.toArray(new Fork<?>[0]));
        receiveFork.start();

        for (Fork<Void> sendFork : sendForks) {
            sendFork.join();
        }
        for (Fork<Void> interruptFork : interruptForks) {
            interruptFork.join();
        }
        assert (receiveFork.join() == concurrency * (concurrency - 1) / 2);
    }
}
