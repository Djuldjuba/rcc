package rcc.repository.impl.painting;

import jakarta.persistence.NoResultException;
import rcc.config.Config;
import rcc.data.entity.PaintingEntity;
import rcc.data.entity.jpa.EntityManagers;
import rcc.repository.PaintingRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PaintingRepositoryHibernate implements PaintingRepository {

    private static final Config CFG = Config.getInstance();

    @Override
    public PaintingEntity create(PaintingEntity painting) {
        String jdbcUrl = CFG.rococoApiUrl();

        return EntityManagers.doInTransaction(jdbcUrl, em -> {
            if (painting.getId() == null) {
                painting.setId(UUID.randomUUID());
            }
            return em.merge(painting);
        });
    }

    @Override
    public PaintingEntity update(PaintingEntity painting) {
        String jdbcUrl = CFG.rococoApiUrl();

        return EntityManagers.doInTransaction(jdbcUrl, em -> em.merge(painting));
    }

    @Override
    public Optional<PaintingEntity> findById(UUID id) {
        String jdbcUrl = CFG.rococoApiUrl();

        return EntityManagers.doInQuery(jdbcUrl, em ->
                Optional.ofNullable(em.find(PaintingEntity.class, id))
        );
    }

    @Override
    public Optional<PaintingEntity> findByTitle(String title) {
        String jdbcUrl = CFG.rococoApiUrl();

        return EntityManagers.doInQuery(jdbcUrl, em -> {
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
    }

    @Override
    public List<PaintingEntity> findAll() {
        String jdbcUrl = CFG.rococoApiUrl();

        return EntityManagers.doInQuery(jdbcUrl, em ->
                em.createQuery(
                                "SELECT DISTINCT p FROM PaintingEntity p " +
                                        "LEFT JOIN FETCH p.artist " +
                                        "LEFT JOIN FETCH p.museum",
                                PaintingEntity.class)
                        .getResultList()
        );
    }

    @Override
    public List<PaintingEntity> findAllByArtistId(UUID artistId) {
        String jdbcUrl = CFG.rococoApiUrl();

        return EntityManagers.doInQuery(jdbcUrl, em ->
                em.createQuery(
                                "SELECT DISTINCT p FROM PaintingEntity p " +
                                        "LEFT JOIN FETCH p.artist " +
                                        "LEFT JOIN FETCH p.museum " +
                                        "WHERE p.artist.id = :artistId",
                                PaintingEntity.class)
                        .setParameter("artistId", artistId)
                        .getResultList()
        );
    }

    @Override
    public List<PaintingEntity> findAllByMuseumId(UUID museumId) {
        String jdbcUrl = CFG.rococoApiUrl();

        return EntityManagers.doInQuery(jdbcUrl, em ->
                em.createQuery(
                                "SELECT DISTINCT p FROM PaintingEntity p " +
                                        "LEFT JOIN FETCH p.artist " +
                                        "LEFT JOIN FETCH p.museum " +
                                        "WHERE p.museum.id = :museumId",
                                PaintingEntity.class)
                        .setParameter("museumId", museumId)
                        .getResultList()
        );
    }

    @Override
    public void deleteById(UUID id) {
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