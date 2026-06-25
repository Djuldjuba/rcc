package io.student.rococo.service.impl;

import io.qameta.allure.Step;
import io.student.rococo.config.Config;
import io.student.rococo.data.entity.ArtistEntity;
import io.student.rococo.model.ArtistJson;
import io.student.rococo.data.repository.ArtistRepository;
import io.student.rococo.data.mapper.tpl.XaTransactionTemplate;
import io.student.rococo.service.ArtistClient;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ArtistDbClient implements ArtistClient {

    private static final Config CFG = Config.getInstance();

    private final ArtistRepository artistRepository = ArtistRepository.getInstance();

    private final XaTransactionTemplate xaTransactionTemplate = new XaTransactionTemplate(
            CFG.rococoApiUrl()
    );

    @Override
    @Step("Создать художника в БД")
    public @NonNull ArtistJson createArtist(@NonNull ArtistJson artist) {
        ArtistJson result = xaTransactionTemplate.execute(() -> {
            UUID artistId = artist.id() != null ? artist.id() : UUID.randomUUID();

            ArtistEntity artistEntity = artist.toEntity();
            artistEntity.setId(artistId);

            ArtistEntity created = artistRepository.create(artistEntity);

            return ArtistJson.fromEntity(created);
        });

        if (result == null) {
            throw new IllegalStateException("Failed to create artist");
        }
        return result;
    }

    @Override
    @Step("Обновить художника в БД")
    public @NonNull ArtistJson updateArtist(@NonNull ArtistJson artist) {
        ArtistJson result = xaTransactionTemplate.execute(() -> {
            ArtistEntity existing = artistRepository.findById(artist.id())
                    .orElseThrow(() -> new RuntimeException("Artist not found: " + artist.id()));

            existing.setName(artist.name());
            existing.setBiography(artist.biography());
            if (artist.photo() != null) {
                existing.setPhoto(artist.photo().getBytes());
            }

            ArtistEntity updated = artistRepository.update(existing);
            return ArtistJson.fromEntity(updated);
        });

        if (result == null) {
            throw new IllegalStateException("Failed to update artist");
        }
        return result;
    }

    @Override
    @Step("Найти художника в БД по id")
    public @NonNull Optional<ArtistJson> findArtistById(@NonNull UUID id) {
        return artistRepository.findById(id)
                .map(ArtistJson::fromEntity);
    }

    @Override
    @Step("Найти художника в БД по названию")
    public @NonNull Optional<ArtistJson> findArtistByName(@NonNull String name) {
        return artistRepository.findByName(name)
                .map(ArtistJson::fromEntity);
    }

    @Override
    @Step("Найти всех художников в БД")
    public @NonNull List<ArtistJson> findAllArtists() {
        List<ArtistJson> result = artistRepository.findAll().stream()
                .map(ArtistJson::fromEntity)
                .collect(Collectors.toList());
        return result != null ? result : Collections.emptyList();
    }

    @Override
    @Step("Удалить художника по id")
    public void deleteArtist(@NonNull UUID id) {
        xaTransactionTemplate.execute(() -> {
            artistRepository.deleteById(id);
            return null;
        });
    }

    @Override
    @Step("Удалить всех художников")
    public void deleteAllArtists() {
        xaTransactionTemplate.execute(() -> {
            artistRepository.deleteAll();
            return null;
        });
    }
}