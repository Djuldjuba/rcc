package io.student.rococo.data.repository.impl.authUser;

import io.student.rococo.data.entity.Authority;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import io.student.rococo.config.Config;
import io.student.rococo.data.entity.AuthUserEntity;
import io.student.rococo.data.entity.AuthorityEntity;
import io.student.rococo.data.mapper.extractor.AuthUserWithAuthoritiesExtractor;
import io.student.rococo.data.repository.AuthUserRepository;
import io.student.rococo.data.mapper.tpl.DataSources;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class AuthUserRepositorySpringJdbc implements AuthUserRepository {

    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.rococoAuthUrl()));

    @Override
    public @NonNull AuthUserEntity create(@NonNull AuthUserEntity user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO \"user\" (id, username, password, enabled, account_non_expired, " +
                            "account_non_locked, credentials_non_expired) VALUES (UUID_TO_BIN(?), ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            UUID userId = user.getId() != null ? user.getId() : UUID.randomUUID();
            ps.setString(1, userId.toString());
            ps.setString(2, user.getUsername());
            ps.setString(3, passwordEncoder.encode(user.getPassword()));
            ps.setBoolean(4, user.getEnabled());
            ps.setBoolean(5, user.getAccountNonExpired());
            ps.setBoolean(6, user.getAccountNonLocked());
            ps.setBoolean(7, user.getCredentialsNonExpired());
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

        for (AuthorityEntity authority : user.getAuthorities()) {
            UUID authorityId = authority.getId() != null ? authority.getId() : UUID.randomUUID();
            jdbcTemplate.update(
                    "INSERT INTO authority (id, authority, user_id) VALUES (UUID_TO_BIN(?), ?, UUID_TO_BIN(?))",
                    authorityId.toString(),
                    authority.getAuthority().name(),
                    user.getId().toString()
            );
        }

        return user;
    }

    @Override
    public @NonNull Optional<AuthUserEntity> findById(@NonNull UUID id) {
        String sql = """
                 SELECT\s
                     BIN_TO_UUID(u.id) as user_id,
                     u.username,
                     u.password,
                     u.enabled,
                     u.account_non_expired,
                     u.account_non_locked,
                     u.credentials_non_expired,
                     BIN_TO_UUID(a.id) as authority_id,
                     a.authority
                 FROM "user" u
                 LEFT JOIN authority a ON u.id = a.user_id
                 WHERE u.id = UUID_TO_BIN(?)
                \s""";

        return Objects.requireNonNullElseGet(
                jdbcTemplate.query(sql, AuthUserWithAuthoritiesExtractor.instance, id.toString()),
                Optional::empty
        );
    }

    @Override
    public @NonNull List<AuthUserEntity> findAll() {
        String sql = """
                 SELECT\s
                     BIN_TO_UUID(u.id) as user_id,
                     u.username,
                     u.password,
                     u.enabled,
                     u.account_non_expired,
                     u.account_non_locked,
                     u.credentials_non_expired,
                     BIN_TO_UUID(a.id) as authority_id,
                     a.authority
                 FROM "user" u
                 LEFT JOIN authority a ON u.id = a.user_id
                 ORDER BY u.username
                \s""";

        List<AuthUserEntity> result = jdbcTemplate.query(sql, rs -> {
            java.util.Map<UUID, AuthUserEntity> userMap = new java.util.LinkedHashMap<>();
            while (rs.next()) {
                UUID userId = UUID.fromString(rs.getString("user_id"));
                AuthUserEntity user = userMap.get(userId);
                if (user == null) {
                    user = new AuthUserEntity();
                    user.setId(userId);
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setEnabled(rs.getBoolean("enabled"));
                    user.setAccountNonExpired(rs.getBoolean("account_non_expired"));
                    user.setAccountNonLocked(rs.getBoolean("account_non_locked"));
                    user.setCredentialsNonExpired(rs.getBoolean("credentials_non_expired"));
                    user.setAuthorities(new java.util.ArrayList<>());
                    userMap.put(userId, user);
                }

                String authorityId = rs.getString("authority_id");
                if (authorityId != null && !rs.wasNull()) {
                    AuthorityEntity authority = new AuthorityEntity();
                    authority.setId(UUID.fromString(authorityId));
                    authority.setAuthority(Authority.valueOf(rs.getString("authority")));
                    authority.setUser(user);
                    user.getAuthorities().add(authority);
                }
            }
            return new java.util.ArrayList<>(userMap.values());
        });

        return result != null ? result : Collections.emptyList();
    }

    @Override
    public @NonNull Optional<AuthUserEntity> findByUsername(@NonNull String username) {
        String sql = """
                 SELECT\s
                     BIN_TO_UUID(u.id) as user_id,
                     u.username,
                     u.password,
                     u.enabled,
                     u.account_non_expired,
                     u.account_non_locked,
                     u.credentials_non_expired,
                     BIN_TO_UUID(a.id) as authority_id,
                     a.authority
                 FROM "user" u
                 LEFT JOIN authority a ON u.id = a.user_id
                 WHERE u.username = ?
                \s""";

        return Objects.requireNonNullElseGet(
                jdbcTemplate.query(sql, AuthUserWithAuthoritiesExtractor.instance, username),
                Optional::empty
        );
    }
}