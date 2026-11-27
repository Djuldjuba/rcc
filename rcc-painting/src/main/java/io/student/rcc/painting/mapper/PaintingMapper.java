package io.student.rcc.painting.mapper;

import io.student.rcc.painting.data.entity.PaintingEntity;
import io.student.rcc.painting.domain.PaintingResponse;
import io.student.rcc.painting.domain.CreatePaintingRequest;
import io.student.rcc.painting.domain.UpdatePaintingRequest;
import io.student.rcc.painting.grpc.PaintingListResponseProto;
import io.student.rcc.painting.grpc.PaintingResponseProto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PaintingMapper {

    public PaintingResponse toResponse(PaintingEntity entity, String artistName, String museumTitle) {
        return new PaintingResponse(
                entity.getId().toString(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getContentAsString(),
                entity.getArtistId().toString(),
                artistName,
                entity.getMuseumId() != null ? entity.getMuseumId().toString() : null,
                museumTitle
        );
    }

    public PaintingResponse toResponse(PaintingEntity entity) {
        return new PaintingResponse(
                entity.getId().toString(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getContentAsString(),
                entity.getArtistId().toString(),
                null,
                entity.getMuseumId() != null ? entity.getMuseumId().toString() : null,
                null
        );
    }

    public PaintingEntity toEntity(CreatePaintingRequest request) {
        PaintingEntity entity = new PaintingEntity();
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setContentFromString(request.content());
        entity.setArtistId(UUID.fromString(request.artistId()));
        if (request.museumId() != null && !request.museumId().isBlank()) {
            entity.setMuseumId(UUID.fromString(request.museumId()));
        }
        return entity;
    }

    public void updateEntity(PaintingEntity entity, UpdatePaintingRequest request) {
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setContentFromString(request.content());
        entity.setArtistId(UUID.fromString(request.artistId()));
        if (request.museumId() != null && !request.museumId().isBlank()) {
            entity.setMuseumId(UUID.fromString(request.museumId()));
        } else {
            entity.setMuseumId(null);
        }
    }

    // gRPC mappings
    public PaintingResponseProto toGrpcResponse(PaintingResponse response) {
        return PaintingResponseProto.newBuilder()
                .setId(response.id())
                .setTitle(response.title())
                .setDescription(response.description() != null ? response.description() : "")
                .setContent(response.content() != null ? response.content() : "")
                .setArtistId(response.artistId())
                .setArtistName(response.artistName() != null ? response.artistName() : "")
                .setMuseumId(response.museumId() != null ? response.museumId() : "")
                .setMuseumTitle(response.museumTitle() != null ? response.museumTitle() : "")
                .build();
    }

    public PaintingListResponseProto toGrpcListResponse(List<PaintingResponse> responses, int totalPages, long totalElements) {
        List<PaintingResponseProto> protoList = responses.stream()
                .map(this::toGrpcResponse)
                .toList();
        return PaintingListResponseProto.newBuilder()
                .addAllPaintings(protoList)
                .setTotalPages(totalPages)
                .setTotalElements(totalElements)
                .build();
    }
}