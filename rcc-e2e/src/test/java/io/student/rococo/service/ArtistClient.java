package io.student.rococo.service;

import io.student.rococo.model.ArtistJson;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArtistClient {

    @NonNull ArtistJson createArtist(@NonNull ArtistJson artist);

    @NonNull ArtistJson updateArtist(@NonNull ArtistJson artist);

    @NonNull Optional<ArtistJson> findArtistById(@NonNull UUID id);

    @NonNull Optional<ArtistJson> findArtistByName(@NonNull String name);

    @NonNull List<ArtistJson> findAllArtists();

    void deleteArtist(@NonNull UUID id);

    void deleteAllArtists();
}