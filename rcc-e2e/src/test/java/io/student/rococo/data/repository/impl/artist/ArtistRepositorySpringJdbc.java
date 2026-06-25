package io.student.rococo.data.repository.impl.artist;

import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.JdbcTemplate;
import io.student.rococo.config.Config;
import io.student.rococo.data.entity.ArtistEntity;
import io.student.rococo.data.mapper.extractor.ArtistResultSetExtractor;
import io.student.rococo.data.repository.ArtistRepository;
import io.student.rococo.data.mapper.tpl.DataSources;

import java.util.Collections;
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
    public @NonNull ArtistEntity create(@NonNull ArtistEntity artist) {
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
    public @NonNull ArtistEntity update(@NonNull ArtistEntity artist) {
        if (artist.getId() == null) {
            throw new IllegalArgumentException("Cannot update artist without ID");
        }

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
    public @NonNull Optional<ArtistEntity> findById(@NonNull UUID id) {
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
        return result != null && !result.isEmpty() ? Optional.of(result.get(0)) : Optional.empty();
    }

    @Override
    public @NonNull Optional<ArtistEntity> findByName(@NonNull String name) {
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
        return result != null && !result.isEmpty() ? Optional.of(result.get(0)) : Optional.empty();
    }

    @Override
    public @NonNull List<ArtistEntity> findAll() {
        String sql = """
                 SELECT\s
                     BIN_TO_UUID(id) as artist_id,
                     name,
                     biography,
                     photo
                 FROM artist
                 ORDER BY name
                \s""";

        List<ArtistEntity> result = jdbcTemplate.query(sql, ArtistResultSetExtractor.instance);
        return result != null ? result : Collections.emptyList();
    }

    @Override
    public void deleteById(@NonNull UUID id) {
        jdbcTemplate.update("DELETE FROM artist WHERE id = UUID_TO_BIN(?)", id.toString());
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.execute("DELETE FROM artist");
    }
}