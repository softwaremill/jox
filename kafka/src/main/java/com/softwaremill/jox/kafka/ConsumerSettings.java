package com.softwaremill.jox.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.softwaremill.jox.structured.ActorRef;
import com.softwaremill.jox.structured.Scope;

/**
 * Settings for a Kafka consumer.
 *
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 */
public record ConsumerSettings<K, V>(
        List<String> bootstrapServers,
        String groupId,
        Deserializer<K> keyDeserializer,
        Deserializer<V> valueDeserializer,
        boolean autoCommit,
        AutoOffsetReset autoOffsetReset,
        Map<String, String> otherProperties) {

    private static final List<String> DEFAULT_BOOTSTRAP_SERVERS = List.of("localhost:9092");

    public ConsumerSettings<K, V> bootstrapServers(String... servers) {
        return new ConsumerSettings<>(
                List.of(servers),
                groupId,
                keyDeserializer,
                valueDeserializer,
                autoCommit,
                autoOffsetReset,
                otherProperties);
    }

    public ConsumerSettings<K, V> groupId(String newGroupId) {
        return new ConsumerSettings<>(
                bootstrapServers,
                newGroupId,
                keyDeserializer,
                valueDeserializer,
                autoCommit,
                autoOffsetReset,
                otherProperties);
    }

    public <KK> ConsumerSettings<KK, V> keyDeserializer(Deserializer<KK> deserializer) {
        return new ConsumerSettings<>(
                bootstrapServers,
                groupId,
                deserializer,
                valueDeserializer,
                autoCommit,
                autoOffsetReset,
                otherProperties);
    }

    public <VV> ConsumerSettings<K, VV> valueDeserializer(Deserializer<VV> deserializer) {
        return new ConsumerSettings<>(
                bootstrapServers,
                groupId,
                keyDeserializer,
                deserializer,
                autoCommit,
                autoOffsetReset,
                otherProperties);
    }

    public ConsumerSettings<K, V> autoOffsetReset(AutoOffsetReset reset) {
        return new ConsumerSettings<>(
                bootstrapServers,
                groupId,
                keyDeserializer,
                valueDeserializer,
                autoCommit,
                reset,
                otherProperties);
    }

    public ConsumerSettings<K, V> property(String key, String value) {
        final var newProperties = new HashMap<>(otherProperties);
        newProperties.put(key, value);
        return new ConsumerSettings<>(
                bootstrapServers,
                groupId,
                keyDeserializer,
                valueDeserializer,
                autoCommit,
                autoOffsetReset,
                Map.copyOf(newProperties));
    }

    public Properties toProperties() {
        final var props = new Properties();
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(autoCommit));
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, String.join(",", bootstrapServers));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        if (autoOffsetReset != null) {
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset.toConfigString());
        }
        props.putAll(otherProperties);
        return props;
    }

    public KafkaConsumer<K, V> toConsumer() {
        return new KafkaConsumer<>(toProperties(), keyDeserializer, valueDeserializer);
    }

    public ActorRef<KafkaConsumerWrapper<K, V>> toThreadSafeConsumerWrapper(Scope scope)
            throws InterruptedException {
        return KafkaConsumerWrapper.create(scope, toConsumer(), true);
    }

    public static ConsumerSettings<String, String> defaults(String groupId) {
        return new ConsumerSettings<>(
                DEFAULT_BOOTSTRAP_SERVERS,
                groupId,
                new StringDeserializer(),
                new StringDeserializer(),
                false,
                null,
                Map.of());
    }

    public enum AutoOffsetReset {
        EARLIEST,
        LATEST,
        NONE;

        public String toConfigString() {
            return name().toLowerCase();
        }
    }
}
