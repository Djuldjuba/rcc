package rcc.model.extractor;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import rcc.data.entity.CountryEntity;
import rcc.data.entity.MuseumEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MuseumResultSetExtractor implements ResultSetExtractor<List<MuseumEntity>> {

    public static final MuseumResultSetExtractor instance = new MuseumResultSetExtractor();

    private MuseumResultSetExtractor() {
    }

    @Override
    public List<MuseumEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<UUID, MuseumEntity> museumMap = new LinkedHashMap<>();

        while (rs.next()) {
            UUID museumId = UUID.fromString(rs.getString("museum_id"));
            MuseumEntity museum = museumMap.get(museumId);

            if (museum == null) {
                museum = new MuseumEntity();
                museum.setId(museumId);
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

                museumMap.put(museumId, museum);
            }
        }

        return new ArrayList<>(museumMap.values());
    }
}
