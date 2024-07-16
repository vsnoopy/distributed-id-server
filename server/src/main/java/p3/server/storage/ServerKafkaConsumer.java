package p3.server.storage;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Arrays;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kafka consumer server class
 */
public class ServerKafkaConsumer implements Runnable {
    private final KafkaConsumer<String, String> consumer;
    private final RedisDatabase db;
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ServerKafkaConsumer.class);

    /**
     * Constructor.
     * @param db Radis database.
     * @param id Kafka consumer ID.
     * @param bootstrapServers Boot strap server.
     */
    public ServerKafkaConsumer(RedisDatabase db, String id, String bootstrapServers) {
        this.consumer = KafkaConfig.createKafkaConsumer(id, bootstrapServers);
        this.db = db;
    }

    /**
     * Thread entry point for the Kafka consumer server.
     */
    @Override
    public void run() {
        try {
            consumer.subscribe(Arrays.asList("ID_TOPIC", "UUID_TOPIC"));
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    LOGGER.info("Received update: {}", record);
                    if (record.topic().equals("ID_TOPIC")) {
                        if (record.value().equals("delete")) {
                            db.deleteRecord(record.key());
                        } else {
                            LOGGER.info("Saving record");
                            db.saveRecord(record.key(), record.value());
                        }
                    } else if (record.topic().equals("UUID_TOPIC")) {
                        if (record.value().equals("delete")) {
                            db.deleteUUID(record.key());
                        } else {
                            LOGGER.info("Saving UUID");
                            db.saveUUID(record.key(), record.value());
                        }
                    }
                }
            }
        } finally {
            consumer.close();
        }
    }
}