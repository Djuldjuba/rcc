package io.student.rococo.data.repository.impl.authUser;

import jakarta.persistence.NoResultException;
import io.student.rococo.config.Config;
import io.student.rococo.data.entity.AuthUserEntity;
import io.student.rococo.data.entity.AuthorityEntity;
import io.student.rococo.data.mapper.jpa.EntityManagers;
import io.student.rococo.data.repository.AuthUserRepository;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AuthUserRepositoryHibernate implements AuthUserRepository {

    private static final Config CFG = Config.getInstance();

    @Override
    public @NonNull AuthUserEntity create(@NonNull AuthUserEntity user) {
        String jdbcUrl = CFG.rococoAuthUrl();

        AuthUserEntity result = EntityManagers.doInTransaction(jdbcUrl, em -> {
            if (user.getId() == null) {
                user.setId(UUID.randomUUID());
            }

            AuthUserEntity merged = em.merge(user);

            for (AuthorityEntity authority : user.getAuthorities()) {
                if (authority.getId() == null) {
                    authority.setId(UUID.randomUUID());
                }
                if (authority.getUser() == null) {
                    authority.setUser(merged);
                }
                em.merge(authority);
            }

            return merged;
        });

        if (result == null) {
            throw new IllegalStateException("Failed to create auth user");
        }
        return result;
    }

    @Override
    public @NonNull Optional<AuthUserEntity> findById(@NonNull UUID id) {
        String jdbcUrl = CFG.rococoAuthUrl();

        Optional<AuthUserEntity> result = EntityManagers.doInQuery(jdbcUrl, em ->
                Optional.ofNullable(em.find(AuthUserEntity.class, id))
        );

        return result != null ? result : Optional.empty();
    }

    @Override
    public @NonNull List<AuthUserEntity> findAll() {
        String jdbcUrl = CFG.rococoAuthUrl();

        List<AuthUserEntity> result = EntityManagers.doInQuery(jdbcUrl, em ->
                em.createQuery(
                                "SELECT DISTINCT u FROM AuthUserEntity u LEFT JOIN FETCH u.authorities",
                                AuthUserEntity.class)
                        .getResultList()
        );

        return result != null ? result : Collections.emptyList();
    }

    @Override
    public @NonNull Optional<AuthUserEntity> findByUsername(@NonNull String username) {
        String jdbcUrl = CFG.rococoAuthUrl();

        Optional<AuthUserEntity> result = EntityManagers.doInQuery(jdbcUrl, em -> {
            try {
                AuthUserEntity user = em.createQuery(
                                "SELECT DISTINCT u FROM AuthUserEntity u LEFT JOIN FETCH u.authorities WHERE u.username = :username",
                                AuthUserEntity.class)
                        .setParameter("username", username)
                        .getSingleResult();
                return Optional.of(user);
            } catch (NoResultException e) {
                return Optional.empty();
            }
        });

        return result != null ? result : Optional.empty();
    }

    public void deleteAll() {
        String jdbcUrl = CFG.rococoAuthUrl();

        EntityManagers.doInTransaction(jdbcUrl, em -> {
            em.createQuery("DELETE FROM AuthorityEntity").executeUpdate();
            em.createQuery("DELETE FROM AuthUserEntity").executeUpdate();
            return null;
        });
    }
}