package io.student.rcc.gateway.service;

import io.student.rcc.gateway.model.CountryJson;
import io.student.rcc.geo.proto.CountryProto;
import io.student.rcc.geo.proto.CountryServiceGrpc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CountryClientTest {

    @Mock
    private CountryServiceGrpc.CountryServiceBlockingStub stub;

    private CountryClient client;

    @BeforeEach
    void setUp() {
        client = new CountryClient();
        ReflectionTestUtils.setField(client, "stub", stub);
    }

    @Test
    void getCountriesPageShouldReturnMap() {
        CountryProto.Country proto = CountryProto.Country.newBuilder()
                .setId("c-1").setName("Россия").build();
        when(stub.getAllCountries(any(CountryProto.GetAllCountriesRequest.class)))
                .thenReturn(CountryProto.GetAllCountriesResponse.newBuilder()
                        .addCountries(proto)
                        .setTotalPages(1).setTotalElements(1).setPage(0).setSize(20).build());

        Map<String, Object> result = client.getCountriesPage(0, 20, "Рос");

        assertEquals(1, ((List<?>) result.get("content")).size());
        assertEquals(1, result.get("totalPages"));
        assertEquals(1L, result.get("totalElements"));
        assertEquals(0, result.get("page"));
        assertEquals(20, result.get("size"));
    }

    @Test
    void getCountryByIdShouldMap() {
        when(stub.getCountryById(any(CountryProto.GetCountryByIdRequest.class)))
                .thenReturn(CountryProto.Country.newBuilder().setId("c-1").setName("Россия").build());

        CountryJson result = client.getCountryById("c-1");

        assertEquals("c-1", result.id());
        assertEquals("Россия", result.name());
    }

    @Test
    void getCountryByNameShouldMap() {
        when(stub.getCountryByName(any(CountryProto.GetCountryByNameRequest.class)))
                .thenReturn(CountryProto.Country.newBuilder().setId("c-1").setName("Россия").build());

        CountryJson result = client.getCountryByName("Россия");

        assertEquals("Россия", result.name());
    }
}