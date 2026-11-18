package io.student.rcc.gateway.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.student.rcc.gateway.model.PaintingJson;
import io.student.rcc.painting.grpc.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaintingClientTest {

    @Mock
    private PaintingServiceGrpc.PaintingServiceBlockingStub stub;

    private PaintingClient client;

    @BeforeEach
    void setUp() {
        client = new PaintingClient();
        ReflectionTestUtils.setField(client, "stub", stub);
    }

    @Test
    void getPaintingsPageShouldReturnMap() {
        PaintingResponseProto proto = PaintingResponseProto.newBuilder()
                .setId("p-1").setTitle("Мона Лиза").build();
        when(stub.searchPaintings(any(SearchRequest.class)))
                .thenReturn(PaintingListResponseProto.newBuilder()
                        .addPaintings(proto).setTotalPages(1).setTotalElements(1).build());

        Map<String, Object> result = client.getPaintingsPage(0, 9);

        assertEquals(1, ((List<?>) result.get("content")).size());
    }

    @Test
    void getPaintingsByArtistShouldCallStub() {
        when(stub.getPaintingsByArtist(any(GetPaintingsByArtistRequest.class)))
                .thenReturn(PaintingListResponseProto.getDefaultInstance());

        client.getPaintingsByArtist("a-1", 0, 9);

        verify(stub).getPaintingsByArtist(argThat(r ->
                r.getArtistId().equals("a-1") && r.getPage() == 0 && r.getSize() == 9));
    }

    @Test
    void getPaintingsByMuseumShouldCallStub() {
        when(stub.getPaintingsByMuseum(any(GetPaintingsByMuseumRequest.class)))
                .thenReturn(PaintingListResponseProto.getDefaultInstance());

        client.getPaintingsByMuseum("m-1", 0, 9);

        verify(stub).getPaintingsByMuseum(argThat(r -> r.getMuseumId().equals("m-1")));
    }

    @Test
    void getPaintingByIdShouldReturnNullOnNotFound() {
        when(stub.getPainting(any(GetPaintingRequest.class)))
                .thenThrow(new StatusRuntimeException(Status.NOT_FOUND));

        assertNull(client.getPaintingById("missing"));
    }

    @Test
    void addPaintingShouldMapArtistAndMuseum() {
        when(stub.addPainting(any(CreatePaintingRequestProto.class)))
                .thenReturn(PaintingResponseProto.newBuilder().setId("new").build());

        PaintingJson painting = new PaintingJson(
                null, "T", null, null,
                new PaintingJson.ArtistJson("a-1", null),
                new PaintingJson.MuseumJson("m-1", null)
        );

        client.addPainting(painting);

        verify(stub).addPainting(argThat(r ->
                r.getArtistId().equals("a-1") && r.getMuseumId().equals("m-1")));
    }

    @Test
    void updatePaintingShouldSetOnlyNonNullFields() {
        when(stub.updatePainting(any(UpdatePaintingRequestProto.class)))
                .thenReturn(PaintingResponseProto.newBuilder().setId("id").build());

        PaintingJson painting = new PaintingJson(
                null, "New", null, null,
                new PaintingJson.ArtistJson("a-2", null),
                null
        );

        client.updatePainting("id", painting);

        verify(stub).updatePainting(argThat(r ->
                r.getId().equals("id")
                        && r.getTitle().equals("New")
                        && r.getArtistId().equals("a-2")
                        && r.getMuseumId().isEmpty()));
    }

    @Test
    void deletePaintingShouldCallStub() {
        client.deletePainting("id");
        verify(stub).deletePainting(any(DeletePaintingRequest.class));
    }
}