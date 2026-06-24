package io.student.rococo.data.repository.impl.museum;

import io.student.rococo.config.Config;
import io.student.rococo.data.entity.CountryEntity;
import io.student.rococo.data.entity.MuseumEntity;
import io.student.rococo.data.repository.MuseumRepository;
import io.student.rococo.data.mapper.tpl.DataSources;
import org.jspecify.annotations.NonNull;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MuseumRepositoryJdbc implements MuseumRepository {

    private static final Config CFG = Config.getInstance();
    private final DataSource dataSource = DataSources.dataSource(CFG.rococoApiUrl());

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public @NonNull MuseumEntity create(@NonNull MuseumEntity museum) {
        UUID id = museum.getId() != null ? museum.getId() : UUID.randomUUID();
        UUID countryId = museum.getCountry() != null ? museum.getCountry().getId() : null;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO museum (id, title, description, city, photo, country_id) " +
                             "VALUES (UUID_TO_BIN(?), ?, ?, ?, ?, UUID_TO_BIN(?))")) {

            ps.setString(1, id.toString());
            ps.setString(2, museum.getTitle());
            ps.setString(3, museum.getDescription());
            ps.setString(4, museum.getCity());
            ps.setBytes(5, museum.getPhoto());
            ps.setString(6, countryId != null ? countryId.toString() : null);
            ps.executeUpdate();

            museum.setId(id);
            return museum;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NonNull MuseumEntity update(@NonNull MuseumEntity museum) {
        UUID countryId = museum.getCountry() != null ? museum.getCountry().getId() : null;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE museum SET title = ?, description = ?, city = ?, photo = ?, country_id = UUID_TO_BIN(?) " +
                             "WHERE id = UUID_TO_BIN(?)")) {

            ps.setString(1, museum.getTitle());
            ps.setString(2, museum.getDescription());
            ps.setString(3, museum.getCity());
            ps.setBytes(4, museum.getPhoto());
            ps.setString(5, countryId != null ? countryId.toString() : null);
            ps.setString(6, museum.getId().toString());

            ps.executeUpdate();
            return museum;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NonNull Optional<MuseumEntity> findById(@NonNull UUID id) {
        String sql = """
                 SELECT\s
                     BIN_TO_UUID(m.id) as museum_id,
                     m.title,
                     m.description,
                     m.city,
                     m.photo,
                     BIN_TO_UUID(m.country_id) as country_id,
                     c.name as country_name
                 FROM museum m
                 LEFT JOIN country c ON m.country_id = c.id
                 WHERE m.id = UUID_TO_BIN(?)
                \s""";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapMuseum(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NonNull Optional<MuseumEntity> findByTitle(@NonNull String title) {
        String sql = """
                 SELECT\s
                   BIN_TO_UUID(m.id) as museum_id,
                   m.title,
                   m.description,
                   m.city,
                   m.photo,
                   BIN_TO_UUID(m.country_id) as country_id,
                   c.name as country_name
                 FROM museum m
                 LEFT JOIN country c ON m.country_id = c.id
                 WHERE m.title = ?
                \s""";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapMuseum(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NonNull List<MuseumEntity> findAll() {
        String sql = """
                 SELECT\s
                   BIN_TO_UUID(m.id) as museum_id,
                   m.title,
                   m.description,
                   m.city,
                   m.photo,
                   BIN_TO_UUID(m.country_id) as country_id,
                   c.name as country_name
                 FROM museum m
                 LEFT JOIN country c ON m.country_id = c.id
                \s""";

        List<MuseumEntity> museums = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                museums.add(mapMuseum(rs));
            }
            return museums;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteById(@NonNull UUID id) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM museum WHERE id = UUID_TO_BIN(?)")) {

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
            stmt.execute("DELETE FROM museum");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private MuseumEntity mapMuseum(ResultSet rs) throws SQLException {
        MuseumEntity museum = new MuseumEntity();
        museum.setId(UUID.fromString(rs.getString("museum_id")));
        museum.setTitle(rs.getString("title"));
        museum.setDescription(rs.getString("description"));
        museum.setCity(rs.getString("city"));
        museum.setPhoto(rs.getBytes("photo"));

        String countryId = rs.getString("country_id");
        if (countryId != null && !rs.wasNull()) {
            CountryEntity country = new CountryEntity();
            country.setId(UUID.fromString(countryId));
            country.setName(rs.getString("country_name"));
            museum.setCountry(country);
        }

        return museum;
    }
}
