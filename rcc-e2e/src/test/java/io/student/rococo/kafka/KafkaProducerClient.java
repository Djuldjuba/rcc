package io.student.rococo.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.student.rococo.config.Config;
import io.student.rococo.model.UserRegisteredEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

public class KafkaProducerClient implements AutoCloseable {

    private static final Config CFG = Config.getInstance();
    private static final String TOPIC = "users";

    private final KafkaProducer<String, String> producer;
    private final ObjectMapper om = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public KafkaProducerClient() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, CFG.kafkaAddress());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        this.producer = new KafkaProducer<>(props);
    }

    public void sendUserRegistered(String userId, String username) throws Exception {
        UserRegisteredEvent event = new UserRegisteredEvent(
                userId,
                username,
                "USER_REGISTERED",
                LocalDateTime.now()
        );
        String json = om.writeValueAsString(event);
        producer.send(new ProducerRecord<>(TOPIC, username, json)).get();
        producer.flush();
    }

    @Override
    public void close() {
        producer.close();
    }
}