package io.student.rcc.config;

public enum LocalConfig implements Config {
    INSTANCE;

    @Override
    public String frontUrl() {
        return "http://localhost:3000";
    }

    @Override
    public String userJdbcUrl() {
        return "jdbc:mysql://localhost:3306/rococo-auth?serverTimezone=UTC&createDatabaseIfNotExist=true";
    }

    @Override
    public String dbUserName() {
        return "root";
    }

    @Override
    public String dbPassword() {
        return "secret";
    }
}
