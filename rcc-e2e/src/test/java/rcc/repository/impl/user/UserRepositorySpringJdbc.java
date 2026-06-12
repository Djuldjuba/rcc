package rcc.repository.impl.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import rcc.config.Config;
import rcc.data.entity.UserEntity;
import rcc.repository.UserRepository;
import rcc.repository.tpl.DataSources;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserRepositorySpringJdbc implements UserRepository {

    private static final Config CFG = Config.getInstance();
    private final JdbcTemplate jdbcTemplate;

    public UserRepositorySpringJdbc() {
        this.jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.rococoApiUrl()));
    }

    @Override
    public UserEntity create(UserEntity user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO \"user\" (id, username, firstname, lastname, avatar) " +
                            "VALUES (UUID_TO_BIN(?), ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            UUID userId = user.getId() != null ? user.getId() : UUID.randomUUID();
            ps.setString(1, userId.toString());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getFirstname());
            ps.setString(4, user.getLastname());
            ps.setBytes(5, user.getAvatar());
            return ps;
        }, keyHolder);

        if (user.getId() == null && keyHolder.getKeys() != null) {
            Object generatedId = keyHolder.getKeys().get("id");
            if (generatedId instanceof byte[]) {
                user.setId(UUID.nameUUIDFromBytes((byte[]) generatedId));
            } else if (generatedId instanceof String) {
                user.setId(UUID.fromString((String) generatedId));
            }
        }

        return user;
    }

    @Override
    public UserEntity update(UserEntity user) {
        int updatedRows = jdbcTemplate.update(
                "UPDATE \"user\" SET username = ?, firstname = ?, lastname = ?, avatar = ? " +
                        "WHERE id = UUID_TO_BIN(?)",
                user.getUsername(),
                user.getFirstname(),
                user.getLastname(),
                user.getAvatar(),
                user.getId().toString()
        );

        if (updatedRows == 0) {
            throw new RuntimeException("User not found with id: " + user.getId());
        }

        return user;
    }

    @Override
    public Optional<UserEntity> findById(UUID id) {
        String sql = "SELECT BIN_TO_UUID(id) as id, username, firstname, lastname, avatar " +
                "FROM \"user\" WHERE id = UUID_TO_BIN(?)";

        List<UserEntity> result = jdbcTemplate.query(sql, (rs, rowNum) -> {
            UserEntity user = new UserEntity();
            user.setId(UUID.fromString(rs.getString("id")));
            user.setUsername(rs.getString("username"));
            user.setFirstname(rs.getString("firstname"));
            user.setLastname(rs.getString("lastname"));
            user.setAvatar(rs.getBytes("avatar"));
            return user;
        }, id.toString());

        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        String sql = "SELECT BIN_TO_UUID(id) as id, username, firstname, lastname, avatar " +
                "FROM \"user\" WHERE username = ?";

        List<UserEntity> result = jdbcTemplate.query(sql, (rs, rowNum) -> {
            UserEntity user = new UserEntity();
            user.setId(UUID.fromString(rs.getString("id")));
            user.setUsername(rs.getString("username"));
            user.setFirstname(rs.getString("firstname"));
            user.setLastname(rs.getString("lastname"));
            user.setAvatar(rs.getBytes("avatar"));
            return user;
        }, username);

        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<UserEntity> findAll() {
        String sql = "SELECT BIN_TO_UUID(id) as id, username, firstname, lastname, avatar FROM \"user\"";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            UserEntity user = new UserEntity();
            user.setId(UUID.fromString(rs.getString("id")));
            user.setUsername(rs.getString("username"));
            user.setFirstname(rs.getString("firstname"));
            user.setLastname(rs.getString("lastname"));
            user.setAvatar(rs.getBytes("avatar"));
            return user;
        });
    }
}
