package io.student.rcc.userdata.kafka;

import io.student.rcc.userdata.data.entity.UserEntity;
import io.student.rcc.userdata.data.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserRegisteredKafkaListener {

    private static final Logger LOG = LoggerFactory.getLogger(UserRegisteredKafkaListener.class);

    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public UserRegisteredKafkaListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @KafkaListener(topics = "users", groupId = "rcc-userdata")
    @Transactional
    public void onUserRegistered(UserRegisteredEvent event) {
        if (!isValid(event)) {
            return;
        }

        UUID expectedId = UUID.fromString(event.userId());

        if (userRepository.existsById(expectedId)) {
            LOG.debug("User {} already present with correct id={}", event.username(), expectedId);
            return;
        }

        Optional<UserEntity> existingByUsername = userRepository.findByUsername(event.username());
        if (existingByUsername.isPresent()) {
            UserEntity old = existingByUsername.get();
            LOG.warn("User {} already present with id={}, expected={} — fixing",
                    event.username(), old.getId(), expectedId);
            userRepository.delete(old);
            userRepository.flush();
        }

        UserEntity entity = new UserEntity();
        entity.setId(expectedId);
        entity.setUsername(event.username());
        entityManager.persist(entity);

        LOG.info("User {} created in userdata DB with id={}", event.username(), expectedId);
    }

    private boolean isValid(UserRegisteredEvent event) {
        if (event == null
                || event.userId() == null || event.userId().isBlank()
                || event.username() == null || event.username().isBlank()) {
            LOG.warn("Skipping malformed user registration event: {}", event);
            return false;
        }
        return true;
    }
}