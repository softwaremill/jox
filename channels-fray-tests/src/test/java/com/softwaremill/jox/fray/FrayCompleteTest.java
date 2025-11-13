package com.softwaremill.jox.fray;

import static com.softwaremill.jox.fray.Config.CHANNEL_SIZE;

import org.junit.jupiter.api.extension.ExtendWith;
import org.pastalab.fray.junit.junit5.FrayTestExtension;
import org.pastalab.fray.junit.junit5.annotations.ConcurrencyTest;

import com.softwaremill.jox.Channel;
import com.softwaremill.jox.ChannelDone;
import com.softwaremill.jox.ChannelError;

@ExtendWith(FrayTestExtension.class)
public class FrayCompleteTest {
    // send + done | receive

    @ConcurrencyTest
    public void sendDoneReceiveTest() throws InterruptedException {
        Channel<Integer> ch = Channel.newBufferedChannel(CHANNEL_SIZE);

        var f1 =
                Fork.newNoResult(
                        () -> {
                            ch.send(10);
                            ch.send(11);
                            ch.send(12);
                            ch.done();
                        });
        var f2 =
                Fork.newNoResult(
                        () -> {
                            assert (ch.receive() == 10);
                            assert (ch.receive() == 11);
                            assert (ch.receive() == 12);
                            assert (ch.receiveOrClosed() instanceof ChannelDone);
                        });

        Fork.startAll(f1, f2);
        Fork.joinAll(f1, f2);
    }

    // send + error | receive

    @ConcurrencyTest
    public void sendErrorReceiveTest_rendezvous() throws InterruptedException {
        Channel<Integer> ch = Channel.newRendezvousChannel();

        var f1 =
                Fork.newNoResult(
                        () -> {
                            ch.send(10);
                            ch.send(11);
                            ch.send(12);
                            ch.error(new RuntimeException("boom!"));
                        });
        var f2 =
                Fork.newNoResult(
                        () -> {
                            var r1 = ch.receiveOrClosed();
                            assert (Integer.valueOf(10).equals(r1) || r1 instanceof ChannelError);

                            var r2 = ch.receiveOrClosed();
                            assert (((!(r1 instanceof ChannelError))
                                            && Integer.valueOf(11).equals(r2))
                                    || r2 instanceof ChannelError);

                            var r3 = ch.receiveOrClosed();
                            assert (((!(r2 instanceof ChannelError))
                                            && Integer.valueOf(12).equals(r3))
                                    || r3 instanceof ChannelError);

                            var r4 = ch.receiveOrClosed();
                            assert (r4 instanceof ChannelError);
                        });

        Fork.startAll(f1, f2);
        Fork.joinAll(f1, f2);
    }
}
