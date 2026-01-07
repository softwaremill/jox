package com.softwaremill.jox.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * Settings for a Kafka producer.
 *
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 */
public record ProducerSettings<K, V>(
        List<String> bootstrapServers,
        Serializer<K> keySerializer,
        Serializer<V> valueSerializer,
        Map<String, String> otherProperties) {

    private static final List<String> DEFAULT_BOOTSTRAP_SERVERS = List.of("localhost:9092");

    public ProducerSettings<K, V> bootstrapServers(String... servers) {
        return new ProducerSettings<>(
                List.of(servers), keySerializer, valueSerializer, otherProperties);
    }

    public <KK> ProducerSettings<KK, V> keySerializer(Serializer<KK> serializer) {
        return new ProducerSettings<>(
                bootstrapServers, serializer, valueSerializer, otherProperties);
    }

    public <VV> ProducerSettings<K, VV> valueSerializer(Serializer<VV> serializer) {
        return new ProducerSettings<>(bootstrapServers, keySerializer, serializer, otherProperties);
    }

    public ProducerSettings<K, V> property(String key, String value) {
        final var newProperties = new HashMap<>(otherProperties);
        newProperties.put(key, value);
        return new ProducerSettings<>(
                bootstrapServers, keySerializer, valueSerializer, Map.copyOf(newProperties));
    }

    public Properties toProperties() {
        final var props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, String.join(",", bootstrapServers));
        props.putAll(otherProperties);
        return props;
    }

    public KafkaProducer<K, V> toProducer() {
        return new KafkaProducer<>(toProperties(), keySerializer, valueSerializer);
    }

    public static ProducerSettings<String, String> defaults() {
        return new ProducerSettings<>(
                DEFAULT_BOOTSTRAP_SERVERS,
                new StringSerializer(),
                new StringSerializer(),
                Map.of());
    }
}
