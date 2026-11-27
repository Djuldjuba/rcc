package io.student.rcc.museum.service;

import io.student.rcc.museum.client.CountryGrpcClient;
import io.student.rcc.museum.data.entity.MuseumEntity;
import io.student.rcc.museum.data.repository.MuseumRepository;
import io.student.rcc.museum.domain.CreateMuseumRequest;
import io.student.rcc.museum.domain.MuseumResponse;
import io.student.rcc.museum.domain.UpdateMuseumRequest;
import io.student.rcc.museum.exception.ResourceNotFoundException;
import io.student.rcc.museum.mapper.MuseumMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MuseumService {

    private static final Logger log = LoggerFactory.getLogger(MuseumService.class);

    private final MuseumRepository museumRepository;
    private final MuseumMapper museumMapper;
    private final CountryGrpcClient countryGrpcClient;

    private final Map<String, String> countryNameCache = new ConcurrentHashMap<>();

    public MuseumService(MuseumRepository museumRepository,
                         MuseumMapper museumMapper,
                         CountryGrpcClient countryGrpcClient) {
        this.museumRepository = museumRepository;
        this.museumMapper = museumMapper;
        this.countryGrpcClient = countryGrpcClient;
    }

    private String getCountryName(String countryId) {
        if (countryId == null) {
            return null;
        }
        return countryNameCache.computeIfAbsent(countryId, id ->
                countryGrpcClient.getCountryName(id)
                        .orElseGet(() -> "Country " + id)
        );
    }

    @Transactional(readOnly = true)
    public List<MuseumResponse> getAllMuseums() {
        return museumRepository.findAll().stream()
                .map(entity -> {
                    String countryName = getCountryName(entity.getCountryId().toString());
                    return museumMapper.toResponse(entity, countryName);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<MuseumResponse> getMuseumsPage(String title, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MuseumEntity> entityPage;

        if (title != null && !title.trim().isEmpty()) {
            entityPage = museumRepository.findAllByTitleContainingIgnoreCase(title.trim(), pageable);
        } else {
            entityPage = museumRepository.findAll(pageable);
        }

        return entityPage.map(entity -> {
            String countryName = getCountryName(entity.getCountryId().toString());
            return museumMapper.toResponse(entity, countryName);
        });
    }

    @Transactional(readOnly = true)
    public MuseumResponse getMuseumById(String id) {
        UUID uuid = UUID.fromString(id);
        MuseumEntity entity = museumRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Музей", "id", id));
        String countryName = getCountryName(entity.getCountryId().toString());
        return museumMapper.toResponse(entity, countryName);
    }

    @Transactional(readOnly = true)
    public Page<MuseumResponse> getMuseumsByCountry(String countryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        UUID countryUuid = UUID.fromString(countryId);
        Page<MuseumEntity> entityPage = museumRepository.findAllByCountryId(countryUuid, pageable);
        String countryName = getCountryName(countryId);
        return entityPage.map(entity -> museumMapper.toResponse(entity, countryName));
    }

    @Transactional
    public MuseumResponse createMuseum(CreateMuseumRequest request) {
        log.info("Creating museum with request: {}", request);
        MuseumEntity entity = museumMapper.toEntity(request);
        MuseumEntity saved = museumRepository.save(entity);
        String countryName = getCountryName(saved.getCountryId().toString());
        return museumMapper.toResponse(saved, countryName);
    }

    @Transactional
    public MuseumResponse updateMuseum(String id, UpdateMuseumRequest request) {
        UUID uuid = UUID.fromString(id);
        MuseumEntity entity = museumRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Музей", "id", id));

        museumMapper.updateEntity(entity, request);
        MuseumEntity updated = museumRepository.save(entity);
        String countryName = getCountryName(updated.getCountryId().toString());
        return museumMapper.toResponse(updated, countryName);
    }

    @Transactional
    public void deleteMuseum(String id) {
        UUID uuid = UUID.fromString(id);
        if (!museumRepository.existsById(uuid)) {
            throw new ResourceNotFoundException("Музей", "id", id);
        }
        museumRepository.deleteById(uuid);
    }
}