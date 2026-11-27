package io.student.rcc.artist.service;

import io.student.rcc.artist.data.entity.ArtistEntity;
import io.student.rcc.artist.data.repository.ArtistRepository;
import io.student.rcc.artist.domain.ArtistResponse;
import io.student.rcc.artist.domain.CreateArtistRequest;
import io.student.rcc.artist.domain.UpdateArtistRequest;
import io.student.rcc.artist.exception.ResourceNotFoundException;
import io.student.rcc.artist.mapper.ArtistMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final ArtistMapper artistMapper;

    public ArtistService(ArtistRepository artistRepository, ArtistMapper artistMapper) {
        this.artistRepository = artistRepository;
        this.artistMapper = artistMapper;
    }

    @Transactional(readOnly = true)
    public List<ArtistResponse> getAllArtists() {
        return artistRepository.findAll().stream()
                .map(artistMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ArtistResponse> getArtistsPage(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ArtistEntity> entityPage;

        if (name != null && !name.trim().isEmpty()) {
            entityPage = artistRepository.findAllByNameContainingIgnoreCase(name.trim(), pageable);
        } else {
            entityPage = artistRepository.findAll(pageable);
        }

        return entityPage.map(artistMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ArtistResponse getArtistById(String id) {
        ArtistEntity entity = artistRepository.findById(parseId(id))
                .orElseThrow(() -> new ResourceNotFoundException("Художник", "id", id));
        return artistMapper.toResponse(entity);
    }

    @Transactional
    public ArtistResponse createArtist(CreateArtistRequest request) {
        if (artistRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Художник с именем '" + request.name() + "' уже существует");
        }

        ArtistEntity entity = artistMapper.toEntity(request);
        ArtistEntity saved = artistRepository.save(entity);
        return artistMapper.toResponse(saved);
    }

    @Transactional
    public ArtistResponse updateArtist(String id, UpdateArtistRequest request) {
        ArtistEntity entity = artistRepository.findById(parseId(id))
                .orElseThrow(() -> new ResourceNotFoundException("Художник", "id", id));

        if (!entity.getName().equals(request.name()) && artistRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Художник с именем '" + request.name() + "' уже существует");
        }

        artistMapper.updateEntity(entity, request);
        ArtistEntity updated = artistRepository.save(entity);
        return artistMapper.toResponse(updated);
    }

    @Transactional
    public void deleteArtist(String id) {
        UUID uuid = parseId(id);
        if (!artistRepository.existsById(uuid)) {
            throw new ResourceNotFoundException("Художник", "id", id);
        }
        artistRepository.deleteById(uuid);
    }

    private UUID parseId(String id) {
        return UUID.fromString(id);
    }
}