package io.student.rococo.config;

import org.jspecify.annotations.NonNull;

public enum DockerConfig implements Config {
    INSTANCE;

    @Override
    public @NonNull String frontUrl() {
        return "http://rcc-client/";
    }

    @Override
    public @NonNull String rococoAuthUrl() {
        return "http://rcc-auth:9000";
    }

    @Override
    public @NonNull String rococoApiUrl() {
        return "http://rcc-gateway:8085";
    }

    @Override
    public @NonNull String authJdbcUrl() {
        return "jdbc:mysql://rcc-mysql:3306/rococo-auth?serverTimezone=UTC&createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false";
    }

    @Override
    public @NonNull String artistJdbcUrl() {
        return "jdbc:mysql://rcc-mysql:3306/rcc_artist?serverTimezone=UTC&createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false";
    }

    @Override
    public @NonNull String paintingJdbcUrl() {
        return "jdbc:mysql://rcc-mysql:3306/rcc_painting?serverTimezone=UTC&createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false";
    }

    @Override
    public @NonNull String museumJdbcUrl() {
        return "jdbc:mysql://rcc-mysql:3306/rcc_museum?serverTimezone=UTC&createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false";
    }

    @Override
    public @NonNull String geoJdbcUrl() {
        return "jdbc:mysql://rcc-mysql:3306/rcc_geo?serverTimezone=UTC&createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false";
    }

    @Override
    public @NonNull String userdataJdbcUrl() {
        return "jdbc:mysql://rcc-mysql:3306/rcc_userdata?serverTimezone=UTC&createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false";
    }

    @Override
    public @NonNull String dbUserName() {
        return "root";
    }

    @Override
    public @NonNull String dbPassword() {
        return "secret";
    }

    @Override
    public @NonNull String kafkaAddress() {
        return "rcc-kafka:9092";
    }

    @Override
    public @NonNull String rococoGrpcMuseumUrl() {
        return "rcc-museum:9093";
    }

    @Override
    public @NonNull String rococoGrpcGeoUrl() {
        return "rcc-geo:9094";
    }

    @Override
    public @NonNull String rococoGrpcArtistUrl() { return "rcc-artist:9090"; }

    @Override
    public @NonNull String rococoGrpcPaintingUrl() { return "rcc-painting:9091"; }

    @Override
    public @NonNull String rococoGrpcUserdataUrl() { return "rcc-userdata:9095"; }
}