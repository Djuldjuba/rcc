package io.student.rococo.service.impl;

import io.qameta.allure.Step;
import io.student.rococo.config.Config;
import io.student.rococo.data.entity.CountryEntity;
import io.student.rococo.data.entity.MuseumEntity;
import io.student.rococo.model.CountryJson;
import io.student.rococo.model.MuseumJson;
import io.student.rococo.data.repository.CountryRepository;
import io.student.rococo.data.repository.MuseumRepository;
import io.student.rococo.data.mapper.tpl.XaTransactionTemplate;
import io.student.rococo.service.MuseumClient;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class MuseumDbClient implements MuseumClient {

    private static final Config CFG = Config.getInstance();

    private final MuseumRepository museumRepository = MuseumRepository.getInstance();
    private final CountryRepository countryRepository = CountryRepository.getInstance();

    private final XaTransactionTemplate xaTransactionTemplate = new XaTransactionTemplate(
            CFG.rococoApiUrl()
    );

    @Override
    @Step("Создать музей в БД")
    public @NonNull MuseumJson createMuseum(@NonNull MuseumJson museum) {
        MuseumJson result = xaTransactionTemplate.execute(() -> {
            if (museum.title() != null && !museum.title().isEmpty()) {
                Optional<MuseumEntity> existing = museumRepository.findByTitle(museum.title());
                if (existing.isPresent()) {
                    return MuseumJson.fromEntity(existing.get());
                }
            }

            UUID museumId = museum.id() != null ? museum.id() : UUID.randomUUID();

            CountryEntity country = getCountryEntity(museum.country());

            MuseumEntity museumEntity = new MuseumEntity();
            museumEntity.setId(museumId);
            museumEntity.setTitle(museum.title());
            museumEntity.setDescription(museum.description());
            museumEntity.setCity(museum.city());
            if (museum.photo() != null) {
                museumEntity.setPhoto(museum.photo().getBytes());
            }
            museumEntity.setCountry(country);

            MuseumEntity created = museumRepository.create(museumEntity);

            return MuseumJson.fromEntity(created);
        });

        if (result == null) {
            throw new IllegalStateException("Failed to create museum");
        }
        return result;
    }

    @Override
    @Step("Обновить музей в БД")
    public @NonNull MuseumJson updateMuseum(@NonNull MuseumJson museum) {
        MuseumJson result = xaTransactionTemplate.execute(() -> {
            MuseumEntity existing = museumRepository.findById(museum.id())
                    .orElseThrow(() -> new RuntimeException("Museum not found: " + museum.id()));

            CountryEntity country = getCountryEntity(museum.country());

            existing.setTitle(museum.title());
            existing.setDescription(museum.description());
            existing.setCity(museum.city());
            existing.setCountry(country);

            if (museum.photo() != null) {
                existing.setPhoto(museum.photo().getBytes());
            }

            MuseumEntity updated = museumRepository.update(existing);
            return MuseumJson.fromEntity(updated);
        });

        if (result == null) {
            throw new IllegalStateException("Failed to update museum");
        }
        return result;
    }

    @Override
    @Step("Найти музей в БД по id")
    public @NonNull Optional<MuseumJson> findMuseumById(@NonNull UUID id) {
        return museumRepository.findById(id)
                .map(MuseumJson::fromEntity);
    }

    @Override
    @Step("Найти музей в БД по названию")
    public @NonNull Optional<MuseumJson> findMuseumByTitle(@NonNull String title) {
        return museumRepository.findByTitle(title)
                .map(MuseumJson::fromEntity);
    }

    @Override
    @Step("Найти все музеи в БД")
    public @NonNull List<MuseumJson> findAllMuseums() {
        List<MuseumJson> result = museumRepository.findAll().stream()
                .map(MuseumJson::fromEntity)
                .collect(Collectors.toList());
        return result != null ? result : Collections.emptyList();
    }

    @Override
    @Step("Удалить музей из БД")
    public void deleteMuseum(@NonNull UUID id) {
        xaTransactionTemplate.execute(() -> {
            museumRepository.deleteById(id);
            return null;
        });
    }

    @Override
    @Step("Удалить все музеи из БД")
    public void deleteAllMuseums() {
        xaTransactionTemplate.execute(() -> {
            museumRepository.deleteAll();
            return null;
        });
    }

    private CountryEntity getCountryEntity(CountryJson countryJson) {
        if (countryJson == null) {
            throw new RuntimeException("Country is required");
        }

        if (countryJson.id() != null) {
            return countryRepository.findById(countryJson.id())
                    .orElseThrow(() -> new RuntimeException("Country not found by id: " + countryJson.id()));
        } else if (countryJson.name() != null && !countryJson.name().isEmpty()) {
            return countryRepository.findByName(countryJson.name())
                    .orElseThrow(() -> new RuntimeException("Country not found by name: " + countryJson.name()));
        }

        throw new RuntimeException("Country id or name is required");
    }

    @Step("Найти страну по имени")
    public Optional<CountryJson> findCountryByName(@NonNull String name) {
        return countryRepository.findByName(name)
                .map(CountryJson::fromEntity);
    }
}