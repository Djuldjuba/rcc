package rcc.service;

import rcc.model.MuseumJson;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MuseumClient {

    MuseumJson createMuseum(MuseumJson museum);

    MuseumJson updateMuseum(MuseumJson museum);

    Optional<MuseumJson> findMuseumById(UUID id);

    Optional<MuseumJson> findMuseumByTitle(String title);

    List<MuseumJson> findAllMuseums();

    void deleteMuseum(UUID id);

    void deleteAllMuseums();
}

