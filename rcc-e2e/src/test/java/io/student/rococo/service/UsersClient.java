package io.student.rococo.service;

import io.student.rococo.model.UserJson;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface UsersClient {

    @NonNull UserJson createUser(@NonNull String username, @NonNull String password);
}