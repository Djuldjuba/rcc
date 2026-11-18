package io.student.rococo.api.grpc;

import io.grpc.ManagedChannel;
import io.qameta.allure.Step;
import io.student.rcc.geo.proto.CountryProto;
import io.student.rcc.geo.proto.CountryServiceGrpc;

import java.util.List;

public class GeoGrpcApi {

    private final CountryServiceGrpc.CountryServiceBlockingStub stub;

    public GeoGrpcApi(ManagedChannel channel) {
        this.stub = CountryServiceGrpc.newBlockingStub(channel);
    }

    @Step("Получить страну по id: {id}")
    public CountryProto.Country getCountryById(String id) {
        return stub.getCountryById(
                CountryProto.GetCountryByIdRequest.newBuilder().setId(id).build()
        );
    }

    @Step("Найти страну по названию: {name}")
    public CountryProto.Country findByName(String name) {
        return stub.getCountryByName(
                CountryProto.GetCountryByNameRequest.newBuilder().setName(name).build()
        );
    }

    @Step("Получить список стран (страница {page}, размер {size})")
    public CountryProto.GetAllCountriesResponse getAllCountries(int page, int size) {
        return stub.getAllCountries(
                CountryProto.GetAllCountriesRequest.newBuilder()
                        .setPage(page).setSize(size).build()
        );
    }

    @Step("Получить все доступные страны")
    public List<CountryProto.Country> all() {
        return getAllCountries(0, 100).getCountriesList();
    }
}