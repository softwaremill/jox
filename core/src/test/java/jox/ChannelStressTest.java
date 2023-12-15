package jox;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;

import static jox.TestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChannelStressTest {
    /**
     * Runs a large number of send/receive operations in multiple threads, occasionally interrupting them. At the end,
     * verifies that messages are not duplicated and sent/received properly, as well as the channel's internal state is
     * correct.
     */
    @TestWithCapacities
    void testMultipleOperations(int capacity) throws Exception {
        System.out.println();

        int numberOfRepetitions = 10;
        int numberOfThreads = 8;
        int numberOfIterations = 1000;

        for (int r = 0; r < numberOfRepetitions; r++) {
            var ch = new Channel<String>(capacity);
            try {
                scoped(scope -> {
                    // start forks
                    var forks = new ArrayList<Future<StressTestThreadData>>();
                    for (int i = 0; i < numberOfThreads; i++) {
                        int finalI = i;
                        forks.add(fork(scope, () -> {
                            // in each fork, run the given number of iterations
                            var data = new StressTestThreadData(scope, ch, new Random(), finalI, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new Counter());
                            for (int j = 0; j < numberOfIterations; j++) {
                                stressTestThread(data, j);
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

                    List<String> remainingInChannel = drainChannel(scope, ch);
                    allReceived.addAll(remainingInChannel);
                    assertEquals(allSent, allReceived, "each sent message should have been received, or drained");

                    var segments = countOccurrences(ch.toString(), "Segment{");
                    // +1, as the buffer might be entirely in the next segment
                    // and another +1, as the send segment might not be moved forward, if in the next segment there are only IRs
                    var expectedSegments = Math.ceil((double) capacity / Segment.SEGMENT_SIZE) + 2;
                    assertTrue(segments <= expectedSegments, "there can be at most as much segments as needed to store the buffer + 1");
                });
            } finally {
                System.out.println("\nChannel state:");
                System.out.println(ch);
            }
        }
    }

    //

    private record StressTestThreadData(StructuredTaskScope<Object> scope, Channel<String> ch, Random random,
                                        int threadId,
                                        List<String> sent, List<String> received,
                                        List<String> sendInterrupted, Counter receiveInterrupted) {}

    private void stressTestThread(StressTestThreadData data, int iteration) throws InterruptedException, ExecutionException {

        switch (data.random.nextInt(2)) {
            case 0 -> {
                // send
                var msg = "T" + data.threadId + "I" + iteration;
                var shouldCancel = data.random.nextInt(4) == 0;

                var f = forkCancelable(data.scope, () -> data.ch.send(msg));

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
                } else {
                    data.sent.add(msg);
                }
            }
            case 1 -> {
                // receive
                var shouldCancel = data.random.nextInt(4) == 0;

                var f = forkCancelable(data.scope, data.ch::receive);

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
                } else {
                    data.received.add((String) result);
                }
            }
        }
    }

    // utilities

    private class Counter {
        int value;
    }

    private List<String> drainChannel(StructuredTaskScope<Object> scope, Channel<String> ch) throws ExecutionException, InterruptedException {
        var result = new ArrayList<String>();
        while (true) {
            var f = forkCancelable(scope, ch::receive);
            try {
                result.add(f.get(100, TimeUnit.MILLISECONDS));
            } catch (TimeoutException e) {
                // assuming nothing to receive
                f.cancel();
                return result;
            }
        }
    }

    private int countOccurrences(String str, String subStr) {
        int count = 0;
        int idx = 0;

        while ((idx = str.indexOf(subStr, idx)) != -1) {
            count++;
            idx += subStr.length();
        }

        return count;
    }
}
