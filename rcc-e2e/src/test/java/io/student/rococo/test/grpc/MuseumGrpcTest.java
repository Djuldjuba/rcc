package io.student.rococo.test.grpc;

import io.grpc.Status;
import io.student.rococo.api.grpc.GeoGrpcApi;
import io.student.rococo.api.grpc.MuseumGrpcApi;
import io.student.rococo.api.grpc.core.GrpcStubs;
import io.student.rococo.jupiter.annotation.User;
import io.student.rococo.service.impl.MuseumDbClient;
import io.student.rcc.geo.proto.CountryProto;
import io.student.rcc.museum.grpc.MuseumListResponseProto;
import io.student.rcc.museum.grpc.MuseumResponseProto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.util.UUID;

import static io.student.rococo.jupiter.TestDataPresets.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ResourceLock("museum")
public class MuseumGrpcTest {

    private static final MuseumGrpcApi museumApi = GrpcStubs.museum();
    private static final GeoGrpcApi geoApi = GrpcStubs.geo();
    private static final MuseumDbClient museumDbClient = new MuseumDbClient();

    private static String russiaId;
    private static String franceId;

    @BeforeAll
    static void setUp() {
        CountryProto.Country russia = geoApi.findByName(LEVITAN_MUSEUM_COUNTRY);
        CountryProto.Country france = geoApi.findByName(LOUVRE_MUSEUM_COUNTRY);
        russiaId = russia.getId();
        franceId = france.getId();
    }

    @AfterEach
    void cleanUp() {
        museumDbClient.deleteAllMuseums();
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка создания музея")
    void createMuseum() {
        MuseumResponseProto created = museumApi.createMuseum(
                LEVITAN_MUSEUM_TITLE, LEVITAN_MUSEUM_DESCRIPTION, null, LEVITAN_MUSEUM_CITY, russiaId
        );

        assertNotNull(created.getId(), "id должен быть сгенерирован");
        museumApi.checkMuseum(created, LEVITAN_MUSEUM_TITLE, LEVITAN_MUSEUM_CITY, russiaId, LEVITAN_MUSEUM_COUNTRY);
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка получения музея по id")
    void getMuseumById() {
        MuseumResponseProto created = museumApi.createMuseum(
                LEVITAN_MUSEUM_TITLE, LEVITAN_MUSEUM_DESCRIPTION, null, LEVITAN_MUSEUM_CITY, russiaId
        );

        museumApi.getMuseum(created.getId())
                .equals(museumApi.checkMuseum(created, LEVITAN_MUSEUM_TITLE, LEVITAN_MUSEUM_CITY, russiaId, LEVITAN_MUSEUM_COUNTRY));
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка получения списка всех музеев")
    void getAllMuseums() {
        museumApi.createMuseum(LEVITAN_MUSEUM_TITLE, LEVITAN_MUSEUM_DESCRIPTION, null, LEVITAN_MUSEUM_CITY, russiaId);
        museumApi.createMuseum(LOUVRE_MUSEUM_TITLE, LOUVRE_MUSEUM_DESCRIPTION, null, LOUVRE_MUSEUM_CITY, franceId);

        MuseumListResponseProto response = museumApi.getAllMuseums();

        museumApi.checkMuseumsList(response, 2, LEVITAN_MUSEUM_TITLE, LOUVRE_MUSEUM_TITLE);
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка поиска музея по названию")
    void searchMuseums() {
        museumApi.createMuseum(LEVITAN_MUSEUM_TITLE, LEVITAN_MUSEUM_DESCRIPTION, null, LEVITAN_MUSEUM_CITY, russiaId);
        museumApi.createMuseum(LOUVRE_MUSEUM_TITLE, LOUVRE_MUSEUM_DESCRIPTION, null, LOUVRE_MUSEUM_CITY, franceId);

        MuseumListResponseProto response = museumApi.searchMuseums("третьяк", 0, 10);

        museumApi.checkMuseumsList(response, 1, LEVITAN_MUSEUM_TITLE);
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка поиска музея, когда ни один музей не будет найден")
    void searchMuseumsNegative() {
        museumApi.createMuseum(LEVITAN_MUSEUM_TITLE, LEVITAN_MUSEUM_DESCRIPTION, null, LEVITAN_MUSEUM_CITY, russiaId);

        MuseumListResponseProto response = museumApi.searchMuseums("эрмитаж", 0, 10);

        museumApi.checkMuseumsList(response, 0);
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка обновления музея")
    void updateMuseum() {
        MuseumResponseProto created = museumApi.createMuseum(
                LEVITAN_MUSEUM_TITLE, LEVITAN_MUSEUM_DESCRIPTION, null, LEVITAN_MUSEUM_CITY, russiaId
        );

        MuseumResponseProto updated = museumApi.updateMuseum(
                created.getId(), LOUVRE_MUSEUM_TITLE, LOUVRE_MUSEUM_DESCRIPTION, null, LOUVRE_MUSEUM_CITY, franceId
        );

        museumApi.checkMuseum(updated, LOUVRE_MUSEUM_TITLE, LOUVRE_MUSEUM_CITY, franceId, LOUVRE_MUSEUM_COUNTRY);
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка удаления музея")
    void deleteMuseum() {
        MuseumResponseProto created = museumApi.createMuseum(
                LEVITAN_MUSEUM_TITLE, LEVITAN_MUSEUM_DESCRIPTION, null, LEVITAN_MUSEUM_CITY, russiaId
        );

        museumApi.deleteMuseum(created.getId());

        museumApi.checkErrorStatus(() -> museumApi.getMuseum(created.getId()), Status.Code.NOT_FOUND);
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка получения музеев по стране")
    void getMuseumsByCountry() {
        museumApi.createMuseum(LEVITAN_MUSEUM_TITLE, LEVITAN_MUSEUM_DESCRIPTION, null, LEVITAN_MUSEUM_CITY, russiaId);
        museumApi.createMuseum(LOUVRE_MUSEUM_TITLE, LOUVRE_MUSEUM_DESCRIPTION, null, LOUVRE_MUSEUM_CITY, franceId);

        MuseumListResponseProto response = museumApi.getMuseumsByCountry(russiaId, 0, 10);

        museumApi.checkMuseumsList(response, 1, LEVITAN_MUSEUM_TITLE);
    }

    @Test
    @DisplayName("gRPC: Проверка получения музея по несуществующему id")
    void getMuseumNotFound() {
        String randomId = UUID.randomUUID().toString();
        museumApi.checkErrorStatus(() -> museumApi.getMuseum(randomId), Status.Code.NOT_FOUND);
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка ошибки при создании музея с невалидным countryId")
    void createMuseumInvalidCountryId() {
        museumApi.checkErrorStatus(
                () -> museumApi.createMuseum(LEVITAN_MUSEUM_TITLE, LEVITAN_MUSEUM_DESCRIPTION, null, LEVITAN_MUSEUM_CITY, "not-a-uuid"),
                Status.Code.INVALID_ARGUMENT
        );
    }

    @Test
    @User
    @DisplayName("gRPC: Проверка обновления несуществующего музея")
    void updateMuseumNotFound() {
        String randomId = UUID.randomUUID().toString();
        museumApi.checkErrorStatus(
                () -> museumApi.updateMuseum(randomId, LOUVRE_MUSEUM_TITLE, LOUVRE_MUSEUM_DESCRIPTION, null, LOUVRE_MUSEUM_CITY, franceId),
                Status.Code.NOT_FOUND
        );
    }

    @Test
    @DisplayName("gRPC: Проверка удаления несуществующего музея")
    void deleteMuseumNotFound() {
        String randomId = UUID.randomUUID().toString();
        museumApi.checkErrorStatus(() -> museumApi.deleteMuseum(randomId), Status.Code.NOT_FOUND);
    }
}