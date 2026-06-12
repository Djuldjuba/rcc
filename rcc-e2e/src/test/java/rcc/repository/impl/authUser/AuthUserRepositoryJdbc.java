package rcc.repository.impl.authUser;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import rcc.config.Config;
import rcc.data.entity.AuthUserEntity;
import rcc.data.entity.Authority;
import rcc.data.entity.AuthorityEntity;
import rcc.repository.AuthUserRepository;
import rcc.repository.tpl.DataSources;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AuthUserRepositoryJdbc implements AuthUserRepository {

    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final DataSource dataSource = DataSources.dataSource(CFG.rococoAuthUrl());

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public AuthUserEntity create(AuthUserEntity user) {
        try (Connection conn = getConnection();
             PreparedStatement userPs = conn.prepareStatement(
                     "INSERT INTO \"user\" (id, username, password, enabled, account_non_expired, account_non_locked, " +
                             "credentials_non_expired) VALUES (UUID_TO_BIN(?), ?, ?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS);
             PreparedStatement authorityPs = conn.prepareStatement(
                     "INSERT INTO authority (id, authority, user_id) VALUES (UUID_TO_BIN(?), ?, UUID_TO_BIN(?))")) {

            UUID userId = user.getId() != null ? user.getId() : UUID.randomUUID();
            userPs.setString(1, userId.toString());
            userPs.setString(2, user.getUsername());
            userPs.setString(3, passwordEncoder.encode(user.getPassword()));
            userPs.setBoolean(4, user.getEnabled());
            userPs.setBoolean(5, user.getAccountNonExpired());
            userPs.setBoolean(6, user.getAccountNonLocked());
            userPs.setBoolean(7, user.getCredentialsNonExpired());

            userPs.executeUpdate();

            user.setId(userId);

            for (AuthorityEntity authority : user.getAuthorities()) {
                UUID authorityId = authority.getId() != null ? authority.getId() : UUID.randomUUID();
                authorityPs.setString(1, authorityId.toString());
                authorityPs.setString(2, authority.getAuthority().name());
                authorityPs.setString(3, userId.toString());
                authorityPs.addBatch();
                authorityPs.clearParameters();
            }
            authorityPs.executeBatch();

            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<AuthUserEntity> findById(UUID id) {
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

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id.toString());

            try (ResultSet rs = ps.executeQuery()) {
                AuthUserEntity user = null;
                List<AuthorityEntity> authorities = new ArrayList<>();

                while (rs.next()) {
                    if (user == null) {
                        user = new AuthUserEntity();
                        user.setId(UUID.fromString(rs.getString("user_id")));
                        user.setUsername(rs.getString("username"));
                        user.setPassword(rs.getString("password"));
                        user.setEnabled(rs.getBoolean("enabled"));
                        user.setAccountNonExpired(rs.getBoolean("account_non_expired"));
                        user.setAccountNonLocked(rs.getBoolean("account_non_locked"));
                        user.setCredentialsNonExpired(rs.getBoolean("credentials_non_expired"));
                        user.setAuthorities(authorities);
                    }

                    String authorityId = rs.getString("authority_id");
                    if (authorityId != null && !rs.wasNull()) {
                        AuthorityEntity authority = new AuthorityEntity();
                        authority.setId(UUID.fromString(authorityId));
                        authority.setAuthority(Authority.valueOf(rs.getString("authority")));
                        authority.setUser(user);
                        authorities.add(authority);
                    }
                }

                return Optional.ofNullable(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<AuthUserEntity> findAll() {
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

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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
                    user.setAuthorities(new ArrayList<>());
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

            return new ArrayList<>(userMap.values());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<AuthUserEntity> findByUsername(String username) {
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

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                AuthUserEntity user = null;
                List<AuthorityEntity> authorities = new ArrayList<>();

                while (rs.next()) {
                    if (user == null) {
                        user = new AuthUserEntity();
                        user.setId(UUID.fromString(rs.getString("user_id")));
                        user.setUsername(rs.getString("username"));
                        user.setPassword(rs.getString("password"));
                        user.setEnabled(rs.getBoolean("enabled"));
                        user.setAccountNonExpired(rs.getBoolean("account_non_expired"));
                        user.setAccountNonLocked(rs.getBoolean("account_non_locked"));
                        user.setCredentialsNonExpired(rs.getBoolean("credentials_non_expired"));
                        user.setAuthorities(authorities);
                    }

                    String authorityId = rs.getString("authority_id");
                    if (authorityId != null && !rs.wasNull()) {
                        AuthorityEntity authority = new AuthorityEntity();
                        authority.setId(UUID.fromString(authorityId));
                        authority.setAuthority(Authority.valueOf(rs.getString("authority")));
                        authority.setUser(user);
                        authorities.add(authority);
                    }
                }

                return Optional.ofNullable(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
