package rcc.repository.impl.museum;

import org.springframework.jdbc.core.JdbcTemplate;
import rcc.config.Config;
import rcc.data.entity.MuseumEntity;
import rcc.model.extractor.MuseumResultSetExtractor;
import rcc.repository.MuseumRepository;
import rcc.repository.tpl.DataSources;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MuseumRepositorySpringJdbc implements MuseumRepository {

    private static final Config CFG = Config.getInstance();
    private final JdbcTemplate jdbcTemplate;

    public MuseumRepositorySpringJdbc() {
        this.jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.rococoApiUrl()));
    }

    @Override
    public MuseumEntity create(MuseumEntity museum) {
        UUID id = museum.getId() != null ? museum.getId() : UUID.randomUUID();
        UUID countryId = museum.getCountry() != null ? museum.getCountry().getId() : null;

        jdbcTemplate.update(
                "INSERT INTO museum (id, title, description, city, photo, country_id) " +
                        "VALUES (UUID_TO_BIN(?), ?, ?, ?, ?, UUID_TO_BIN(?))",
                id.toString(),
                museum.getTitle(),
                museum.getDescription(),
                museum.getCity(),
                museum.getPhoto(),
                countryId != null ? countryId.toString() : null
        );

        museum.setId(id);
        return museum;
    }

    @Override
    public MuseumEntity update(MuseumEntity museum) {
        UUID countryId = museum.getCountry() != null ? museum.getCountry().getId() : null;

        jdbcTemplate.update(
                "UPDATE museum SET title = ?, description = ?, city = ?, photo = ?, country_id = UUID_TO_BIN(?) " +
                        "WHERE id = UUID_TO_BIN(?)",
                museum.getTitle(),
                museum.getDescription(),
                museum.getCity(),
                museum.getPhoto(),
                countryId != null ? countryId.toString() : null,
                museum.getId().toString()
        );
        return museum;
    }

    @Override
    public Optional<MuseumEntity> findById(UUID id) {
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

        List<MuseumEntity> result = jdbcTemplate.query(sql, MuseumResultSetExtractor.instance, id.toString());
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<MuseumEntity> findByTitle(String title) {
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

        List<MuseumEntity> result = jdbcTemplate.query(sql, MuseumResultSetExtractor.instance, title);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<MuseumEntity> findAll() {
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

        return jdbcTemplate.query(sql, MuseumResultSetExtractor.instance);
    }

    @Override
    public void deleteById(UUID id) {
        jdbcTemplate.update("DELETE FROM museum WHERE id = UUID_TO_BIN(?)", id.toString());
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.execute("DELETE FROM museum");
    }
}
