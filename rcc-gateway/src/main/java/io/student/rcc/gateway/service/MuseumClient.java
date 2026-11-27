package io.student.rcc.gateway.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.student.rcc.gateway.model.MuseumJson;
import io.student.rcc.museum.grpc.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MuseumClient {

    @GrpcClient("museum-service")
    private MuseumServiceGrpc.MuseumServiceBlockingStub stub;

    public List<MuseumJson> getAllMuseums() {
        return stub.getAllMuseums(EmptyRequest.newBuilder().build())
                .getMuseumsList().stream()
                .map(MuseumJson::fromGrpc)
                .toList();
    }

    public Map<String, Object> getMuseumsPage(int page, int size) {
        return getMuseumsPage(null, page, size);
    }

    public Map<String, Object> getMuseumsPage(String title, int page, int size) {
        SearchRequest request = SearchRequest.newBuilder()
                .setTitle(title == null ? "" : title)
                .setPage(page)
                .setSize(size)
                .build();

        MuseumListResponseProto response = stub.searchMuseums(request);

        return Map.of(
                "content", response.getMuseumsList().stream()
                        .map(MuseumJson::fromGrpc)
                        .toList(),
                "totalPages", response.getTotalPages(),
                "totalElements", response.getTotalElements()
        );
    }

    public MuseumJson getMuseumById(String id) {
        try {
            return MuseumJson.fromGrpc(
                    stub.getMuseum(GetMuseumRequest.newBuilder().setId(id).build())
            );
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }

    public MuseumJson createMuseum(MuseumJson museum) {
        String city = museum.geo() != null ? museum.geo().city() : null;
        String countryId = (museum.geo() != null && museum.geo().country() != null)
                ? museum.geo().country().id()
                : null;

        CreateMuseumRequestProto request = CreateMuseumRequestProto.newBuilder()
                .setTitle(nullToEmpty(museum.title()))
                .setDescription(nullToEmpty(museum.description()))
                .setPhoto(nullToEmpty(museum.photo()))
                .setCity(nullToEmpty(city))
                .setCountryId(nullToEmpty(countryId))
                .build();

        return MuseumJson.fromGrpc(stub.createMuseum(request));
    }

    public MuseumJson updateMuseum(String id, MuseumJson museum) {
        String city = museum.geo() != null ? museum.geo().city() : null;
        String countryId = (museum.geo() != null && museum.geo().country() != null)
                ? museum.geo().country().id()
                : null;

        UpdateMuseumRequestProto.Builder builder = UpdateMuseumRequestProto.newBuilder()
                .setId(id);

        if (museum.title() != null) builder.setTitle(museum.title());
        if (museum.description() != null) builder.setDescription(museum.description());
        if (museum.photo() != null) builder.setPhoto(museum.photo());
        if (city != null) builder.setCity(city);
        if (countryId != null && !countryId.isBlank()) builder.setCountryId(countryId);

        return MuseumJson.fromGrpc(stub.updateMuseum(builder.build()));
    }

    public void deleteMuseum(String id) {
        stub.deleteMuseum(DeleteMuseumRequest.newBuilder().setId(id).build());
    }

    public MuseumJson patchMuseum(String id, MuseumJson patch) {
        MuseumJson current = getMuseumById(id);
        if (current == null) {
            throw new StatusRuntimeException(Status.NOT_FOUND.withDescription("Museum " + id + " not found"));
        }

        String title = patch.title() != null ? patch.title() : current.title();
        String description = patch.description() != null ? patch.description() : current.description();
        String photo = patch.photo() != null ? patch.photo() : current.photo();

        String city;
        String countryId;
        if (patch.geo() == null) {
            city = current.geo() != null ? current.geo().city() : null;
            countryId = (current.geo() != null && current.geo().country() != null)
                    ? current.geo().country().id()
                    : null;
        } else {
            city = patch.geo().city() != null
                    ? patch.geo().city()
                    : (current.geo() != null ? current.geo().city() : null);

            if (patch.geo().country() != null && patch.geo().country().id() != null) {
                countryId = patch.geo().country().id();
            } else {
                countryId = (current.geo() != null && current.geo().country() != null)
                        ? current.geo().country().id()
                        : null;
            }
        }

        UpdateMuseumRequestProto request = UpdateMuseumRequestProto.newBuilder()
                .setId(id)
                .setTitle(nullToEmpty(title))
                .setDescription(nullToEmpty(description))
                .setPhoto(nullToEmpty(photo))
                .setCity(nullToEmpty(city))
                .setCountryId(nullToEmpty(countryId))
                .build();

        return MuseumJson.fromGrpc(stub.updateMuseum(request));
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}