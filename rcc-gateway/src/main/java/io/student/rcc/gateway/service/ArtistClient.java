package io.student.rcc.gateway.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.student.rcc.artist.grpc.*;
import io.student.rcc.gateway.model.ArtistJson;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ArtistClient {

    @GrpcClient("artist-service")
    private ArtistServiceGrpc.ArtistServiceBlockingStub stub;

    public List<ArtistJson> getAllArtists() {
        return stub.getAllArtists(EmptyRequest.newBuilder().build())
                .getArtistsList().stream()
                .map(ArtistJson::fromGrpc)
                .toList();
    }

    public Map<String, Object> getArtistsPage(String name, int page, int size) {
        SearchRequest request = SearchRequest.newBuilder()
                .setName(name == null ? "" : name)
                .setPage(page)
                .setSize(size)
                .build();

        ArtistListResponseProto response = stub.searchArtists(request);

        return Map.of(
                "content", response.getArtistsList().stream()
                        .map(ArtistJson::fromGrpc)
                        .toList(),
                "totalPages", response.getTotalPages(),
                "totalElements", response.getTotalElements()
        );
    }

    public ArtistJson getArtistById(String id) {
        try {
            return ArtistJson.fromGrpc(
                    stub.getArtist(GetArtistRequest.newBuilder().setId(id).build())
            );
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }

    public ArtistJson createArtist(ArtistJson artist) {
        CreateArtistRequestProto request = CreateArtistRequestProto.newBuilder()
                .setName(nullToEmpty(artist.name()))
                .setBiography(nullToEmpty(artist.biography()))
                .setPhoto(nullToEmpty(artist.photo()))
                .build();

        return ArtistJson.fromGrpc(stub.addArtist(request));
    }

    public ArtistJson updateArtist(String id, ArtistJson artist) {
        UpdateArtistRequestProto.Builder builder = UpdateArtistRequestProto.newBuilder()
                .setId(id);

        if (artist.name() != null) builder.setName(artist.name());
        if (artist.biography() != null) builder.setBiography(artist.biography());
        if (artist.photo() != null) builder.setPhoto(artist.photo());

        return ArtistJson.fromGrpc(stub.updateArtist(builder.build()));
    }

    public void deleteArtist(String id) {
        stub.deleteArtist(DeleteArtistRequest.newBuilder().setId(id).build());
    }

    public ArtistJson patchArtist(String id, ArtistJson patch) {
        ArtistJson current = getArtistById(id);
        if (current == null) {
            throw new StatusRuntimeException(Status.NOT_FOUND.withDescription("Artist " + id + " not found"));
        }

        String name = patch.name() != null ? patch.name() : current.name();
        String biography = patch.biography() != null ? patch.biography() : current.biography();
        String photo = patch.photo() != null ? patch.photo() : current.photo();

        UpdateArtistRequestProto request = UpdateArtistRequestProto.newBuilder()
                .setId(id)
                .setName(nullToEmpty(name))
                .setBiography(nullToEmpty(biography))
                .setPhoto(nullToEmpty(photo))
                .build();

        return ArtistJson.fromGrpc(stub.updateArtist(request));
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}