package com.softwaremill.jox.kafka;

import static com.softwaremill.jox.structured.Scopes.supervised;
import static com.softwaremill.jox.structured.Util.uninterruptible;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softwaremill.jox.Channel;
import com.softwaremill.jox.ChannelDone;
import com.softwaremill.jox.ChannelError;
import com.softwaremill.jox.Select;
import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.structured.ActorRef;

public final class KafkaDrain {

    private static final Logger logger = LoggerFactory.getLogger(KafkaDrain.class);

    private KafkaDrain() {}

    /** Sends all records emitted by the provided {@link Flow} using the given settings. */
    public static <K, V> void runPublish(
            Flow<ProducerRecord<K, V>> flow, ProducerSettings<K, V> settings)
            throws InterruptedException {
        runPublish(flow, settings.toProducer(), true);
    }

    /**
     * Sends all records emitted by the provided {@link Flow} using the given producer. The producer
     * is closed depending on the {@code closeWhenComplete} flag.
     */
    public static <K, V> void runPublish(
            Flow<ProducerRecord<K, V>> flow,
            KafkaProducer<K, V> producer,
            boolean closeWhenComplete)
            throws InterruptedException {
        final var producerExceptions = Channel.newUnlimitedChannel();

        try {
            supervised(
                    scope -> {
                        final var source = flow.runToChannel(scope);
                        while (true) {
                            final var received =
                                    Select.selectOrClosed(
                                            producerExceptions.receiveClause(),
                                            source.receiveClause());

                            if (received instanceof ChannelDone) {
                                break;
                            } else if (received instanceof ChannelError error) {
                                throw error.toException();
                            } else if (received instanceof Throwable t) {
                                throw (Exception) t;
                            } else if (received instanceof ProducerRecord<?, ?> record) {
                                //noinspection unchecked
                                producer.send(
                                        (ProducerRecord<K, V>) record,
                                        (metadata, exception) -> {
                                            if (exception != null) {
                                                logger.error(
                                                        "Exception when sending record", exception);
                                                producerExceptions.trySendOrClosed(exception);
                                            }
                                        });
                            }
                        }
                        return null;
                    });
        } finally {
            if (closeWhenComplete) {
                uninterruptible(
                        () -> {
                            producer.close();
                            return null;
                        });
            }
        }
    }

    /**
     * Consumes all packets emitted by the provided {@link Flow}. For each packet, first all {@code
     * send} messages are sent, then {@code commit} messages are committed.
     */
    public static <K, V> void runPublishAndCommit(
            Flow<SendPacket<K, V>> flow,
            ProducerSettings<K, V> producerSettings,
            ActorRef<KafkaConsumerWrapper<K, V>> consumer)
            throws Exception {
        runPublishAndCommit(flow, producerSettings.toProducer(), consumer, true);
    }

    /**
     * Consumes all packets emitted by the provided {@link Flow}. For each packet, first all {@code
     * send} messages are sent, then {@code commit} messages are committed.
     */
    public static <K, V> void runPublishAndCommit(
            Flow<SendPacket<K, V>> flow,
            KafkaProducer<K, V> producer,
            ActorRef<KafkaConsumerWrapper<K, V>> consumer,
            boolean closeWhenComplete)
            throws Exception {
        KafkaStage.mapPublishAndCommit(flow, producer, consumer, closeWhenComplete).runDrain();
    }

    /**
     * Consumes all commit packets emitted by the provided {@link Flow} and commits them using the
     * given {@code consumer}.
     */
    public static <K, V> void runCommit(
            Flow<CommitPacket> flow, ActorRef<KafkaConsumerWrapper<K, V>> consumer)
            throws Exception {
        KafkaStage.mapCommit(flow, consumer).runDrain();
    }
}
