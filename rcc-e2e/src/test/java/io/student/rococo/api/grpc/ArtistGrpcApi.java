package io.student.rococo.api.grpc;

import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.qameta.allure.Step;
import io.student.rcc.artist.grpc.ArtistListResponseProto;
import io.student.rcc.artist.grpc.ArtistResponseProto;
import io.student.rcc.artist.grpc.ArtistServiceGrpc;
import io.student.rcc.artist.grpc.CreateArtistRequestProto;
import io.student.rcc.artist.grpc.DeleteArtistRequest;
import io.student.rcc.artist.grpc.EmptyRequest;
import io.student.rcc.artist.grpc.GetArtistRequest;
import io.student.rcc.artist.grpc.SearchRequest;
import io.student.rcc.artist.grpc.UpdateArtistRequestProto;
import org.junit.jupiter.api.Assertions;

import static io.student.rococo.api.grpc.utils.GrpcUtils.nullToEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArtistGrpcApi {

    private final ArtistServiceGrpc.ArtistServiceBlockingStub stub;

    public ArtistGrpcApi(ManagedChannel channel) {
        this.stub = ArtistServiceGrpc.newBlockingStub(channel);
    }

    @Step("Получить художника по id: {id}")
    public ArtistResponseProto getArtist(String id) {
        return stub.getArtist(GetArtistRequest.newBuilder().setId(id).build());
    }

    @Step("Получить список всех художников")
    public ArtistListResponseProto getAllArtists() {
        return stub.getAllArtists(EmptyRequest.newBuilder().build());
    }

    @Step("Создать художника: {name}")
    public ArtistResponseProto addArtist(String name, String biography, String photo) {
        return stub.addArtist(
                CreateArtistRequestProto.newBuilder()
                        .setName(name)
                        .setBiography(nullToEmpty(biography))
                        .setPhoto(nullToEmpty(photo))
                        .build()
        );
    }

    @Step("Обновить художника по id: {id} на имя: {name}")
    public ArtistResponseProto updateArtist(String id, String name, String biography, String photo) {
        return stub.updateArtist(
                UpdateArtistRequestProto.newBuilder()
                        .setId(id)
                        .setName(name)
                        .setBiography(nullToEmpty(biography))
                        .setPhoto(nullToEmpty(photo))
                        .build()
        );
    }

    @Step("Удалить художника по id: {id}")
    public void deleteArtist(String id) {
        stub.deleteArtist(DeleteArtistRequest.newBuilder().setId(id).build());
    }

    @Step("Найти художников по имени: {name}")
    public ArtistListResponseProto searchArtists(String name, int page, int size) {
        return stub.searchArtists(
                SearchRequest.newBuilder()
                        .setName(nullToEmpty(name))
                        .setPage(page)
                        .setSize(size)
                        .build()
        );
    }

    @Step("Проверить, что художник содержит данные: имя '{name}', биография '{biography}'")
    public ArtistGrpcApi checkArtist(ArtistResponseProto artist, String name, String biography) {
        Assertions.assertAll(
                () -> assertEquals(name, artist.getName(), "Некорректное имя художника"),
                () -> assertEquals(biography, artist.getBiography(), "Некорректная биография")
        );
        return this;
    }

    @Step("Проверить, что список художников содержит {expectedCount} элементов и включает имена {expectedNames}")
    public ArtistGrpcApi checkArtistsList(ArtistListResponseProto response, int expectedCount, String... expectedNames) {
        assertEquals(expectedCount, response.getArtistsCount(), "Неверное количество художников в списке");
        assertEquals(expectedCount, response.getTotalElements(), "Неверное общее количество элементов");

        for (String name : expectedNames) {
            assertTrue(response.getArtistsList().stream().anyMatch(a -> a.getName().equals(name)),
                    "В списке не найден художник с именем: " + name);
        }
        return this;
    }

    @Step("Проверить, что запрос завершился с ошибкой {expectedCode}")
    public ArtistGrpcApi checkErrorStatus(Runnable action, Status.Code expectedCode) {
        StatusRuntimeException ex = Assertions.assertThrows(StatusRuntimeException.class, action::run);
        assertEquals(expectedCode, ex.getStatus().getCode(), "Неверный статус ошибки gRPC");
        return this;
    }

}