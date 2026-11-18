package io.student.rcc.userdata.mapper;

import io.student.rcc.userdata.data.entity.UserEntity;
import io.student.rcc.userdata.domain.UpdateUserRequest;
import io.student.rcc.userdata.domain.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(UserEntity entity) {
        return new UserResponse(
                entity.getId().toString(),
                entity.getUsername(),
                entity.getFirstname(),
                entity.getLastname(),
                entity.getAvatarAsString()
        );
    }

    public void updateEntity(UserEntity entity, UpdateUserRequest request) {
        if (request.firstname() != null) {
            entity.setFirstname(request.firstname());
        }
        if (request.lastname() != null) {
            entity.setLastname(request.lastname());
        }
        if (request.avatar() != null) {
            entity.setAvatarFromString(request.avatar());
        }
    }

    public UserEntity toEntity(String username) {
        UserEntity entity = new UserEntity();
        entity.setUsername(username);
        return entity;
    }
}