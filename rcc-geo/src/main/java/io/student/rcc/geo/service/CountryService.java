package io.student.rcc.geo.service;

import io.student.rcc.geo.entity.CountryEntity;
import io.student.rcc.geo.exception.CountryNotFoundException;
import io.student.rcc.geo.model.CountryJson;
import io.student.rcc.geo.repository.CountryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CountryService {

    private final CountryRepository countryRepository;

    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    @Transactional(readOnly = true)
    public Page<CountryJson> all(String name, Pageable pageable) {
        Page<CountryEntity> countries = (name == null || name.isBlank())
                ? countryRepository.findAll(pageable)
                : countryRepository.findAllByNameContainsIgnoreCase(name.trim(), pageable);
        return countries.map(CountryJson::fromEntity);
    }

    @Transactional(readOnly = true)
    public CountryJson findCountryByName(String name) {
        return countryRepository.findByName(name)
                .map(CountryJson::fromEntity)
                .orElseThrow(() -> new CountryNotFoundException("Страна не найдена по имени: " + name));
    }

    @Transactional(readOnly = true)
    public CountryJson findCountryById(String id) {
        return countryRepository.findById(UUID.fromString(id))
                .map(CountryJson::fromEntity)
                .orElseThrow(() -> new CountryNotFoundException("Страна не найдена по id: " + id));
    }
}