package io.student.rococo.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeoJson(String city, CountryJson country) {
}
