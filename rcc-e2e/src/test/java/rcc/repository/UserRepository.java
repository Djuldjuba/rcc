package rcc.repository;

import rcc.data.entity.UserEntity;
import rcc.repository.impl.user.UserRepositoryHibernate;
import rcc.repository.impl.user.UserRepositoryJdbc;
import rcc.repository.impl.user.UserRepositorySpringJdbc;

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
    UserEntity create(UserEntity user);
    UserEntity update(UserEntity user);
    Optional<UserEntity> findById(UUID id);
    Optional<UserEntity> findByUsername(String username);
    List<UserEntity> findAll();

}
