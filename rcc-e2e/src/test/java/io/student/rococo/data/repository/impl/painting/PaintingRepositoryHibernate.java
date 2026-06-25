package io.student.rococo.data.repository.impl.painting;

import jakarta.persistence.NoResultException;
import io.student.rococo.config.Config;
import io.student.rococo.data.entity.PaintingEntity;
import io.student.rococo.data.mapper.jpa.EntityManagers;
import io.student.rococo.data.repository.PaintingRepository;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PaintingRepositoryHibernate implements PaintingRepository {

    private static final Config CFG = Config.getInstance();

    @Override
    public @NonNull PaintingEntity create(@NonNull PaintingEntity painting) {
        String jdbcUrl = CFG.rococoApiUrl();

        PaintingEntity result = EntityManagers.doInTransaction(jdbcUrl, em -> {
            if (painting.getId() == null) {
                painting.setId(UUID.randomUUID());
            }
            return em.merge(painting);
        });

        if (result == null) {
            throw new IllegalStateException("Failed to create painting");
        }
        return result;
    }

    @Override
    public @NonNull PaintingEntity update(@NonNull PaintingEntity painting) {
        String jdbcUrl = CFG.rococoApiUrl();

        if (painting.getId() == null) {
            throw new IllegalArgumentException("Cannot update painting without ID");
        }

        PaintingEntity result = EntityManagers.doInTransaction(jdbcUrl, em -> em.merge(painting));

        if (result == null) {
            throw new IllegalStateException("Failed to update painting");
        }
        return result;
    }

    @Override
    public @NonNull Optional<PaintingEntity> findById(@NonNull UUID id) {
        String jdbcUrl = CFG.rococoApiUrl();

        Optional<PaintingEntity> result = EntityManagers.doInQuery(jdbcUrl, em ->
                Optional.ofNullable(em.find(PaintingEntity.class, id))
        );

        return result != null ? result : Optional.empty();
    }

    @Override
    public @NonNull Optional<PaintingEntity> findByTitle(@NonNull String title) {
        String jdbcUrl = CFG.rococoApiUrl();

        Optional<PaintingEntity> result = EntityManagers.doInQuery(jdbcUrl, em -> {
            try {
                PaintingEntity painting = em.createQuery(
                                "SELECT DISTINCT p FROM PaintingEntity p " +
                                        "LEFT JOIN FETCH p.artist " +
                                        "LEFT JOIN FETCH p.museum " +
                                        "WHERE p.title = :title",
                                PaintingEntity.class)
                        .setParameter("title", title)
                        .getSingleResult();
                return Optional.of(painting);
            } catch (NoResultException e) {
                return Optional.empty();
            }
        });

        return result != null ? result : Optional.empty();
    }

    @Override
    public @NonNull List<PaintingEntity> findAll() {
        String jdbcUrl = CFG.rococoApiUrl();

        List<PaintingEntity> result = EntityManagers.doInQuery(jdbcUrl, em ->
                em.createQuery(
                                "SELECT DISTINCT p FROM PaintingEntity p " +
                                        "LEFT JOIN FETCH p.artist " +
                                        "LEFT JOIN FETCH p.museum",
                                PaintingEntity.class)
                        .getResultList()
        );

        return result != null ? result : Collections.emptyList();
    }

    @Override
    public @NonNull List<PaintingEntity> findAllByArtistId(@NonNull UUID artistId) {
        String jdbcUrl = CFG.rococoApiUrl();

        List<PaintingEntity> result = EntityManagers.doInQuery(jdbcUrl, em ->
                em.createQuery(
                                "SELECT DISTINCT p FROM PaintingEntity p " +
                                        "LEFT JOIN FETCH p.artist " +
                                        "LEFT JOIN FETCH p.museum " +
                                        "WHERE p.artist.id = :artistId",
                                PaintingEntity.class)
                        .setParameter("artistId", artistId)
                        .getResultList()
        );

        return result != null ? result : Collections.emptyList();
    }

    @Override
    public @NonNull List<PaintingEntity> findAllByMuseumId(@NonNull UUID museumId) {
        String jdbcUrl = CFG.rococoApiUrl();

        List<PaintingEntity> result = EntityManagers.doInQuery(jdbcUrl, em ->
                em.createQuery(
                                "SELECT DISTINCT p FROM PaintingEntity p " +
                                        "LEFT JOIN FETCH p.artist " +
                                        "LEFT JOIN FETCH p.museum " +
                                        "WHERE p.museum.id = :museumId",
                                PaintingEntity.class)
                        .setParameter("museumId", museumId)
                        .getResultList()
        );

        return result != null ? result : Collections.emptyList();
    }

    @Override
    public void deleteById(@NonNull UUID id) {
        String jdbcUrl = CFG.rococoApiUrl();

        EntityManagers.doInTransaction(jdbcUrl, em -> {
            PaintingEntity painting = em.find(PaintingEntity.class, id);
            if (painting != null) {
                em.remove(painting);
            }
            return null;
        });
    }

    @Override
    public void deleteAll() {
        String jdbcUrl = CFG.rococoApiUrl();

        EntityManagers.doInTransaction(jdbcUrl, em -> {
            em.createQuery("DELETE FROM PaintingEntity").executeUpdate();
            return null;
        });
    }
}