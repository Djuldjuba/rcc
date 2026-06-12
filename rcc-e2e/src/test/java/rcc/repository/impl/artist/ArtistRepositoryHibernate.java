package rcc.repository.impl.artist;

import jakarta.persistence.NoResultException;
import rcc.config.Config;
import rcc.data.entity.ArtistEntity;
import rcc.data.entity.jpa.EntityManagers;
import rcc.repository.ArtistRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ArtistRepositoryHibernate implements ArtistRepository {

    private static final Config CFG = Config.getInstance();

    @Override
    public ArtistEntity create(ArtistEntity artist) {
        String jdbcUrl = CFG.rococoApiUrl();

        return EntityManagers.doInTransaction(jdbcUrl, em -> {
            if (artist.getId() == null) {
                artist.setId(UUID.randomUUID());
            }
            return em.merge(artist);
        });
    }

    @Override
    public ArtistEntity update(ArtistEntity artist) {
        String jdbcUrl = CFG.rococoApiUrl();

        return EntityManagers.doInTransaction(jdbcUrl, em -> {
            if (artist.getId() == null) {
                throw new RuntimeException("Cannot update artist without ID");
            }
            return em.merge(artist);
        });
    }

    @Override
    public Optional<ArtistEntity> findById(UUID id) {
        String jdbcUrl = CFG.rococoApiUrl();

        return EntityManagers.doInQuery(jdbcUrl, em ->
                Optional.ofNullable(em.find(ArtistEntity.class, id))
        );
    }

    @Override
    public Optional<ArtistEntity> findByName(String name) {
        String jdbcUrl = CFG.rococoApiUrl();

        return EntityManagers.doInQuery(jdbcUrl, em -> {
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
    }

    @Override
    public List<ArtistEntity> findAll() {
        String jdbcUrl = CFG.rococoApiUrl();

        return EntityManagers.doInQuery(jdbcUrl, em ->
                em.createQuery("SELECT a FROM ArtistEntity a ORDER BY a.name", ArtistEntity.class)
                        .getResultList()
        );
    }

    @Override
    public void deleteById(UUID id) {
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