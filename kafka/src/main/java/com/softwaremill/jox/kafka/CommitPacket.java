package com.softwaremill.jox.kafka;

import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * A packet containing only commit messages (consumer records) to be committed.
 *
 * @param commit The messages to commit.
 */
public record CommitPacket(List<ConsumerRecord<?, ?>> commit) implements HasCommit {
    public static CommitPacket of(ConsumerRecord<?, ?> commit) {
        return new CommitPacket(List.of(commit));
    }
}
