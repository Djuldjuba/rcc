package io.student.rcc.painting.client;

import io.student.rcc.museum.grpc.GetMuseumRequest;
import io.student.rcc.museum.grpc.MuseumResponseProto;
import io.student.rcc.museum.grpc.MuseumServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MuseumGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(MuseumGrpcClient.class);

    @GrpcClient("museum")
    private MuseumServiceGrpc.MuseumServiceBlockingStub museumStub;

    public Optional<String> getMuseumTitle(String museumId) {
        if (museumId == null || museumId.isBlank()) {
            return Optional.empty();
        }
        try {
            MuseumResponseProto response = museumStub.getMuseum(
                    GetMuseumRequest.newBuilder().setId(museumId).build()
            );
            return Optional.ofNullable(response.getTitle());
        } catch (Exception e) {
            log.warn("Failed to fetch museum title for id={}: {}", museumId, e.getMessage());
            return Optional.empty();
        }
    }
}