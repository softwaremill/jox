package com.softwaremill.jox.kafka;

import static com.softwaremill.jox.structured.Scopes.supervised;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.flows.FlowEmit;
import com.softwaremill.jox.flows.Flows;
import com.softwaremill.jox.structured.ActorRef;

public final class KafkaFlow {

    private static final Logger logger = LoggerFactory.getLogger(KafkaFlow.class);

    private KafkaFlow() {}

    public static <K, V> Flow<ConsumerRecord<K, V>> subscribe(
            ConsumerSettings<K, V> settings, String topic, String... otherTopics) {
        return subscribe(settings.toConsumer(), true, topic, otherTopics);
    }

    public static <K, V> Flow<ConsumerRecord<K, V>> subscribe(
            KafkaConsumer<K, V> kafkaConsumer,
            boolean closeWhenComplete,
            String topic,
            String... otherTopics) {
        return Flows.usingEmit(
                emit -> {
                    supervised(
                            scope -> {
                                final var kafkaConsumerActor =
                                        KafkaConsumerWrapper.create(
                                                scope, kafkaConsumer, closeWhenComplete);
                                doSubscribe(kafkaConsumerActor, topic, otherTopics, emit);
                                return null;
                            });
                });
    }

    public static <K, V> Flow<ConsumerRecord<K, V>> subscribe(
            ActorRef<KafkaConsumerWrapper<K, V>> kafkaConsumerActor,
            String topic,
            String... otherTopics) {
        return Flows.usingEmit(emit -> doSubscribe(kafkaConsumerActor, topic, otherTopics, emit));
    }

    private static <K, V> void doSubscribe(
            ActorRef<KafkaConsumerWrapper<K, V>> kafkaConsumerActor,
            String topic,
            String[] otherTopics,
            FlowEmit<ConsumerRecord<K, V>> emit)
            throws Exception {
        final var topics = new ArrayList<String>();
        topics.add(topic);
        Collections.addAll(topics, otherTopics);

        kafkaConsumerActor.tell(wrapper -> wrapper.subscribe(topics));
        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                var records = kafkaConsumerActor.ask(KafkaConsumerWrapper::poll);
                for (var r : records) {
                    emit.apply(r);
                }
            }
        } catch (Exception e) {
            logger.error("Exception when polling for records", e);
            throw e;
        }
    }
}
