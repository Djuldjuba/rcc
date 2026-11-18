package io.student.rcc.painting.service;

import io.student.rcc.painting.client.ArtistGrpcClient;
import io.student.rcc.painting.client.MuseumGrpcClient;
import io.student.rcc.painting.data.entity.PaintingEntity;
import io.student.rcc.painting.data.repository.PaintingRepository;
import io.student.rcc.painting.domain.CreatePaintingRequest;
import io.student.rcc.painting.domain.PaintingResponse;
import io.student.rcc.painting.domain.UpdatePaintingRequest;
import io.student.rcc.painting.exception.ResourceNotFoundException;
import io.student.rcc.painting.mapper.PaintingMapper;
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
public class PaintingService {

    private final PaintingRepository paintingRepository;
    private final PaintingMapper paintingMapper;
    private final ArtistGrpcClient artistGrpcClient;
    private final MuseumGrpcClient museumGrpcClient;

    private final Map<String, String> artistNameCache = new ConcurrentHashMap<>();
    private final Map<String, String> museumTitleCache = new ConcurrentHashMap<>();

    public PaintingService(PaintingRepository paintingRepository,
                           PaintingMapper paintingMapper,
                           ArtistGrpcClient artistGrpcClient,
                           MuseumGrpcClient museumGrpcClient) {
        this.paintingRepository = paintingRepository;
        this.paintingMapper = paintingMapper;
        this.artistGrpcClient = artistGrpcClient;
        this.museumGrpcClient = museumGrpcClient;
    }

    private String getArtistName(String artistId) {
        if (artistId == null) {
            return null;
        }
        return artistNameCache.computeIfAbsent(artistId, id ->
                artistGrpcClient.getArtistName(id)
                        .orElseGet(() -> "Artist " + id)
        );
    }

    private String getMuseumTitle(String museumId) {
        if (museumId == null) {
            return null;
        }
        return museumTitleCache.computeIfAbsent(museumId, id ->
                museumGrpcClient.getMuseumTitle(id)
                        .orElseGet(() -> "Museum " + id)
        );
    }

    @Transactional(readOnly = true)
    public List<PaintingResponse> getAllPaintings() {
        return paintingRepository.findAll().stream()
                .map(entity -> {
                    String artistName = getArtistName(entity.getArtistId().toString());
                    String museumTitle = entity.getMuseumId() != null
                            ? getMuseumTitle(entity.getMuseumId().toString())
                            : null;
                    return paintingMapper.toResponse(entity, artistName, museumTitle);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<PaintingResponse> getPaintingsPage(String title, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaintingEntity> entityPage;

        if (title != null && !title.trim().isEmpty()) {
            entityPage = paintingRepository.findAllByTitleContainingIgnoreCase(title.trim(), pageable);
        } else {
            entityPage = paintingRepository.findAll(pageable);
        }

        return entityPage.map(entity -> {
            String artistName = getArtistName(entity.getArtistId().toString());
            String museumTitle = entity.getMuseumId() != null
                    ? getMuseumTitle(entity.getMuseumId().toString())
                    : null;
            return paintingMapper.toResponse(entity, artistName, museumTitle);
        });
    }

    @Transactional(readOnly = true)
    public PaintingResponse getPaintingById(String id) {
        UUID uuid = UUID.fromString(id);
        PaintingEntity entity = paintingRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Картина", "id", id));
        String artistName = getArtistName(entity.getArtistId().toString());
        String museumTitle = entity.getMuseumId() != null
                ? getMuseumTitle(entity.getMuseumId().toString())
                : null;
        return paintingMapper.toResponse(entity, artistName, museumTitle);
    }

    @Transactional(readOnly = true)
    public Page<PaintingResponse> getPaintingsByArtist(String artistId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        UUID artistUuid = UUID.fromString(artistId);
        Page<PaintingEntity> entityPage = paintingRepository.findAllByArtistId(artistUuid, pageable);
        String artistName = getArtistName(artistId);
        return entityPage.map(entity -> {
            String museumTitle = entity.getMuseumId() != null
                    ? getMuseumTitle(entity.getMuseumId().toString())
                    : null;
            return paintingMapper.toResponse(entity, artistName, museumTitle);
        });
    }

    @Transactional(readOnly = true)
    public Page<PaintingResponse> getPaintingsByMuseum(String museumId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        UUID museumUuid = UUID.fromString(museumId);
        Page<PaintingEntity> entityPage = paintingRepository.findAllByMuseumId(museumUuid, pageable);
        String museumTitle = getMuseumTitle(museumId);
        return entityPage.map(entity -> {
            String artistName = getArtistName(entity.getArtistId().toString());
            return paintingMapper.toResponse(entity, artistName, museumTitle);
        });
    }

    @Transactional
    public PaintingResponse addPainting(CreatePaintingRequest request) {
        PaintingEntity entity = paintingMapper.toEntity(request);
        PaintingEntity saved = paintingRepository.save(entity);
        String artistName = getArtistName(saved.getArtistId().toString());
        String museumTitle = saved.getMuseumId() != null
                ? getMuseumTitle(saved.getMuseumId().toString())
                : null;
        return paintingMapper.toResponse(saved, artistName, museumTitle);
    }

    @Transactional
    public PaintingResponse updatePainting(String id, UpdatePaintingRequest request) {
        UUID uuid = UUID.fromString(id);
        PaintingEntity entity = paintingRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Картина", "id", id));

        paintingMapper.updateEntity(entity, request);
        PaintingEntity updated = paintingRepository.save(entity);
        String artistName = getArtistName(updated.getArtistId().toString());
        String museumTitle = updated.getMuseumId() != null
                ? getMuseumTitle(updated.getMuseumId().toString())
                : null;
        return paintingMapper.toResponse(updated, artistName, museumTitle);
    }

    @Transactional
    public void deletePainting(String id) {
        UUID uuid = UUID.fromString(id);
        if (!paintingRepository.existsById(uuid)) {
            throw new ResourceNotFoundException("Картина", "id", id);
        }
        paintingRepository.deleteById(uuid);
    }
}