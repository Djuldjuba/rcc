package io.student.rococo.data.repository;

import io.student.rococo.data.entity.ArtistEntity;
import io.student.rococo.data.repository.impl.artist.ArtistRepositoryHibernate;
import io.student.rococo.data.repository.impl.artist.ArtistRepositoryJdbc;
import io.student.rococo.data.repository.impl.artist.ArtistRepositorySpringJdbc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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

    @NonNull ArtistEntity create(@NonNull ArtistEntity artist);

    @NonNull ArtistEntity update(@NonNull ArtistEntity artist);

    @NonNull Optional<ArtistEntity> findById(@NonNull UUID id);

    @NonNull Optional<ArtistEntity> findByName(@NonNull String name);

    @NonNull List<ArtistEntity> findAll();

    void deleteById(@NonNull UUID id);

    void deleteAll();
}