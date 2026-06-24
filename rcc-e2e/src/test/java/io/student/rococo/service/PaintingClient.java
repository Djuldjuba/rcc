package io.student.rococo.service;

import io.student.rococo.model.PaintingJson;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaintingClient {

    @NonNull PaintingJson createPainting(@NonNull PaintingJson painting);

    @NonNull PaintingJson updatePainting(@NonNull PaintingJson painting);

    @NonNull Optional<PaintingJson> findPaintingById(@NonNull UUID id);

    @NonNull Optional<PaintingJson> findPaintingByTitle(@NonNull String title);

    @NonNull List<PaintingJson> findAllPaintings();

    @NonNull List<PaintingJson> findAllPaintingsByArtistId(@NonNull UUID artistId);

    @NonNull List<PaintingJson> findAllPaintingsByMuseumId(@NonNull UUID museumId);

    void deletePainting(@NonNull UUID id);

    void deleteAllPaintings();
}