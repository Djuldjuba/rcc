package io.student.rococo.data.repository.impl.museum;

import jakarta.persistence.NoResultException;
import io.student.rococo.config.Config;
import io.student.rococo.data.entity.MuseumEntity;
import io.student.rococo.data.mapper.jpa.EntityManagers;
import io.student.rococo.data.repository.MuseumRepository;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MuseumRepositoryHibernate implements MuseumRepository {

    private static final Config CFG = Config.getInstance();

    @Override
    public @NonNull MuseumEntity create(@NonNull MuseumEntity museum) {
        String jdbcUrl = CFG.rococoApiUrl();

        MuseumEntity result = EntityManagers.doInTransaction(jdbcUrl, em -> {
            if (museum.getId() == null) {
                museum.setId(UUID.randomUUID());
            }
            return em.merge(museum);
        });

        if (result == null) {
            throw new IllegalStateException("Failed to create museum");
        }
        return result;
    }

    @Override
    public @NonNull MuseumEntity update(@NonNull MuseumEntity museum) {
        String jdbcUrl = CFG.rococoApiUrl();

        if (museum.getId() == null) {
            throw new IllegalArgumentException("Cannot update museum without ID");
        }

        MuseumEntity result = EntityManagers.doInTransaction(jdbcUrl, em -> em.merge(museum));

        if (result == null) {
            throw new IllegalStateException("Failed to update museum");
        }
        return result;
    }

    @Override
    public @NonNull Optional<MuseumEntity> findById(@NonNull UUID id) {
        String jdbcUrl = CFG.rococoApiUrl();

        Optional<MuseumEntity> result = EntityManagers.doInQuery(jdbcUrl, em ->
                Optional.ofNullable(em.find(MuseumEntity.class, id))
        );

        return result != null ? result : Optional.empty();
    }

    @Override
    public @NonNull Optional<MuseumEntity> findByTitle(@NonNull String title) {
        String jdbcUrl = CFG.rococoApiUrl();

        Optional<MuseumEntity> result = EntityManagers.doInQuery(jdbcUrl, em -> {
            try {
                MuseumEntity museum = em.createQuery(
                                "SELECT DISTINCT m FROM MuseumEntity m LEFT JOIN FETCH m.country WHERE m.title = :title",
                                MuseumEntity.class)
                        .setParameter("title", title)
                        .getSingleResult();
                return Optional.of(museum);
            } catch (NoResultException e) {
                return Optional.empty();
            }
        });

        return result != null ? result : Optional.empty();
    }

    @Override
    public @NonNull List<MuseumEntity> findAll() {
        String jdbcUrl = CFG.rococoApiUrl();

        List<MuseumEntity> result = EntityManagers.doInQuery(jdbcUrl, em ->
                em.createQuery("SELECT DISTINCT m FROM MuseumEntity m LEFT JOIN FETCH m.country", MuseumEntity.class)
                        .getResultList()
        );

        return result != null ? result : Collections.emptyList();
    }

    @Override
    public void deleteById(@NonNull UUID id) {
        String jdbcUrl = CFG.rococoApiUrl();

        EntityManagers.doInTransaction(jdbcUrl, em -> {
            MuseumEntity museum = em.find(MuseumEntity.class, id);
            if (museum != null) {
                em.remove(museum);
            }
            return null;
        });
    }

    @Override
    public void deleteAll() {
        String jdbcUrl = CFG.rococoApiUrl();

        EntityManagers.doInTransaction(jdbcUrl, em -> {
            em.createQuery("DELETE FROM MuseumEntity").executeUpdate();
            return null;
        });
    }
}