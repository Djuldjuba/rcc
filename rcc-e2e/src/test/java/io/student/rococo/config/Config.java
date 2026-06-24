package io.student.rococo.config;

import javax.annotation.Nonnull;

public interface Config {

    static Config getInstance() {
        return LocalConfig.INSTANCE;
    }

    @Nonnull
    String frontUrl();

    @Nonnull
    String rococoAuthUrl();

    @Nonnull
    String rococoApiUrl();

    @Nonnull
    String dbUserName();

    @Nonnull
    String dbPassword();
}
