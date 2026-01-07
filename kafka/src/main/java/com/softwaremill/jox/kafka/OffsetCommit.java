package com.softwaremill.jox.kafka;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;

import com.softwaremill.jox.Source;
import com.softwaremill.jox.flows.Flows;
import com.softwaremill.jox.structured.ActorRef;

public final class OffsetCommit {

    private OffsetCommit() {}

    /**
     * Commits the offsets of the messages in the given packets.
     *
     * @param consumer The consumer wrapper to use for committing.
     * @param packets The source of packets containing messages to commit.
     * @param <K> The type of the key.
     * @param <V> The type of the value.
     * @throws Exception If an error occurs while committing.
     */
    public static <K, V> void doCommit(
            ActorRef<KafkaConsumerWrapper<K, V>> consumer, Source<? extends HasCommit> packets)
            throws Exception {
        var commitInterval = Duration.ofSeconds(1);
        var toCommit = new HashMap<TopicPartition, Long>();

        Runnable commitAll =
                () -> {
                    if (consumer != null && !toCommit.isEmpty()) {
                        try {
                            // waiting for the commit to happen
                            var offsetsToCommit = Map.copyOf(toCommit);
                            consumer.ask(
                                    wrapper -> {
                                        wrapper.commit(offsetsToCommit);
                                        return null;
                                    });
                            toCommit.clear();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                };

        // the flow should finish once the `packets` source is done
        var tick = new Object();
        Flows.tick(commitInterval, tick)
                .map(x -> x)
                .merge(Flows.fromSource(packets).map(x -> x), false, true)
                .runForeach(
                        obj -> {
                            if (obj == tick) {
                                commitAll.run();
                            } else if (obj instanceof HasCommit packet) {
                                for (ConsumerRecord<?, ?> consumerRecord : packet.commit()) {
                                    final var tp =
                                            new TopicPartition(
                                                    consumerRecord.topic(),
                                                    consumerRecord.partition());
                                    toCommit.merge(tp, consumerRecord.offset(), Math::max);
                                }
                            }
                        });

        // once done, commit any remaining offsets
        commitAll.run();
    }
}
