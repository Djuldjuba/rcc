package io.student.rcc.userdata.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class UserUpdatedEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(UserUpdatedEventListener.class);
    private static final String TOPIC = "user-updates";

    private final KafkaTemplate<String, UserUpdatedEvent> kafkaTemplate;

    public UserUpdatedEventListener(KafkaTemplate<String, UserUpdatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserUpdated(UserUpdatedEvent event) {
        if (event == null
                || event.userId() == null || event.userId().isBlank()
                || event.username() == null || event.username().isBlank()) {
            LOG.warn("Skipping malformed user updated event: {}", event);
            return;
        }
        try {
            kafkaTemplate.send(TOPIC, event.username(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            LOG.error("Failed to send user updated event to Kafka topic [{}]: {}", TOPIC, event.username(), ex);
                        } else {
                            LOG.info("Kafka topic [{}] sent message: {} (userId={}, offset={})",
                                    TOPIC, event.username(), event.userId(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            LOG.error("Unexpected error while sending user updated event for {}", event.username(), e);
        }
    }
}