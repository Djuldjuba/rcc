package rcc.repository.impl.authUser;

import jakarta.persistence.NoResultException;
import rcc.config.Config;
import rcc.data.entity.AuthUserEntity;
import rcc.data.entity.AuthorityEntity;
import rcc.data.entity.jpa.EntityManagers;
import rcc.repository.AuthUserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AuthUserRepositoryHibernate implements AuthUserRepository {

    private static final Config CFG = Config.getInstance();

    @Override
    public AuthUserEntity create(AuthUserEntity user) {
        String jdbcUrl = CFG.rococoAuthUrl();

        return EntityManagers.doInTransaction(jdbcUrl, em -> {
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
    }

    @Override
    public Optional<AuthUserEntity> findById(UUID id) {
        String jdbcUrl = CFG.rococoAuthUrl();

        return EntityManagers.doInQuery(jdbcUrl, em ->
                Optional.ofNullable(em.find(AuthUserEntity.class, id))
        );
    }

    @Override
    public List<AuthUserEntity> findAll() {
        String jdbcUrl = CFG.rococoAuthUrl();

        return EntityManagers.doInQuery(jdbcUrl, em ->
                em.createQuery(
                                "SELECT DISTINCT u FROM AuthUserEntity u LEFT JOIN FETCH u.authorities",
                                AuthUserEntity.class)
                        .getResultList()
        );
    }

    @Override
    public Optional<AuthUserEntity> findByUsername(String username) {
        String jdbcUrl = CFG.rococoAuthUrl();

        return EntityManagers.doInQuery(jdbcUrl, em -> {
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