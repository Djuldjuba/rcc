package rcc.repository;

import rcc.data.entity.ArtistEntity;
import rcc.repository.impl.artist.ArtistRepositoryHibernate;
import rcc.repository.impl.artist.ArtistRepositoryJdbc;
import rcc.repository.impl.artist.ArtistRepositorySpringJdbc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArtistRepository {

    static ArtistRepository getInstance() {
        return switch (System.getProperty("repository.impl", "jpa")) {
            case "jdbc" -> new ArtistRepositoryJdbc();
            case "spring-jdbc" -> new ArtistRepositorySpringJdbc();
            default -> new ArtistRepositoryHibernate();
        };
    }

    ArtistEntity create(ArtistEntity artist);
    ArtistEntity update(ArtistEntity artist);
    Optional<ArtistEntity> findById(UUID id);
    Optional<ArtistEntity> findByName(String name);
    List<ArtistEntity> findAll();
    void deleteById(UUID id);
    void deleteAll();
}
