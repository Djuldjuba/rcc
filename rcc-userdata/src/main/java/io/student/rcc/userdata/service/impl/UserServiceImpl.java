package io.student.rcc.userdata.service.impl;

import io.student.rcc.userdata.data.entity.UserEntity;
import io.student.rcc.userdata.data.repository.UserRepository;
import io.student.rcc.userdata.domain.UpdateUserRequest;
import io.student.rcc.userdata.domain.UserResponse;
import io.student.rcc.userdata.exception.ResourceNotFoundException;
import io.student.rcc.userdata.kafka.UserUpdatedEvent;
import io.student.rcc.userdata.mapper.UserMapper;
import io.student.rcc.userdata.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;

    public UserServiceImpl(UserRepository userRepository,
                           UserMapper userMapper,
                           ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String username) {
        UserEntity entity = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", "username", username));
        return userMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse createNewUserIfNotPresent(String username) {
        UserEntity entity = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Пользователь", "username", username + " (ожидается создание через Kafka)"));
        return userMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public UserResponse updateUser(String username, UpdateUserRequest request) {
        UserEntity entity = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", "username", username));

        userMapper.updateEntity(entity, request);
        UserEntity updated = userRepository.save(entity);

        eventPublisher.publishEvent(new UserUpdatedEvent(
                updated.getId().toString(),
                updated.getUsername(),
                "USER_UPDATED",
                LocalDateTime.now()
        ));

        return userMapper.toResponse(updated);
    }
}