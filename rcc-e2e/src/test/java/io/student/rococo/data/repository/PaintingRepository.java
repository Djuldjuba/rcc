package io.student.rococo.data.repository;

import io.student.rococo.data.entity.PaintingEntity;
import io.student.rococo.data.repository.impl.painting.PaintingRepositoryHibernate;
import io.student.rococo.data.repository.impl.painting.PaintingRepositoryJdbc;
import io.student.rococo.data.repository.impl.painting.PaintingRepositorySpringJdbc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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

    @NonNull PaintingEntity create(@NonNull PaintingEntity painting);

    @NonNull PaintingEntity update(@NonNull PaintingEntity painting);

    @NonNull Optional<PaintingEntity> findById(@NonNull UUID id);

    @NonNull Optional<PaintingEntity> findByTitle(@NonNull String title);

    @NonNull List<PaintingEntity> findAll();

    @NonNull List<PaintingEntity> findAllByArtistId(@NonNull UUID artistId);

    @NonNull List<PaintingEntity> findAllByMuseumId(@NonNull UUID museumId);

    void deleteById(@NonNull UUID id);

    void deleteAll();
}