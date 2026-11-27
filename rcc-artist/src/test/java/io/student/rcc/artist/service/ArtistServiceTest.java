package io.student.rcc.artist.service;

import io.student.rcc.artist.data.entity.ArtistEntity;
import io.student.rcc.artist.data.repository.ArtistRepository;
import io.student.rcc.artist.domain.ArtistResponse;
import io.student.rcc.artist.domain.CreateArtistRequest;
import io.student.rcc.artist.domain.UpdateArtistRequest;
import io.student.rcc.artist.exception.ResourceNotFoundException;
import io.student.rcc.artist.mapper.ArtistMapper;
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
class ArtistServiceTest {

    @Test
    void getArtistByIdShouldThrowWhenNotFound(
            @Mock ArtistRepository artistRepository,
            @Mock ArtistMapper artistMapper
    ) {
        String id = UUID.randomUUID().toString();
        when(artistRepository.findById(eq(UUID.fromString(id)))).thenReturn(Optional.empty());

        ArtistService service = new ArtistService(artistRepository, artistMapper);

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> service.getArtistById(id)
        );
        assertEquals("Художник не найден с id: '" + id + "'", ex.getMessage());
        verifyNoInteractions(artistMapper);
    }

    @Test
    void getArtistByIdShouldReturnResponse(
            @Mock ArtistRepository artistRepository,
            @Mock ArtistMapper artistMapper
    ) {
        UUID id = UUID.randomUUID();

        ArtistEntity entity = new ArtistEntity();
        entity.setId(id);
        entity.setName("Иван Айвазовский");
        entity.setBiography("Русский художник-маринист");
        entity.setPhotoFromString("data:image/jpeg;base64,abc");

        ArtistResponse expected = new ArtistResponse(
                id.toString(),
                "Иван Айвазовский",
                "Русский художник-маринист",
                "data:image/jpeg;base64,abc"
        );

        when(artistRepository.findById(eq(id))).thenReturn(Optional.of(entity));
        when(artistMapper.toResponse(entity)).thenReturn(expected);

        ArtistService service = new ArtistService(artistRepository, artistMapper);
        ArtistResponse result = service.getArtistById(id.toString());

        assertEquals("Иван Айвазовский", result.name());
        verify(artistMapper).toResponse(entity);
    }

    @Test
    void createArtistShouldSaveAndReturn(
            @Mock ArtistRepository artistRepository,
            @Mock ArtistMapper artistMapper
    ) {
        UUID savedId = UUID.randomUUID();

        CreateArtistRequest request = new CreateArtistRequest(
                "Василий Кандинский",
                "Русский художник-абстракционист",
                "photo-data"
        );

        ArtistEntity toSave = new ArtistEntity();
        toSave.setName(request.name());
        toSave.setBiography(request.biography());

        ArtistEntity saved = new ArtistEntity();
        saved.setId(savedId);
        saved.setName(request.name());
        saved.setBiography(request.biography());

        ArtistResponse expected = new ArtistResponse(
                savedId.toString(),
                "Василий Кандинский",
                "Русский художник-абстракционист",
                "photo-data"
        );

        when(artistRepository.existsByName(eq("Василий Кандинский"))).thenReturn(false);
        when(artistMapper.toEntity(request)).thenReturn(toSave);
        when(artistRepository.save(toSave)).thenReturn(saved);
        when(artistMapper.toResponse(saved)).thenReturn(expected);

        ArtistService service = new ArtistService(artistRepository, artistMapper);
        ArtistResponse result = service.createArtist(request);

        assertEquals(savedId.toString(), result.id());
        assertEquals("Василий Кандинский", result.name());
        verify(artistRepository).save(toSave);
    }

    @Test
    void createArtistShouldThrowWhenNameAlreadyExists(
            @Mock ArtistRepository artistRepository,
            @Mock ArtistMapper artistMapper
    ) {
        CreateArtistRequest request = new CreateArtistRequest(
                "Иван Айвазовский",
                "Дубликат",
                null
        );

        when(artistRepository.existsByName(eq("Иван Айвазовский"))).thenReturn(true);

        ArtistService service = new ArtistService(artistRepository, artistMapper);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.createArtist(request)
        );
        assertEquals("Художник с именем 'Иван Айвазовский' уже существует", ex.getMessage());
        verify(artistRepository, never()).save(any());
        verifyNoInteractions(artistMapper);
    }

    @Test
    void updateArtistShouldThrowWhenNotFound(
            @Mock ArtistRepository artistRepository,
            @Mock ArtistMapper artistMapper
    ) {
        String id = UUID.randomUUID().toString();
        UpdateArtistRequest request = new UpdateArtistRequest(
                "Новое имя", "Новая биография", null
        );

        when(artistRepository.findById(eq(UUID.fromString(id)))).thenReturn(Optional.empty());

        ArtistService service = new ArtistService(artistRepository, artistMapper);

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateArtist(id, request));
        verify(artistRepository, never()).save(any());
        verifyNoInteractions(artistMapper);
    }

    @Test
    void updateArtistShouldThrowWhenNewNameAlreadyTaken(
            @Mock ArtistRepository artistRepository,
            @Mock ArtistMapper artistMapper
    ) {
        UUID id = UUID.randomUUID();
        ArtistEntity entity = new ArtistEntity();
        entity.setId(id);
        entity.setName("Старое имя");

        UpdateArtistRequest request = new UpdateArtistRequest(
                "Новое имя", "Биография", null
        );

        when(artistRepository.findById(eq(id))).thenReturn(Optional.of(entity));
        when(artistRepository.existsByName(eq("Новое имя"))).thenReturn(true);

        ArtistService service = new ArtistService(artistRepository, artistMapper);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateArtist(id.toString(), request)
        );
        assertEquals("Художник с именем 'Новое имя' уже существует", ex.getMessage());
        verify(artistRepository, never()).save(any());
        verify(artistMapper, never()).updateEntity(any(), any());
    }

    @Test
    void updateArtistShouldAllowSameName(
            @Mock ArtistRepository artistRepository,
            @Mock ArtistMapper artistMapper
    ) {
        UUID id = UUID.randomUUID();
        ArtistEntity entity = new ArtistEntity();
        entity.setId(id);
        entity.setName("Иван Айвазовский");
        entity.setBiography("Старая биография");

        UpdateArtistRequest request = new UpdateArtistRequest(
                "Иван Айвазовский", "Новая биография", null
        );

        ArtistResponse expected = new ArtistResponse(
                id.toString(), "Иван Айвазовский", "Новая биография", null
        );

        when(artistRepository.findById(eq(id))).thenReturn(Optional.of(entity));
        when(artistRepository.save(entity)).thenReturn(entity);
        when(artistMapper.toResponse(entity)).thenReturn(expected);

        ArtistService service = new ArtistService(artistRepository, artistMapper);
        ArtistResponse result = service.updateArtist(id.toString(), request);

        assertEquals("Иван Айвазовский", result.name());
        assertEquals("Новая биография", result.biography());
        verify(artistMapper).updateEntity(entity, request);
        verify(artistRepository).save(entity);
        verify(artistRepository, never()).existsByName(anyString());
    }

    @Test
    void updateArtistShouldSucceedWithNewUniqueName(
            @Mock ArtistRepository artistRepository,
            @Mock ArtistMapper artistMapper
    ) {
        UUID id = UUID.randomUUID();
        ArtistEntity entity = new ArtistEntity();
        entity.setId(id);
        entity.setName("Старое имя");

        UpdateArtistRequest request = new UpdateArtistRequest(
                "Новое уникальное имя", "Био", null
        );

        ArtistResponse expected = new ArtistResponse(
                id.toString(), "Новое уникальное имя", "Био", null
        );

        when(artistRepository.findById(eq(id))).thenReturn(Optional.of(entity));
        when(artistRepository.existsByName(eq("Новое уникальное имя"))).thenReturn(false);
        when(artistRepository.save(entity)).thenReturn(entity);
        when(artistMapper.toResponse(entity)).thenReturn(expected);

        ArtistService service = new ArtistService(artistRepository, artistMapper);
        ArtistResponse result = service.updateArtist(id.toString(), request);

        assertEquals("Новое уникальное имя", result.name());
        verify(artistMapper).updateEntity(entity, request);
        verify(artistRepository).save(entity);
    }

    @Test
    void deleteArtistShouldThrowWhenNotFound(
            @Mock ArtistRepository artistRepository,
            @Mock ArtistMapper artistMapper
    ) {
        String id = UUID.randomUUID().toString();
        when(artistRepository.existsById(eq(UUID.fromString(id)))).thenReturn(false);

        ArtistService service = new ArtistService(artistRepository, artistMapper);

        assertThrows(ResourceNotFoundException.class,
                () -> service.deleteArtist(id));
        verify(artistRepository, never()).deleteById(any());
    }

    @Test
    void deleteArtistShouldCallDelete(
            @Mock ArtistRepository artistRepository,
            @Mock ArtistMapper artistMapper
    ) {
        UUID id = UUID.randomUUID();
        when(artistRepository.existsById(eq(id))).thenReturn(true);

        ArtistService service = new ArtistService(artistRepository, artistMapper);
        service.deleteArtist(id.toString());

        verify(artistRepository).deleteById(eq(id));
    }

    @Test
    void getAllArtistsShouldReturnList(
            @Mock ArtistRepository artistRepository,
            @Mock ArtistMapper artistMapper
    ) {
        ArtistEntity a1 = new ArtistEntity();
        a1.setId(UUID.randomUUID());
        a1.setName("Иван Айвазовский");

        ArtistEntity a2 = new ArtistEntity();
        a2.setId(UUID.randomUUID());
        a2.setName("Исаак Левитан");

        when(artistRepository.findAll()).thenReturn(List.of(a1, a2));
        when(artistMapper.toResponse(a1)).thenReturn(
                new ArtistResponse(a1.getId().toString(), "Иван Айвазовский", null, null)
        );
        when(artistMapper.toResponse(a2)).thenReturn(
                new ArtistResponse(a2.getId().toString(), "Исаак Левитан", null, null)
        );

        ArtistService service = new ArtistService(artistRepository, artistMapper);
        List<ArtistResponse> result = service.getAllArtists();

        assertEquals(2, result.size());
        assertEquals("Иван Айвазовский", result.get(0).name());
        assertEquals("Исаак Левитан", result.get(1).name());
        verify(artistRepository).findAll();
    }

    @Test
    void getArtistsPageWithoutNameShouldCallFindAll(
            @Mock ArtistRepository artistRepository,
            @Mock ArtistMapper artistMapper
    ) {
        ArtistEntity entity = new ArtistEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Художник");

        Page<ArtistEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 9), 1);

        when(artistRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(artistMapper.toResponse(entity)).thenReturn(
                new ArtistResponse(entity.getId().toString(), "Художник", null, null)
        );

        ArtistService service = new ArtistService(artistRepository, artistMapper);
        Page<ArtistResponse> result = service.getArtistsPage(null, 0, 9);

        assertEquals(1, result.getTotalElements());
        verify(artistRepository).findAll(any(Pageable.class));
        verify(artistRepository, never()).findAllByNameContainingIgnoreCase(anyString(), any());
    }

    @Test
    void getArtistsPageWithNameShouldSearch(
            @Mock ArtistRepository artistRepository,
            @Mock ArtistMapper artistMapper
    ) {
        when(artistRepository.findAllByNameContainingIgnoreCase(eq("Левитан"), any(Pageable.class)))
                .thenReturn(Page.empty());

        ArtistService service = new ArtistService(artistRepository, artistMapper);
        Page<ArtistResponse> result = service.getArtistsPage("Левитан", 0, 9);

        assertTrue(result.isEmpty());
        verify(artistRepository).findAllByNameContainingIgnoreCase(eq("Левитан"), any(Pageable.class));
    }

    @Test
    void getArtistsPageWithBlankNameShouldCallFindAll(
            @Mock ArtistRepository artistRepository,
            @Mock ArtistMapper artistMapper
    ) {
        when(artistRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        ArtistService service = new ArtistService(artistRepository, artistMapper);
        service.getArtistsPage("   ", 0, 9);

        verify(artistRepository).findAll(any(Pageable.class));
        verify(artistRepository, never()).findAllByNameContainingIgnoreCase(anyString(), any());
    }
}