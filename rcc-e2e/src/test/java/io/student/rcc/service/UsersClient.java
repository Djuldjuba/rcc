package io.student.rcc.service;

import io.student.rcc.model.UserJson;

public interface UsersClient {

    UserJson createUser(String username, String password);
}
