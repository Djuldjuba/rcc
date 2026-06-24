package io.student.rococo.data.repository.impl.user;

import jakarta.persistence.NoResultException;
import io.student.rococo.config.Config;
import io.student.rococo.data.entity.UserEntity;
import io.student.rococo.data.mapper.jpa.EntityManagers;
import io.student.rococo.data.repository.UserRepository;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserRepositoryHibernate implements UserRepository {

    private static final Config CFG = Config.getInstance();

    @Override
    public @NonNull UserEntity create(@NonNull UserEntity user) {
        String jdbcUrl = CFG.rococoApiUrl();

        UserEntity result = EntityManagers.doInTransaction(jdbcUrl, em -> {
            if (user.getId() != null) {
                UserEntity existing = em.find(UserEntity.class, user.getId());
                if (existing == null) {
                    em.createNativeQuery(
                                    "INSERT INTO `user` (id, username, firstname, lastname, avatar) " +
                                            "VALUES (UUID_TO_BIN(:id), :username, :firstname, :lastname, :avatar)")
                            .setParameter("id", user.getId().toString())
                            .setParameter("username", user.getUsername())
                            .setParameter("firstname", user.getFirstname())
                            .setParameter("lastname", user.getLastname())
                            .setParameter("avatar", user.getAvatar())
                            .executeUpdate();
                    return user;
                } else {
                    return em.merge(user);
                }
            } else {
                em.persist(user);
                return user;
            }
        });

        if (result == null) {
            throw new IllegalStateException("Failed to create user");
        }
        return result;
    }

    @Override
    public @NonNull UserEntity update(@NonNull UserEntity user) {
        String jdbcUrl = CFG.rococoApiUrl();

        if (user.getId() == null) {
            throw new IllegalArgumentException("Cannot update user without ID");
        }

        UserEntity result = EntityManagers.doInTransaction(jdbcUrl, em -> em.merge(user));

        if (result == null) {
            throw new IllegalStateException("Failed to update user");
        }
        return result;
    }

    @Override
    public @NonNull Optional<UserEntity> findById(@NonNull UUID id) {
        String jdbcUrl = CFG.rococoApiUrl();

        Optional<UserEntity> result = EntityManagers.doInQuery(jdbcUrl, em ->
                Optional.ofNullable(em.find(UserEntity.class, id))
        );

        return result != null ? result : Optional.empty();
    }

    @Override
    public @NonNull Optional<UserEntity> findByUsername(@NonNull String username) {
        String jdbcUrl = CFG.rococoApiUrl();

        Optional<UserEntity> result = EntityManagers.doInQuery(jdbcUrl, em -> {
            try {
                return Optional.of(em.createQuery(
                                "SELECT u FROM UserEntity u WHERE u.username = :username",
                                UserEntity.class)
                        .setParameter("username", username)
                        .getSingleResult());
            } catch (NoResultException e) {
                return Optional.empty();
            }
        });

        return result != null ? result : Optional.empty();
    }

    @Override
    public @NonNull List<UserEntity> findAll() {
        String jdbcUrl = CFG.rococoApiUrl();

        List<UserEntity> result = EntityManagers.doInQuery(jdbcUrl, em ->
                em.createQuery("SELECT u FROM UserEntity u", UserEntity.class)
                        .getResultList()
        );

        return result != null ? result : Collections.emptyList();
    }

    public void deleteById(UUID id) {
        String jdbcUrl = CFG.rococoApiUrl();

        EntityManagers.doInTransaction(jdbcUrl, em -> {
            UserEntity user = em.find(UserEntity.class, id);
            if (user != null) {
                em.remove(user);
            }
            return null;
        });
    }

    public void deleteAll() {
        String jdbcUrl = CFG.rococoApiUrl();

        EntityManagers.doInTransaction(jdbcUrl, em -> {
            em.createQuery("DELETE FROM UserEntity").executeUpdate();
            return null;
        });
    }
}