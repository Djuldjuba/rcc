package io.student.rcc.gateway.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.student.rcc.artist.grpc.*;
import io.student.rcc.gateway.model.ArtistJson;
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
class ArtistClientTest {

    @Mock
    private ArtistServiceGrpc.ArtistServiceBlockingStub stub;

    private ArtistClient client;

    @BeforeEach
    void setUp() {
        client = new ArtistClient();
        ReflectionTestUtils.setField(client, "stub", stub);
    }

    @Test
    void getAllArtistsShouldMap() {
        ArtistResponseProto proto = ArtistResponseProto.newBuilder()
                .setId("id-1").setName("Picasso").setBiography("bio").build();
        when(stub.getAllArtists(any(EmptyRequest.class)))
                .thenReturn(ArtistListResponseProto.newBuilder().addArtists(proto).build());

        List<ArtistJson> result = client.getAllArtists();

        assertEquals(1, result.size());
        assertEquals("Picasso", result.get(0).name());
    }

    @Test
    void getArtistsPageShouldReturnMap() {
        ArtistResponseProto proto = ArtistResponseProto.newBuilder()
                .setId("id-1").setName("Picasso").build();
        when(stub.searchArtists(any(SearchRequest.class)))
                .thenReturn(ArtistListResponseProto.newBuilder()
                        .addArtists(proto).setTotalPages(1).setTotalElements(1).build());

        Map<String, Object> result = client.getArtistsPage("pic", 0, 10);

        assertEquals(1, ((List<?>) result.get("content")).size());
        assertEquals(1, result.get("totalPages"));
        assertEquals(1L, result.get("totalElements"));
    }

    @Test
    void getArtistByIdShouldReturnNullOnNotFound() {
        when(stub.getArtist(any(GetArtistRequest.class)))
                .thenThrow(new StatusRuntimeException(Status.NOT_FOUND));

        assertNull(client.getArtistById("missing"));
    }

    @Test
    void getArtistByIdShouldRethrowOtherErrors() {
        when(stub.getArtist(any(GetArtistRequest.class)))
                .thenThrow(new StatusRuntimeException(Status.INTERNAL));

        assertThrows(StatusRuntimeException.class, () -> client.getArtistById("x"));
    }

    @Test
    void createArtistShouldMap() {
        when(stub.addArtist(any(CreateArtistRequestProto.class)))
                .thenReturn(ArtistResponseProto.newBuilder().setId("new").setName("Dali").build());

        ArtistJson result = client.createArtist(new ArtistJson(null, "Dali", null, null));

        assertEquals("new", result.id());
        assertEquals("Dali", result.name());
    }

    @Test
    void updateArtistShouldSetOnlyNonNullFields() {
        when(stub.updateArtist(any(UpdateArtistRequestProto.class)))
                .thenReturn(ArtistResponseProto.newBuilder().setId("id").setName("New").build());

        ArtistJson result = client.updateArtist("id", new ArtistJson(null, "New", null, null));

        assertEquals("New", result.name());
        verify(stub).updateArtist(argThat(r ->
                r.getId().equals("id")
                        && r.getName().equals("New")
                        && r.getBiography().isEmpty()
                        && r.getPhoto().isEmpty()));
    }

    @Test
    void deleteArtistShouldCallStub() {
        client.deleteArtist("id");
        verify(stub).deleteArtist(any(DeleteArtistRequest.class));
    }
}