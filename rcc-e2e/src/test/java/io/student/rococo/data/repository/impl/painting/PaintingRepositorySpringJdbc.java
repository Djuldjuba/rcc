package io.student.rococo.data.repository.impl.painting;

import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.JdbcTemplate;
import io.student.rococo.config.Config;
import io.student.rococo.data.entity.PaintingEntity;
import io.student.rococo.data.mapper.extractor.PaintingResultSetExtractor;
import io.student.rococo.data.repository.PaintingRepository;
import io.student.rococo.data.mapper.tpl.DataSources;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PaintingRepositorySpringJdbc implements PaintingRepository {

    private static final Config CFG = Config.getInstance();
    private final JdbcTemplate jdbcTemplate;

    public PaintingRepositorySpringJdbc() {
        this.jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.paintingJdbcUrl()));
    }

    @Override
    public @NonNull PaintingEntity create(@NonNull PaintingEntity painting) {
        UUID id = painting.getId() != null ? painting.getId() : UUID.randomUUID();
        UUID artistId = painting.getArtistId();
        UUID museumId = painting.getMuseumId();

        jdbcTemplate.update(
                "INSERT INTO painting (id, title, description, content, artist_id, museum_id) " +
                        "VALUES (UUID_TO_BIN(?), ?, ?, ?, UUID_TO_BIN(?), UUID_TO_BIN(?))",
                id.toString(),
                painting.getTitle(),
                painting.getDescription(),
                painting.getContent(),
                artistId != null ? artistId.toString() : null,
                museumId != null ? museumId.toString() : null
        );

        painting.setId(id);
        return painting;
    }

    @Override
    public @NonNull PaintingEntity update(@NonNull PaintingEntity painting) {
        if (painting.getId() == null) {
            throw new IllegalArgumentException("Cannot update painting without ID");
        }

        UUID artistId = painting.getArtistId();
        UUID museumId = painting.getMuseumId();

        jdbcTemplate.update(
                "UPDATE painting SET title = ?, description = ?, content = ?, " +
                        "artist_id = UUID_TO_BIN(?), museum_id = UUID_TO_BIN(?) " +
                        "WHERE id = UUID_TO_BIN(?)",
                painting.getTitle(),
                painting.getDescription(),
                painting.getContent(),
                artistId != null ? artistId.toString() : null,
                museumId != null ? museumId.toString() : null,
                painting.getId().toString()
        );
        return painting;
    }

    @Override
    public @NonNull Optional<PaintingEntity> findById(@NonNull UUID id) {
        String sql = """
                SELECT
                    BIN_TO_UUID(p.id) as painting_id,
                    p.title,
                    p.description,
                    p.content,
                    BIN_TO_UUID(p.artist_id) as artist_id,
                    BIN_TO_UUID(p.museum_id) as museum_id
                FROM painting p
                WHERE p.id = UUID_TO_BIN(?)
               """;

        List<PaintingEntity> result = jdbcTemplate.query(sql, PaintingResultSetExtractor.instance, id.toString());
        return result != null && !result.isEmpty() ? Optional.of(result.get(0)) : Optional.empty();
    }

    @Override
    public @NonNull Optional<PaintingEntity> findByTitle(@NonNull String title) {
        String sql = """
                SELECT
                    BIN_TO_UUID(p.id) as painting_id,
                    p.title,
                    p.description,
                    p.content,
                    BIN_TO_UUID(p.artist_id) as artist_id,
                    BIN_TO_UUID(p.museum_id) as museum_id
                FROM painting p
                WHERE p.title = ?
               """;

        List<PaintingEntity> result = jdbcTemplate.query(sql, PaintingResultSetExtractor.instance, title);
        return result != null && !result.isEmpty() ? Optional.of(result.get(0)) : Optional.empty();
    }

    @Override
    public @NonNull List<PaintingEntity> findAll() {
        String sql = """
                SELECT
                    BIN_TO_UUID(p.id) as painting_id,
                    p.title,
                    p.description,
                    p.content,
                    BIN_TO_UUID(p.artist_id) as artist_id,
                    BIN_TO_UUID(p.museum_id) as museum_id
                FROM painting p
               """;

        List<PaintingEntity> result = jdbcTemplate.query(sql, PaintingResultSetExtractor.instance);
        return result != null ? result : Collections.emptyList();
    }

    @Override
    public @NonNull List<PaintingEntity> findAllByArtistId(@NonNull UUID artistId) {
        String sql = """
                SELECT
                    BIN_TO_UUID(p.id) as painting_id,
                    p.title,
                    p.description,
                    p.content,
                    BIN_TO_UUID(p.artist_id) as artist_id,
                    BIN_TO_UUID(p.museum_id) as museum_id
                FROM painting p
                WHERE p.artist_id = UUID_TO_BIN(?)
               """;

        List<PaintingEntity> result = jdbcTemplate.query(sql, PaintingResultSetExtractor.instance, artistId.toString());
        return result != null ? result : Collections.emptyList();
    }

    @Override
    public @NonNull List<PaintingEntity> findAllByMuseumId(@NonNull UUID museumId) {
        String sql = """
                SELECT
                    BIN_TO_UUID(p.id) as painting_id,
                    p.title,
                    p.description,
                    p.content,
                    BIN_TO_UUID(p.artist_id) as artist_id,
                    BIN_TO_UUID(p.museum_id) as museum_id
                FROM painting p
                WHERE p.museum_id = UUID_TO_BIN(?)
               """;

        List<PaintingEntity> result = jdbcTemplate.query(sql, PaintingResultSetExtractor.instance, museumId.toString());
        return result != null ? result : Collections.emptyList();
    }

    @Override
    public void deleteById(@NonNull UUID id) {
        jdbcTemplate.update("DELETE FROM painting WHERE id = UUID_TO_BIN(?)", id.toString());
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.execute("DELETE FROM painting");
    }
}