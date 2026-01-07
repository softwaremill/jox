package com.softwaremill.jox.kafka;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.LoggerFactory;

import com.softwaremill.jox.structured.ActorRef;
import com.softwaremill.jox.structured.Scope;

public interface KafkaConsumerWrapper<K, V> {

    void subscribe(List<String> topics);

    ConsumerRecords<K, V> poll();

    void commit(Map<TopicPartition, Long> offsets);

    static <K, V> ActorRef<KafkaConsumerWrapper<K, V>> create(
            Scope scope, KafkaConsumer<K, V> consumer, boolean closeWhenComplete)
            throws InterruptedException {
        final var logger = LoggerFactory.getLogger(KafkaConsumerWrapper.class);

        KafkaConsumerWrapper<K, V> logic =
                new KafkaConsumerWrapper<>() {
                    @Override
                    public void subscribe(List<String> topics) {
                        try {
                            consumer.subscribe(topics);
                        } catch (Throwable t) {
                            logger.error("Exception when subscribing to {}", topics, t);
                            throw t;
                        }
                    }

                    @Override
                    public ConsumerRecords<K, V> poll() {
                        try {
                            return consumer.poll(Duration.ofMillis(100));
                        } catch (Throwable t) {
                            logger.error("Exception when polling for records in Kafka", t);
                            throw t;
                        }
                    }

                    @Override
                    public void commit(Map<TopicPartition, Long> offsets) {
                        try {
                            final var offsetAndMetadataMap =
                                    offsets.entrySet().stream()
                                            .collect(
                                                    Collectors.toMap(
                                                            Map.Entry::getKey,
                                                            e ->
                                                                    new OffsetAndMetadata(
                                                                            e.getValue() + 1)));
                            consumer.commitSync(offsetAndMetadataMap);
                        } catch (Throwable t) {
                            logger.error("Exception when committing offsets", t);
                            throw t;
                        }
                    }
                };

        return ActorRef.create(
                scope,
                logic,
                _ -> {
                    if (closeWhenComplete) {
                        logger.debug("Closing the Kafka consumer");
                        consumer.close();
                    }
                });
    }
}
