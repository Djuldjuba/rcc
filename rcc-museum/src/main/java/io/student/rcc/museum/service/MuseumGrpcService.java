package io.student.rcc.museum.service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.student.rcc.museum.domain.CreateMuseumRequest;
import io.student.rcc.museum.domain.MuseumResponse;
import io.student.rcc.museum.domain.UpdateMuseumRequest;
import io.student.rcc.museum.exception.ResourceNotFoundException;
import io.student.rcc.museum.grpc.*;
import io.student.rcc.museum.mapper.MuseumMapper;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

@GrpcService
public class MuseumGrpcService extends MuseumServiceGrpc.MuseumServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(MuseumGrpcService.class);

    private final MuseumService museumService;
    private final MuseumMapper mapper;

    public MuseumGrpcService(MuseumService museumService, MuseumMapper mapper) {
        this.museumService = museumService;
        this.mapper = mapper;
    }

    @Override
    public void getMuseum(GetMuseumRequest request, StreamObserver<MuseumResponseProto> responseObserver) {
        try {
            log.debug("gRPC getMuseum called with id: {}", request.getId());
            MuseumResponse response = museumService.getMuseumById(request.getId());
            responseObserver.onNext(mapper.toGrpcResponse(response));
            responseObserver.onCompleted();
        } catch (ResourceNotFoundException e) {
            log.warn("Museum not found: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            failInternal(responseObserver, "getMuseum", e);
        }
    }

    @Override
    public void getAllMuseums(EmptyRequest request, StreamObserver<MuseumListResponseProto> responseObserver) {
        try {
            log.debug("gRPC getAllMuseums called");
            var museums = museumService.getAllMuseums();
            MuseumListResponseProto response = mapper.toGrpcListResponse(museums, 1, museums.size());
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            failInternal(responseObserver, "getAllMuseums", e);
        }
    }

    @Override
    public void createMuseum(CreateMuseumRequestProto request, StreamObserver<MuseumResponseProto> responseObserver) {
        try {
            log.debug("gRPC createMuseum called with title: {}", request.getTitle());

            CreateMuseumRequest.GeoWrapper geo = new CreateMuseumRequest.GeoWrapper(
                    request.getCity(),
                    new CreateMuseumRequest.CountryRef(request.getCountryId())
            );

            CreateMuseumRequest createRequest = new CreateMuseumRequest(
                    request.getTitle(),
                    request.getDescription(),
                    request.getPhoto(),
                    geo
            );
            MuseumResponse response = museumService.createMuseum(createRequest);
            responseObserver.onNext(mapper.toGrpcResponse(response));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid argument: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            failInternal(responseObserver, "createMuseum", e);
        }
    }

    @Override
    public void updateMuseum(UpdateMuseumRequestProto request, StreamObserver<MuseumResponseProto> responseObserver) {
        try {
            log.debug("gRPC updateMuseum called with id: {}", request.getId());

            UpdateMuseumRequest.GeoWrapper geo = new UpdateMuseumRequest.GeoWrapper(
                    request.getCity(),
                    new UpdateMuseumRequest.CountryRef(request.getCountryId())
            );

            UpdateMuseumRequest updateRequest = new UpdateMuseumRequest(
                    request.getTitle(),
                    request.getDescription(),
                    request.getPhoto(),
                    geo
            );
            MuseumResponse response = museumService.updateMuseum(request.getId(), updateRequest);
            responseObserver.onNext(mapper.toGrpcResponse(response));
            responseObserver.onCompleted();
        } catch (ResourceNotFoundException e) {
            log.warn("Museum not found: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid argument: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            failInternal(responseObserver, "updateMuseum", e);
        }
    }

    @Override
    public void deleteMuseum(DeleteMuseumRequest request, StreamObserver<EmptyResponse> responseObserver) {
        try {
            log.debug("gRPC deleteMuseum called with id: {}", request.getId());
            museumService.deleteMuseum(request.getId());
            responseObserver.onNext(EmptyResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (ResourceNotFoundException e) {
            log.warn("Museum not found: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            failInternal(responseObserver, "deleteMuseum", e);
        }
    }

    @Override
    public void searchMuseums(SearchRequest request, StreamObserver<MuseumListResponseProto> responseObserver) {
        try {
            log.debug("gRPC searchMuseums called with title: {}, page: {}, size: {}",
                    request.getTitle(), request.getPage(), request.getSize());

            String title = request.getTitle().isEmpty() ? null : request.getTitle();
            int page = request.getPage();
            int size = request.getSize() > 0 ? request.getSize() : 20;

            Page<MuseumResponse> pageResult = museumService.getMuseumsPage(title, page, size);
            MuseumListResponseProto response = mapper.toGrpcListResponse(
                    pageResult.getContent(),
                    pageResult.getTotalPages(),
                    pageResult.getTotalElements()
            );
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            failInternal(responseObserver, "searchMuseums", e);
        }
    }

    @Override
    public void getMuseumsByCountry(GetMuseumsByCountryRequest request, StreamObserver<MuseumListResponseProto> responseObserver) {
        try {
            log.debug("gRPC getMuseumsByCountry called with countryId: {}, page: {}, size: {}",
                    request.getCountryId(), request.getPage(), request.getSize());

            int page = request.getPage();
            int size = request.getSize() > 0 ? request.getSize() : 20;

            Page<MuseumResponse> pageResult = museumService.getMuseumsByCountry(
                    request.getCountryId(), page, size);
            MuseumListResponseProto response = mapper.toGrpcListResponse(
                    pageResult.getContent(),
                    pageResult.getTotalPages(),
                    pageResult.getTotalElements()
            );
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid countryId: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            failInternal(responseObserver, "getMuseumsByCountry", e);
        }
    }

    private void failInternal(StreamObserver<?> responseObserver, String operation, Exception e) {
        log.error("Error in {}", operation, e);
        responseObserver.onError(Status.INTERNAL
                .withDescription("Internal server error")
                .asRuntimeException());
    }
}