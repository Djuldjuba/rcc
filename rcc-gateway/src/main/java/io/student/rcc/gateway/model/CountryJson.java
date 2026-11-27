package io.student.rcc.gateway.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.student.rcc.geo.proto.CountryProto;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CountryJson(
        String id,
        String name
) {
    public static CountryJson fromGrpc(CountryProto.Country c) {
        return new CountryJson(
                emptyToNull(c.getId()),
                emptyToNull(c.getName())
        );
    }

    private static String emptyToNull(String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }
}