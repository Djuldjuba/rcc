package io.student.rcc.gateway.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.student.rcc.gateway.model.MuseumJson;
import io.student.rcc.museum.grpc.*;
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
class MuseumClientTest {

    @Mock
    private MuseumServiceGrpc.MuseumServiceBlockingStub stub;

    private MuseumClient client;

    @BeforeEach
    void setUp() {
        client = new MuseumClient();
        ReflectionTestUtils.setField(client, "stub", stub);
    }

    @Test
    void getAllMuseumsShouldMap() {
        MuseumResponseProto proto = MuseumResponseProto.newBuilder()
                .setId("id-1").setTitle("Эрмитаж").setCity("СПб").build();
        when(stub.getAllMuseums(any(EmptyRequest.class)))
                .thenReturn(MuseumListResponseProto.newBuilder().addMuseums(proto).build());

        List<MuseumJson> result = client.getAllMuseums();

        assertEquals(1, result.size());
        assertEquals("Эрмитаж", result.get(0).title());
        assertEquals("СПб", result.get(0).geo().city());
    }

    @Test
    void getMuseumsPageShouldReturnMap() {
        MuseumResponseProto proto = MuseumResponseProto.newBuilder()
                .setId("id-1").setTitle("Эрмитаж").build();
        when(stub.searchMuseums(any(SearchRequest.class)))
                .thenReturn(MuseumListResponseProto.newBuilder()
                        .addMuseums(proto).setTotalPages(2).setTotalElements(5).build());

        Map<String, Object> result = client.getMuseumsPage("эрм", 0, 10);

        assertEquals(1, ((List<?>) result.get("content")).size());
        assertEquals(2, result.get("totalPages"));
        assertEquals(5L, result.get("totalElements"));
    }

    @Test
    void getMuseumsPageWithoutTitleShouldDelegateToSearch() {
        when(stub.searchMuseums(any(SearchRequest.class)))
                .thenReturn(MuseumListResponseProto.getDefaultInstance());

        client.getMuseumsPage(0, 20);

        verify(stub).searchMuseums(argThat(r -> r.getTitle().isEmpty()));
    }

    @Test
    void getMuseumByIdShouldReturnNullOnNotFound() {
        when(stub.getMuseum(any(GetMuseumRequest.class)))
                .thenThrow(new StatusRuntimeException(Status.NOT_FOUND));

        assertNull(client.getMuseumById("missing"));
    }

    @Test
    void createMuseumShouldExtractGeo() {
        when(stub.createMuseum(any(CreateMuseumRequestProto.class)))
                .thenReturn(MuseumResponseProto.newBuilder().setId("new").setTitle("T").build());

        MuseumJson request = new MuseumJson(
                null, "T", null, null,
                new MuseumJson.Geo("Москва", new MuseumJson.Country("c-1", null))
        );

        MuseumJson result = client.createMuseum(request);

        assertEquals("new", result.id());
        verify(stub).createMuseum(argThat(r ->
                r.getCity().equals("Москва") && r.getCountryId().equals("c-1")));
    }

    @Test
    void updateMuseumShouldSetOnlyProvidedFields() {
        when(stub.updateMuseum(any(UpdateMuseumRequestProto.class)))
                .thenReturn(MuseumResponseProto.newBuilder().setId("id").build());

        MuseumJson request = new MuseumJson(
                null, "New", null, null,
                new MuseumJson.Geo("СПб", new MuseumJson.Country("c-2", null))
        );

        client.updateMuseum("id", request);

        verify(stub).updateMuseum(argThat(r ->
                r.getId().equals("id")
                        && r.getTitle().equals("New")
                        && r.getCity().equals("СПб")
                        && r.getCountryId().equals("c-2")));
    }

    @Test
    void deleteMuseumShouldCallStub() {
        client.deleteMuseum("id");
        verify(stub).deleteMuseum(any(DeleteMuseumRequest.class));
    }
}