package rcc.repository.impl.country;

import org.springframework.jdbc.core.JdbcTemplate;
import rcc.config.Config;
import rcc.data.entity.CountryEntity;
import rcc.model.extractor.CountryListExtractor;
import rcc.repository.CountryRepository;
import rcc.repository.tpl.DataSources;

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
    public Optional<CountryEntity> findById(UUID id) {
        String sql = """
                 SELECT\s
                     BIN_TO_UUID(c.id) as country_id,
                     c.name
                 FROM country c
                 WHERE c.id = UUID_TO_BIN(?)
                \s""";

        List<CountryEntity> result = jdbcTemplate.query(sql, CountryListExtractor.instance, id.toString());
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<CountryEntity> findByName(String name) {
        String sql = """
                 SELECT\s
                     BIN_TO_UUID(c.id) as country_id,
                     c.name
                 FROM country c
                 WHERE c.name = ?
                \s""";

        List<CountryEntity> result = jdbcTemplate.query(sql, CountryListExtractor.instance, name);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<CountryEntity> findAll() {
        String sql = """
                 SELECT\s
                     BIN_TO_UUID(c.id) as country_id,
                     c.name
                 FROM country c
                 ORDER BY c.name
                \s""";

        return jdbcTemplate.query(sql, CountryListExtractor.instance);
    }
}
