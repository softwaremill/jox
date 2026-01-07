package com.softwaremill.jox.kafka;

import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/** Common interface for packets that contain messages to commit. */
public interface HasCommit {
    List<ConsumerRecord<?, ?>> commit();
}
