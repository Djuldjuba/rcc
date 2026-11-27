package io.student.rcc.gateway.controller;

import io.student.rcc.gateway.model.CountryJson;
import io.student.rcc.gateway.service.CountryClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/country")
public class CountryController {

    private final CountryClient countryClient;

    public CountryController(CountryClient countryClient) {
        this.countryClient = countryClient;
    }

    @GetMapping
    public Map<String, Object> getCountries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String title) {
        return countryClient.getCountriesPage(page, size, title);
    }

    @GetMapping("/{id}")
    public CountryJson getCountryById(@PathVariable String id) {
        return countryClient.getCountryById(id);
    }

    @GetMapping("/name/{name}")
    public CountryJson getCountryByName(@PathVariable String name) {
        return countryClient.getCountryByName(name);
    }
}