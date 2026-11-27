package io.student.rococo.api.grpc;

import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.qameta.allure.Step;
import io.student.rcc.painting.grpc.CreatePaintingRequestProto;
import io.student.rcc.painting.grpc.DeletePaintingRequest;
import io.student.rcc.painting.grpc.EmptyRequest;
import io.student.rcc.painting.grpc.GetPaintingRequest;
import io.student.rcc.painting.grpc.GetPaintingsByArtistRequest;
import io.student.rcc.painting.grpc.GetPaintingsByMuseumRequest;
import io.student.rcc.painting.grpc.PaintingListResponseProto;
import io.student.rcc.painting.grpc.PaintingResponseProto;
import io.student.rcc.painting.grpc.PaintingServiceGrpc;
import io.student.rcc.painting.grpc.SearchRequest;
import io.student.rcc.painting.grpc.UpdatePaintingRequestProto;
import org.junit.jupiter.api.Assertions;

import static io.student.rococo.api.grpc.utils.GrpcUtils.nullToEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaintingGrpcApi {

    private final PaintingServiceGrpc.PaintingServiceBlockingStub stub;

    public PaintingGrpcApi(ManagedChannel channel) {
        this.stub = PaintingServiceGrpc.newBlockingStub(channel);
    }

    @Step("Получить картину по id: {id}")
    public PaintingResponseProto getPainting(String id) {
        return stub.getPainting(GetPaintingRequest.newBuilder().setId(id).build());
    }

    @Step("Получить список всех картин")
    public PaintingListResponseProto getAllPaintings() {
        return stub.getAllPaintings(EmptyRequest.newBuilder().build());
    }

    @Step("Создать картину: {title}, художник: {artistId}")
    public PaintingResponseProto addPainting(String title, String description, String content, String artistId, String museumId) {
        return stub.addPainting(
                CreatePaintingRequestProto.newBuilder()
                        .setTitle(title)
                        .setDescription(nullToEmpty(description))
                        .setContent(nullToEmpty(content))
                        .setArtistId(artistId)
                        .setMuseumId(nullToEmpty(museumId))
                        .build()
        );
    }

    @Step("Обновить картину по id: {id} на название: {title}")
    public PaintingResponseProto updatePainting(String id, String title, String description, String content, String artistId, String museumId) {
        return stub.updatePainting(
                UpdatePaintingRequestProto.newBuilder()
                        .setId(id)
                        .setTitle(title)
                        .setDescription(nullToEmpty(description))
                        .setContent(nullToEmpty(content))
                        .setArtistId(artistId)
                        .setMuseumId(nullToEmpty(museumId))
                        .build()
        );
    }

    @Step("Удалить картину по id: {id}")
    public void deletePainting(String id) {
        stub.deletePainting(DeletePaintingRequest.newBuilder().setId(id).build());
    }

    @Step("Найти картины по названию: {title}")
    public PaintingListResponseProto searchPaintings(String title, int page, int size) {
        return stub.searchPaintings(
                SearchRequest.newBuilder()
                        .setTitle(nullToEmpty(title))
                        .setPage(page)
                        .setSize(size)
                        .build()
        );
    }

    @Step("Получить картины художника: {artistId}")
    public PaintingListResponseProto getPaintingsByArtist(String artistId, int page, int size) {
        return stub.getPaintingsByArtist(
                GetPaintingsByArtistRequest.newBuilder()
                        .setArtistId(artistId)
                        .setPage(page)
                        .setSize(size)
                        .build()
        );
    }

    @Step("Получить картины музея: {museumId}")
    public PaintingListResponseProto getPaintingsByMuseum(String museumId, int page, int size) {
        return stub.getPaintingsByMuseum(
                GetPaintingsByMuseumRequest.newBuilder()
                        .setMuseumId(museumId)
                        .setPage(page)
                        .setSize(size)
                        .build()
        );
    }

    @Step("Проверить, что картина содержит данные: название '{title}', художник '{artistName}'")
    public PaintingGrpcApi checkPainting(PaintingResponseProto painting, String title, String artistName, String museumTitle) {
        Assertions.assertAll(
                () -> assertEquals(title, painting.getTitle(), "Некорректное название картины"),
                () -> assertEquals(artistName, painting.getArtistName(), "Некорректное имя художника"),
                () -> assertEquals(museumTitle, painting.getMuseumTitle(), "Некорректное название музея")
        );
        return this;
    }

    @Step("Проверить, что список картин содержит {expectedCount} элементов и включает названия {expectedTitles}")
    public PaintingGrpcApi checkPaintingsList(PaintingListResponseProto response, int expectedCount, String... expectedTitles) {
        assertEquals(expectedCount, response.getPaintingsCount(), "Неверное количество картин в списке");
        assertEquals(expectedCount, response.getTotalElements(), "Неверное общее количество элементов");

        for (String title : expectedTitles) {
            assertTrue(response.getPaintingsList().stream().anyMatch(m -> m.getTitle().equals(title)),
                    "В списке не найдена картина с названием: " + title);
        }
        return this;
    }

    @Step("Проверить, что запрос завершился с ошибкой {expectedCode}")
    public PaintingGrpcApi checkErrorStatus(Runnable action, Status.Code expectedCode) {
        StatusRuntimeException ex = Assertions.assertThrows(StatusRuntimeException.class, action::run);
        assertEquals(expectedCode, ex.getStatus().getCode(), "Неверный статус ошибки gRPC");
        return this;
    }
}