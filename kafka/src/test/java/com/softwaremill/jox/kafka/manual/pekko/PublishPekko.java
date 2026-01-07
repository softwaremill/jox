package com.softwaremill.jox.kafka.manual.pekko;

import static com.softwaremill.jox.kafka.manual.Util.randomString;
import static com.softwaremill.jox.kafka.manual.Util.timedAndLogged;

import java.util.stream.IntStream;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.kafka.ProducerSettings;
import org.apache.pekko.kafka.javadsl.Producer;
import org.apache.pekko.stream.javadsl.Source;

public class PublishPekko {
    static void main() throws Exception {
        final var topic = "t2";
        timedAndLogged(
                "publish-pekko",
                () -> {
                    final var system = ActorSystem.create("publish");

                    final var producerSettings =
                            ProducerSettings.create(
                                            system, new StringSerializer(), new StringSerializer())
                                    .withBootstrapServers("localhost:29092");

                    final var source =
                            Source.fromJavaStream(
                                    () ->
                                            IntStream.rangeClosed(1, 10_000_000)
                                                    .mapToObj(_ -> randomString()));

                    final var producerRecordSource =
                            source.map(m -> new ProducerRecord<String, String>(topic, m));

                    final var sink = Producer.plainSink(producerSettings);
                    producerRecordSource.runWith(sink, system);
                    system.terminate();
                    system.getWhenTerminated().toCompletableFuture().get();
                    return null;
                });
    }
}
