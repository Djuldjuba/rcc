package io.student.rcc.geo.controller;

import io.student.rcc.geo.model.CountryJson;
import io.student.rcc.geo.service.CountryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/country")
public class CountryController {

    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @GetMapping
    public Page<CountryJson> getAll(
            @RequestParam(required = false) String title,
            @PageableDefault Pageable pageable) {
        return countryService.all(title, pageable);
    }

    @GetMapping("/{id}")
    public CountryJson findCountryById(@PathVariable String id) {
        return countryService.findCountryById(id);
    }

    @GetMapping("/name/{name}")
    public CountryJson findCountryByName(@PathVariable String name) {
        return countryService.findCountryByName(name);
    }
}