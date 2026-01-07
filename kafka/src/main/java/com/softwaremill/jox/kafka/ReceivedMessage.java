package com.softwaremill.jox.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.record.TimestampType;

/**
 * A wrapper for a {@link ConsumerRecord} that provides easy access to its fields.
 *
 * @param consumerRecord The underlying Kafka consumer record.
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 */
public record ReceivedMessage<K, V>(ConsumerRecord<K, V> consumerRecord) {

    public K key() {
        return consumerRecord.key();
    }

    public V value() {
        return consumerRecord.value();
    }

    public Iterable<Header> headers() {
        return consumerRecord.headers();
    }

    public long offset() {
        return consumerRecord.offset();
    }

    public int partition() {
        return consumerRecord.partition();
    }

    public String topic() {
        return consumerRecord.topic();
    }

    public long timestamp() {
        return consumerRecord.timestamp();
    }

    public TimestampType timestampType() {
        return consumerRecord.timestampType();
    }
}
