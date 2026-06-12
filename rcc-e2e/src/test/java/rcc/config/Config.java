package rcc.config;

public interface Config {

    static Config getInstance() {
        return LocalConfig.INSTANCE;
    }

    String frontUrl();

    String rococoAuthUrl();

    String rococoApiUrl();

    String dbUserName();

    String dbPassword();
}
