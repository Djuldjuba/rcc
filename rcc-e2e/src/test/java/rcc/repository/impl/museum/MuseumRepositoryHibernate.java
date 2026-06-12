package rcc.repository.impl.museum;

import jakarta.persistence.NoResultException;
import rcc.config.Config;
import rcc.data.entity.MuseumEntity;
import rcc.data.entity.jpa.EntityManagers;
import rcc.repository.MuseumRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MuseumRepositoryHibernate implements MuseumRepository {

    private static final Config CFG = Config.getInstance();

    @Override
    public MuseumEntity create(MuseumEntity museum) {
        String jdbcUrl = CFG.rococoApiUrl();

        return EntityManagers.doInTransaction(jdbcUrl, em -> {
            if (museum.getId() == null) {
                museum.setId(UUID.randomUUID());
            }
            return em.merge(museum);
        });
    }

    @Override
    public MuseumEntity update(MuseumEntity museum) {
        String jdbcUrl = CFG.rococoApiUrl();

        return EntityManagers.doInTransaction(jdbcUrl, em -> em.merge(museum));
    }

    @Override
    public Optional<MuseumEntity> findById(UUID id) {
        String jdbcUrl = CFG.rococoApiUrl();

        return EntityManagers.doInQuery(jdbcUrl, em ->
                Optional.ofNullable(em.find(MuseumEntity.class, id))
        );
    }

    @Override
    public Optional<MuseumEntity> findByTitle(String title) {
        String jdbcUrl = CFG.rococoApiUrl();

        return EntityManagers.doInQuery(jdbcUrl, em -> {
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
    }

    @Override
    public List<MuseumEntity> findAll() {
        String jdbcUrl = CFG.rococoApiUrl();

        return EntityManagers.doInQuery(jdbcUrl, em ->
                em.createQuery("SELECT DISTINCT m FROM MuseumEntity m LEFT JOIN FETCH m.country", MuseumEntity.class)
                        .getResultList()
        );
    }

    @Override
    public void deleteById(UUID id) {
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