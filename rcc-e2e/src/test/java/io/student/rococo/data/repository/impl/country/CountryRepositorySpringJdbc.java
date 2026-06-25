package io.student.rococo.data.repository.impl.country;

import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.JdbcTemplate;
import io.student.rococo.config.Config;
import io.student.rococo.data.entity.CountryEntity;
import io.student.rococo.data.mapper.extractor.CountryListExtractor;
import io.student.rococo.data.repository.CountryRepository;
import io.student.rococo.data.mapper.tpl.DataSources;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CountryRepositorySpringJdbc implements CountryRepository {

    private static final Config CFG = Config.getInstance();
    private final JdbcTemplate jdbcTemplate;

    public CountryRepositorySpringJdbc() {
        this.jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.rococoApiUrl()));
    }

    @Override
    public @NonNull Optional<CountryEntity> findById(@NonNull UUID id) {
        String sql = """
                 SELECT\s
                     BIN_TO_UUID(c.id) as country_id,
                     c.name
                 FROM country c
                 WHERE c.id = UUID_TO_BIN(?)
                \s""";

        List<CountryEntity> result = jdbcTemplate.query(sql, CountryListExtractor.instance, id.toString());
        return result != null && !result.isEmpty() ? Optional.of(result.get(0)) : Optional.empty();
    }

    @Override
    public @NonNull Optional<CountryEntity> findByName(@NonNull String name) {
        String sql = """
                 SELECT\s
                     BIN_TO_UUID(c.id) as country_id,
                     c.name
                 FROM country c
                 WHERE c.name = ?
                \s""";

        List<CountryEntity> result = jdbcTemplate.query(sql, CountryListExtractor.instance, name);
        return result != null && !result.isEmpty() ? Optional.of(result.get(0)) : Optional.empty();
    }

    @Override
    public @NonNull List<CountryEntity> findAll() {
        String sql = """
                 SELECT\s
                     BIN_TO_UUID(c.id) as country_id,
                     c.name
                 FROM country c
                 ORDER BY c.name
                \s""";

        List<CountryEntity> result = jdbcTemplate.query(sql, CountryListExtractor.instance);
        return result != null ? result : Collections.emptyList();
    }
}