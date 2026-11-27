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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MuseumDbClient implements MuseumClient {

    private static final Config CFG = Config.getInstance();

    private final MuseumRepository museumRepository = MuseumRepository.getInstance();
    private final CountryRepository countryRepository = CountryRepository.getInstance();

    private final XaTransactionTemplate xaTransactionTemplate = new XaTransactionTemplate(
            CFG.museumJdbcUrl()
    );

    @Override
    @Step("Создать музей в БД")
    public @NonNull MuseumJson createMuseum(@NonNull MuseumJson museum) {
        MuseumJson result = xaTransactionTemplate.execute(() -> {
            if (museum.title() != null && !museum.title().isEmpty()) {
                Optional<MuseumEntity> existing = museumRepository.findByTitle(museum.title());
                if (existing.isPresent()) {
                    return enrich(existing.get());
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
            museumEntity.setCountryId(country.getId());

            MuseumEntity created = museumRepository.create(museumEntity);

            return MuseumJson.fromEntity(created, country);
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
            existing.setCountryId(country.getId());

            if (museum.photo() != null) {
                existing.setPhoto(museum.photo().getBytes());
            }

            MuseumEntity updated = museumRepository.update(existing);
            return MuseumJson.fromEntity(updated, country);
        });

        if (result == null) {
            throw new IllegalStateException("Failed to update museum");
        }
        return result;
    }

    @Override
    @Step("Найти музей в БД по id")
    public @NonNull Optional<MuseumJson> findMuseumById(@NonNull UUID id) {
        return museumRepository.findById(id).map(this::enrich);
    }

    @Override
    @Step("Найти музей в БД по названию")
    public @NonNull Optional<MuseumJson> findMuseumByTitle(@NonNull String title) {
        return museumRepository.findByTitle(title).map(this::enrich);
    }

    @Override
    @Step("Найти все музеи в БД")
    public @NonNull List<MuseumJson> findAllMuseums() {
        List<MuseumEntity> museums = museumRepository.findAll();
        if (museums == null || museums.isEmpty()) {
            return Collections.emptyList();
        }

        Map<UUID, CountryEntity> countries = countryRepository.findAll().stream()
                .collect(Collectors.toMap(CountryEntity::getId, Function.identity()));

        return museums.stream()
                .map(m -> {
                    CountryEntity country = countries.get(m.getCountryId());
                    return MuseumJson.fromEntity(m, country);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Step("Удалить музей из БД")
    public void deleteMuseum(@NonNull UUID id) {
        xaTransactionTemplate.execute(() -> {
            museumRepository.deleteById(id);
            return null;
        });
    }

    @Step("Удалить музей из БД по ID с проверкой существования")
    public boolean deleteMuseumById(@NonNull UUID id) {
        try {
            Boolean result = xaTransactionTemplate.execute(() -> {
                Optional<MuseumEntity> existing = museumRepository.findById(id);
                if (existing.isEmpty()) {
                    return false;
                }
                museumRepository.deleteById(id);
                return true;
            });
            return result != null && result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete museum by id: " + id, e);
        }
    }

    @Step("Удалить музей из БД по названию")
    public boolean deleteMuseumByTitle(@NonNull String title) {
        try {
            Boolean result = xaTransactionTemplate.execute(() -> {
                Optional<MuseumEntity> existing = museumRepository.findByTitle(title);
                if (existing.isEmpty()) {
                    return false;
                }
                museumRepository.deleteById(existing.get().getId());
                return true;
            });
            return result != null && result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete museum by title: " + title, e);
        }
    }

    @Override
    @Step("Удалить все музеи из БД")
    public void deleteAllMuseums() {
        xaTransactionTemplate.execute(() -> {
            museumRepository.deleteAll();
            return null;
        });
    }

    private MuseumJson enrich(MuseumEntity entity) {
        CountryEntity country = entity.getCountryId() == null
                ? null
                : countryRepository.findById(entity.getCountryId()).orElse(null);
        return MuseumJson.fromEntity(entity, country);
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

    @Step("Получить все страны из БД")
    public @NonNull List<CountryJson> getAllCountries() {
        try {
            List<CountryEntity> countries = countryRepository.findAll();
            if (countries == null || countries.isEmpty()) {
                return Collections.emptyList();
            }
            return countries.stream()
                    .map(CountryJson::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch all countries from database", e);
        }
    }
}