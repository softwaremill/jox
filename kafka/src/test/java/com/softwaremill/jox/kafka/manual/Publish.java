package com.softwaremill.jox.kafka.manual;

import static com.softwaremill.jox.kafka.manual.Util.randomString;
import static com.softwaremill.jox.kafka.manual.Util.timedAndLogged;
import static java.util.Map.entry;

import java.util.Optional;

import org.apache.kafka.clients.producer.ProducerRecord;

import com.softwaremill.jox.flows.Flows;
import com.softwaremill.jox.kafka.KafkaStage;
import com.softwaremill.jox.kafka.ProducerSettings;

public class Publish {
    static void main() throws Exception {
        final var topic = "t1";

        timedAndLogged(
                "publish",
                () -> {
                    final var bootstrapServer = "localhost:29092";
                    final var settings =
                            ProducerSettings.defaults().bootstrapServers(bootstrapServer);
                    KafkaStage.mapPublish(
                                    Flows.unfold(
                                                    null,
                                                    _ -> Optional.of(entry(randomString(), null)))
                                            .take(10_000_000)
                                            .map(msg -> new ProducerRecord<>(topic, msg)),
                                    settings)
                            .runDrain();
                    return null;
                });
    }
}
