package io.student.rococo.data.mapper.extractor;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import io.student.rococo.data.entity.CountryEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CountryListExtractor implements ResultSetExtractor<List<CountryEntity>> {

    public static final CountryListExtractor instance = new CountryListExtractor();

    private CountryListExtractor() {
    }

    @Override
    public List<CountryEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<UUID, CountryEntity> countryMap = new LinkedHashMap<>();

        while (rs.next()) {
            UUID countryId = UUID.fromString(rs.getString("country_id"));
            CountryEntity country = countryMap.get(countryId);

            if (country == null) {
                country = new CountryEntity();
                country.setId(countryId);
                country.setName(rs.getString("name"));
                countryMap.put(countryId, country);
            }
        }

        return new ArrayList<>(countryMap.values());
    }
}
