package io.student.rococo.api.grpc;

import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.qameta.allure.Step;
import io.student.rcc.museum.grpc.CreateMuseumRequestProto;
import io.student.rcc.museum.grpc.DeleteMuseumRequest;
import io.student.rcc.museum.grpc.EmptyRequest;
import io.student.rcc.museum.grpc.GetMuseumRequest;
import io.student.rcc.museum.grpc.GetMuseumsByCountryRequest;
import io.student.rcc.museum.grpc.MuseumListResponseProto;
import io.student.rcc.museum.grpc.MuseumResponseProto;
import io.student.rcc.museum.grpc.MuseumServiceGrpc;
import io.student.rcc.museum.grpc.SearchRequest;
import io.student.rcc.museum.grpc.UpdateMuseumRequestProto;
import org.junit.jupiter.api.Assertions;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MuseumGrpcApi {

    private final MuseumServiceGrpc.MuseumServiceBlockingStub stub;

    public MuseumGrpcApi(ManagedChannel channel) {
        this.stub = MuseumServiceGrpc.newBlockingStub(channel);
    }

    @Step("Получить музей по id: {id}")
    public MuseumResponseProto getMuseum(String id) {
        return stub.getMuseum(GetMuseumRequest.newBuilder().setId(id).build());
    }

    @Step("Получить список всех музеев")
    public MuseumListResponseProto getAllMuseums() {
        return stub.getAllMuseums(EmptyRequest.newBuilder().build());
    }

    @Step("Создать музей: {title}, город: {city}")
    public MuseumResponseProto createMuseum(String title, String description, String photo, String city, String countryId) {
        return stub.createMuseum(
                CreateMuseumRequestProto.newBuilder()
                        .setTitle(title)
                        .setDescription(description != null ? description : "")
                        .setPhoto(photo != null ? photo : "")
                        .setCity(city != null ? city : "")
                        .setCountryId(countryId != null ? countryId : "")
                        .build()
        );
    }

    @Step("Обновить музей по id: {id} на название: {title}, город: {city}")
    public MuseumResponseProto updateMuseum(String id, String title, String description, String photo, String city, String countryId) {
        return stub.updateMuseum(
                UpdateMuseumRequestProto.newBuilder()
                        .setId(id)
                        .setTitle(title)
                        .setDescription(description != null ? description : "")
                        .setPhoto(photo != null ? photo : "")
                        .setCity(city != null ? city : "")
                        .setCountryId(countryId != null ? countryId : "")
                        .build()
        );
    }

    @Step("Удалить музей по id: {id}")
    public void deleteMuseum(String id) {
        stub.deleteMuseum(DeleteMuseumRequest.newBuilder().setId(id).build());
    }

    @Step("Найти музеи по названию: {title}")
    public MuseumListResponseProto searchMuseums(String title, int page, int size) {
        return stub.searchMuseums(
                SearchRequest.newBuilder()
                        .setTitle(title != null ? title : "")
                        .setPage(page)
                        .setSize(size)
                        .build()
        );
    }

    @Step("Получить музеи страны: {countryId}")
    public MuseumListResponseProto getMuseumsByCountry(String countryId, int page, int size) {
        return stub.getMuseumsByCountry(
                GetMuseumsByCountryRequest.newBuilder()
                        .setCountryId(countryId)
                        .setPage(page)
                        .setSize(size)
                        .build()
        );
    }

    @Step("Проверить, что музей содержит данные: название '{title}', город '{city}'")
    public MuseumGrpcApi checkMuseum(MuseumResponseProto museum, String title, String city, String countryId, String countryName) {
        Assertions.assertAll(
                () -> assertEquals(title, museum.getTitle(), "Некорректное название музея"),
                () -> assertEquals(city, museum.getCity(), "Некорректный город музея"),
                () -> assertEquals(countryId, museum.getCountryId(), "Некорректный id страны"),
                () -> assertEquals(countryName, museum.getCountryName(), "Некорректное название страны")
        );
        return this;
    }

    @Step("Проверить, что список музеев содержит {expectedCount} элементов и включает названия {expectedTitles}")
    public MuseumGrpcApi checkMuseumsList(MuseumListResponseProto response, int expectedCount, String... expectedTitles) {
        assertEquals(expectedCount, response.getMuseumsCount(), "Неверное количество музеев в списке");
        assertEquals(expectedCount, response.getTotalElements(), "Неверное общее количество элементов");

        for (String title : expectedTitles) {
            assertTrue(response.getMuseumsList().stream().anyMatch(m -> m.getTitle().equals(title)),
                    "В списке не найден музей с названием: " + title);
        }
        return this;
    }

    @Step("Проверить, что запрос завершился с ошибкой {expectedCode}")
    public MuseumGrpcApi checkErrorStatus(Runnable action, Status.Code expectedCode) {
        StatusRuntimeException ex = Assertions.assertThrows(StatusRuntimeException.class, action::run);
        assertEquals(expectedCode, ex.getStatus().getCode(), "Неверный статус ошибки gRPC");
        return this;
    }
}