package p3.server.storage;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.Properties;

/**
 * Kafka configuration class.
 */
public class KafkaConfig {
    /**
     * Creates a Kafka producer.
     * @param id ID of the Kafka producer.
     * @param bootstrapServers The book strap server.
     * @return New Kafka producer.
     */
    public static KafkaProducer<String, String> createKafkaProducer(String id, String bootstrapServers) {
        // Create Kafka producer
        Properties producerProps = new Properties();
        producerProps.put("bootstrap.servers", bootstrapServers);
        producerProps.put("client.id", id);
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaProducer<>(producerProps);
    }

    /**
     * Creates a Kafka consumer.
     * @param id ID of the Kafka consumer.
     * @param bootstrapServers The book strap server.
     * @return New Kafka consumer.
     */
    public static KafkaConsumer<String, String> createKafkaConsumer(String id, String bootstrapServers) {
        // Create Kafka consumer
        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", bootstrapServers);
        consumerProps.put("group.id", id);
        consumerProps.put("auto.offset.reset", "earliest");
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return new KafkaConsumer<>(consumerProps);
    }
}