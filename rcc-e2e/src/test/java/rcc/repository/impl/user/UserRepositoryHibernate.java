package rcc.repository.impl.user;

import jakarta.persistence.NoResultException;
import rcc.config.Config;
import rcc.data.entity.UserEntity;
import rcc.data.entity.jpa.EntityManagers;
import rcc.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserRepositoryHibernate implements UserRepository {

    private static final Config CFG = Config.getInstance();

    @Override
    public UserEntity create(UserEntity user) {
        String jdbcUrl = CFG.rococoApiUrl();

        return EntityManagers.doInTransaction(jdbcUrl, em -> {
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
    }

    @Override
    public UserEntity update(UserEntity user) {
        String jdbcUrl = CFG.rococoApiUrl();

        return EntityManagers.doInTransaction(jdbcUrl, em -> em.merge(user));
    }

    @Override
    public Optional<UserEntity> findById(UUID id) {
        String jdbcUrl = CFG.rococoApiUrl();

        return EntityManagers.doInQuery(jdbcUrl, em ->
                Optional.ofNullable(em.find(UserEntity.class, id))
        );
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        String jdbcUrl = CFG.rococoApiUrl();

        return EntityManagers.doInQuery(jdbcUrl, em -> {
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
    }

    @Override
    public List<UserEntity> findAll() {
        String jdbcUrl = CFG.rococoApiUrl();

        return EntityManagers.doInQuery(jdbcUrl, em ->
                em.createQuery("SELECT u FROM UserEntity u", UserEntity.class)
                        .getResultList()
        );
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