package io.student.rococo.service;

import io.student.rococo.model.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class UserRegisteredEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(UserRegisteredEventListener.class);
    private static final String TOPIC = "users";

    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    @Autowired
    public UserRegisteredEventListener(KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        try {
            kafkaTemplate.send(TOPIC, event.username(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            LOG.error("### Failed to send user registration event to Kafka topic [{}]: {}", TOPIC, event.username(), ex);
                        } else {
                            LOG.info("### Kafka topic [{}] sent message: {} (offset={})",
                                    TOPIC, event.username(), result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            LOG.error("### Unexpected error while sending user registration event for {}", event.username(), e);
        }
    }
}