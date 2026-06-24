package io.student.rococo.data.repository.impl.user;

import io.student.rococo.config.Config;
import io.student.rococo.data.entity.UserEntity;
import io.student.rococo.data.repository.UserRepository;
import io.student.rococo.data.mapper.tpl.DataSources;
import org.jspecify.annotations.NonNull;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserRepositoryJdbc implements UserRepository {

    private static final Config CFG = Config.getInstance();
    private final DataSource dataSource = DataSources.dataSource(CFG.rococoApiUrl());

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public @NonNull UserEntity create(@NonNull UserEntity user) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO \"user\" (id, username, firstname, lastname, avatar) " +
                             "VALUES (UUID_TO_BIN(?), ?, ?, ?, ?)")) {

            UUID userId = user.getId() != null ? user.getId() : UUID.randomUUID();
            ps.setString(1, userId.toString());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getFirstname());
            ps.setString(4, user.getLastname());
            ps.setBytes(5, user.getAvatar());
            ps.executeUpdate();

            user.setId(userId);
            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NonNull UserEntity update(@NonNull UserEntity user) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE \"user\" SET username = ?, firstname = ?, lastname = ?, avatar = ? " +
                             "WHERE id = UUID_TO_BIN(?)")) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getFirstname());
            ps.setString(3, user.getLastname());
            ps.setBytes(4, user.getAvatar());
            ps.setString(5, user.getId().toString());

            int updatedRows = ps.executeUpdate();
            if (updatedRows == 0) {
                throw new RuntimeException("User not found with id: " + user.getId());
            }

            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NonNull Optional<UserEntity> findById(@NonNull UUID id) {
        String sql = "SELECT BIN_TO_UUID(id) as id, username, firstname, lastname, avatar " +
                "FROM \"user\" WHERE id = UUID_TO_BIN(?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserEntity user = new UserEntity();
                    user.setId(UUID.fromString(rs.getString("id")));
                    user.setUsername(rs.getString("username"));
                    user.setFirstname(rs.getString("firstname"));
                    user.setLastname(rs.getString("lastname"));
                    user.setAvatar(rs.getBytes("avatar"));
                    return Optional.of(user);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NonNull Optional<UserEntity> findByUsername(@NonNull String username) {
        String sql = "SELECT BIN_TO_UUID(id) as id, username, firstname, lastname, avatar " +
                "FROM \"user\" WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserEntity user = new UserEntity();
                    user.setId(UUID.fromString(rs.getString("id")));
                    user.setUsername(rs.getString("username"));
                    user.setFirstname(rs.getString("firstname"));
                    user.setLastname(rs.getString("lastname"));
                    user.setAvatar(rs.getBytes("avatar"));
                    return Optional.of(user);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NonNull List<UserEntity> findAll() {
        String sql = "SELECT BIN_TO_UUID(id) as id, username, firstname, lastname, avatar FROM \"user\"";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<UserEntity> users = new ArrayList<>();
            while (rs.next()) {
                UserEntity user = new UserEntity();
                user.setId(UUID.fromString(rs.getString("id")));
                user.setUsername(rs.getString("username"));
                user.setFirstname(rs.getString("firstname"));
                user.setLastname(rs.getString("lastname"));
                user.setAvatar(rs.getBytes("avatar"));
                users.add(user);
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
