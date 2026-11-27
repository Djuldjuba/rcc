package io.student.rcc.geo.service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.student.rcc.geo.exception.CountryNotFoundException;
import io.student.rcc.geo.model.CountryJson;
import io.student.rcc.geo.proto.CountryProto;
import io.student.rcc.geo.proto.CountryServiceGrpc;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@GrpcService
public class CountryGrpcService extends CountryServiceGrpc.CountryServiceImplBase {

    private final CountryService countryService;

    public CountryGrpcService(CountryService countryService) {
        this.countryService = countryService;
    }

    @Override
    public void getAllCountries(
            CountryProto.GetAllCountriesRequest request,
            StreamObserver<CountryProto.GetAllCountriesResponse> responseObserver) {
        try {
            String name = request.getName().isEmpty() ? null : request.getName();
            int page = request.getPage();
            int size = request.getSize() > 0 ? request.getSize() : 20;

            Page<CountryJson> result = countryService.all(name, PageRequest.of(page, size));

            CountryProto.GetAllCountriesResponse.Builder response =
                    CountryProto.GetAllCountriesResponse.newBuilder()
                            .setTotalPages(result.getTotalPages())
                            .setTotalElements(result.getTotalElements())
                            .setPage(result.getNumber())
                            .setSize(result.getSize());

            result.getContent().forEach(c ->
                    response.addCountries(
                            CountryProto.Country.newBuilder()
                                    .setId(c.id().toString())
                                    .setName(c.name())
                                    .build()
                    )
            );

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            failInternal(responseObserver, e);
        }
    }

    @Override
    public void getCountryById(
            CountryProto.GetCountryByIdRequest request,
            StreamObserver<CountryProto.Country> responseObserver) {
        try {
            CountryJson country = countryService.findCountryById(request.getId());
            responseObserver.onNext(toProto(country));
            responseObserver.onCompleted();
        } catch (CountryNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            failInternal(responseObserver, e);
        }
    }

    @Override
    public void getCountryByName(
            CountryProto.GetCountryByNameRequest request,
            StreamObserver<CountryProto.Country> responseObserver) {
        try {
            CountryJson country = countryService.findCountryByName(request.getName());
            responseObserver.onNext(toProto(country));
            responseObserver.onCompleted();
        } catch (CountryNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            failInternal(responseObserver, e);
        }
    }

    private CountryProto.Country toProto(CountryJson country) {
        return CountryProto.Country.newBuilder()
                .setId(country.id().toString())
                .setName(country.name())
                .build();
    }

    private void failInternal(StreamObserver<?> responseObserver, Exception e) {
        responseObserver.onError(Status.INTERNAL
                .withDescription(e.getMessage())
                .asRuntimeException());
    }
}