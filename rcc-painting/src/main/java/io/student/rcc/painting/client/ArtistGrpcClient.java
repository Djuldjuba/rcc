package io.student.rcc.painting.client;

import io.student.rcc.artist.grpc.ArtistResponseProto;
import io.student.rcc.artist.grpc.ArtistServiceGrpc;
import io.student.rcc.artist.grpc.GetArtistRequest;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ArtistGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(ArtistGrpcClient.class);

    @GrpcClient("artist")
    private ArtistServiceGrpc.ArtistServiceBlockingStub artistStub;

    public Optional<String> getArtistName(String artistId) {
        if (artistId == null || artistId.isBlank()) {
            return Optional.empty();
        }
        try {
            ArtistResponseProto response = artistStub.getArtist(
                    GetArtistRequest.newBuilder().setId(artistId).build()
            );
            return Optional.ofNullable(response.getName());
        } catch (Exception e) {
            log.warn("Failed to fetch artist name for id={}: {}", artistId, e.getMessage());
            return Optional.empty();
        }
    }
}