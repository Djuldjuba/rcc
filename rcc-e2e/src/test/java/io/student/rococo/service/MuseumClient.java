package io.student.rococo.service;

import io.student.rococo.model.MuseumJson;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MuseumClient {

    @NonNull MuseumJson createMuseum(@NonNull MuseumJson museum);

    @NonNull MuseumJson updateMuseum(@NonNull MuseumJson museum);

    @NonNull Optional<MuseumJson> findMuseumById(@NonNull UUID id);

    @NonNull Optional<MuseumJson> findMuseumByTitle(@NonNull String title);

    @NonNull List<MuseumJson> findAllMuseums();

    void deleteMuseum(@NonNull UUID id);

    void deleteAllMuseums();
}