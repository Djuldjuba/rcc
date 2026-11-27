package io.student.rococo.test.grpc;

import io.grpc.Status;
import io.student.rococo.api.grpc.ArtistGrpcApi;
import io.student.rococo.api.grpc.core.GrpcStubs;
import io.student.rococo.jupiter.annotation.User;
import io.student.rococo.service.impl.ArtistDbClient;
import io.student.rcc.artist.grpc.ArtistListResponseProto;
import io.student.rcc.artist.grpc.ArtistResponseProto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.util.UUID;

import static io.student.rococo.jupiter.TestDataPresets.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ResourceLock("artist")
public class ArtistGrpcTest {

    private static final ArtistGrpcApi artistApi = GrpcStubs.artist();
    private static final ArtistDbClient artistDbClient = new ArtistDbClient();

    @AfterEach
    void cleanUp() {
        artistDbClient.deleteAllArtists();
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка создания художника")
    void addArtist() {
        ArtistResponseProto created = artistApi.addArtist(
                LEVITAN_ARTIST_NAME, LEVITAN_ARTIST_BIOGRAPHY, null
        );

        assertNotNull(created.getId(), "id должен быть сгенерирован");
        artistApi.checkArtist(created, LEVITAN_ARTIST_NAME, LEVITAN_ARTIST_BIOGRAPHY);
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка получения художника по id")
    void getArtistById() {
        ArtistResponseProto created = artistApi.addArtist(
                LEVITAN_ARTIST_NAME, LEVITAN_ARTIST_BIOGRAPHY, null
        );

        ArtistResponseProto actual = artistApi.getArtist(created.getId());

        artistApi.checkArtist(actual, LEVITAN_ARTIST_NAME, LEVITAN_ARTIST_BIOGRAPHY);
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка получения списка всех художников")
    void getAllArtists() {
        artistApi.addArtist(LEVITAN_ARTIST_NAME, LEVITAN_ARTIST_BIOGRAPHY, null);
        artistApi.addArtist(LOUVRE_ARTIST_NAME, LOUVRE_ARTIST_BIOGRAPHY, null);

        ArtistListResponseProto response = artistApi.getAllArtists();

        artistApi.checkArtistsList(response, 2, LEVITAN_ARTIST_NAME, LOUVRE_ARTIST_NAME);
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка поиска художника по имени")
    void searchArtists() {
        artistApi.addArtist(LEVITAN_ARTIST_NAME, LEVITAN_ARTIST_BIOGRAPHY, null);
        artistApi.addArtist(LOUVRE_ARTIST_NAME, LOUVRE_ARTIST_BIOGRAPHY, null);

        ArtistListResponseProto response = artistApi.searchArtists("Левит", 0, 10);

        artistApi.checkArtistsList(response, 1, LEVITAN_ARTIST_NAME);
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка поиска художника, когда ни один художник не будет найден")
    void searchArtistsNegative() {
        artistApi.addArtist(LEVITAN_ARTIST_NAME, LEVITAN_ARTIST_BIOGRAPHY, null);

        ArtistListResponseProto response = artistApi.searchArtists("Пикассо", 0, 10);

        artistApi.checkArtistsList(response, 0);
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка обновления художника")
    void updateArtist() {
        ArtistResponseProto created = artistApi.addArtist(
                LEVITAN_ARTIST_NAME, LEVITAN_ARTIST_BIOGRAPHY, null
        );

        ArtistResponseProto updated = artistApi.updateArtist(
                created.getId(), LOUVRE_ARTIST_NAME, LOUVRE_ARTIST_BIOGRAPHY, null
        );

        artistApi.checkArtist(updated, LOUVRE_ARTIST_NAME, LOUVRE_ARTIST_BIOGRAPHY);
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка удаления художника")
    void deleteArtist() {
        ArtistResponseProto created = artistApi.addArtist(
                LEVITAN_ARTIST_NAME, LEVITAN_ARTIST_BIOGRAPHY, null
        );

        artistApi.deleteArtist(created.getId());

        artistApi.checkErrorStatus(() -> artistApi.getArtist(created.getId()), Status.Code.NOT_FOUND);
    }

    @Test
    @DisplayName("gRPC: Проверка получения художника по несуществующему id")
    void getArtistNotFound() {
        String randomId = UUID.randomUUID().toString();
        artistApi.checkErrorStatus(() -> artistApi.getArtist(randomId), Status.Code.NOT_FOUND);
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка ошибки при создании художника с существующим именем")
    void addArtistAlreadyExists() {
        artistApi.addArtist(LEVITAN_ARTIST_NAME, LEVITAN_ARTIST_BIOGRAPHY, null);

        artistApi.checkErrorStatus(
                () -> artistApi.addArtist(LEVITAN_ARTIST_NAME, "Другая биография", null),
                Status.Code.ALREADY_EXISTS
        );
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка обновления несуществующего художника")
    void updateArtistNotFound() {
        String randomId = UUID.randomUUID().toString();
        artistApi.checkErrorStatus(
                () -> artistApi.updateArtist(randomId, LOUVRE_ARTIST_NAME, LOUVRE_ARTIST_BIOGRAPHY, null),
                Status.Code.NOT_FOUND
        );
    }

    @Test
    @DisplayName("gRPC: Проверка удаления несуществующего художника")
    void deleteArtistNotFound() {
        String randomId = UUID.randomUUID().toString();
        artistApi.checkErrorStatus(() -> artistApi.deleteArtist(randomId), Status.Code.NOT_FOUND);
    }
}