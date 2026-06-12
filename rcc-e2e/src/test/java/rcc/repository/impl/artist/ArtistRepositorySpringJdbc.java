package rcc.repository.impl.artist;

import org.springframework.jdbc.core.JdbcTemplate;
import rcc.config.Config;
import rcc.data.entity.ArtistEntity;
import rcc.model.extractor.ArtistResultSetExtractor;
import rcc.repository.ArtistRepository;
import rcc.repository.tpl.DataSources;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ArtistRepositorySpringJdbc implements ArtistRepository {

    private static final Config CFG = Config.getInstance();
    private final JdbcTemplate jdbcTemplate;

    public ArtistRepositorySpringJdbc() {
        this.jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.rococoApiUrl()));
    }

    @Override
    public ArtistEntity create(ArtistEntity artist) {
        UUID id = artist.getId() != null ? artist.getId() : UUID.randomUUID();

        jdbcTemplate.update(
                "INSERT INTO artist (id, name, biography, photo) " +
                        "VALUES (UUID_TO_BIN(?), ?, ?, ?)",
                id.toString(),
                artist.getName(),
                artist.getBiography(),
                artist.getPhoto()
        );

        artist.setId(id);
        return artist;
    }

    @Override
    public ArtistEntity update(ArtistEntity artist) {
        jdbcTemplate.update(
                "UPDATE artist SET name = ?, biography = ?, photo = ? " +
                        "WHERE id = UUID_TO_BIN(?)",
                artist.getName(),
                artist.getBiography(),
                artist.getPhoto(),
                artist.getId().toString()
        );
        return artist;
    }

    @Override
    public Optional<ArtistEntity> findById(UUID id) {
        String sql = """
                 SELECT\s
                     BIN_TO_UUID(id) as artist_id,
                     name,
                     biography,
                     photo
                 FROM artist
                 WHERE id = UUID_TO_BIN(?)
                \s""";

        List<ArtistEntity> result = jdbcTemplate.query(sql, ArtistResultSetExtractor.instance, id.toString());
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<ArtistEntity> findByName(String name) {
        String sql = """
                 SELECT\s
                     BIN_TO_UUID(id) as artist_id,
                     name,
                     biography,
                     photo
                 FROM artist
                 WHERE name = ?
                \s""";

        List<ArtistEntity> result = jdbcTemplate.query(sql, ArtistResultSetExtractor.instance, name);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<ArtistEntity> findAll() {
        String sql = """
                 SELECT\s
                     BIN_TO_UUID(id) as artist_id,
                     name,
                     biography,
                     photo
                 FROM artist
                 ORDER BY name
                \s""";

        return jdbcTemplate.query(sql, ArtistResultSetExtractor.instance);
    }

    @Override
    public void deleteById(UUID id) {
        jdbcTemplate.update("DELETE FROM artist WHERE id = UUID_TO_BIN(?)", id.toString());
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.execute("DELETE FROM artist");
    }
}
