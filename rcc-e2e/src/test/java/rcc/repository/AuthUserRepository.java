package rcc.repository;

import rcc.data.entity.AuthUserEntity;
import rcc.repository.impl.authUser.AuthUserRepositoryHibernate;
import rcc.repository.impl.authUser.AuthUserRepositoryJdbc;
import rcc.repository.impl.authUser.AuthUserRepositorySpringJdbc;

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
    AuthUserEntity create(AuthUserEntity user);
    Optional<AuthUserEntity> findById(UUID id);
    List<AuthUserEntity> findAll();
    Optional<AuthUserEntity> findByUsername(String username);
}
