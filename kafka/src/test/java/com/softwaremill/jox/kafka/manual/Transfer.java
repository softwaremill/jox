package com.softwaremill.jox.kafka.manual;

import static com.softwaremill.jox.kafka.manual.Util.timedAndLogged;
import static com.softwaremill.jox.structured.Scopes.supervised;

import java.util.List;

import org.apache.kafka.clients.producer.ProducerRecord;

import com.softwaremill.jox.kafka.*;

public class Transfer {
    static void main() throws Exception {
        final var sourceTopic = "t1";
        final var destTopic = "t1mapped";
        final var group = "group1";

        timedAndLogged(
                "transfer",
                () -> {
                    final var bootstrapServer = "localhost:29092";
                    final var consumerSettings =
                            ConsumerSettings.defaults(group)
                                    .bootstrapServers(bootstrapServer)
                                    .autoOffsetReset(ConsumerSettings.AutoOffsetReset.EARLIEST);
                    final var producerSettings =
                            ProducerSettings.defaults().bootstrapServers(bootstrapServer);

                    supervised(
                            scope -> {
                                final var consumer =
                                        consumerSettings.toThreadSafeConsumerWrapper(scope);
                                KafkaDrain.runPublishAndCommit(
                                        KafkaFlow.subscribe(consumer, sourceTopic)
                                                .take(10_000_000)
                                                .map(
                                                        in ->
                                                                new SendPacket<>(
                                                                        List.of(
                                                                                new ProducerRecord<>(
                                                                                        destTopic,
                                                                                        new StringBuilder(
                                                                                                        in
                                                                                                                .value())
                                                                                                .reverse()
                                                                                                .toString())),
                                                                        List.of(in))),
                                        producerSettings,
                                        consumer);
                                return null;
                            });
                    return null;
                });
    }
}
