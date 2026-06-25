package io.student.rococo.data.repository;

import io.student.rococo.data.entity.UserEntity;
import io.student.rococo.data.repository.impl.user.UserRepositoryHibernate;
import io.student.rococo.data.repository.impl.user.UserRepositoryJdbc;
import io.student.rococo.data.repository.impl.user.UserRepositorySpringJdbc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    static UserRepository getInstance() {
        return switch (System.getProperty("repository.impl", "jpa")) {
            case "jdbc" -> new UserRepositoryJdbc();
            case "spring-jdbc" -> new UserRepositorySpringJdbc();
            default -> new UserRepositoryHibernate();
        };
    }

    @NonNull UserEntity create(@NonNull UserEntity user);

    @NonNull UserEntity update(@NonNull UserEntity user);

    @NonNull Optional<UserEntity> findById(@NonNull UUID id);

    @NonNull Optional<UserEntity> findByUsername(@NonNull String username);

    @NonNull List<UserEntity> findAll();
}