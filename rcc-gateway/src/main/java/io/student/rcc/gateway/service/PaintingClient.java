package io.student.rcc.gateway.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.student.rcc.gateway.model.PaintingJson;
import io.student.rcc.painting.grpc.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PaintingClient {

    @GrpcClient("painting-service")
    private PaintingServiceGrpc.PaintingServiceBlockingStub stub;

    public Map<String, Object> getPaintingsPage(int page, int size) {
        SearchRequest request = SearchRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .build();

        PaintingListResponseProto response = stub.searchPaintings(request);

        return Map.of(
                "content", response.getPaintingsList().stream()
                        .map(PaintingJson::fromGrpc)
                        .toList(),
                "totalPages", response.getTotalPages(),
                "totalElements", response.getTotalElements()
        );
    }

    public Map<String, Object> getPaintingsByArtist(String artistId, int page, int size) {
        GetPaintingsByArtistRequest request = GetPaintingsByArtistRequest.newBuilder()
                .setArtistId(artistId)
                .setPage(page)
                .setSize(size)
                .build();

        PaintingListResponseProto response = stub.getPaintingsByArtist(request);

        return Map.of(
                "content", response.getPaintingsList().stream()
                        .map(PaintingJson::fromGrpc)
                        .toList(),
                "totalPages", response.getTotalPages(),
                "totalElements", response.getTotalElements()
        );
    }

    public Map<String, Object> getPaintingsByMuseum(String museumId, int page, int size) {
        GetPaintingsByMuseumRequest request = GetPaintingsByMuseumRequest.newBuilder()
                .setMuseumId(museumId)
                .setPage(page)
                .setSize(size)
                .build();

        PaintingListResponseProto response = stub.getPaintingsByMuseum(request);

        return Map.of(
                "content", response.getPaintingsList().stream()
                        .map(PaintingJson::fromGrpc)
                        .toList(),
                "totalPages", response.getTotalPages(),
                "totalElements", response.getTotalElements()
        );
    }

    public PaintingJson getPaintingById(String id) {
        try {
            return PaintingJson.fromGrpc(
                    stub.getPainting(GetPaintingRequest.newBuilder().setId(id).build())
            );
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }

    public PaintingJson addPainting(PaintingJson painting) {
        CreatePaintingRequestProto request = CreatePaintingRequestProto.newBuilder()
                .setTitle(nullToEmpty(painting.title()))
                .setDescription(nullToEmpty(painting.description()))
                .setContent(nullToEmpty(painting.content()))
                .setArtistId(painting.artist() != null ? nullToEmpty(painting.artist().id()) : "")
                .setMuseumId(painting.museum() != null ? nullToEmpty(painting.museum().id()) : "")
                .build();

        return PaintingJson.fromGrpc(stub.addPainting(request));
    }

    public PaintingJson updatePainting(String id, PaintingJson painting) {
        UpdatePaintingRequestProto.Builder builder = UpdatePaintingRequestProto.newBuilder()
                .setId(id);

        if (painting.title() != null) {
            builder.setTitle(painting.title());
        }
        if (painting.description() != null) {
            builder.setDescription(painting.description());
        }
        if (painting.content() != null) {
            builder.setContent(painting.content());
        }
        if (painting.artist() != null && painting.artist().id() != null) {
            builder.setArtistId(painting.artist().id());
        }
        if (painting.museum() != null && painting.museum().id() != null) {
            builder.setMuseumId(painting.museum().id());
        }

        return PaintingJson.fromGrpc(stub.updatePainting(builder.build()));
    }

    public void deletePainting(String id) {
        stub.deletePainting(DeletePaintingRequest.newBuilder().setId(id).build());
    }

    public PaintingJson patchPainting(String id, PaintingJson patch) {
        PaintingJson current = getPaintingById(id);
        if (current == null) {
            throw new StatusRuntimeException(Status.NOT_FOUND.withDescription("Painting " + id + " not found"));
        }

        String title = patch.title() != null ? patch.title() : current.title();
        String description = patch.description() != null ? patch.description() : current.description();
        String content = patch.content() != null ? patch.content() : current.content();

        String artistId;
        if (patch.artist() != null && patch.artist().id() != null) {
            artistId = patch.artist().id();
        } else {
            artistId = current.artist() != null ? current.artist().id() : null;
        }

        String museumId;
        if (patch.museum() != null && patch.museum().id() != null) {
            museumId = patch.museum().id();
        } else {
            museumId = current.museum() != null ? current.museum().id() : null;
        }

        UpdatePaintingRequestProto request = UpdatePaintingRequestProto.newBuilder()
                .setId(id)
                .setTitle(nullToEmpty(title))
                .setDescription(nullToEmpty(description))
                .setContent(nullToEmpty(content))
                .setArtistId(nullToEmpty(artistId))
                .setMuseumId(nullToEmpty(museumId))
                .build();

        return PaintingJson.fromGrpc(stub.updatePainting(request));
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}