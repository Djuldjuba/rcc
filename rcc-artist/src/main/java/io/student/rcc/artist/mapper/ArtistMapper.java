package io.student.rcc.artist.mapper;

import io.student.rcc.artist.data.entity.ArtistEntity;
import io.student.rcc.artist.domain.ArtistResponse;
import io.student.rcc.artist.domain.CreateArtistRequest;
import io.student.rcc.artist.domain.UpdateArtistRequest;
import io.student.rcc.artist.grpc.ArtistListResponseProto;
import io.student.rcc.artist.grpc.ArtistResponseProto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ArtistMapper {

    public ArtistResponse toResponse(ArtistEntity entity) {
        return new ArtistResponse(
                entity.getId().toString(),
                entity.getName(),
                entity.getBiography(),
                entity.getPhotoAsString()
        );
    }

    public ArtistEntity toEntity(CreateArtistRequest request) {
        ArtistEntity entity = new ArtistEntity();
        entity.setName(request.name());
        entity.setBiography(request.biography());
        entity.setPhotoFromString(request.photo());
        return entity;
    }

    public void updateEntity(ArtistEntity entity, UpdateArtistRequest request) {
        entity.setName(request.name());
        entity.setBiography(request.biography());
        entity.setPhotoFromString(request.photo());
    }

    public ArtistResponseProto toGrpcResponse(ArtistResponse response) {
        return ArtistResponseProto.newBuilder()
                .setId(response.id())
                .setName(response.name())
                .setBiography(response.biography())
                .setPhoto(Objects.requireNonNullElse(response.photo(), ""))
                .build();
    }

    public ArtistListResponseProto toGrpcListResponse(List<ArtistResponse> responses, int totalPages, long totalElements) {
        List<ArtistResponseProto> protoList = responses.stream()
                .map(this::toGrpcResponse)
                .toList();
        return ArtistListResponseProto.newBuilder()
                .addAllArtists(protoList)
                .setTotalPages(totalPages)
                .setTotalElements(totalElements)
                .build();
    }
}