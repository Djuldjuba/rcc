package io.student.rococo.data.repository;

import io.student.rococo.data.entity.MuseumEntity;
import io.student.rococo.data.repository.impl.museum.MuseumRepositoryJdbc;
import io.student.rococo.data.repository.impl.museum.MuseumRepositoryHibernate;
import io.student.rococo.data.repository.impl.museum.MuseumRepositorySpringJdbc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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

    @NonNull MuseumEntity create(@NonNull MuseumEntity museum);

    @NonNull MuseumEntity update(@NonNull MuseumEntity museum);

    @NonNull Optional<MuseumEntity> findById(@NonNull UUID id);

    @NonNull Optional<MuseumEntity> findByTitle(@NonNull String title);

    @NonNull List<MuseumEntity> findAll();

    void deleteById(@NonNull UUID id);

    void deleteAll();
}