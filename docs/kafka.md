# Kafka flows

Dependency:

Maven:

```xml
<dependency>
    <groupId>com.softwaremill.jox</groupId>
    <artifactId>kafka</artifactId>
    <version>0.5.1</version>
</dependency>
```

Gradle:

```gradle
implementation 'com.softwaremill.jox:kafka:0.5.1'
```

Gradle (Kotlin DSL):

```kotlin
implementation("com.softwaremill.jox:kafka:0.5.1")
```

`Flow`s which read from a Kafka topic, mapping stages and drains which publish to Kafka topics are available through
the `KafkaFlow`, `KafkaStage` and `KafkaDrain` classes.

In all cases kafka producers and consumers can be provided:
* by manually creating (and closing) an instance of a `KafkaProducer` / `KafkaConsumer`
* through a `ProducerSettings` / `ConsumerSettings`, with the bootstrap servers, consumer group id, key/value
  serializers, etc. The lifetime is then managed by the flow operators.
* through a thread-safe wrapper on a consumer (`ActorRef<KafkaConsumerWrapper<K, V>>`), for which the lifetime is bound
  to the current concurrency scope

## Reading from Kafka

To read from a Kafka topic, use:

```java
import com.softwaremill.jox.kafka.ConsumerSettings;
import com.softwaremill.jox.kafka.KafkaFlow;
import com.softwaremill.jox.kafka.ConsumerSettings.AutoOffsetReset;
import org.apache.kafka.clients.consumer.ConsumerRecord;

void main() throws Exception {
    var settings = ConsumerSettings.defaults("my_group")
        .bootstrapServers("localhost:9092")
        .autoOffsetReset(AutoOffsetReset.EARLIEST);
    var topic = "my_topic";

    KafkaFlow.subscribe(settings, topic)
        .runForeach((ConsumerRecord<String, String> msg) -> {
            // process message
        });
}
```

## Publishing to Kafka

To publish data to a Kafka topic:

```java
import com.softwaremill.jox.flows.Flows;
import com.softwaremill.jox.kafka.ProducerSettings;
import com.softwaremill.jox.kafka.KafkaDrain;
import org.apache.kafka.clients.producer.ProducerRecord;

void main() throws Exception {
    var settings = ProducerSettings.defaults().bootstrapServers("localhost:9092");
    KafkaDrain.runPublish(
        Flows.fromIterable(List.of("a", "b", "c"))
            .map(msg -> new ProducerRecord<String, String>("my_topic", msg)),
        settings
    );
}
```

To publish data as a mapping stage:

```java
import com.softwaremill.jox.flows.Flows;
import com.softwaremill.jox.kafka.ProducerSettings;
import com.softwaremill.jox.kafka.KafkaStage;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

void main() {
    var settings = ProducerSettings.defaults().bootstrapServers("localhost:9092");
    var metadatas = KafkaStage.mapPublish(
        Flows.fromIterable(List.of("a", "b", "c"))
            .map(msg -> new ProducerRecord<String, String>("my_topic", msg)),
        settings
    );

    // process & run the metadatas flow further
}
```

## Reading & publishing to Kafka with offset commits

Quite often data to be published to a topic (`topic1`) is computed basing on data received from another topic 
(`topic2`). In such a case, it's possible to commit messages from `topic2`, after the messages to `topic1` are 
successfully published.

In order to do so, a `Flow<SendPacket>` needs to be created. The definition of `SendPacket` is:

```java
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;

record SendPacket<K, V>(
    List<ProducerRecord<K, V>> send, 
    List<ConsumerRecord<?, ?>> commit) {}
```

The `send` list contains the messages to be sent (each message is a Kafka `ProducerRecord`). The `commit` list contains
the messages, basing on which the data to be sent was computed. These are the consumer records, as produced by a 
`KafkaFlow`. When committing, for each topic-partition that appears in the consumer records, the maximum offset is
computed. For example:

```java
import com.softwaremill.jox.kafka.*;
import com.softwaremill.jox.kafka.ConsumerSettings.AutoOffsetReset;
import static com.softwaremill.jox.structured.Scopes.supervised;
import org.apache.kafka.clients.producer.ProducerRecord;

void main() throws Exception {
    var consumerSettings = ConsumerSettings.defaults("my_group")
        .bootstrapServers("localhost:9092")
        .autoOffsetReset(AutoOffsetReset.EARLIEST);
    var producerSettings = ProducerSettings.defaults().bootstrapServers("localhost:9092");
    var sourceTopic = "source_topic";
    var destTopic = "dest_topic";

    supervised(scope -> {
        var consumer = consumerSettings.toThreadSafeConsumerWrapper(scope);
        KafkaDrain.runPublishAndCommit(
            KafkaFlow.subscribe(consumer, sourceTopic)
                .map(in -> SendPacket.of(
                    new ProducerRecord<String, String>(destTopic, String.valueOf(Long.parseLong(in.value()) * 2)),
                    in
                )),
            producerSettings,
            consumer
        );
        return null;
    });
}
```

The offsets are committed every second in a background process.

## Reading from Kafka, processing data & committing offsets

Offsets can also be committed after the data has been processed, without producing any records to write to a topic.
For that, we can use the `runCommit` drain, or the `mapCommit` stage, both of which work with a `Flow<CommitPacket>`:

```java
import com.softwaremill.jox.kafka.*;
import com.softwaremill.jox.kafka.ConsumerSettings.AutoOffsetReset;
import static com.softwaremill.jox.structured.Scopes.supervised;

void main() throws Exception {
    var consumerSettings = ConsumerSettings.defaults("my_group")
        .bootstrapServers("localhost:9092")
        .autoOffsetReset(AutoOffsetReset.EARLIEST);
    var sourceTopic = "source_topic";

    supervised(scope -> {
        var consumer = consumerSettings.toThreadSafeConsumerWrapper(scope);
        KafkaDrain.runCommit(
            KafkaFlow.subscribe(consumer, sourceTopic)
                .mapPar(10, in -> {
                    // process the message, e.g. send an HTTP request
                    return CommitPacket.of(in);
                }),
            consumer
        );
        return null;
    });
}
```

