package com.softwaremill.jox.kafka.manual.pekko;

import static com.softwaremill.jox.kafka.manual.Util.timedAndLogged;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.kafka.*;
import org.apache.pekko.kafka.javadsl.Committer;
import org.apache.pekko.kafka.javadsl.Consumer;
import org.apache.pekko.kafka.javadsl.Producer;

public class TransferPekko {
    static void main() throws Exception {
        final var sourceTopic = "t2";
        final var destTopic = "t2mapped";
        final var group = "group2";

        timedAndLogged(
                "transfer-pekko",
                () -> {
                    final var system = ActorSystem.create("transfer");

                    final var producerSettings =
                            ProducerSettings.create(
                                            system, new StringSerializer(), new StringSerializer())
                                    .withBootstrapServers("localhost:29092");

                    final var consumerSettings =
                            ConsumerSettings.create(
                                            system,
                                            new StringDeserializer(),
                                            new StringDeserializer())
                                    .withBootstrapServers("localhost:29092")
                                    .withGroupId(group)
                                    .withProperty(
                                            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

                    final var stream =
                            Consumer.committableSource(
                                            consumerSettings, Subscriptions.topics(sourceTopic))
                                    .take(10_000_000)
                                    .map(
                                            msg ->
                                                    ProducerMessage.single(
                                                            new ProducerRecord<>(
                                                                    destTopic,
                                                                    msg.record().key(),
                                                                    new StringBuilder(
                                                                                    msg.record()
                                                                                            .value())
                                                                            .reverse()
                                                                            .toString()),
                                                            msg.committableOffset()))
                                    .via(Producer.flexiFlow(producerSettings))
                                    .map(ProducerMessage.Results::passThrough)
                                    .toMat(
                                            Committer.sink(CommitterSettings.create(system)),
                                            (_, completion) -> completion)
                                    .run(system);

                    stream.toCompletableFuture().get();
                    system.terminate();
                    system.getWhenTerminated().toCompletableFuture().get();
                    return null;
                });
    }
}
