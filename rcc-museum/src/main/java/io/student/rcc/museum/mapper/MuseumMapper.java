package io.student.rcc.museum.mapper;

import io.student.rcc.museum.data.entity.MuseumEntity;
import io.student.rcc.museum.domain.MuseumResponse;
import io.student.rcc.museum.domain.CreateMuseumRequest;
import io.student.rcc.museum.domain.UpdateMuseumRequest;
import io.student.rcc.museum.grpc.MuseumListResponseProto;
import io.student.rcc.museum.grpc.MuseumResponseProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class MuseumMapper {

    private static final Logger log = LoggerFactory.getLogger(MuseumMapper.class);

    public MuseumResponse toResponse(MuseumEntity entity, String countryName) {
        return new MuseumResponse(
                entity.getId().toString(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPhotoAsString(),
                entity.getCity(),
                entity.getCountryId().toString(),
                countryName
        );
    }

    public MuseumResponse toResponse(MuseumEntity entity) {
        return new MuseumResponse(
                entity.getId().toString(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPhotoAsString(),
                entity.getCity(),
                entity.getCountryId().toString(),
                null
        );
    }

    public MuseumEntity toEntity(CreateMuseumRequest request) {
        MuseumEntity entity = new MuseumEntity();
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setPhotoFromString(request.photo());

        String city = request.getCity();
        log.info("Creating museum: title={}, city={}, countryId={}",
                request.title(), city, request.getCountryId());

        entity.setCity(city);

        String countryId = request.getCountryId();
        if (countryId == null) {
            throw new IllegalArgumentException("Country ID is required");
        }
        entity.setCountryId(UUID.fromString(countryId));
        return entity;
    }

    public void updateEntity(MuseumEntity entity, UpdateMuseumRequest request) {
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setPhotoFromString(request.photo());

        String city = request.getCity();
        log.info("Updating museum: title={}, city={}, countryId={}",
                request.title(), city, request.getCountryId());

        entity.setCity(city);

        String countryId = request.getCountryId();
        if (countryId != null) {
            entity.setCountryId(UUID.fromString(countryId));
        }
    }

    public MuseumResponseProto toGrpcResponse(MuseumResponse response) {
        return MuseumResponseProto.newBuilder()
                .setId(response.id())
                .setTitle(response.title())
                .setDescription(response.description() != null ? response.description() : "")
                .setPhoto(response.photo() != null ? response.photo() : "")
                .setCity(response.city() != null ? response.city() : "")
                .setCountryId(response.countryId())
                .setCountryName(response.countryName() != null ? response.countryName() : "")
                .build();
    }

    public MuseumListResponseProto toGrpcListResponse(List<MuseumResponse> responses, int totalPages, long totalElements) {
        List<MuseumResponseProto> protoList = responses.stream()
                .map(this::toGrpcResponse)
                .toList();
        return MuseumListResponseProto.newBuilder()
                .addAllMuseums(protoList)
                .setTotalPages(totalPages)
                .setTotalElements(totalElements)
                .build();
    }
}