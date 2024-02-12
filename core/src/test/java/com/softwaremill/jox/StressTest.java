package com.softwaremill.jox;

import java.util.*;
import java.util.concurrent.*;

import static com.softwaremill.jox.Select.selectSafe;
import static com.softwaremill.jox.TestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

public class StressTest {
    @TestWithCapacities
    void testMultipleOperationsDirect(int capacity) throws Exception {
        testAndVerify(capacity, true);
    }

    @TestWithCapacities
    void testMultipleOperationsSelect(int capacity) throws Exception {
        testAndVerify(capacity, false);
    }

    /**
     * Runs a large number of send/receive operations in multiple threads, occasionally interrupting them. At the end,
     * closes the channel as "done".
     * <p>
     * Verifies that messages are not duplicated and sent/received properly, as well as the channel's internal state is
     * correct.
     */
    private void testAndVerify(int capacity, boolean direct) throws Exception {
        boolean ci = System.getenv("CI") != null;
        System.out.println("Running in ci: " + ci);

        int numberOfRepetitions = ci ? 20 : 5;
        int numberOfThreads = 8;
        int numberOfIterations = ci ? 2000 : 100;
        int numberOfChannels = direct ? 1 : 10;

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
                            // in each fork, run the given number of iterations; copying the channels list as it might be mutated by each thread
                            var data = new StressTestThreadData(scope, new ArrayList<>(chs), new Random(), finalI, direct, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new Counter());
                            for (int j = 0; j < numberOfIterations; j++) {
                                stressTestIteration(data, j);
                            }
                            // if this is the first fork, after all the iterations complete, close the channels
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
                        assertTrue(ch.isClosedForReceive());
                    }
                    assertEquals(allSent, allReceived, "each sent message should have been received, or drained");

                    for (var ch : chs) {
                        var segments = countOccurrences(ch.toString(), "Segment{");
                        // In a worst-case scenario, the first thread might close the channel (using `done()`): this prevents the
                        // `sendSegment` reference from advancing. All other threads might have started a `send()` just before this,
                        // so the number of in-flight elements, waiting to be received (at the moment of calling `done()`) is
                        // `numberOfThreads + bufferSize`. Each element might have a separate segment (as all other cells might be
                        // interrupted/broken, so this also is a theoretical number of segments.
                        // This needs to be incremented by the number of in-buffer cells. Additionally, they might only start in the
                        // next segment - if there's a buffer.
                        // And another +1, as the tail segment might consist of IRs only.
                        var maxSegments = numberOfThreads + capacity + Math.ceil((double) capacity / Segment.SEGMENT_SIZE) + (capacity > 0 ? 1 : 0) + 1;
                        assertTrue(segments <= maxSegments, "there can be at most as much segments as needed to store the buffer + 1, but got: " + segments + " instead of " + maxSegments + ".");
                    }
                });
            } catch (Exception e) {
                System.out.println("\nFailed!");
            } finally {
                System.out.println("\nChannel state:");
                for (var ch : chs) {
                    System.out.println(ch);
                }
                System.out.println();
            }
        }
    }

    //

    private record StressTestThreadData(StructuredTaskScope<Object> scope, List<Channel<String>> chs, Random random,
                                        int threadId, boolean direct, List<String> sent, List<String> received,
                                        List<String> sendInterrupted, Counter receiveInterrupted) {}

    private void stressTestIteration(StressTestThreadData data, int iteration) throws InterruptedException, ExecutionException {
        switch (data.random.nextInt(2)) {
            case 0 -> {
                // send
                var msg = "T" + data.threadId + "I" + iteration;
                var shouldCancel = data.random.nextInt(4) == 0;

                Fork<Object> f;
                if (data.direct) {
                    f = forkCancelable(data.scope, () -> data.chs.get(0).sendSafe(msg));
                } else {
                    var channels = data.chs;
                    Collections.shuffle(channels);
                    f = forkCancelable(data.scope, () -> selectSafe(channels.stream().map(ch -> ch.sendClause(msg)).toArray(SelectClause[]::new)));
                }

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

                Fork<Object> f;
                if (data.direct) {
                    f = forkCancelable(data.scope, data.chs.get(0)::receiveSafe);
                } else {
                    var channels = data.chs;
                    Collections.shuffle(channels);
                    f = forkCancelable(data.scope, () -> selectSafe(channels.stream().map(Channel::receiveClause).toArray(SelectClause[]::new)));
                }

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
