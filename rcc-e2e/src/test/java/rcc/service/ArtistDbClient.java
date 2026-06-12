package rcc.service;

import rcc.config.Config;
import rcc.data.entity.ArtistEntity;
import rcc.model.ArtistJson;
import rcc.repository.ArtistRepository;
import rcc.repository.tpl.XaTransactionTemplate;

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
    public ArtistJson createArtist(ArtistJson artist) {
        return xaTransactionTemplate.execute(() -> {
            UUID artistId = artist.id() != null ? artist.id() : UUID.randomUUID();

            ArtistEntity artistEntity = artist.toEntity();
            artistEntity.setId(artistId);

            ArtistEntity created = artistRepository.create(artistEntity);

            return ArtistJson.fromEntity(created);
        });
    }

    @Override
    public ArtistJson updateArtist(ArtistJson artist) {
        return xaTransactionTemplate.execute(() -> {
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
    }

    @Override
    public Optional<ArtistJson> findArtistById(UUID id) {
        return artistRepository.findById(id)
                .map(ArtistJson::fromEntity);
    }

    @Override
    public Optional<ArtistJson> findArtistByName(String name) {
        return artistRepository.findByName(name)
                .map(ArtistJson::fromEntity);
    }

    @Override
    public List<ArtistJson> findAllArtists() {
        return artistRepository.findAll().stream()
                .map(ArtistJson::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteArtist(UUID id) {
        xaTransactionTemplate.execute(() -> {
            artistRepository.deleteById(id);
            return null;
        });
    }

    @Override
    public void deleteAllArtists() {
        xaTransactionTemplate.execute(() -> {
            artistRepository.deleteAll();
            return null;
        });
    }
}
