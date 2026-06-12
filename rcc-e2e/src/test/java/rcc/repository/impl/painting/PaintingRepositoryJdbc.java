package rcc.repository.impl.painting;

import rcc.config.Config;
import rcc.data.entity.ArtistEntity;
import rcc.data.entity.MuseumEntity;
import rcc.data.entity.PaintingEntity;
import rcc.repository.PaintingRepository;
import rcc.repository.tpl.DataSources;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PaintingRepositoryJdbc implements PaintingRepository {

    private static final Config CFG = Config.getInstance();
    private final DataSource dataSource = DataSources.dataSource(CFG.rococoApiUrl());

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public PaintingEntity create(PaintingEntity painting) {
        UUID id = painting.getId() != null ? painting.getId() : UUID.randomUUID();
        UUID artistId = painting.getArtist() != null ? painting.getArtist().getId() : null;
        UUID museumId = painting.getMuseum() != null ? painting.getMuseum().getId() : null;

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
    public PaintingEntity update(PaintingEntity painting) {
        UUID artistId = painting.getArtist() != null ? painting.getArtist().getId() : null;
        UUID museumId = painting.getMuseum() != null ? painting.getMuseum().getId() : null;

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
    public Optional<PaintingEntity> findById(UUID id) {
        String sql = """
                 SELECT\s
                     BIN_TO_UUID(p.id) as painting_id,
                     p.title,
                     p.description,
                     p.content,
                     BIN_TO_UUID(p.artist_id) as artist_id,
                     a.name as artist_name,
                     a.biography as artist_biography,
                     a.photo as artist_photo,
                     BIN_TO_UUID(p.museum_id) as museum_id,
                     m.title as museum_title,
                     m.description as museum_description,
                     m.city as museum_city,
                     m.photo as museum_photo,
                     BIN_TO_UUID(m.country_id) as country_id,
                     c.name as country_name
                 FROM painting p
                 LEFT JOIN artist a ON p.artist_id = a.id
                 LEFT JOIN museum m ON p.museum_id = m.id
                 LEFT JOIN country c ON m.country_id = c.id
                 WHERE p.id = UUID_TO_BIN(?)
                \s""";

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
    public Optional<PaintingEntity> findByTitle(String title) {
        String sql = """
                 SELECT\s
                     BIN_TO_UUID(p.id) as painting_id,
                     p.title,
                     p.description,
                     p.content,
                     BIN_TO_UUID(p.artist_id) as artist_id,
                     a.name as artist_name,
                     a.biography as artist_biography,
                     a.photo as artist_photo,
                     BIN_TO_UUID(p.museum_id) as museum_id,
                     m.title as museum_title,
                     m.description as museum_description,
                     m.city as museum_city,
                     m.photo as museum_photo,
                     BIN_TO_UUID(m.country_id) as country_id,
                     c.name as country_name
                 FROM painting p
                 LEFT JOIN artist a ON p.artist_id = a.id
                 LEFT JOIN museum m ON p.museum_id = m.id
                 LEFT JOIN country c ON m.country_id = c.id
                 WHERE p.title = ?
                \s""";

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
    public List<PaintingEntity> findAll() {
        String sql = """
                 SELECT\s
                     BIN_TO_UUID(p.id) as painting_id,
                     p.title,
                     p.description,
                     p.content,
                     BIN_TO_UUID(p.artist_id) as artist_id,
                     a.name as artist_name,
                     a.biography as artist_biography,
                     a.photo as artist_photo,
                     BIN_TO_UUID(p.museum_id) as museum_id,
                     m.title as museum_title,
                     m.description as museum_description,
                     m.city as museum_city,
                     m.photo as museum_photo,
                     BIN_TO_UUID(m.country_id) as country_id,
                     c.name as country_name
                 FROM painting p
                 LEFT JOIN artist a ON p.artist_id = a.id
                 LEFT JOIN museum m ON p.museum_id = m.id
                 LEFT JOIN country c ON m.country_id = c.id
                \s""";

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
    public List<PaintingEntity> findAllByArtistId(UUID artistId) {
        String sql = """
                 SELECT\s
                     BIN_TO_UUID(p.id) as painting_id,
                     p.title,
                     p.description,
                     p.content,
                     BIN_TO_UUID(p.artist_id) as artist_id,
                     a.name as artist_name,
                     a.biography as artist_biography,
                     a.photo as artist_photo,
                     BIN_TO_UUID(p.museum_id) as museum_id,
                     m.title as museum_title,
                     m.description as museum_description,
                     m.city as museum_city,
                     m.photo as museum_photo,
                     BIN_TO_UUID(m.country_id) as country_id,
                     c.name as country_name
                 FROM painting p
                 LEFT JOIN artist a ON p.artist_id = a.id
                 LEFT JOIN museum m ON p.museum_id = m.id
                 LEFT JOIN country c ON m.country_id = c.id
                 WHERE p.artist_id = UUID_TO_BIN(?)
                \s""";

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
    public List<PaintingEntity> findAllByMuseumId(UUID museumId) {
        String sql = """
                 SELECT\s
                     BIN_TO_UUID(p.id) as painting_id,
                     p.title,
                     p.description,
                     p.content,
                     BIN_TO_UUID(p.artist_id) as artist_id,
                     a.name as artist_name,
                     a.biography as artist_biography,
                     a.photo as artist_photo,
                     BIN_TO_UUID(p.museum_id) as museum_id,
                     m.title as museum_title,
                     m.description as museum_description,
                     m.city as museum_city,
                     m.photo as museum_photo,
                     BIN_TO_UUID(m.country_id) as country_id,
                     c.name as country_name
                 FROM painting p
                 LEFT JOIN artist a ON p.artist_id = a.id
                 LEFT JOIN museum m ON p.museum_id = m.id
                 LEFT JOIN country c ON m.country_id = c.id
                 WHERE p.museum_id = UUID_TO_BIN(?)
                \s""";

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
    public void deleteById(UUID id) {
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
            ArtistEntity artist = new ArtistEntity();
            artist.setId(UUID.fromString(artistId));
            artist.setName(rs.getString("artist_name"));
            artist.setBiography(rs.getString("artist_biography"));
            artist.setPhoto(rs.getBytes("artist_photo"));
            painting.setArtist(artist);
        }

        String museumId = rs.getString("museum_id");
        if (museumId != null && !rs.wasNull()) {
            MuseumEntity museum = new MuseumEntity();
            museum.setId(UUID.fromString(museumId));
            museum.setTitle(rs.getString("museum_title"));
            museum.setDescription(rs.getString("museum_description"));
            museum.setCity(rs.getString("museum_city"));
            museum.setPhoto(rs.getBytes("museum_photo"));
            painting.setMuseum(museum);
        }

        return painting;
    }
}