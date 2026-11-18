package io.student.rcc.artist.service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.student.rcc.artist.domain.ArtistResponse;
import io.student.rcc.artist.domain.CreateArtistRequest;
import io.student.rcc.artist.domain.UpdateArtistRequest;
import io.student.rcc.artist.exception.ResourceNotFoundException;
import io.student.rcc.artist.grpc.*;
import io.student.rcc.artist.mapper.ArtistMapper;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;

@GrpcService
public class ArtistGrpcService extends ArtistServiceGrpc.ArtistServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(ArtistGrpcService.class);

    private final ArtistService artistService;
    private final ArtistMapper mapper;

    public ArtistGrpcService(ArtistService artistService, ArtistMapper mapper) {
        this.artistService = artistService;
        this.mapper = mapper;
    }

    @Override
    public void getArtist(GetArtistRequest request, StreamObserver<ArtistResponseProto> responseObserver) {
        try {
            log.debug("gRPC getArtist called with id: {}", request.getId());
            ArtistResponse response = artistService.getArtistById(request.getId());
            responseObserver.onNext(mapper.toGrpcResponse(response));
            responseObserver.onCompleted();
        } catch (ResourceNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            failInternal(responseObserver, "getArtist", e);
        }
    }

    @Override
    public void getAllArtists(EmptyRequest request, StreamObserver<ArtistListResponseProto> responseObserver) {
        try {
            log.debug("gRPC getAllArtists called");
            var artists = artistService.getAllArtists();
            responseObserver.onNext(mapper.toGrpcListResponse(artists, 1, artists.size()));
            responseObserver.onCompleted();
        } catch (Exception e) {
            failInternal(responseObserver, "getAllArtists", e);
        }
    }

    @Override
    public void addArtist(CreateArtistRequestProto request, StreamObserver<ArtistResponseProto> responseObserver) {
        try {
            log.debug("gRPC addArtist called with name: {}", request.getName());
            CreateArtistRequest createRequest = new CreateArtistRequest(
                    request.getName(),
                    request.getBiography(),
                    request.getPhoto()
            );
            ArtistResponse response = artistService.createArtist(createRequest);
            responseObserver.onNext(mapper.toGrpcResponse(response));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            failInternal(responseObserver, "addArtist", e);
        }
    }

    @Override
    public void updateArtist(UpdateArtistRequestProto request, StreamObserver<ArtistResponseProto> responseObserver) {
        try {
            log.debug("gRPC updateArtist called with id: {}", request.getId());
            UpdateArtistRequest updateRequest = new UpdateArtistRequest(
                    request.getName(),
                    request.getBiography(),
                    request.getPhoto()
            );
            ArtistResponse response = artistService.updateArtist(request.getId(), updateRequest);
            responseObserver.onNext(mapper.toGrpcResponse(response));
            responseObserver.onCompleted();
        } catch (ResourceNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            failInternal(responseObserver, "updateArtist", e);
        }
    }

    @Override
    public void deleteArtist(DeleteArtistRequest request, StreamObserver<EmptyResponse> responseObserver) {
        try {
            log.debug("gRPC deleteArtist called with id: {}", request.getId());
            artistService.deleteArtist(request.getId());
            responseObserver.onNext(EmptyResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (ResourceNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            failInternal(responseObserver, "deleteArtist", e);
        }
    }

    @Override
    public void searchArtists(SearchRequest request, StreamObserver<ArtistListResponseProto> responseObserver) {
        try {
            log.debug("gRPC searchArtists called with name: {}, page: {}, size: {}",
                    request.getName(), request.getPage(), request.getSize());

            String name = StringUtils.hasText(request.getName()) ? request.getName() : null;
            int page = request.getPage();
            int size = request.getSize() > 0 ? request.getSize() : 20;

            Page<ArtistResponse> pageResult = artistService.getArtistsPage(name, page, size);
            ArtistListResponseProto response = mapper.toGrpcListResponse(
                    pageResult.getContent(),
                    pageResult.getTotalPages(),
                    pageResult.getTotalElements()
            );
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            failInternal(responseObserver, "searchArtists", e);
        }
    }

    private void failInternal(StreamObserver<?> responseObserver, String operation, Exception e) {
        log.error("Error in {}", operation, e);
        responseObserver.onError(Status.INTERNAL
                .withDescription("Internal server error")
                .asRuntimeException());
    }
}