package io.student.rococo.data.repository.impl.painting;

import io.student.rococo.config.Config;
import io.student.rococo.data.entity.PaintingEntity;
import io.student.rococo.data.repository.PaintingRepository;
import io.student.rococo.data.mapper.tpl.DataSources;
import org.jspecify.annotations.NonNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PaintingRepositoryJdbc implements PaintingRepository {

    private static final Config CFG = Config.getInstance();
    private final DataSource dataSource = DataSources.dataSource(CFG.paintingJdbcUrl());

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public @NonNull PaintingEntity create(@NonNull PaintingEntity painting) {
        UUID id = painting.getId() != null ? painting.getId() : UUID.randomUUID();
        UUID artistId = painting.getArtistId();
        UUID museumId = painting.getMuseumId();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO painting (id, title, description, content, artist_id, museum_id) " +
                             "VALUES (UUID_TO_BIN(?), ?, ?, ?, UUID_TO_BIN(?), UUID_TO_BIN(?))")) {

            ps.setString(1, id.toString());
            ps.setString(2, painting.getTitle());
            ps.setString(3, painting.getDescription());
            ps.setBytes(4, painting.getContent());
            ps.setString(5, artistId != null ? artistId.toString() : null);
            ps.setString(6, museumId != null ? museumId.toString() : null);
            ps.executeUpdate();

            painting.setId(id);
            return painting;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NonNull PaintingEntity update(@NonNull PaintingEntity painting) {
        UUID artistId = painting.getArtistId();
        UUID museumId = painting.getMuseumId();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE painting SET title = ?, description = ?, content = ?, " +
                             "artist_id = UUID_TO_BIN(?), museum_id = UUID_TO_BIN(?) " +
                             "WHERE id = UUID_TO_BIN(?)")) {

            ps.setString(1, painting.getTitle());
            ps.setString(2, painting.getDescription());
            ps.setBytes(3, painting.getContent());
            ps.setString(4, artistId != null ? artistId.toString() : null);
            ps.setString(5, museumId != null ? museumId.toString() : null);
            ps.setString(6, painting.getId().toString());

            ps.executeUpdate();
            return painting;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapPainting(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapPainting(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

        List<PaintingEntity> paintings = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                paintings.add(mapPainting(rs));
            }
            return paintings;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

        List<PaintingEntity> paintings = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, artistId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    paintings.add(mapPainting(rs));
                }
                return paintings;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

        List<PaintingEntity> paintings = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, museumId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    paintings.add(mapPainting(rs));
                }
                return paintings;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteById(@NonNull UUID id) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM painting WHERE id = UUID_TO_BIN(?)")) {

            ps.setString(1, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAll() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM painting");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private PaintingEntity mapPainting(ResultSet rs) throws SQLException {
        PaintingEntity painting = new PaintingEntity();
        painting.setId(UUID.fromString(rs.getString("painting_id")));
        painting.setTitle(rs.getString("title"));
        painting.setDescription(rs.getString("description"));
        painting.setContent(rs.getBytes("content"));

        String artistId = rs.getString("artist_id");
        if (artistId != null && !rs.wasNull()) {
            painting.setArtistId(UUID.fromString(artistId));
        }

        String museumId = rs.getString("museum_id");
        if (museumId != null && !rs.wasNull()) {
            painting.setMuseumId(UUID.fromString(museumId));
        }

        return painting;
    }
}