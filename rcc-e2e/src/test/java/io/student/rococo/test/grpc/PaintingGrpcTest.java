package io.student.rococo.test.grpc;

import io.grpc.Status;
import io.student.rococo.api.grpc.ArtistGrpcApi;
import io.student.rococo.api.grpc.GeoGrpcApi;
import io.student.rococo.api.grpc.MuseumGrpcApi;
import io.student.rococo.api.grpc.PaintingGrpcApi;
import io.student.rococo.api.grpc.core.GrpcStubs;
import io.student.rococo.jupiter.annotation.User;
import io.student.rococo.service.impl.ArtistDbClient;
import io.student.rococo.service.impl.MuseumDbClient;
import io.student.rococo.service.impl.PaintingDbClient;
import io.student.rcc.artist.grpc.ArtistResponseProto;
import io.student.rcc.geo.proto.CountryProto;
import io.student.rcc.museum.grpc.MuseumResponseProto;
import io.student.rcc.painting.grpc.PaintingListResponseProto;
import io.student.rcc.painting.grpc.PaintingResponseProto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.util.UUID;

import static io.student.rococo.jupiter.TestDataPresets.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ResourceLock("artist")
@ResourceLock("museum")
@ResourceLock("painting")
public class PaintingGrpcTest {

    private static final PaintingGrpcApi paintingApi = GrpcStubs.painting();
    private static final ArtistGrpcApi artistApi = GrpcStubs.artist();
    private static final MuseumGrpcApi museumApi = GrpcStubs.museum();
    private static final GeoGrpcApi geoApi = GrpcStubs.geo();

    private static final PaintingDbClient paintingDbClient = new PaintingDbClient();
    private static final ArtistDbClient artistDbClient = new ArtistDbClient();
    private static final MuseumDbClient museumDbClient = new MuseumDbClient();

    private static String levitanId;
    private static String louvreId;
    private static String tretyakovkaId;
    private static String louvreMuseumId;

    @BeforeAll
    static void setUp() {
        artistDbClient.deleteAllArtists();
        museumDbClient.deleteAllMuseums();

        CountryProto.Country russia = geoApi.findByName(LEVITAN_MUSEUM_COUNTRY);
        CountryProto.Country france = geoApi.findByName(LOUVRE_MUSEUM_COUNTRY);

        ArtistResponseProto levitan = artistApi.addArtist(LEVITAN_ARTIST_NAME, LEVITAN_ARTIST_BIOGRAPHY, null);
        ArtistResponseProto louvreArtist = artistApi.addArtist(LOUVRE_ARTIST_NAME, LOUVRE_ARTIST_BIOGRAPHY, null);
        levitanId = levitan.getId();
        louvreId = louvreArtist.getId();

        MuseumResponseProto tretyakovka = museumApi.createMuseum(
                LEVITAN_MUSEUM_TITLE, LEVITAN_MUSEUM_DESCRIPTION, null, LEVITAN_MUSEUM_CITY, russia.getId()
        );
        MuseumResponseProto louvreMuseum = museumApi.createMuseum(
                LOUVRE_MUSEUM_TITLE, LOUVRE_MUSEUM_DESCRIPTION, null, LOUVRE_MUSEUM_CITY, france.getId()
        );
        tretyakovkaId = tretyakovka.getId();
        louvreMuseumId = louvreMuseum.getId();
    }

    @AfterEach
    void cleanUp() {
        paintingDbClient.deleteAllPaintings();
    }

    @AfterAll
    static void tearDown() {
        paintingDbClient.deleteAllPaintings();
        artistDbClient.deleteAllArtists();
        museumDbClient.deleteAllMuseums();
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка добавления картины")
    void addPainting() {
        PaintingResponseProto created = paintingApi.addPainting(
                LEVITAN_PAINTING_TITLE, LEVITAN_PAINTING_DESCRIPTION, null, levitanId, tretyakovkaId
        );

        assertNotNull(created.getId(), "id должен быть сгенерирован");
        paintingApi.checkPainting(created, LEVITAN_PAINTING_TITLE, LEVITAN_ARTIST_NAME, LEVITAN_MUSEUM_TITLE);
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка получения картины по id")
    void getPaintingById() {
        PaintingResponseProto created = paintingApi.addPainting(
                LEVITAN_PAINTING_TITLE, LEVITAN_PAINTING_DESCRIPTION, null, levitanId, tretyakovkaId
        );

        PaintingResponseProto actual = paintingApi.getPainting(created.getId());

        paintingApi.checkPainting(actual, LEVITAN_PAINTING_TITLE, LEVITAN_ARTIST_NAME, LEVITAN_MUSEUM_TITLE);
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка получения списка всех картин")
    void getAllPaintings() {
        paintingApi.addPainting(LEVITAN_PAINTING_TITLE, LEVITAN_PAINTING_DESCRIPTION, null, levitanId, tretyakovkaId);
        paintingApi.addPainting(LOUVRE_PAINTING_TITLE, LOUVRE_PAINTING_DESCRIPTION, null, louvreId, louvreMuseumId);

        PaintingListResponseProto response = paintingApi.getAllPaintings();

        paintingApi.checkPaintingsList(response, 2, LEVITAN_PAINTING_TITLE, LOUVRE_PAINTING_TITLE);
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка поиска картины по названию")
    void searchPaintings() {
        paintingApi.addPainting(LEVITAN_PAINTING_TITLE, LEVITAN_PAINTING_DESCRIPTION, null, levitanId, tretyakovkaId);
        paintingApi.addPainting(LOUVRE_PAINTING_TITLE, LOUVRE_PAINTING_DESCRIPTION, null, louvreId, louvreMuseumId);

        PaintingListResponseProto response = paintingApi.searchPaintings("вечн", 0, 10);

        paintingApi.checkPaintingsList(response, 1, LEVITAN_PAINTING_TITLE);
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка поиска картины, когда ни одна картина не будет найдена")
    void searchPaintingsNegative() {
        paintingApi.addPainting(LEVITAN_PAINTING_TITLE, LEVITAN_PAINTING_DESCRIPTION, null, levitanId, tretyakovkaId);

        PaintingListResponseProto response = paintingApi.searchPaintings("мона лиза", 0, 10);

        paintingApi.checkPaintingsList(response, 0);
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка обновления картины")
    void updatePainting() {
        PaintingResponseProto created = paintingApi.addPainting(
                LEVITAN_PAINTING_TITLE, LEVITAN_PAINTING_DESCRIPTION, null, levitanId, tretyakovkaId
        );

        PaintingResponseProto updated = paintingApi.updatePainting(
                created.getId(), LOUVRE_PAINTING_TITLE, LOUVRE_PAINTING_DESCRIPTION, null, louvreId, louvreMuseumId
        );

        paintingApi.checkPainting(updated, LOUVRE_PAINTING_TITLE, LOUVRE_ARTIST_NAME, LOUVRE_MUSEUM_TITLE);
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка удаления картины")
    void deletePainting() {
        PaintingResponseProto created = paintingApi.addPainting(
                LEVITAN_PAINTING_TITLE, LEVITAN_PAINTING_DESCRIPTION, null, levitanId, tretyakovkaId
        );

        paintingApi.deletePainting(created.getId());

        paintingApi.checkErrorStatus(() -> paintingApi.getPainting(created.getId()), Status.Code.NOT_FOUND);
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка получения картин по художнику")
    void getPaintingsByArtist() {
        paintingApi.addPainting(LEVITAN_PAINTING_TITLE, LEVITAN_PAINTING_DESCRIPTION, null, levitanId, tretyakovkaId);
        paintingApi.addPainting(LOUVRE_PAINTING_TITLE, LOUVRE_PAINTING_DESCRIPTION, null, louvreId, louvreMuseumId);

        PaintingListResponseProto response = paintingApi.getPaintingsByArtist(levitanId, 0, 10);

        paintingApi.checkPaintingsList(response, 1, LEVITAN_PAINTING_TITLE);
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка получения картин по музею")
    void getPaintingsByMuseum() {
        paintingApi.addPainting(LEVITAN_PAINTING_TITLE, LEVITAN_PAINTING_DESCRIPTION, null, levitanId, tretyakovkaId);
        paintingApi.addPainting(LOUVRE_PAINTING_TITLE, LOUVRE_PAINTING_DESCRIPTION, null, louvreId, louvreMuseumId);

        PaintingListResponseProto response = paintingApi.getPaintingsByMuseum(tretyakovkaId, 0, 10);

        paintingApi.checkPaintingsList(response, 1, LEVITAN_PAINTING_TITLE);
    }

    @Test
    @DisplayName("gRPC: Проверка получения картины по несуществующему id")
    void getPaintingNotFound() {
        String randomId = UUID.randomUUID().toString();
        paintingApi.checkErrorStatus(() -> paintingApi.getPainting(randomId), Status.Code.NOT_FOUND);
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка ошибки при создании картины с невалидным artistId")
    void addPaintingInvalidArtistId() {
        paintingApi.checkErrorStatus(
                () -> paintingApi.addPainting(LEVITAN_PAINTING_TITLE, LEVITAN_PAINTING_DESCRIPTION, null, "not-a-uuid", null),
                Status.Code.INVALID_ARGUMENT
        );
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка обновления несуществующей картины")
    void updatePaintingNotFound() {
        String randomId = UUID.randomUUID().toString();
        paintingApi.checkErrorStatus(
                () -> paintingApi.updatePainting(randomId, LOUVRE_PAINTING_TITLE, LOUVRE_PAINTING_DESCRIPTION, null, louvreId, louvreMuseumId),
                Status.Code.NOT_FOUND
        );
    }

    @Test
    @DisplayName("gRPC: Проверка удаления несуществующей картины")
    void deletePaintingNotFound() {
        String randomId = UUID.randomUUID().toString();
        paintingApi.checkErrorStatus(() -> paintingApi.deletePainting(randomId), Status.Code.NOT_FOUND);
    }
}