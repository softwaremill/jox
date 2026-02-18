package com.softwaremill.jox.fray;

import static com.softwaremill.jox.fray.Config.CHANNEL_SIZE;

import java.util.ArrayList;

import org.junit.jupiter.api.extension.ExtendWith;
import org.pastalab.fray.junit.junit5.FrayTestExtension;
import org.pastalab.fray.junit.junit5.annotations.ConcurrencyTest;

import com.softwaremill.jox.Channel;
import com.softwaremill.jox.ChannelDone;

@ExtendWith(FrayTestExtension.class)
public class FrayTrySendReceiveTest {

    // trySend | receive

    @ConcurrencyTest
    public void trySendReceiveTest() throws InterruptedException {
        Channel<Integer> ch = Channel.newBufferedChannel(CHANNEL_SIZE);

        Fork<Void> f1 =
                Fork.newNoResult(
                        () -> {
                            if (!ch.trySend(10)) {
                                ch.send(10);
                            }
                        });
        Fork<Integer> f2 = Fork.newWithResult(ch::receive);

        Fork.startAll(f1, f2);
        f1.join();

        assert (f2.join() == 10);
    }

    // send | tryReceive

    @ConcurrencyTest
    public void sendTryReceiveTest() throws InterruptedException {
        Channel<Integer> ch = Channel.newBufferedChannel(CHANNEL_SIZE);

        Fork<Void> f1 = Fork.newNoResult(() -> ch.send(10));
        Fork<Integer> f2 = Fork.newWithResult(ch::tryReceive);

        Fork.startAll(f1, f2);
        f1.join();
        Integer received = f2.join();

        assert (received == null || received == 10);
    }

    // trySend | tryReceive

    @ConcurrencyTest
    public void trySendTryReceiveTest() throws InterruptedException {
        Channel<Integer> ch = Channel.newBufferedChannel(CHANNEL_SIZE);

        Fork<Boolean> f1 = Fork.newWithResult(() -> ch.trySend(10));
        Fork<Integer> f2 = Fork.newWithResult(ch::tryReceive);

        Fork.startAll(f1, f2);
        boolean sent = f1.join();
        Integer received = f2.join();

        if (received != null) {
            assert sent;
            assert (received == 10);
        }
    }

    // multiple trySend | multiple tryReceive

    @ConcurrencyTest
    public void multiTrySendMultiTryReceiveTest() throws InterruptedException {
        Channel<Integer> ch = Channel.newBufferedChannel(CHANNEL_SIZE);

        int concurrency = 4;

        var sendForks = new ArrayList<Fork<Void>>();
        var receiveForks = new ArrayList<Fork<Integer>>();

        for (int i = 0; i < concurrency; i++) {
            final var finalI = i;
            sendForks.add(
                    Fork.newNoResult(
                            () -> {
                                if (!ch.trySend(finalI)) {
                                    ch.send(finalI);
                                }
                            }));
            receiveForks.add(
                    Fork.newWithResult(
                            () -> {
                                Integer result = ch.tryReceive();
                                if (result != null) return result;
                                return ch.receive();
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

    // trySend | tryReceive (rendezvous)

    @ConcurrencyTest
    public void trySendTryReceive_rendezvousTest() throws InterruptedException {
        Channel<Integer> ch = Channel.newRendezvousChannel();

        Fork<Void> f1 =
                Fork.newNoResult(
                        () -> {
                            if (!ch.trySend(10)) {
                                ch.send(10);
                            }
                        });
        Fork<Integer> f2 =
                Fork.newWithResult(
                        () -> {
                            Integer result = ch.tryReceive();
                            if (result != null) return result;
                            return ch.receive();
                        });

        Fork.startAll(f1, f2);
        f1.join();

        assert (f2.join() == 10);
    }

    // trySend | receive (rendezvous)

    @ConcurrencyTest
    public void trySendReceive_rendezvousTest() throws InterruptedException {
        Channel<Integer> ch = Channel.newRendezvousChannel();

        Fork<Void> f1 =
                Fork.newNoResult(
                        () -> {
                            if (!ch.trySend(10)) {
                                ch.send(10);
                            }
                        });
        Fork<Integer> f2 = Fork.newWithResult(ch::receive);

        Fork.startAll(f1, f2);
        f1.join();

        assert (f2.join() == 10);
    }

    // send | tryReceive (rendezvous)

    @ConcurrencyTest
    public void sendTryReceive_rendezvousTest() throws InterruptedException {
        Channel<Integer> ch = Channel.newRendezvousChannel();

        Fork<Void> f1 = Fork.newNoResult(() -> ch.send(10));
        Fork<Integer> f2 =
                Fork.newWithResult(
                        () -> {
                            Integer result = ch.tryReceive();
                            if (result != null) return result;
                            return ch.receive();
                        });

        Fork.startAll(f1, f2);
        f1.join();

        assert (f2.join() == 10);
    }

    // trySend | tryReceive (unlimited)

    @ConcurrencyTest
    public void trySendTryReceive_unlimitedTest() throws InterruptedException {
        Channel<Integer> ch = Channel.newUnlimitedChannel();

        Fork<Boolean> f1 = Fork.newWithResult(() -> ch.trySend(10));
        Fork<Integer> f2 = Fork.newWithResult(ch::tryReceive);

        Fork.startAll(f1, f2);
        boolean sent = f1.join();
        Integer received = f2.join();

        assert sent;
        assert received == null || (received == 10);
    }

    // trySend | close | tryReceive

    @ConcurrencyTest
    public void trySendCloseTryReceiveTest() throws InterruptedException {
        Channel<Integer> ch = Channel.newBufferedChannel(CHANNEL_SIZE);

        Fork<Boolean> f1 =
                Fork.newWithResult(
                        () -> {
                            Object r = ch.trySendOrClosed(10);
                            return r == null;
                        });

        Fork<Void> f2 = Fork.newNoResult(ch::done);

        Fork<Object> f3 = Fork.newWithResult(ch::tryReceiveOrClosed);

        Fork.startAll(f1, f2, f3);

        boolean sent = f1.join();
        f2.join();
        Object received = f3.join();

        if (received != null && !(received instanceof ChannelDone)) {
            assert (received.equals(10));
            assert sent;
        }
    }
}
