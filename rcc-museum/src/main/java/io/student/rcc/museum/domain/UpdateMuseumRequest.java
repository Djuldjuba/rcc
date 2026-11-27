package io.student.rcc.museum.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMuseumRequest(
        @NotBlank(message = "Название музея не может быть пустым")
        @Size(max = 255, message = "Название музея не может превышать 255 символов")
        String title,

        @Size(max = 1000, message = "Описание не может превышать 1000 символов")
        String description,

        String photo,

        @JsonProperty("geo") GeoWrapper geo
) {
        public record CountryRef(String id) {}
        public record GeoWrapper(
                @NotBlank(message = "Город не может быть пустым")
                String city,
                @JsonProperty("country") CountryRef country
        ) {}

        public String getCity() {
                return geo != null ? geo.city() : null;
        }

        public String getCountryId() {
                return geo != null && geo.country() != null ? geo.country().id() : null;
        }
}