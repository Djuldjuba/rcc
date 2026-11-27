package io.student.rococo.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.student.rococo.config.Config;
import io.student.rococo.model.UserRegisteredEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaService implements Runnable {

    private static final Config CFG = Config.getInstance();
    private static final AtomicBoolean isRun = new AtomicBoolean(false);
    private static final Properties properties = new Properties();
    private static final ObjectMapper om = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private static final MapWithWait<String, UserRegisteredEvent> store = new MapWithWait<>();

    private final List<String> topics;
    private final KafkaConsumer<String, String> consumer;

    static {
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, CFG.kafkaAddress());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "rococo-tests");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    }

    public KafkaService() {
        this(List.of("users"));
    }

    public KafkaService(List<String> topics) {
        this.topics = topics;
        this.consumer = new KafkaConsumer<>(properties);
    }

    public static UserRegisteredEvent getUser(String username) throws InterruptedException {
        return store.get(username, 10_000L);
    }

    public static UserRegisteredEvent getUser(String username, long timeoutMs) throws InterruptedException {
        return store.get(username, timeoutMs);
    }

    @Override
    public void run() {
        try {
            isRun.set(true);
            consumer.subscribe(topics);
            while (isRun.get()) {
                ConsumerRecords<String, String> records =
                        consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        UserRegisteredEvent event =
                                om.readValue(record.value(), UserRegisteredEvent.class);
                        if (event.username() != null) {
                            store.put(event.username(), event);
                        }
                    } catch (Exception e) {
                        // nope
                    }
                }
            }
        } finally {
            consumer.close();
            Thread.currentThread().interrupt();
        }
    }

    public void shutdown() {
        isRun.set(false);
    }
}