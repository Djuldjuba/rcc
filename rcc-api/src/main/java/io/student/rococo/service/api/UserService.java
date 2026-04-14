package io.student.rococo.service.api;

import io.student.rococo.model.UserJson;

public interface UserService {
  UserJson getCurrentUser(String username);

  UserJson update(UserJson user);

  UserJson createNewUserIfNotPresent(String username);
}
