package io.student.rococo.service.impl;

import io.qameta.allure.Step;
import io.student.rococo.config.Config;
import io.student.rococo.data.entity.ArtistEntity;
import io.student.rococo.data.entity.MuseumEntity;
import io.student.rococo.data.entity.PaintingEntity;
import io.student.rococo.model.ArtistJson;
import io.student.rococo.model.MuseumJson;
import io.student.rococo.model.PaintingJson;
import io.student.rococo.data.repository.ArtistRepository;
import io.student.rococo.data.repository.MuseumRepository;
import io.student.rococo.data.repository.PaintingRepository;
import io.student.rococo.data.mapper.tpl.XaTransactionTemplate;
import io.student.rococo.service.PaintingClient;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class PaintingDbClient implements PaintingClient {

    private static final Config CFG = Config.getInstance();

    private final PaintingRepository paintingRepository = PaintingRepository.getInstance();
    private final ArtistRepository artistRepository = ArtistRepository.getInstance();
    private final MuseumRepository museumRepository = MuseumRepository.getInstance();

    private final XaTransactionTemplate xaTransactionTemplate = new XaTransactionTemplate(
            CFG.rococoApiUrl()
    );

    @Override
    @Step("Создать картину в БД")
    public @NonNull PaintingJson createPainting(@NonNull PaintingJson painting) {
        PaintingJson result = xaTransactionTemplate.execute(() -> {
            UUID paintingId = painting.id() != null ? painting.id() : UUID.randomUUID();

            PaintingEntity paintingEntity = painting.toEntity();
            paintingEntity.setId(paintingId);

            if (painting.artist() != null && paintingEntity.getArtist() == null) {
                ArtistEntity artist = getOrCreateArtist(painting.artist());
                paintingEntity.setArtist(artist);
            }

            if (painting.museum() != null && paintingEntity.getMuseum() == null) {
                MuseumEntity museum = getOrCreateMuseum(painting.museum());
                paintingEntity.setMuseum(museum);
            }

            PaintingEntity created = paintingRepository.create(paintingEntity);
            return PaintingJson.fromEntity(created);
        });

        if (result == null) {
            throw new IllegalStateException("Failed to create painting");
        }
        return result;
    }

    @Override
    @Step("Обновить картину в БД")
    public @NonNull PaintingJson updatePainting(@NonNull PaintingJson painting) {
        PaintingJson result = xaTransactionTemplate.execute(() -> {
            PaintingEntity existing = paintingRepository.findById(painting.id())
                    .orElseThrow(() -> new RuntimeException("Painting not found: " + painting.id()));

            existing.setTitle(painting.title());
            existing.setDescription(painting.description());
            if (painting.content() != null) {
                existing.setContent(painting.content().getBytes());
            }

            if (painting.artist() != null && painting.artist().id() != null) {
                artistRepository.findById(painting.artist().id())
                        .ifPresent(existing::setArtist);
            } else if (painting.artist() != null && painting.artist().name() != null) {
                ArtistEntity artist = getOrCreateArtist(painting.artist());
                existing.setArtist(artist);
            }

            if (painting.museum() != null && painting.museum().id() != null) {
                museumRepository.findById(painting.museum().id())
                        .ifPresent(existing::setMuseum);
            } else if (painting.museum() != null && painting.museum().title() != null) {
                MuseumEntity museum = getOrCreateMuseum(painting.museum());
                existing.setMuseum(museum);
            }

            PaintingEntity updated = paintingRepository.update(existing);
            return PaintingJson.fromEntity(updated);
        });

        if (result == null) {
            throw new IllegalStateException("Failed to update painting");
        }
        return result;
    }

    @Override
    @Step("Найти картину в БД по id")
    public @NonNull Optional<PaintingJson> findPaintingById(@NonNull UUID id) {
        return paintingRepository.findById(id)
                .map(PaintingJson::fromEntity);
    }

    @Override
    @Step("Найти картину в БД по названию")
    public @NonNull Optional<PaintingJson> findPaintingByTitle(@NonNull String title) {
        return paintingRepository.findByTitle(title)
                .map(PaintingJson::fromEntity);
    }

    @Override
    @Step("Найти все картины в БД")
    public @NonNull List<PaintingJson> findAllPaintings() {
        return paintingRepository.findAll().stream()
                .map(PaintingJson::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Step("Найти все картины в БД по художнику")
    public @NonNull List<PaintingJson> findAllPaintingsByArtistId(@NonNull UUID artistId) {
        return paintingRepository.findAllByArtistId(artistId).stream()
                .map(PaintingJson::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Step("Найти все картины в БД по музею")
    public @NonNull List<PaintingJson> findAllPaintingsByMuseumId(@NonNull UUID museumId) {
        return paintingRepository.findAllByMuseumId(museumId).stream()
                .map(PaintingJson::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Step("Удалить картину из БД")
    public void deletePainting(@NonNull UUID id) {
        xaTransactionTemplate.execute(() -> {
            paintingRepository.deleteById(id);
            return null;
        });
    }

    @Override
    @Step("Удалить все картины из БД")
    public void deleteAllPaintings() {
        xaTransactionTemplate.execute(() -> {
            paintingRepository.deleteAll();
            return null;
        });
    }

    private ArtistEntity getOrCreateArtist(ArtistJson artistJson) {
        if (artistJson.id() != null) {
            return artistRepository.findById(artistJson.id())
                    .orElseThrow(() -> new RuntimeException("Artist not found: " + artistJson.id()));
        } else if (artistJson.name() != null && !artistJson.name().isEmpty()) {
            return artistRepository.findByName(artistJson.name())
                    .orElseGet(() -> {
                        ArtistEntity newArtist = artistJson.toEntity();
                        if (newArtist.getId() == null) {
                            newArtist.setId(UUID.randomUUID());
                        }
                        return artistRepository.create(newArtist);
                    });
        }
        throw new RuntimeException("Artist id or name is required");
    }

    private MuseumEntity getOrCreateMuseum(MuseumJson museumJson) {
        if (museumJson.id() != null) {
            return museumRepository.findById(museumJson.id())
                    .orElseThrow(() -> new RuntimeException("Museum not found: " + museumJson.id()));
        } else if (museumJson.title() != null && !museumJson.title().isEmpty()) {
            return museumRepository.findByTitle(museumJson.title())
                    .orElseGet(() -> {
                        MuseumEntity newMuseum = museumJson.toEntity();
                        if (newMuseum.getId() == null) {
                            newMuseum.setId(UUID.randomUUID());
                        }
                        return museumRepository.create(newMuseum);
                    });
        }
        throw new RuntimeException("Museum id or title is required");
    }
}