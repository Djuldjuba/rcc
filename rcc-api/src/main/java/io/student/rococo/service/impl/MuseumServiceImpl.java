package io.student.rococo.service.impl;

import io.student.rococo.data.entity.CountryEntity;
import io.student.rococo.data.entity.MuseumEntity;
import io.student.rococo.data.repository.CountryRepository;
import io.student.rococo.data.repository.MuseumRepository;
import io.student.rococo.exception.ResourceNotFoundException;
import io.student.rococo.model.MuseumJson;
import io.student.rococo.service.api.MuseumService;
import io.student.rococo.util.StringAsBytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MuseumServiceImpl implements MuseumService {
  private final MuseumRepository museumRepository;
  private final CountryRepository countryRepository;

  @Autowired
  public MuseumServiceImpl(MuseumRepository museumRepository, CountryRepository countryRepository) {
    this.museumRepository = museumRepository;
    this.countryRepository = countryRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<MuseumJson> all(String title, Pageable pageable) {
    Page<MuseumEntity> museums = (title == null)
        ? museumRepository.findAll(pageable)
        : museumRepository.findAllByTitleContainsIgnoreCase(title, pageable);
    return museums.map(MuseumJson::fromEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public MuseumJson findById(String id) {
    return MuseumJson.fromEntity(
        museumRepository.findById(
            UUID.fromString(id)
        ).orElseThrow(
            () -> new ResourceNotFoundException(String.format("Музей не найден по id: %s", id))
        )
    );
  }

  @Override
  @Transactional
  public MuseumJson update(MuseumJson museum) {
    MuseumEntity museumEntity = getRequiredMuseum(museum.id());
    museumEntity.setTitle(museum.title());
    museumEntity.setCity(museum.geo().city());
    museumEntity.setDescription(museum.description());
    museumEntity.setPhoto(
        new StringAsBytes(
            museum.photo()
        ).bytes()
    );
    museumEntity.setCountry(getRequiredCountry(museum.geo().country().id()));
    return MuseumJson.fromEntity(
        museumRepository.save(museumEntity)
    );
  }

  @Override
  @Transactional
  public MuseumJson create(MuseumJson museum) {
    MuseumEntity museumEntity = museum.toEntity();
    CountryEntity country = museum.geo().country().id() != null
        ? getRequiredCountry(museum.geo().country().id())
        : getRequiredCountry(museum.geo().country().name());

    museumEntity.setCountry(country);
    return MuseumJson.fromEntity(
        museumRepository.save(
            museumEntity
        )
    );
  }

  private MuseumEntity getRequiredMuseum(UUID id) {
    return museumRepository.findById(id).orElseThrow(
        () -> new ResourceNotFoundException(String.format("Музей не найден по id: %s", id))
    );
  }

  private CountryEntity getRequiredCountry(UUID id) {
    return countryRepository.findById(id).orElseThrow(
        () -> new ResourceNotFoundException(String.format("Страна не найдена по id: %s", id))
    );
  }

  private CountryEntity getRequiredCountry(String name) {
    return countryRepository.findByName(name).orElseThrow(
        () -> new ResourceNotFoundException(String.format("Страна не найдена по имени: %s", name))
    );
  }
}