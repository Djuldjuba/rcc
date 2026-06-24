package io.student.rococo.data.repository;

import io.student.rococo.data.entity.AuthUserEntity;
import io.student.rococo.data.repository.impl.authUser.AuthUserRepositoryHibernate;
import io.student.rococo.data.repository.impl.authUser.AuthUserRepositoryJdbc;
import io.student.rococo.data.repository.impl.authUser.AuthUserRepositorySpringJdbc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthUserRepository {

    static AuthUserRepository getInstance() {
        return switch (System.getProperty("repository.impl", "jpa")) {
            case "jdbc" -> new AuthUserRepositoryJdbc();
            case "spring-jdbc" -> new AuthUserRepositorySpringJdbc();
            default -> new AuthUserRepositoryHibernate();
        };
    }

    @NonNull AuthUserEntity create(@NonNull AuthUserEntity user);

    @NonNull Optional<AuthUserEntity> findById(@NonNull UUID id);

    @NonNull List<AuthUserEntity> findAll();

    @NonNull Optional<AuthUserEntity> findByUsername(@NonNull String username);
}