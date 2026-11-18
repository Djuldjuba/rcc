package io.student.rcc.museum.client;

import io.student.rcc.geo.proto.CountryProto;
import io.student.rcc.geo.proto.CountryServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CountryGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(CountryGrpcClient.class);

    @GrpcClient("country")
    private CountryServiceGrpc.CountryServiceBlockingStub countryStub;

    public Optional<String> getCountryName(String countryId) {
        if (countryId == null || countryId.isBlank()) {
            return Optional.empty();
        }
        try {
            CountryProto.Country response = countryStub.getCountryById(
                    CountryProto.GetCountryByIdRequest.newBuilder()
                            .setId(countryId)
                            .build()
            );
            return Optional.ofNullable(response.getName());
        } catch (Exception e) {
            log.warn("Failed to fetch country name for id={}: {}", countryId, e.getMessage());
            return Optional.empty();
        }
    }
}