package rcc.repository;

import rcc.data.entity.MuseumEntity;
import rcc.repository.impl.museum.MuseumRepositoryJdbc;
import rcc.repository.impl.museum.MuseumRepositoryHibernate;
import rcc.repository.impl.museum.MuseumRepositorySpringJdbc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MuseumRepository {

    static MuseumRepository getInstance() {
        return switch (System.getProperty("repository.impl", "jpa")) {
            case "jdbc" -> new MuseumRepositoryJdbc();
            case "spring-jdbc" -> new MuseumRepositorySpringJdbc();
            default -> new MuseumRepositoryHibernate();
        };
    }

    MuseumEntity create(MuseumEntity museum);

    MuseumEntity update(MuseumEntity museum);

    Optional<MuseumEntity> findById(UUID id);

    Optional<MuseumEntity> findByTitle(String title);

    List<MuseumEntity> findAll();

    void deleteById(UUID id);

    void deleteAll();
}
