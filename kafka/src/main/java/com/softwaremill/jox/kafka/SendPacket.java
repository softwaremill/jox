package com.softwaremill.jox.kafka;

import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * A packet containing messages to send and messages to commit.
 *
 * @param send The messages to send.
 * @param commit The messages to commit.
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 */
public record SendPacket<K, V>(List<ProducerRecord<K, V>> send, List<ConsumerRecord<?, ?>> commit)
        implements HasCommit {
    public static <K, V> SendPacket<K, V> of(
            ProducerRecord<K, V> send, ConsumerRecord<?, ?> commit) {
        return new SendPacket<>(List.of(send), List.of(commit));
    }

    public static <K, V> SendPacket<K, V> of(
            List<ProducerRecord<K, V>> send, ConsumerRecord<?, ?> commit) {
        return new SendPacket<>(send, List.of(commit));
    }
}
