package io.student.rococo.config;

import javax.annotation.Nonnull;

public interface Config {

    static Config getInstance() {
        return "docker".equals(System.getProperty("test.env"))
                ? DockerConfig.INSTANCE
                : LocalConfig.INSTANCE;
    }

    @Nonnull
    String frontUrl();

    @Nonnull
    String rococoAuthUrl();

    @Nonnull
    String rococoApiUrl();

    @Nonnull
    String authJdbcUrl();

    @Nonnull
    String artistJdbcUrl();

    @Nonnull
    String paintingJdbcUrl();

    @Nonnull
    String museumJdbcUrl();

    @Nonnull
    String geoJdbcUrl();

    @Nonnull
    String userdataJdbcUrl();

    @Nonnull
    String dbUserName();

    @Nonnull
    String dbPassword();

    @Nonnull
    default String clientId() {
        return "client";
    }

    @Nonnull
    String kafkaAddress();

    @Nonnull
    String rococoGrpcMuseumUrl();

    @Nonnull
    String rococoGrpcGeoUrl();

    @Nonnull
    String rococoGrpcArtistUrl();

    @Nonnull
    String rococoGrpcPaintingUrl();

    @Nonnull
    String rococoGrpcUserdataUrl();
}