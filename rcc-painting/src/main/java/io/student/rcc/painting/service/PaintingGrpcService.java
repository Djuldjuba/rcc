package io.student.rcc.painting.service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.student.rcc.painting.domain.CreatePaintingRequest;
import io.student.rcc.painting.domain.PaintingResponse;
import io.student.rcc.painting.domain.UpdatePaintingRequest;
import io.student.rcc.painting.exception.ResourceNotFoundException;
import io.student.rcc.painting.grpc.*;
import io.student.rcc.painting.mapper.PaintingMapper;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

@GrpcService
public class PaintingGrpcService extends PaintingServiceGrpc.PaintingServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(PaintingGrpcService.class);

    private final PaintingService paintingService;
    private final PaintingMapper mapper;

    public PaintingGrpcService(PaintingService paintingService, PaintingMapper mapper) {
        this.paintingService = paintingService;
        this.mapper = mapper;
    }

    @Override
    public void getPainting(GetPaintingRequest request, StreamObserver<PaintingResponseProto> responseObserver) {
        try {
            log.debug("gRPC getPainting called with id: {}", request.getId());
            PaintingResponse response = paintingService.getPaintingById(request.getId());
            responseObserver.onNext(mapper.toGrpcResponse(response));
            responseObserver.onCompleted();
        } catch (ResourceNotFoundException e) {
            log.warn("Painting not found: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            failInternal(responseObserver, "getPainting", e);
        }
    }

    @Override
    public void getAllPaintings(EmptyRequest request, StreamObserver<PaintingListResponseProto> responseObserver) {
        try {
            log.debug("gRPC getAllPaintings called");
            var paintings = paintingService.getAllPaintings();
            PaintingListResponseProto response = mapper.toGrpcListResponse(paintings, 1, paintings.size());
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            failInternal(responseObserver, "getAllPaintings", e);
        }
    }

    @Override
    public void addPainting(CreatePaintingRequestProto request, StreamObserver<PaintingResponseProto> responseObserver) {
        try {
            log.debug("gRPC addPainting called with title: {}", request.getTitle());
            CreatePaintingRequest createRequest = new CreatePaintingRequest(
                    request.getTitle(),
                    request.getDescription(),
                    request.getContent(),
                    request.getArtistId(),
                    request.getMuseumId()
            );
            PaintingResponse response = paintingService.addPainting(createRequest);
            responseObserver.onNext(mapper.toGrpcResponse(response));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid argument: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            failInternal(responseObserver, "addPainting", e);
        }
    }

    @Override
    public void updatePainting(UpdatePaintingRequestProto request, StreamObserver<PaintingResponseProto> responseObserver) {
        try {
            log.debug("gRPC updatePainting called with id: {}", request.getId());
            UpdatePaintingRequest updateRequest = new UpdatePaintingRequest(
                    request.getTitle(),
                    request.getDescription(),
                    request.getContent(),
                    request.getArtistId(),
                    request.getMuseumId()
            );
            PaintingResponse response = paintingService.updatePainting(request.getId(), updateRequest);
            responseObserver.onNext(mapper.toGrpcResponse(response));
            responseObserver.onCompleted();
        } catch (ResourceNotFoundException e) {
            log.warn("Painting not found: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid argument: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            failInternal(responseObserver, "updatePainting", e);
        }
    }

    @Override
    public void deletePainting(DeletePaintingRequest request, StreamObserver<EmptyResponse> responseObserver) {
        try {
            log.debug("gRPC deletePainting called with id: {}", request.getId());
            paintingService.deletePainting(request.getId());
            responseObserver.onNext(EmptyResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (ResourceNotFoundException e) {
            log.warn("Painting not found: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            failInternal(responseObserver, "deletePainting", e);
        }
    }

    @Override
    public void searchPaintings(SearchRequest request, StreamObserver<PaintingListResponseProto> responseObserver) {
        try {
            log.debug("gRPC searchPaintings called with title: {}, page: {}, size: {}",
                    request.getTitle(), request.getPage(), request.getSize());

            String title = request.getTitle().isEmpty() ? null : request.getTitle();
            int page = request.getPage();
            int size = request.getSize() > 0 ? request.getSize() : 20;

            Page<PaintingResponse> pageResult = paintingService.getPaintingsPage(title, page, size);
            PaintingListResponseProto response = mapper.toGrpcListResponse(
                    pageResult.getContent(),
                    pageResult.getTotalPages(),
                    pageResult.getTotalElements()
            );
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            failInternal(responseObserver, "searchPaintings", e);
        }
    }

    @Override
    public void getPaintingsByArtist(GetPaintingsByArtistRequest request, StreamObserver<PaintingListResponseProto> responseObserver) {
        try {
            log.debug("gRPC getPaintingsByArtist called with artistId: {}, page: {}, size: {}",
                    request.getArtistId(), request.getPage(), request.getSize());

            int page = request.getPage();
            int size = request.getSize() > 0 ? request.getSize() : 20;

            Page<PaintingResponse> pageResult = paintingService.getPaintingsByArtist(
                    request.getArtistId(), page, size);
            PaintingListResponseProto response = mapper.toGrpcListResponse(
                    pageResult.getContent(),
                    pageResult.getTotalPages(),
                    pageResult.getTotalElements()
            );
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid artistId: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            failInternal(responseObserver, "getPaintingsByArtist", e);
        }
    }

    @Override
    public void getPaintingsByMuseum(GetPaintingsByMuseumRequest request, StreamObserver<PaintingListResponseProto> responseObserver) {
        try {
            log.debug("gRPC getPaintingsByMuseum called with museumId: {}, page: {}, size: {}",
                    request.getMuseumId(), request.getPage(), request.getSize());

            int page = request.getPage();
            int size = request.getSize() > 0 ? request.getSize() : 20;

            Page<PaintingResponse> pageResult = paintingService.getPaintingsByMuseum(
                    request.getMuseumId(), page, size);
            PaintingListResponseProto response = mapper.toGrpcListResponse(
                    pageResult.getContent(),
                    pageResult.getTotalPages(),
                    pageResult.getTotalElements()
            );
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid museumId: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            failInternal(responseObserver, "getPaintingsByMuseum", e);
        }
    }

    private void failInternal(StreamObserver<?> responseObserver, String operation, Exception e) {
        log.error("Error in {}", operation, e);
        responseObserver.onError(Status.INTERNAL
                .withDescription("Internal server error")
                .asRuntimeException());
    }
}