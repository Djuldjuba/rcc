package rcc.repository;

import rcc.data.entity.PaintingEntity;
import rcc.repository.impl.painting.PaintingRepositoryHibernate;
import rcc.repository.impl.painting.PaintingRepositoryJdbc;
import rcc.repository.impl.painting.PaintingRepositorySpringJdbc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaintingRepository {

    static PaintingRepository getInstance() {
        return switch (System.getProperty("repository.impl", "jpa")) {
            case "jdbc" -> new PaintingRepositoryJdbc();
            case "spring-jdbc" -> new PaintingRepositorySpringJdbc();
            default -> new PaintingRepositoryHibernate();
        };
    }

    PaintingEntity create(PaintingEntity painting);
    PaintingEntity update(PaintingEntity painting);
    Optional<PaintingEntity> findById(UUID id);
    Optional<PaintingEntity> findByTitle(String title);
    List<PaintingEntity> findAll();
    List<PaintingEntity> findAllByArtistId(UUID artistId);
    List<PaintingEntity> findAllByMuseumId(UUID museumId);
    void deleteById(UUID id);
    void deleteAll();
}
