package rcc.service;

import rcc.model.PaintingJson;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaintingClient {

    PaintingJson createPainting(PaintingJson painting);
    PaintingJson updatePainting(PaintingJson painting);
    Optional<PaintingJson> findPaintingById(UUID id);
    Optional<PaintingJson> findPaintingByTitle(String title);
    List<PaintingJson> findAllPaintings();
    List<PaintingJson> findAllPaintingsByArtistId(UUID artistId);
    List<PaintingJson> findAllPaintingsByMuseumId(UUID museumId);
    void deletePainting(UUID id);
    void deleteAllPaintings();
}