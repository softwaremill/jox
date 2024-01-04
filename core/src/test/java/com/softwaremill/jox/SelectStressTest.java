package com.softwaremill.jox;

import java.util.*;
import java.util.concurrent.*;

import static com.softwaremill.jox.Select.selectSafe;
import static com.softwaremill.jox.TestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SelectStressTest {
    /**
     * Runs a large number of send/receive operations in multiple threads, occasionally interrupting them. At the end,
     * closes the channel as "done".
     * <p>
     * Verifies that messages are not duplicated and sent/received properly, as well as the channel's internal state is
     * correct.
     */
    @TestWithCapacities
    void testMultipleOperations(int capacity) throws Exception {
        System.out.println();

        int numberOfRepetitions = 10;
        int numberOfThreads = 8;
        int numberOfIterations = 2000;
        int numberOfChannels = 10;

        for (int r = 0; r < numberOfRepetitions; r++) {
            var chs = new ArrayList<Channel<String>>();
            for (int i = 0; i < numberOfChannels; i++) {
                chs.add(new Channel<>(capacity));
            }
            try {
                scoped(scope -> {
                    // start forks
                    var forks = new ArrayList<Future<StressTestThreadData>>();
                    for (int i = 0; i < numberOfThreads; i++) {
                        int finalI = i;
                        forks.add(fork(scope, () -> {
                            // in each fork, run the given number of iterations; copying the list as it's mutated by each thread
                            var data = new StressTestThreadData(scope, new ArrayList<>(chs), new Random(), finalI, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new Counter());
                            for (int j = 0; j < numberOfIterations; j++) {
                                stressTestIteration(data, j);
                            }
                            // if this is the first fork, after all the iterations complete, close the channel
                            if (finalI == 0) {
                                for (var ch : chs) {
                                    ch.done();
                                }
                            }
                            return data;
                        }));
                    }

                    // collect data from all forks, perform duplicate checks
                    Set<String> allSent = new HashSet<>();
                    Set<String> allReceived = new HashSet<>();

                    for (var fork : forks) {
                        var data = fork.get();
                        System.out.println("Fork done, sent: " + data.sent.size() + ", received: " + data.received.size() + ", sendInterrupted: " + data.sendInterrupted.size() + ", receiveInterrupted: " + data.receiveInterrupted.value);

                        // sanity check: each iteration should produce a result
                        assertEquals(numberOfIterations, data.sent.size() + data.sendInterrupted.size() + data.received.size() + data.receiveInterrupted.value);

                        var sentSet = new HashSet<>(data.sent);
                        var receivedSet = new HashSet<>(data.received);

                        // every message should be sent & received once
                        assertEquals(data.sent.size(), sentSet.size());
                        assertEquals(data.received.size(), receivedSet.size());

                        allSent.addAll(sentSet);
                        allReceived.addAll(receivedSet);
                    }

                    // check received messages
                    Set<String> receivedNotSent = new HashSet<>(allReceived);
                    receivedNotSent.removeAll(allSent);
                    assertEquals(Set.of(), receivedNotSent, "each received message should have been sent");

                    for (var ch : chs) {
                        List<String> remainingInChannel = drainChannel(ch);
                        allReceived.addAll(remainingInChannel);
                    }
                    assertEquals(allSent, allReceived, "each sent message should have been received, or drained");

                    for (var ch : chs) {
                        var segments = countOccurrences(ch.toString(), "Segment{");
                        // +1, as the buffer might be entirely in the next segment
                        // and another +1, as the send segment might not be moved forward, if in the next segment there are only IRs
                        var expectedSegments = Math.ceil((double) capacity / Segment.SEGMENT_SIZE) + 2;
                        assertTrue(segments <= expectedSegments, "there can be at most as much segments as needed to store the buffer + 1");
                    }
                });
            } finally {
                System.out.println("\nChannels state:");
                for (var ch : chs) {
                    System.out.println(ch);
                }
            }
        }
    }

    //

    private record StressTestThreadData(StructuredTaskScope<Object> scope, List<Channel<String>> chs, Random random,
                                        int threadId,
                                        List<String> sent, List<String> received,
                                        List<String> sendInterrupted, Counter receiveInterrupted) {}

    private void stressTestIteration(StressTestThreadData data, int iteration) throws InterruptedException, ExecutionException {
        switch (data.random.nextInt(2)) {
            case 0 -> {
                // send
                var msg = "T" + data.threadId + "I" + iteration;
                var shouldCancel = data.random.nextInt(4) == 0;

                var channels = data.chs;
                Collections.shuffle(channels);
                var f = forkCancelable(data.scope, () -> selectSafe(channels.stream().map(ch -> ch.sendClause(msg)).toArray(SelectClause[]::new)));

                Object result;
                if (shouldCancel) {
                    Thread.yield();
                    result = f.cancel();
                } else {
                    try {
                        result = f.get(10, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException e) {
                        result = f.cancel();
                    }
                }

                if (result instanceof Exception) {
                    data.sendInterrupted.add(msg);
                } else if (result instanceof ChannelClosed) {
                    // we count the cases where the channel was closed as interrupted
                    data.sendInterrupted.add(msg);
                } else {
                    data.sent.add(msg);
                }
            }
            case 1 -> {
                // receive
                var shouldCancel = data.random.nextInt(4) == 0;

                var channels = data.chs;
                Collections.shuffle(channels);
                var f = forkCancelable(data.scope, () -> selectSafe(channels.stream().map(Channel::receiveClause).toArray(SelectClause[]::new)));

                Object result;
                if (shouldCancel) {
                    Thread.yield();
                    result = f.cancel();
                } else {
                    try {
                        result = f.get(10, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException e) {
                        result = f.cancel();
                    }
                }

                if (result instanceof Exception) {
                    data.receiveInterrupted.value++;
                } else if (result instanceof ChannelClosed) {
                    // we count the cases where the channel was closed as interrupted
                    data.receiveInterrupted.value++;
                } else {
                    data.received.add((String) result);
                }
            }
        }
    }

    // utilities

    private static class Counter {
        int value;
    }
}
