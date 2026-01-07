package com.softwaremill.jox.kafka;

import java.util.List;

/**
 * A packet containing only commit messages (consumer records) to be committed.
 *
 * @param commit The messages to commit.
 */
public record CommitPacket(List<ReceivedMessage<?, ?>> commit) implements HasCommit {
    public static CommitPacket of(ReceivedMessage<?, ?> commit) {
        return new CommitPacket(List.of(commit));
    }
}
