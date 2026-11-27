package io.student.rococo.test;

import io.student.rococo.data.entity.UserEntity;
import io.student.rococo.data.repository.UserRepository;
import io.student.rococo.kafka.KafkaProducerClient;
import io.student.rococo.utils.RandomDataUtils;
import io.student.rococo.utils.Waiter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserdataKafkaConsumerTest {

    private final UserRepository userRepository = UserRepository.getInstance();
    private final KafkaProducerClient kafkaProducer = new KafkaProducerClient();

    @AfterEach
    void tearDown() {
        kafkaProducer.close();
    }

    @Test
    @DisplayName("UserRegisteredEvent создаёт пользователя в БД userdata")
    void shouldCreateUserInDbWhenEventReceivedFromKafka() throws Exception {
        String username = RandomDataUtils.randomUserName();
        String userId = UUID.randomUUID().toString();

        kafkaProducer.sendUserRegistered(userId, username);

        Optional<UserEntity> created = Waiter.waitOptional(
                () -> userRepository.findByUsername(username),
                15_000L,
                300L
        );

        assertTrue(created.isPresent(),
                "Пользователь '" + username + "' должен появиться в БД");
        UserEntity user = created.get();
        assertEquals(username, user.getUsername());
        assertEquals(UUID.fromString(userId), user.getId());
    }
}