package com.softwaremill.jox.kafka;

import static com.softwaremill.jox.structured.Scopes.supervised;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static scala.jdk.javaapi.CollectionConverters.asJava;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.softwaremill.jox.Channel;
import com.softwaremill.jox.Select;
import com.softwaremill.jox.flows.Flows;

import io.github.embeddedkafka.EmbeddedKafka$;
import io.github.embeddedkafka.EmbeddedKafkaConfig;
import scala.concurrent.duration.Duration;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KafkaTest {
    private String bootstrapServer;
    private final EmbeddedKafkaConfig config = EmbeddedKafkaConfig.defaultConfig();

    @BeforeAll
    void setUp() {
        final var server = EmbeddedKafka$.MODULE$.start(config);
        bootstrapServer = "localhost:" + server.config().kafkaPort();
    }

    @AfterAll
    void tearDown() {
        EmbeddedKafka$.MODULE$.stop();
    }

    private void publishStringMessageToKafka(String topic, String msg) {
        EmbeddedKafka$.MODULE$.publishStringMessageToKafka(topic, msg, config);
    }

    private List<String> consumeNumberMessagesFrom(String topic, int count) {
        return asJava(
                EmbeddedKafka$.MODULE$.consumeNumberMessagesFrom(
                        topic,
                        count,
                        true,
                        Duration.apply(30, "s"),
                        config,
                        new StringDeserializer()));
    }

    @Test
    void shouldReceiveMessagesFromTopic() throws Exception {
        // given
        final var topic = "t1_java";
        final var group = "g1_java";

        // when
        publishStringMessageToKafka(topic, "msg1");
        publishStringMessageToKafka(topic, "msg2");
        publishStringMessageToKafka(topic, "msg3");

        supervised(
                scope -> {
                    // then
                    final var settings =
                            ConsumerSettings.defaults(group)
                                    .bootstrapServers(bootstrapServer)
                                    .autoOffsetReset(ConsumerSettings.AutoOffsetReset.EARLIEST);
                    final var source = KafkaFlow.subscribe(settings, topic).runToChannel(scope);

                    assertEquals("msg1", source.receive().value());
                    assertEquals("msg2", source.receive().value());
                    assertEquals("msg3", source.receive().value());

                    // give a chance for a potential message to be received from Kafka & sent to the
                    // channel
                    Thread.sleep(250);
                    assertEquals(
                            "none",
                            Select.select(source.receiveClause(), Select.defaultClause("none")));

                    publishStringMessageToKafka(topic, "msg4");
                    assertEquals("msg4", source.receive().value());
                    return null;
                });
    }

    @Test
    void shouldPublishMessagesToTopic() throws Exception {
        // given
        final var topic = "t2_java";
        final var count = 1000;
        final var msgs =
                IntStream.rangeClosed(1, count)
                        .mapToObj(n -> "msg" + n)
                        .collect(Collectors.toList());

        // when
        final var metadata =
                supervised(
                        _ -> {
                            final var settings =
                                    ProducerSettings.defaults().bootstrapServers(bootstrapServer);
                            return KafkaStage.mapPublish(
                                            Flows.fromIterable(msgs)
                                                    .map(msg -> new ProducerRecord<>(topic, msg)),
                                            settings)
                                    .runToList();
                        });

        // then
        assertEquals(
                LongStream.range(0, count).boxed().collect(Collectors.toList()),
                metadata.stream().map(RecordMetadata::offset).collect(Collectors.toList()));

        assertEquals(msgs, consumeNumberMessagesFrom(topic, count));
    }

    @Test
    void shouldCommitOffsetsOfProcessedMessages() throws Exception {
        // given
        final var sourceTopic = "t3_1_java";
        final var destTopic = "t3_2_java";
        final var group1 = "g3_1_java";
        final var group2 = "g3_2_java";

        final var consumerSettings =
                ConsumerSettings.defaults(group1)
                        .bootstrapServers(bootstrapServer)
                        .autoOffsetReset(ConsumerSettings.AutoOffsetReset.EARLIEST);
        final var producerSettings = ProducerSettings.defaults().bootstrapServers(bootstrapServer);

        // when
        publishStringMessageToKafka(sourceTopic, "10");
        publishStringMessageToKafka(sourceTopic, "25");
        publishStringMessageToKafka(sourceTopic, "92");

        final var metadata = Channel.<RecordMetadata>newBufferedDefaultChannel();

        supervised(
                scope -> {
                    // then
                    final var consumer = consumerSettings.toThreadSafeConsumerWrapper(scope);

                    scope.fork(
                            () -> {
                                supervised(
                                        _ -> {
                                            KafkaStage.mapPublishAndCommit(
                                                            KafkaFlow.subscribe(
                                                                            consumer, sourceTopic)
                                                                    .map(
                                                                            in ->
                                                                                    SendPacket.of(
                                                                                            new ProducerRecord<>(
                                                                                                    destTopic,
                                                                                                    String
                                                                                                            .valueOf(
                                                                                                                    Long
                                                                                                                                    .parseLong(
                                                                                                                                            in
                                                                                                                                                    .value())
                                                                                                                            * 2)),
                                                                                            in)),
                                                            producerSettings,
                                                            consumer)
                                                    .runPipeToSink(metadata, false);
                                            return null;
                                        });
                                return null;
                            });

                    final var inDest =
                            KafkaFlow.subscribe(consumerSettings, destTopic).runToChannel(scope);
                    assertEquals("20", inDest.receive().value());
                    assertEquals("50", inDest.receive().value());
                    assertEquals("184", inDest.receive().value());

                    // giving the commit process a chance to commit
                    Thread.sleep(2000);

                    // checking the metadata
                    assertEquals(0L, metadata.receive().offset());
                    assertEquals(1L, metadata.receive().offset());
                    assertEquals(2L, metadata.receive().offset());

                    // interrupting the stream processing by exiting the scope
                    return null;
                });

        // sending some more messages to source
        publishStringMessageToKafka(sourceTopic, "4");

        supervised(
                scope -> {
                    // reading from source, using the same consumer group as before, should start
                    // from the last committed offset
                    final var inSource =
                            KafkaFlow.subscribe(consumerSettings, sourceTopic).runToChannel(scope);
                    assertEquals("4", inSource.receive().value());

                    // while reading using another group, should start from the earliest offset
                    final var inSource2 =
                            KafkaFlow.subscribe(consumerSettings.groupId(group2), sourceTopic)
                                    .runToChannel(scope);
                    assertEquals("10", inSource2.receive().value());
                    return null;
                });
    }

    @Test
    void shouldPublishMessagesUsingDrain() throws Exception {
        // given
        final var topic = "t4_java";
        final var msgs = List.of("a", "b", "c");

        // when
        supervised(
                _ -> {
                    final var settings =
                            ProducerSettings.defaults().bootstrapServers(bootstrapServer);
                    KafkaDrain.runPublish(
                            Flows.fromIterable(msgs).map(msg -> new ProducerRecord<>(topic, msg)),
                            settings);
                    return null;
                });

        // then
        assertEquals(msgs, consumeNumberMessagesFrom(topic, 3));
    }

    @Test
    void shouldCommitOffsetsUsingDrain() throws Exception {
        // given
        final var sourceTopic = "t5_1_java";
        final var destTopic = "t5_2_java";
        final var group1 = "g5_1_java";
        final var group2 = "g5_2_java";

        final var consumerSettings =
                ConsumerSettings.defaults(group1)
                        .bootstrapServers(bootstrapServer)
                        .autoOffsetReset(ConsumerSettings.AutoOffsetReset.EARLIEST);
        final var producerSettings = ProducerSettings.defaults().bootstrapServers(bootstrapServer);

        // when
        publishStringMessageToKafka(sourceTopic, "10");
        publishStringMessageToKafka(sourceTopic, "25");
        publishStringMessageToKafka(sourceTopic, "92");

        supervised(
                scope -> {
                    // then
                    scope.fork(
                            () -> {
                                supervised(
                                        innerScope -> {
                                            final var consumer =
                                                    consumerSettings.toThreadSafeConsumerWrapper(
                                                            innerScope);
                                            KafkaDrain.runPublishAndCommit(
                                                    KafkaFlow.subscribe(consumer, sourceTopic)
                                                            .map(
                                                                    in ->
                                                                            SendPacket.of(
                                                                                    new ProducerRecord<>(
                                                                                            destTopic,
                                                                                            String
                                                                                                    .valueOf(
                                                                                                            Long
                                                                                                                            .parseLong(
                                                                                                                                    in
                                                                                                                                            .value())
                                                                                                                    * 2)),
                                                                                    in)),
                                                    producerSettings,
                                                    consumer);
                                            return null;
                                        });
                                return null;
                            });

                    final var inDest =
                            KafkaFlow.subscribe(consumerSettings, destTopic).runToChannel(scope);
                    assertEquals("20", inDest.receive().value());
                    assertEquals("50", inDest.receive().value());
                    assertEquals("184", inDest.receive().value());

                    Thread.sleep(2000);
                    return null;
                });

        publishStringMessageToKafka(sourceTopic, "4");

        supervised(
                scope -> {
                    // reading from source, using the same consumer group as before, should start
                    // from the last committed offset
                    final var inSource =
                            KafkaFlow.subscribe(consumerSettings, sourceTopic).runToChannel(scope);
                    assertEquals("4", inSource.receive().value());

                    // while reading using another group, should start from the earliest offset
                    final var inSource2 =
                            KafkaFlow.subscribe(consumerSettings.groupId(group2), sourceTopic)
                                    .runToChannel(scope);
                    assertEquals("10", inSource2.receive().value());
                    return null;
                });
    }

    @Test
    void shouldCommitOffsetsUsingRunCommit() throws Exception {
        // given
        final var sourceTopic = "t6_1_java";
        final var group1 = "g6_1_java";
        final var group2 = "g6_2_java";

        final var consumerSettings =
                ConsumerSettings.defaults(group1)
                        .bootstrapServers(bootstrapServer)
                        .autoOffsetReset(ConsumerSettings.AutoOffsetReset.EARLIEST);

        // when
        publishStringMessageToKafka(sourceTopic, "msg1");
        publishStringMessageToKafka(sourceTopic, "msg2");
        publishStringMessageToKafka(sourceTopic, "msg3");

        final var consumedCount = Channel.<Integer>newBufferedDefaultChannel();

        supervised(
                scope -> {
                    // then
                    scope.fork(
                            () -> {
                                supervised(
                                        innerScope -> {
                                            final var consumer =
                                                    consumerSettings.toThreadSafeConsumerWrapper(
                                                            innerScope);
                                            final var count = new int[] {0};
                                            KafkaDrain.runCommit(
                                                    KafkaFlow.subscribe(consumer, sourceTopic)
                                                            .map(
                                                                    in -> {
                                                                        count[0]++;
                                                                        if (count[0] == 3)
                                                                            consumedCount.send(
                                                                                    count[0]);
                                                                        return CommitPacket.of(in);
                                                                    }),
                                                    consumer);
                                            return null;
                                        });
                                return null;
                            });

                    assertEquals(3, consumedCount.receive());
                    Thread.sleep(2000);
                    return null;
                });

        publishStringMessageToKafka(sourceTopic, "msg4");

        supervised(
                scope -> {
                    // reading from source, using the same consumer group as before, should start
                    // from the last committed offset
                    final var inSource =
                            KafkaFlow.subscribe(consumerSettings, sourceTopic).runToChannel(scope);
                    assertEquals("msg4", inSource.receive().value());

                    // while reading using another group, should start from the earliest offset
                    final var inSource2 =
                            KafkaFlow.subscribe(consumerSettings.groupId(group2), sourceTopic)
                                    .runToChannel(scope);
                    assertEquals("msg1", inSource2.receive().value());
                    return null;
                });
    }

    @Test
    void shouldCommitOffsetsUsingMapCommit() throws Exception {
        // given
        final var sourceTopic = "t7_1_java";
        final var group1 = "g7_1_java";
        final var group2 = "g7_2_java";

        final var consumerSettings =
                ConsumerSettings.defaults(group1)
                        .bootstrapServers(bootstrapServer)
                        .autoOffsetReset(ConsumerSettings.AutoOffsetReset.EARLIEST);

        // when
        publishStringMessageToKafka(sourceTopic, "msg1");
        publishStringMessageToKafka(sourceTopic, "msg2");
        publishStringMessageToKafka(sourceTopic, "msg3");

        final var consumedCount = Channel.<Integer>newBufferedDefaultChannel();

        supervised(
                scope -> {
                    // then
                    scope.fork(
                            () -> {
                                supervised(
                                        innerScope -> {
                                            final var consumer =
                                                    consumerSettings.toThreadSafeConsumerWrapper(
                                                            innerScope);
                                            final var count = new int[] {0};
                                            KafkaStage.mapCommit(
                                                            KafkaFlow.subscribe(
                                                                            consumer, sourceTopic)
                                                                    .map(
                                                                            in -> {
                                                                                count[0]++;
                                                                                if (count[0] == 3)
                                                                                    consumedCount
                                                                                            .send(
                                                                                                    count[
                                                                                                            0]);
                                                                                return CommitPacket
                                                                                        .of(in);
                                                                            }),
                                                            consumer)
                                                    .runDrain();
                                            return null;
                                        });
                                return null;
                            });

                    assertEquals(3, consumedCount.receive());
                    Thread.sleep(2000);
                    return null;
                });

        publishStringMessageToKafka(sourceTopic, "msg4");

        supervised(
                scope -> {
                    // reading from source, using the same consumer group as before, should start
                    // from the last committed offset
                    final var inSource =
                            KafkaFlow.subscribe(consumerSettings, sourceTopic).runToChannel(scope);
                    assertEquals("msg4", inSource.receive().value());

                    // while reading using another group, should start from the earliest offset
                    final var inSource2 =
                            KafkaFlow.subscribe(consumerSettings.groupId(group2), sourceTopic)
                                    .runToChannel(scope);
                    assertEquals("msg1", inSource2.receive().value());
                    return null;
                });
    }

    @Test
    void shouldCommitOffsetsWhenConsumingFiniteStreamUsingTake() throws Exception {
        // given
        final var sourceTopic = "t8_1_java";
        final var group1 = "g8_1_java";
        final var group2 = "g8_2_java";

        final var consumerSettings =
                ConsumerSettings.defaults(group1)
                        .bootstrapServers(bootstrapServer)
                        .autoOffsetReset(ConsumerSettings.AutoOffsetReset.EARLIEST);

        publishStringMessageToKafka(sourceTopic, "msg1");
        publishStringMessageToKafka(sourceTopic, "msg2");
        publishStringMessageToKafka(sourceTopic, "msg3");
        publishStringMessageToKafka(sourceTopic, "msg4");
        publishStringMessageToKafka(sourceTopic, "msg5");

        // when
        // consume only first 3 messages using take, synchronously
        final var consumed =
                supervised(
                        scope -> {
                            final var consumer =
                                    consumerSettings.toThreadSafeConsumerWrapper(scope);
                            return KafkaStage.mapCommit(
                                            KafkaFlow.subscribe(consumer, sourceTopic)
                                                    .take(3)
                                                    .map(CommitPacket::of),
                                            consumer)
                                    .runToList();
                        });

        // then
        assertEquals(3, consumed.size());

        supervised(
                scope -> {
                    // reading from source, using the same consumer group as before, should start
                    // from the last committed offset (after msg3)
                    final var inSource =
                            KafkaFlow.subscribe(consumerSettings, sourceTopic).runToChannel(scope);
                    assertEquals("msg4", inSource.receive().value());
                    assertEquals("msg5", inSource.receive().value());

                    // while reading using another group, should start from the earliest offset
                    final var inSource2 =
                            KafkaFlow.subscribe(consumerSettings.groupId(group2), sourceTopic)
                                    .runToChannel(scope);
                    assertEquals("msg1", inSource2.receive().value());
                    return null;
                });
    }
}
