package rcc.service;

import rcc.model.ArtistJson;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArtistClient {

    ArtistJson createArtist(ArtistJson artist);

    ArtistJson updateArtist(ArtistJson artist);

    Optional<ArtistJson> findArtistById(UUID id);

    Optional<ArtistJson> findArtistByName(String name);

    List<ArtistJson> findAllArtists();

    void deleteArtist(UUID id);

    void deleteAllArtists();
}
