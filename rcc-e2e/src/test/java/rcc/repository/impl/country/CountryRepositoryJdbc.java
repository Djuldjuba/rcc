package rcc.repository.impl.country;

import rcc.config.Config;
import rcc.data.entity.CountryEntity;
import rcc.repository.CountryRepository;
import rcc.repository.tpl.DataSources;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CountryRepositoryJdbc implements CountryRepository {

    private static final Config CFG = Config.getInstance();
    private final DataSource dataSource = DataSources.dataSource(CFG.rococoApiUrl());

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public Optional<CountryEntity> findById(UUID id) {
        String sql = "SELECT BIN_TO_UUID(id) as country_id, name FROM country WHERE id = UUID_TO_BIN(?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapCountry(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<CountryEntity> findByName(String name) {
        String sql = "SELECT BIN_TO_UUID(id) as country_id, name FROM country WHERE name = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapCountry(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<CountryEntity> findAll() {
        String sql = "SELECT BIN_TO_UUID(id) as country_id, name FROM country ORDER BY name";

        List<CountryEntity> countries = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                countries.add(mapCountry(rs));
            }
            return countries;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private CountryEntity mapCountry(ResultSet rs) throws SQLException {
        CountryEntity country = new CountryEntity();
        country.setId(UUID.fromString(rs.getString("country_id")));
        country.setName(rs.getString("name"));
        return country;
    }
}
