package com.softwaremill.jox.kafka;

import static com.softwaremill.jox.structured.Scopes.supervised;
import static com.softwaremill.jox.structured.Util.uninterruptible;

import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softwaremill.jox.*;
import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.flows.FlowEmit;
import com.softwaremill.jox.flows.Flows;
import com.softwaremill.jox.structured.ActorRef;

public final class KafkaStage {

    private static final Logger logger = LoggerFactory.getLogger(KafkaStage.class);

    private KafkaStage() {}

    public static <K, V> Flow<RecordMetadata> mapPublish(
            Flow<ProducerRecord<K, V>> flow, ProducerSettings<K, V> settings) {
        return mapPublish(flow, settings.toProducer(), true);
    }

    public static <K, V> Flow<RecordMetadata> mapPublish(
            Flow<ProducerRecord<K, V>> flow,
            KafkaProducer<K, V> producer,
            boolean closeWhenComplete) {
        return mapPublishAndCommit(
                flow.map(r -> new SendPacket<>(List.of(r), List.of())),
                producer,
                null,
                closeWhenComplete);
    }

    public static <K, V> Flow<RecordMetadata> mapPublishAndCommit(
            Flow<SendPacket<K, V>> flow,
            ProducerSettings<K, V> producerSettings,
            ActorRef<KafkaConsumerWrapper<K, V>> consumer) {
        return mapPublishAndCommit(flow, producerSettings.toProducer(), consumer, true);
    }

    public static <K, V> Flow<RecordMetadata> mapPublishAndCommit(
            Flow<SendPacket<K, V>> flow,
            KafkaProducer<K, V> producer,
            ActorRef<KafkaConsumerWrapper<K, V>> consumer,
            boolean closeWhenComplete) {
        return mapPublishAndCommitImpl(flow, producer, consumer, closeWhenComplete);
    }

    private static <K, V> Flow<RecordMetadata> mapPublishAndCommitImpl(
            Flow<SendPacket<K, V>> flow,
            KafkaProducer<K, V> producer,
            ActorRef<KafkaConsumerWrapper<K, V>> consumer,
            boolean closeWhenComplete) {
        return Flows.usingEmit(
                emit -> {
                    var exceptions = Channel.<Throwable>newUnlimitedChannel();
                    var metadata = Channel.<Pair<Long, RecordMetadata>>newUnlimitedChannel();
                    var toCommit = Channel.<SendPacket<?, ?>>newUnlimitedChannel();

                    var sendInSequence = new SendInSequence<>(emit);

                    try {
                        supervised(
                                scope -> {
                                    var source = flow.runToChannel(scope);

                                    var commitDoneFlow = Flows.empty();
                                    if (consumer != null) {
                                        commitDoneFlow =
                                                Flows.fromFork(
                                                        scope.fork(
                                                                () -> {
                                                                    try {
                                                                        OffsetCommit.doCommit(
                                                                                consumer, toCommit);
                                                                    } catch (Throwable t) {
                                                                        exceptions.sendOrClosed(t);
                                                                        throw t;
                                                                    }
                                                                    return null;
                                                                }));
                                    }

                                    while (true) {
                                        var received =
                                                Select.selectOrClosed(
                                                        exceptions.receiveClause(),
                                                        metadata.receiveClause(),
                                                        source.receiveClause());

                                        if (received instanceof ChannelDone) {
                                            sendInSequence.drainFrom(metadata, exceptions);
                                            toCommit.done();
                                            commitDoneFlow.runToChannel(scope).receiveOrClosed();
                                            break;
                                        } else if (received instanceof ChannelError error) {
                                            throw error.toException();
                                        } else if (received instanceof Throwable t) {
                                            throw (Exception) t;
                                        } else if (received instanceof Pair<?, ?> p) {
                                            //noinspection unchecked
                                            var pair = (Pair<Long, RecordMetadata>) p;
                                            sendInSequence.send(pair.first(), pair.second());
                                        } else if (received instanceof SendPacket<?, ?> packet) {
                                            //noinspection unchecked
                                            sendPacket(
                                                    producer,
                                                    (SendPacket<K, V>) packet,
                                                    sendInSequence,
                                                    toCommit,
                                                    exceptions,
                                                    metadata,
                                                    consumer != null);
                                        }
                                    }
                                    return null;
                                });
                    } finally {
                        if (closeWhenComplete) {
                            logger.debug("Closing the Kafka producer");
                            uninterruptible(
                                    () -> {
                                        producer.close();
                                        return null;
                                    });
                        }
                    }
                });
    }

    public static <K, V> Flow<Void> mapCommit(
            Flow<CommitPacket> flow, ActorRef<KafkaConsumerWrapper<K, V>> consumer) {
        return Flows.usingEmit(
                emit -> {
                    var exceptions = Channel.<Throwable>newUnlimitedChannel();
                    var toCommit = Channel.<CommitPacket>newUnlimitedChannel();

                    supervised(
                            scope -> {
                                var source = flow.runToChannel(scope);

                                var commitDoneFlow =
                                        Flows.fromFork(
                                                scope.fork(
                                                        () -> {
                                                            try {
                                                                OffsetCommit.doCommit(
                                                                        consumer, toCommit);
                                                            } catch (Throwable t) {
                                                                exceptions.sendOrClosed(t);
                                                                throw t;
                                                            }
                                                            return null;
                                                        }));

                                while (true) {
                                    var received =
                                            Select.selectOrClosed(
                                                    exceptions.receiveClause(),
                                                    source.receiveClause());

                                    if (received instanceof ChannelDone) {
                                        toCommit.done();
                                        commitDoneFlow.runToChannel(scope).receiveOrClosed();
                                        break;
                                    } else if (received instanceof ChannelError error) {
                                        throw error.toException();
                                    } else if (received instanceof Throwable t) {
                                        throw (Exception) t;
                                    } else if (received instanceof CommitPacket packet) {
                                        toCommit.send(packet);
                                        emit.apply(null);
                                    }
                                }
                                return null;
                            });
                });
    }

    private static <K, V> void sendPacket(
            KafkaProducer<K, V> producer,
            SendPacket<K, V> packet,
            SendInSequence<RecordMetadata> sendInSequence,
            Sink<SendPacket<?, ?>> toCommit,
            Sink<Throwable> exceptions,
            Sink<Pair<Long, RecordMetadata>> metadata,
            boolean commitOffsets) {
        var leftToSend = new AtomicInteger(packet.send().size());
        for (ProducerRecord<K, V> toSend : packet.send()) {
            var sequenceNo = sendInSequence.nextSequenceNo();
            producer.send(
                    toSend,
                    (m, e) -> {
                        if (e != null) {
                            exceptions.trySendOrClosed(e);
                        } else {
                            if (commitOffsets && leftToSend.decrementAndGet() == 0) {
                                toCommit.trySendOrClosed(packet);
                            }
                            metadata.trySendOrClosed(new Pair<>(sequenceNo, m));
                        }
                    });
        }
    }

    private static class SendInSequence<T> {
        private long sequenceNoNext = 0L;
        private long sequenceNoToSendNext = 0L;
        private final TreeMap<Long, T> toSend = new TreeMap<>();
        private final FlowEmit<T> emit;

        SendInSequence(FlowEmit<T> emit) {
            this.emit = emit;
        }

        long nextSequenceNo() {
            var n = sequenceNoNext;
            sequenceNoNext += 1;
            return n;
        }

        void send(long sequenceNo, T v) throws Exception {
            toSend.put(sequenceNo, v);
            trySend();
        }

        boolean allSent() {
            return sequenceNoNext == sequenceNoToSendNext;
        }

        private void trySend() throws Exception {
            while (!toSend.isEmpty() && toSend.firstKey() == sequenceNoToSendNext) {
                var m = toSend.pollFirstEntry().getValue();
                emit.apply(m);
                sequenceNoToSendNext += 1;
            }
        }

        void drainFrom(Source<Pair<Long, T>> incoming, Source<Throwable> exceptions)
                throws Exception {
            while (!allSent()) {
                var received =
                        Select.selectOrClosed(exceptions.receiveClause(), incoming.receiveClause());
                if (received instanceof ChannelDone) {
                    throw new IllegalStateException();
                } else if (received instanceof ChannelError error) {
                    throw error.toException();
                } else if (received instanceof Throwable t) {
                    throw (Exception) t;
                } else if (received instanceof Pair<?, ?> p) {
                    //noinspection unchecked
                    var pair = (Pair<Long, T>) p;
                    send(pair.first(), pair.second());
                }
            }
        }
    }

    private record Pair<F, S>(F first, S second) {}
}
