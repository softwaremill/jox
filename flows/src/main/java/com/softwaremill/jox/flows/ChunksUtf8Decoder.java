package com.softwaremill.jox.flows;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;


/**
 * The general algorithm and some helper functions (with their comments) are copied from ox:
 * see <a href="https://github.com/softwaremill/ox/blob/master/core/src/main/scala/ox/flow/FlowTextOps.scala">ox.flow.FlowTextOps</a>
 * Which was copied from fs2: see fs2.text.decodeC <a href="https://github.com/typelevel/fs2/blob/9b1b27cf7a8d7027df852d890555b341da70ef9e/core/shared/src/main/scala/fs2/text.scala"">link</a>
 * <p>
 * Extracted to separate class for better readability
 */
class ChunksUtf8Decoder {
    private static final int BOM_SIZE = 3; // const for UTF-8
    private static final byte[] BOM_UTF8 = new byte[]{-17, -69, -65};

    public static <T> Flow<String> decodeStringUtf8(FlowStage<T> flowStage) {
        return Flows.usingEmit(emit -> {
            final AtomicReference<State> state = new AtomicReference<>(State.ProcessBOM);
            final AtomicReference<byte[]> buffer = new AtomicReference<>(null);

            flowStage.run(t -> {
                if (!(t instanceof byte[] bytes)) {
                    throw new IllegalArgumentException("requirement failed: method can be called only on flow containing byte[]");
                }
                byte[] newBuffer;
                State newState;
                if (state.get() == State.ProcessBOM) {
                    Map.Entry<byte[], State> processResult = processByteOrderMark(bytes, buffer.get());
                    newBuffer = processResult.getKey();
                    newState = processResult.getValue();
                } else {
                    newBuffer = doPull(bytes, buffer.get(), emit);
                    newState = State.Pull;
                }
                buffer.set(newBuffer);
                state.set(newState);
            });
            // A common case, worth checking in advance

            if (buffer.get() != null && buffer.get().length > 0) {
                emit.apply(new String(buffer.get(), StandardCharsets.UTF_8));
            }
        });
    }

    private static Map.Entry<byte[], State> processByteOrderMark(byte[] bytes, byte[] buffer) {
        // A common case, worth checking in advance
        if (buffer == null && bytes.length >= BOM_SIZE && startsWith(bytes, BOM_UTF8)) {
            return Map.entry(bytes, State.Pull);
        } else {
            byte[] newBuffer0 = buffer == null ? new byte[0] : buffer;
            byte[] newBuffer = Arrays.copyOf(newBuffer0, newBuffer0.length + bytes.length);
            newBuffer = ByteBuffer.wrap(newBuffer).put(newBuffer0.length, bytes).array();
            if (newBuffer.length >= BOM_SIZE) {
                byte[] rem = startsWith(newBuffer, BOM_UTF8) ? drop(newBuffer, BOM_SIZE) : newBuffer;
                return Map.entry(rem, State.Pull);
            } else if (startsWith(newBuffer, take(BOM_UTF8, newBuffer.length))) {
                return Map.entry(newBuffer, State.ProcessBOM); // we've accumulated less than the full BOM, let's pull some more
            } else {
                return Map.entry(newBuffer, State.Pull); // We've accumulated less than BOM size but we already know that these bytes aren't BOM
            }
        }
    }

    private static byte[] doPull(byte[] bytes, byte[] buffer, FlowEmit<String> output) throws Exception {
        var result = processSingleChunk(buffer, bytes);
        Optional<String> str = result.getKey();
        if (str.isPresent()) {
            output.apply(str.get());
        }
        return result.getValue();
    }

    private static Map.Entry<Optional<String>, byte[]> processSingleChunk(byte[] buffer, byte[] nextBytes) {
        byte[] allBytes;
        if (buffer == null || buffer.length == 0) {
            allBytes = nextBytes;
        } else {
            allBytes = Arrays.copyOf(buffer, buffer.length + nextBytes.length);
            allBytes = ByteBuffer.wrap(allBytes).put(buffer.length, nextBytes).array();
        }

        int splitAt = allBytes.length - lastIncompleteBytes(allBytes);
        if (splitAt == allBytes.length) {
            // in the common case of ASCII chars
            // we are in this branch so the next buffer will
            // be empty
            return Map.entry(Optional.of(new String(allBytes, StandardCharsets.UTF_8)), new byte[0]);
        } else if (splitAt == 0) {
            return Map.entry(Optional.empty(), allBytes);
        } else {
            return Map.entry(
                    // character
                    Optional.of(new String(Arrays.copyOfRange(allBytes, 0, splitAt), StandardCharsets.UTF_8)),
                    // remaining bytes
                    Arrays.copyOfRange(allBytes, splitAt, allBytes.length)
            );
        }
    }

    /**
     * Takes n elements from the beginning of the array and returns copy of the result
     */
    private static byte[] take(byte[] a, int n) {
        return Arrays.copyOfRange(a, 0, n);
    }

    /**
     * Drops n elements from the beginning of the array and returns copy of the result
     */
    private static byte[] drop(byte[] a, int n) {
        return Arrays.copyOfRange(a, n, a.length);
    }

    /**
     * Checks if array a starts with array b
     */
    private static boolean startsWith(byte[] a, byte[] b) {
        return ByteBuffer.wrap(a, 0, b.length).equals(ByteBuffer.wrap(b));
    }

    /*
     * Copied from scala lib fs2 (fs2.text.decodeC.lastIncompleteBytes)
     * Returns the length of an incomplete multi-byte sequence at the end of
     * `bs`. If `bs` ends with an ASCII byte or a complete multi-byte sequence,
     * 0 is returned.
     */
    private static int lastIncompleteBytes(byte[] bs) {
        int minIdx = Math.max(0, bs.length - 3);
        int idx = bs.length - 1;
        int counter = 0;
        int res = 0;
        while (minIdx <= idx) {
            int c = continuationBytes(bs[idx]);
            if (c >= 0) {
                if (c != counter) {
                    res = counter + 1;
                }
                return res;
            }
            idx--;
            counter++;
        }
        return res;
    }

    /*
     * Copied from scala lib fs2 (fs2.text.decodeC.continuationBytes)
     * Returns the number of continuation bytes if `b` is an ASCII byte or a
     * leading byte of a multi-byte sequence, and -1 otherwise.
     */
    private static int continuationBytes(byte b) {
        if ((b & 0x80) == 0x00) return 0; // ASCII byte
        else if ((b & 0xe0) == 0xc0) return 1; // leading byte of a 2 byte seq
        else if ((b & 0xf0) == 0xe0) return 2; // leading byte of a 3 byte seq
        else if ((b & 0xf8) == 0xf0) return 3; // leading byte of a 4 byte seq
        else return -1; // continuation byte or garbage
    }

    // we start in the ProcessBOM state, and then transit to the Pull state
    private enum State {
        ProcessBOM, Pull
    }
}
