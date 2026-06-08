package io.student.rcc.config;

public interface Config {

    static Config getInstance() {
        return LocalConfig.INSTANCE;
    }

    String frontUrl();

    String userJdbcUrl();

    String dbUserName();

    String dbPassword();
}
