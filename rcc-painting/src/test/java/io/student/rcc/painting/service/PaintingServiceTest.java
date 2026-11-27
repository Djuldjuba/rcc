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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaintingServiceTest {

    @Test
    void getPaintingByIdShouldThrowWhenNotFound(
            @Mock PaintingRepository paintingRepository,
            @Mock PaintingMapper paintingMapper,
            @Mock ArtistGrpcClient artistGrpcClient,
            @Mock MuseumGrpcClient museumGrpcClient
    ) {
        String id = UUID.randomUUID().toString();
        when(paintingRepository.findById(eq(UUID.fromString(id)))).thenReturn(Optional.empty());

        PaintingService service = new PaintingService(
                paintingRepository, paintingMapper, artistGrpcClient, museumGrpcClient);

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> service.getPaintingById(id)
        );
        assertEquals("Картина не найден с id: '" + id + "'", ex.getMessage());
        verifyNoInteractions(paintingMapper, artistGrpcClient, museumGrpcClient);
    }

    @Test
    void getPaintingByIdShouldReturnResponse(
            @Mock PaintingRepository paintingRepository,
            @Mock PaintingMapper paintingMapper,
            @Mock ArtistGrpcClient artistGrpcClient,
            @Mock MuseumGrpcClient museumGrpcClient
    ) {
        UUID id = UUID.randomUUID();
        UUID artistId = UUID.randomUUID();
        UUID museumId = UUID.randomUUID();

        PaintingEntity entity = new PaintingEntity();
        entity.setId(id);
        entity.setTitle("Над вечным покоем");
        entity.setDescription("Пейзаж");
        entity.setContentFromString("data:image/jpeg;base64,abc");
        entity.setArtistId(artistId);
        entity.setMuseumId(museumId);

        PaintingResponse expected = new PaintingResponse(
                id.toString(), "Над вечным покоем", "Пейзаж",
                "data:image/jpeg;base64,abc",
                artistId.toString(), "Real Artist",
                museumId.toString(), "Real Museum"
        );

        when(paintingRepository.findById(eq(id))).thenReturn(Optional.of(entity));
        when(artistGrpcClient.getArtistName(artistId.toString())).thenReturn(Optional.of("Real Artist"));
        when(museumGrpcClient.getMuseumTitle(museumId.toString())).thenReturn(Optional.of("Real Museum"));
        when(paintingMapper.toResponse(eq(entity), eq("Real Artist"), eq("Real Museum"))).thenReturn(expected);

        PaintingService service = new PaintingService(
                paintingRepository, paintingMapper, artistGrpcClient, museumGrpcClient);
        PaintingResponse result = service.getPaintingById(id.toString());

        assertEquals("Над вечным покоем", result.title());
        verify(paintingMapper).toResponse(eq(entity), eq("Real Artist"), eq("Real Museum"));
    }

    @Test
    void addPaintingShouldSaveAndReturn(
            @Mock PaintingRepository paintingRepository,
            @Mock PaintingMapper paintingMapper,
            @Mock ArtistGrpcClient artistGrpcClient,
            @Mock MuseumGrpcClient museumGrpcClient
    ) {
        UUID artistId = UUID.randomUUID();
        UUID museumId = UUID.randomUUID();
        UUID savedId = UUID.randomUUID();

        CreatePaintingRequest request = new CreatePaintingRequest(
                "Девятый вал", "Описание", "data:image/...",
                artistId.toString(), museumId.toString()
        );

        PaintingEntity toSave = new PaintingEntity();
        toSave.setTitle(request.title());
        toSave.setArtistId(artistId);
        toSave.setMuseumId(museumId);

        PaintingEntity saved = new PaintingEntity();
        saved.setId(savedId);
        saved.setTitle(request.title());
        saved.setArtistId(artistId);
        saved.setMuseumId(museumId);

        PaintingResponse expected = new PaintingResponse(
                savedId.toString(), "Девятый вал", "Описание", "data:image/...",
                artistId.toString(), "Real Artist",
                museumId.toString(), "Real Museum"
        );

        when(paintingMapper.toEntity(request)).thenReturn(toSave);
        when(paintingRepository.save(toSave)).thenReturn(saved);
        when(artistGrpcClient.getArtistName(artistId.toString())).thenReturn(Optional.of("Real Artist"));
        when(museumGrpcClient.getMuseumTitle(museumId.toString())).thenReturn(Optional.of("Real Museum"));
        when(paintingMapper.toResponse(eq(saved), eq("Real Artist"), eq("Real Museum"))).thenReturn(expected);

        PaintingService service = new PaintingService(
                paintingRepository, paintingMapper, artistGrpcClient, museumGrpcClient);
        PaintingResponse result = service.addPainting(request);

        assertEquals(savedId.toString(), result.id());
        assertEquals("Девятый вал", result.title());
        verify(paintingRepository).save(toSave);
    }

    @Test
    void updatePaintingShouldThrowWhenNotFound(
            @Mock PaintingRepository paintingRepository,
            @Mock PaintingMapper paintingMapper,
            @Mock ArtistGrpcClient artistGrpcClient,
            @Mock MuseumGrpcClient museumGrpcClient
    ) {
        String id = UUID.randomUUID().toString();
        UpdatePaintingRequest request = new UpdatePaintingRequest(
                "Новое", "Описание", "content",
                UUID.randomUUID().toString(), UUID.randomUUID().toString()
        );

        when(paintingRepository.findById(eq(UUID.fromString(id)))).thenReturn(Optional.empty());

        PaintingService service = new PaintingService(
                paintingRepository, paintingMapper, artistGrpcClient, museumGrpcClient);

        assertThrows(ResourceNotFoundException.class,
                () -> service.updatePainting(id, request));
        verify(paintingRepository, never()).save(any());
    }

    @Test
    void updatePaintingShouldUpdateAndReturn(
            @Mock PaintingRepository paintingRepository,
            @Mock PaintingMapper paintingMapper,
            @Mock ArtistGrpcClient artistGrpcClient,
            @Mock MuseumGrpcClient museumGrpcClient
    ) {
        UUID id = UUID.randomUUID();
        UUID artistId = UUID.randomUUID();
        UUID museumId = UUID.randomUUID();

        PaintingEntity entity = new PaintingEntity();
        entity.setId(id);
        entity.setTitle("Старое");
        entity.setArtistId(artistId);
        entity.setMuseumId(museumId);

        UpdatePaintingRequest request = new UpdatePaintingRequest(
                "Новое", "Новое описание", "new-content",
                artistId.toString(), museumId.toString()
        );

        PaintingResponse expected = new PaintingResponse(
                id.toString(), "Новое", "Новое описание", "new-content",
                artistId.toString(), "Real Artist",
                museumId.toString(), "Real Museum"
        );

        when(paintingRepository.findById(eq(id))).thenReturn(Optional.of(entity));
        when(paintingRepository.save(entity)).thenReturn(entity);
        when(artistGrpcClient.getArtistName(artistId.toString())).thenReturn(Optional.of("Real Artist"));
        when(museumGrpcClient.getMuseumTitle(museumId.toString())).thenReturn(Optional.of("Real Museum"));
        when(paintingMapper.toResponse(eq(entity), eq("Real Artist"), eq("Real Museum"))).thenReturn(expected);

        PaintingService service = new PaintingService(
                paintingRepository, paintingMapper, artistGrpcClient, museumGrpcClient);
        PaintingResponse result = service.updatePainting(id.toString(), request);

        assertEquals("Новое", result.title());
        verify(paintingMapper).updateEntity(entity, request);
        verify(paintingRepository).save(entity);
    }

    @Test
    void deletePaintingShouldThrowWhenNotFound(
            @Mock PaintingRepository paintingRepository,
            @Mock PaintingMapper paintingMapper,
            @Mock ArtistGrpcClient artistGrpcClient,
            @Mock MuseumGrpcClient museumGrpcClient
    ) {
        String id = UUID.randomUUID().toString();
        when(paintingRepository.existsById(eq(UUID.fromString(id)))).thenReturn(false);

        PaintingService service = new PaintingService(
                paintingRepository, paintingMapper, artistGrpcClient, museumGrpcClient);

        assertThrows(ResourceNotFoundException.class,
                () -> service.deletePainting(id));
        verify(paintingRepository, never()).deleteById(any());
    }

    @Test
    void deletePaintingShouldCallDelete(
            @Mock PaintingRepository paintingRepository,
            @Mock PaintingMapper paintingMapper,
            @Mock ArtistGrpcClient artistGrpcClient,
            @Mock MuseumGrpcClient museumGrpcClient
    ) {
        UUID id = UUID.randomUUID();
        when(paintingRepository.existsById(eq(id))).thenReturn(true);

        PaintingService service = new PaintingService(
                paintingRepository, paintingMapper, artistGrpcClient, museumGrpcClient);
        service.deletePainting(id.toString());

        verify(paintingRepository).deleteById(eq(id));
    }

    @Test
    void getPaintingsPageWithoutTitleShouldCallFindAll(
            @Mock PaintingRepository paintingRepository,
            @Mock PaintingMapper paintingMapper,
            @Mock ArtistGrpcClient artistGrpcClient,
            @Mock MuseumGrpcClient museumGrpcClient
    ) {
        PaintingEntity entity = new PaintingEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitle("Картина");
        entity.setArtistId(UUID.randomUUID());

        Page<PaintingEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 9), 1);

        when(paintingRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(artistGrpcClient.getArtistName(anyString())).thenReturn(Optional.empty());
        when(paintingMapper.toResponse(eq(entity), anyString(), any()))
                .thenReturn(new PaintingResponse(
                        entity.getId().toString(), "Картина", null, null,
                        entity.getArtistId().toString(), "Artist", null, null
                ));

        PaintingService service = new PaintingService(
                paintingRepository, paintingMapper, artistGrpcClient, museumGrpcClient);
        Page<PaintingResponse> result = service.getPaintingsPage(null, 0, 9);

        assertEquals(1, result.getTotalElements());
        verify(paintingRepository).findAll(any(Pageable.class));
        verify(paintingRepository, never()).findAllByTitleContainingIgnoreCase(anyString(), any());
    }

    @Test
    void getPaintingsPageWithTitleShouldSearch(
            @Mock PaintingRepository paintingRepository,
            @Mock PaintingMapper paintingMapper,
            @Mock ArtistGrpcClient artistGrpcClient,
            @Mock MuseumGrpcClient museumGrpcClient
    ) {
        when(paintingRepository.findAllByTitleContainingIgnoreCase(eq("Левитан"), any(Pageable.class)))
                .thenReturn(Page.empty());

        PaintingService service = new PaintingService(
                paintingRepository, paintingMapper, artistGrpcClient, museumGrpcClient);
        Page<PaintingResponse> result = service.getPaintingsPage("Левитан", 0, 9);

        assertTrue(result.isEmpty());
        verify(paintingRepository).findAllByTitleContainingIgnoreCase(eq("Левитан"), any(Pageable.class));
    }

    @Test
    void getPaintingsByArtistShouldCallRepository(
            @Mock PaintingRepository paintingRepository,
            @Mock PaintingMapper paintingMapper,
            @Mock ArtistGrpcClient artistGrpcClient,
            @Mock MuseumGrpcClient museumGrpcClient
    ) {
        UUID artistId = UUID.randomUUID();
        when(paintingRepository.findAllByArtistId(eq(artistId), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(artistGrpcClient.getArtistName(artistId.toString())).thenReturn(Optional.of("Real Artist"));

        PaintingService service = new PaintingService(
                paintingRepository, paintingMapper, artistGrpcClient, museumGrpcClient);
        assertTrue(service.getPaintingsByArtist(artistId.toString(), 0, 9).isEmpty());
        verify(paintingRepository).findAllByArtistId(eq(artistId), any(Pageable.class));
    }

    @Test
    void getPaintingsByMuseumShouldCallRepository(
            @Mock PaintingRepository paintingRepository,
            @Mock PaintingMapper paintingMapper,
            @Mock ArtistGrpcClient artistGrpcClient,
            @Mock MuseumGrpcClient museumGrpcClient
    ) {
        UUID museumId = UUID.randomUUID();
        when(paintingRepository.findAllByMuseumId(eq(museumId), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(museumGrpcClient.getMuseumTitle(museumId.toString())).thenReturn(Optional.of("Real Museum"));

        PaintingService service = new PaintingService(
                paintingRepository, paintingMapper, artistGrpcClient, museumGrpcClient);
        assertTrue(service.getPaintingsByMuseum(museumId.toString(), 0, 9).isEmpty());
        verify(paintingRepository).findAllByMuseumId(eq(museumId), any(Pageable.class));
    }
}