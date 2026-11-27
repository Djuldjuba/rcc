package io.student.rcc.gateway.service;

import io.student.rcc.gateway.model.CountryJson;
import io.student.rcc.geo.proto.CountryProto;
import io.student.rcc.geo.proto.CountryServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CountryClient {

    @GrpcClient("geo-service")
    private CountryServiceGrpc.CountryServiceBlockingStub stub;

    public Map<String, Object> getCountriesPage(int page, int size, String title) {
        CountryProto.GetAllCountriesRequest request = CountryProto.GetAllCountriesRequest.newBuilder()
                .setName(title == null ? "" : title)
                .setPage(page)
                .setSize(size)
                .build();

        CountryProto.GetAllCountriesResponse response = stub.getAllCountries(request);

        return Map.of(
                "content", response.getCountriesList().stream()
                        .map(CountryJson::fromGrpc)
                        .toList(),
                "totalPages", response.getTotalPages(),
                "totalElements", response.getTotalElements(),
                "page", response.getPage(),
                "size", response.getSize()
        );
    }

    public CountryJson getCountryById(String id) {
        return CountryJson.fromGrpc(
                stub.getCountryById(
                        CountryProto.GetCountryByIdRequest.newBuilder().setId(id).build()
                )
        );
    }

    public CountryJson getCountryByName(String name) {
        return CountryJson.fromGrpc(
                stub.getCountryByName(
                        CountryProto.GetCountryByNameRequest.newBuilder().setName(name).build()
                )
        );
    }
}