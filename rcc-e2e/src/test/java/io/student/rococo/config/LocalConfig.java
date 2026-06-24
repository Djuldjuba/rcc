package io.student.rococo.config;

import org.jspecify.annotations.NonNull;

public enum LocalConfig implements Config {
    INSTANCE;

    @Override
    public @NonNull String frontUrl() {
        return "http://localhost:3000";
    }

    @Override
    public @NonNull String rococoAuthUrl() {
        return "jdbc:mysql://localhost:3306/rococo-auth?serverTimezone=UTC&createDatabaseIfNotExist=true";
    }

    @Override
    public @NonNull String rococoApiUrl() {
        return "jdbc:mysql://localhost:3306/rococo-api?serverTimezone=UTC&createDatabaseIfNotExist=true";
    }

    @Override
    public @NonNull String dbUserName() {
        return "root";
    }

    @Override
    public @NonNull String dbPassword() {
        return "secret";
    }
}
