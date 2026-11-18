package io.student.rcc.userdata.service;

import io.student.rcc.userdata.domain.UpdateUserRequest;
import io.student.rcc.userdata.domain.UserResponse;

public interface UserService {

    UserResponse getCurrentUser(String username);

    UserResponse createNewUserIfNotPresent(String username);

    UserResponse updateUser(String username, UpdateUserRequest request);
}