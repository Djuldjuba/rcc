package io.student.rococo.data.repository.impl.artist;

import jakarta.persistence.NoResultException;
import io.student.rococo.config.Config;
import io.student.rococo.data.entity.ArtistEntity;
import io.student.rococo.data.mapper.jpa.EntityManagers;
import io.student.rococo.data.repository.ArtistRepository;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ArtistRepositoryHibernate implements ArtistRepository {

    private static final Config CFG = Config.getInstance();

    @Override
    public @NonNull ArtistEntity create(@NonNull ArtistEntity artist) {
        String jdbcUrl = CFG.rococoApiUrl();

        ArtistEntity result = EntityManagers.doInTransaction(jdbcUrl, em -> {
            if (artist.getId() == null) {
                artist.setId(UUID.randomUUID());
            }
            return em.merge(artist);
        });

        if (result == null) {
            throw new IllegalStateException("Failed to create artist");
        }
        return result;
    }

    @Override
    public @NonNull ArtistEntity update(@NonNull ArtistEntity artist) {
        String jdbcUrl = CFG.rococoApiUrl();

        if (artist.getId() == null) {
            throw new IllegalArgumentException("Cannot update artist without ID");
        }

        ArtistEntity result = EntityManagers.doInTransaction(jdbcUrl, em -> {
            if (artist.getId() == null) {
                throw new RuntimeException("Cannot update artist without ID");
            }
            return em.merge(artist);
        });

        if (result == null) {
            throw new IllegalStateException("Failed to update artist");
        }
        return result;
    }

    @Override
    public @NonNull Optional<ArtistEntity> findById(@NonNull UUID id) {
        String jdbcUrl = CFG.rococoApiUrl();

        Optional<ArtistEntity> result = EntityManagers.doInQuery(jdbcUrl, em ->
                Optional.ofNullable(em.find(ArtistEntity.class, id))
        );

        return result != null ? result : Optional.empty();
    }

    @Override
    public @NonNull Optional<ArtistEntity> findByName(@NonNull String name) {
        String jdbcUrl = CFG.rococoApiUrl();

        Optional<ArtistEntity> result = EntityManagers.doInQuery(jdbcUrl, em -> {
            try {
                ArtistEntity artist = em.createQuery(
                                "SELECT a FROM ArtistEntity a WHERE a.name = :name",
                                ArtistEntity.class)
                        .setParameter("name", name)
                        .getSingleResult();
                return Optional.of(artist);
            } catch (NoResultException e) {
                return Optional.empty();
            }
        });

        return result != null ? result : Optional.empty();
    }

    @Override
    public @NonNull List<ArtistEntity> findAll() {
        String jdbcUrl = CFG.rococoApiUrl();

        List<ArtistEntity> result = EntityManagers.doInQuery(jdbcUrl, em ->
                em.createQuery("SELECT a FROM ArtistEntity a ORDER BY a.name", ArtistEntity.class)
                        .getResultList()
        );

        return result != null ? result : Collections.emptyList();
    }

    @Override
    public void deleteById(@NonNull UUID id) {
        String jdbcUrl = CFG.rococoApiUrl();

        EntityManagers.doInTransaction(jdbcUrl, em -> {
            ArtistEntity artist = em.find(ArtistEntity.class, id);
            if (artist != null) {
                em.remove(artist);
            }
            return null;
        });
    }

    @Override
    public void deleteAll() {
        String jdbcUrl = CFG.rococoApiUrl();

        EntityManagers.doInTransaction(jdbcUrl, em -> {
            em.createQuery("DELETE FROM ArtistEntity").executeUpdate();
            return null;
        });
    }
}