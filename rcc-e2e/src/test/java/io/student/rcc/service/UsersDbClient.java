package io.student.rcc.service;

import io.student.rcc.config.Config;
import io.student.rcc.model.UserJson;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

public class UsersDbClient implements UsersClient {

    private static Config CFG = Config.getInstance();
    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Override
    public UserJson createUser(String username, String password) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(
                new SingleConnectionDataSource(
                        CFG.userJdbcUrl(),
                        CFG.dbUserName(),
                        CFG.dbPassword(),
                        false
                )
        );

        final UUID userId = UUID.randomUUID();

        // Используем UUID_TO_BIN для конвертации UUID в binary(16)
        jdbcTemplate.update(
                "INSERT INTO `user` (id, username, `password`, enabled, account_non_expired, account_non_locked, credentials_non_expired) "
                        + "VALUES (UUID_TO_BIN(?, true), ?, ?, ?, ?, ?, ?)",
                userId.toString(),
                username,
                passwordEncoder.encode(password),
                true,
                true,
                true,
                true
        );

        return new UserJson(
                userId,
                username,
                null,
                null,
                null
        );
    }
}
