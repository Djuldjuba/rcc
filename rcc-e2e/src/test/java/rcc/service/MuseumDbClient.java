package rcc.service;

import rcc.config.Config;
import rcc.data.entity.CountryEntity;
import rcc.data.entity.MuseumEntity;
import rcc.model.CountryJson;
import rcc.model.MuseumJson;
import rcc.repository.CountryRepository;
import rcc.repository.MuseumRepository;
import rcc.repository.tpl.XaTransactionTemplate;

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
    public MuseumJson createMuseum(MuseumJson museum) {
        return xaTransactionTemplate.execute(() -> {
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
    }

    @Override
    public MuseumJson updateMuseum(MuseumJson museum) {
        return xaTransactionTemplate.execute(() -> {
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
    }

    @Override
    public Optional<MuseumJson> findMuseumById(UUID id) {
        return museumRepository.findById(id)
                .map(MuseumJson::fromEntity);
    }

    @Override
    public Optional<MuseumJson> findMuseumByTitle(String title) {
        return museumRepository.findByTitle(title)
                .map(MuseumJson::fromEntity);
    }

    @Override
    public List<MuseumJson> findAllMuseums() {
        return museumRepository.findAll().stream()
                .map(MuseumJson::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteMuseum(UUID id) {
        xaTransactionTemplate.execute(() -> {
            museumRepository.deleteById(id);
            return null;
        });
    }

    @Override
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
}