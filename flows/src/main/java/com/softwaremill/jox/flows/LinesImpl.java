package com.softwaremill.jox.flows;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class LinesImpl {

    static <T> Flow<String> lines(Charset charset, Flow<T> parentFlow) {
        return parentFlow.mapStatefulConcat(Optional::<ByteChunk>empty,
                (buffer, nextChunk) -> {
                    ByteChunk chunk;
                    if (nextChunk instanceof ByteChunk byteChunk) {
                        chunk = byteChunk;
                    } else if (nextChunk instanceof byte[] rawBytes) {
                        chunk = ByteChunk.fromArray(rawBytes);
                    } else {
                        throw new IllegalArgumentException("requirement failed: method can be called only on flow containing ByteChunk or byte[]");
                    }
                    if (chunk.length() == 0) {
                        // get next incoming chunk
                        return Map.entry(Optional.empty(), Collections.emptyList());
                    }

                    // check if chunk contains newline character, if not proceed to the next chunk
                    int newLineIndex = chunk.indexWhere(b -> b == '\n');
                    if (newLineIndex == -1) {
                        if (buffer.isEmpty()) {
                            return Map.entry(Optional.of(chunk), Collections.emptyList());
                        }
                        return Map.entry(Optional.of(buffer.get().concat(chunk)), Collections.emptyList());
                    }

                    // buffer for lines, if chunk contains more than one newline character
                    List<ByteChunk> lines = new ArrayList<>();

                    // variable used to clear buffer after using it
                    ByteChunk bufferFromPreviousChunk = buffer.orElse(ByteChunk.empty());
                    while (chunk.length() > 0 && newLineIndex != -1) {
                        Map.Entry<ByteChunk, ByteChunk> split = chunk.splitAt(newLineIndex);
                        var line = split.getKey();
                        var newChunk = split.getValue().drop(1);

                        if (bufferFromPreviousChunk.length() > 0) {
                            // concat accumulated buffer and line
                            lines.add(bufferFromPreviousChunk.concat(line));
                            // cleanup buffer
                            bufferFromPreviousChunk = ByteChunk.empty();
                        } else {
                            lines.add(line);
                        }
                        chunk = newChunk;
                        newLineIndex = chunk.indexWhere(b -> b == '\n');
                    }
                    return Map.entry(Optional.of(chunk), lines);
                },
                buf -> buf
        )
                .map(chunk -> chunk.convertToString(charset));
    }
}
