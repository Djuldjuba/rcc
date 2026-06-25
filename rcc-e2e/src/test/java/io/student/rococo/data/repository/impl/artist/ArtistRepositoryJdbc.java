package io.student.rococo.data.repository.impl.artist;

import io.student.rococo.config.Config;
import io.student.rococo.data.entity.ArtistEntity;
import io.student.rococo.data.repository.ArtistRepository;
import io.student.rococo.data.mapper.tpl.DataSources;
import org.jspecify.annotations.NonNull;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ArtistRepositoryJdbc implements ArtistRepository {

    private static final Config CFG = Config.getInstance();
    private final DataSource dataSource = DataSources.dataSource(CFG.rococoApiUrl());

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public @NonNull ArtistEntity create(@NonNull ArtistEntity artist) {
        UUID id = artist.getId() != null ? artist.getId() : UUID.randomUUID();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO artist (id, name, biography, photo) " +
                             "VALUES (UUID_TO_BIN(?), ?, ?, ?)")) {

            ps.setString(1, id.toString());
            ps.setString(2, artist.getName());
            ps.setString(3, artist.getBiography());
            ps.setBytes(4, artist.getPhoto());
            ps.executeUpdate();

            artist.setId(id);
            return artist;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NonNull ArtistEntity update(@NonNull ArtistEntity artist) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE artist SET name = ?, biography = ?, photo = ? " +
                             "WHERE id = UUID_TO_BIN(?)")) {

            ps.setString(1, artist.getName());
            ps.setString(2, artist.getBiography());
            ps.setBytes(3, artist.getPhoto());
            ps.setString(4, artist.getId().toString());

            ps.executeUpdate();
            return artist;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NonNull Optional<ArtistEntity> findById(@NonNull UUID id) {
        String sql = "SELECT BIN_TO_UUID(id) as artist_id, name, biography, photo FROM artist WHERE id = UUID_TO_BIN(?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapArtist(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NonNull Optional<ArtistEntity> findByName(@NonNull String name) {
        String sql = "SELECT BIN_TO_UUID(id) as artist_id, name, biography, photo FROM artist WHERE name = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapArtist(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NonNull List<ArtistEntity> findAll() {
        String sql = "SELECT BIN_TO_UUID(id) as artist_id, name, biography, photo FROM artist ORDER BY name";

        List<ArtistEntity> artists = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                artists.add(mapArtist(rs));
            }
            return artists;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteById(@NonNull UUID id) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM artist WHERE id = UUID_TO_BIN(?)")) {

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
            stmt.execute("DELETE FROM artist");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ArtistEntity mapArtist(ResultSet rs) throws SQLException {
        ArtistEntity artist = new ArtistEntity();
        artist.setId(UUID.fromString(rs.getString("artist_id")));
        artist.setName(rs.getString("name"));
        artist.setBiography(rs.getString("biography"));
        artist.setPhoto(rs.getBytes("photo"));
        return artist;
    }
}
