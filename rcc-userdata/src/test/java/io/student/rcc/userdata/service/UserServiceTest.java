package io.student.rcc.userdata.service;

import io.student.rcc.userdata.data.entity.UserEntity;
import io.student.rcc.userdata.data.repository.UserRepository;
import io.student.rcc.userdata.domain.UpdateUserRequest;
import io.student.rcc.userdata.domain.UserResponse;
import io.student.rcc.userdata.exception.ResourceNotFoundException;
import io.student.rcc.userdata.mapper.UserMapper;
import io.student.rcc.userdata.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getCurrentUserShouldReturnResponse() {
        String username = "alice";
        UUID id = UUID.randomUUID();
        UserEntity entity = new UserEntity();
        entity.setId(id);
        entity.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(entity));
        when(userMapper.toResponse(entity)).thenReturn(
                new UserResponse(id.toString(), username, null, null, null)
        );

        UserResponse result = userService.getCurrentUser(username);

        assertEquals(username, result.username());
        assertEquals(id.toString(), result.id());
        verify(userRepository).findByUsername(username);
        verify(userMapper).toResponse(entity);
    }

    @Test
    void getCurrentUserShouldThrowWhenNotFound() {
        String username = "ghost";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getCurrentUser(username));
        verify(userRepository).findByUsername(username);
        verifyNoInteractions(userMapper);
    }

    @Test
    void createNewUserShouldReturnExistingWhenPresent() {
        String username = "alice";
        UUID id = UUID.randomUUID();
        UserEntity entity = new UserEntity();
        entity.setId(id);
        entity.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(entity));
        when(userMapper.toResponse(entity)).thenReturn(
                new UserResponse(id.toString(), username, null, null, null)
        );

        UserResponse result = userService.createNewUserIfNotPresent(username);

        assertEquals(username, result.username());
        verify(userRepository).findByUsername(username);
        verify(userRepository, never()).save(any());
        verify(userMapper).toResponse(entity);
    }

    @Test
    void createNewUserShouldSaveWhenNotPresent() {
        String username = "newbie";
        UUID id = UUID.randomUUID();

        UserEntity saved = new UserEntity();
        saved.setId(id);
        saved.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenReturn(saved);
        when(userMapper.toResponse(saved)).thenReturn(
                new UserResponse(id.toString(), username, null, null, null)
        );

        UserResponse result = userService.createNewUserIfNotPresent(username);

        assertEquals(username, result.username());
        verify(userRepository).findByUsername(username);
        verify(userRepository).save(any(UserEntity.class));
        verify(userMapper).toResponse(saved);
    }

    @Test
    void updateUserShouldUpdateAndReturnResponse() {
        String username = "alice";
        UUID id = UUID.randomUUID();

        UserEntity existing = new UserEntity();
        existing.setId(id);
        existing.setUsername(username);
        existing.setFirstname("Old");

        UpdateUserRequest request = new UpdateUserRequest("New", "Petrova", null);

        UserEntity updated = new UserEntity();
        updated.setId(id);
        updated.setUsername(username);
        updated.setFirstname("New");
        updated.setLastname("Petrova");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existing));
        doNothing().when(userMapper).updateEntity(existing, request);
        when(userRepository.save(existing)).thenReturn(updated);
        when(userMapper.toResponse(updated)).thenReturn(
                new UserResponse(id.toString(), username, "New", "Petrova", null)
        );

        UserResponse result = userService.updateUser(username, request);

        assertEquals("New", result.firstname());
        assertEquals("Petrova", result.lastname());
        verify(userRepository).findByUsername(username);
        verify(userMapper).updateEntity(existing, request);
        verify(userRepository).save(existing);
        verify(userMapper).toResponse(updated);
    }

    @Test
    void updateUserShouldThrowWhenNotFound() {
        String username = "ghost";
        UpdateUserRequest request = new UpdateUserRequest("A", "B", null);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUser(username, request));
        verify(userRepository).findByUsername(username);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(userMapper);
    }
}