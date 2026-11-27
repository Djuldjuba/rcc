package io.student.rcc.logkafka.kafka;

import io.student.rcc.logkafka.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LogEventsListener {

    private static final Logger LOG = LoggerFactory.getLogger(LogEventsListener.class);

    private final LogService logService;

    public LogEventsListener(LogService logService) {
        this.logService = logService;
    }

    @KafkaListener(topics = "users", groupId = "rcc-log-kafka-service")
    @Transactional
    public void onUserRegistered(LogEvent event) {
        if (!isValid(event, "users")) {
            return;
        }
        LOG.info("Log received from users: {} ({})", event.username(), event.eventType());
        logService.save(event.userId(), null, event.eventType(), event.eventDate());
    }

    @KafkaListener(topics = "user-updates", groupId = "rcc-log-kafka-service")
    @Transactional
    public void onUserUpdated(LogEvent event) {
        if (!isValid(event, "user-updates")) {
            return;
        }
        LOG.info("Log received from user-updates: {} ({})", event.username(), event.eventType());
        logService.save(event.userId(), null, event.eventType(), event.eventDate());
    }

    private boolean isValid(LogEvent event, String topic) {
        if (event == null) {
            LOG.warn("Skipping null event from topic [{}]", topic);
            return false;
        }
        if (event.userId() == null || event.userId().isBlank()
                || event.username() == null || event.username().isBlank()
                || event.eventType() == null || event.eventType().isBlank()
                || event.eventDate() == null) {
            LOG.warn("Skipping malformed event from topic [{}]: {}", topic, event);
            return false;
        }
        return true;
    }
}