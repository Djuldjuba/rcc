package io.student.rococo.service.impl;

import io.student.rococo.data.entity.CountryEntity;
import io.student.rococo.data.repository.CountryRepository;
import io.student.rococo.exception.ResourceNotFoundException;
import io.student.rococo.model.CountryJson;
import io.student.rococo.service.api.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CountryServiceImpl implements CountryService {
  private final CountryRepository countryRepository;

  @Autowired
  public CountryServiceImpl(CountryRepository countryRepository) {
    this.countryRepository = countryRepository;
  }

  @Transactional(readOnly = true)
  public Page<CountryJson> all(String name, Pageable pageable) {
    Page<CountryEntity> countries = (name == null)
        ? countryRepository.findAll(pageable)
        : countryRepository.findAllByNameContainsIgnoreCase(name, pageable);
    return countries.map(CountryJson::fromEntity);
  }

  @Transactional(readOnly = true)
  public CountryJson findCountryByName(String name) {
    return CountryJson.fromEntity(
        getRequiredCountry(
            name
        )
    );
  }

  @Override
  @Transactional(readOnly = true)
  public CountryJson findCountryById(String id) {
    return CountryJson.fromEntity(
        getRequiredCountry(
            UUID.fromString(id)
        )
    );
  }

  private CountryEntity getRequiredCountry(String name) {
    return countryRepository.findByName(name).orElseThrow(
        () -> new ResourceNotFoundException(String.format("Страна не найдена по имени: %s", name))
    );
  }

  private CountryEntity getRequiredCountry(UUID id) {
    return countryRepository.findById(id).orElseThrow(
        () -> new ResourceNotFoundException(String.format("Страна не найдена по id: %s", id))
    );
  }
}