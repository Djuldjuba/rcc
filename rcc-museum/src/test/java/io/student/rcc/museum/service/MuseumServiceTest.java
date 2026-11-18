package io.student.rcc.museum.service;

import io.student.rcc.museum.data.entity.MuseumEntity;
import io.student.rcc.museum.data.repository.MuseumRepository;
import io.student.rcc.museum.domain.CreateMuseumRequest;
import io.student.rcc.museum.domain.MuseumResponse;
import io.student.rcc.museum.domain.UpdateMuseumRequest;
import io.student.rcc.museum.exception.ResourceNotFoundException;
import io.student.rcc.museum.mapper.MuseumMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MuseumServiceTest {

    @Mock
    private MuseumRepository museumRepository;

    @Mock
    private MuseumMapper museumMapper;

    @InjectMocks
    private MuseumService museumService;

    // ---------- getMuseumById ----------

    @Test
    void getMuseumByIdShouldReturn() {
        UUID id = UUID.randomUUID();
        UUID countryId = UUID.randomUUID();
        MuseumEntity entity = new MuseumEntity();
        entity.setId(id);
        entity.setTitle("Эрмитаж");
        entity.setCountryId(countryId);

        when(museumRepository.findById(id)).thenReturn(Optional.of(entity));
        when(museumMapper.toResponse(any(), any())).thenReturn(
                new MuseumResponse(id.toString(), "Эрмитаж", null, null, null, countryId.toString(), "Country " + countryId)
        );

        MuseumResponse result = museumService.getMuseumById(id.toString());

        assertEquals("Эрмитаж", result.title());
        assertEquals(id.toString(), result.id());
        verify(museumRepository).findById(id);
        verify(museumMapper).toResponse(eq(entity), anyString());
    }

    @Test
    void getMuseumByIdShouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(museumRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> museumService.getMuseumById(id.toString()));
        verify(museumRepository).findById(id);
        verifyNoInteractions(museumMapper);
    }

    // ---------- getAllMuseums ----------

    @Test
    void getAllMuseumsShouldReturnMappedList() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID countryId = UUID.randomUUID();

        MuseumEntity e1 = new MuseumEntity();
        e1.setId(id1);
        e1.setTitle("Эрмитаж");
        e1.setCountryId(countryId);

        MuseumEntity e2 = new MuseumEntity();
        e2.setId(id2);
        e2.setTitle("Русский музей");
        e2.setCountryId(countryId);

        when(museumRepository.findAll()).thenReturn(List.of(e1, e2));
        when(museumMapper.toResponse(eq(e1), anyString()))
                .thenReturn(new MuseumResponse(id1.toString(), "Эрмитаж", null, null, null, countryId.toString(), "Country"));
        when(museumMapper.toResponse(eq(e2), anyString()))
                .thenReturn(new MuseumResponse(id2.toString(), "Русский музей", null, null, null, countryId.toString(), "Country"));

        List<MuseumResponse> result = museumService.getAllMuseums();

        assertEquals(2, result.size());
        assertEquals("Эрмитаж", result.get(0).title());
        assertEquals("Русский музей", result.get(1).title());
        verify(museumRepository).findAll();
        verify(museumMapper, times(2)).toResponse(any(MuseumEntity.class), anyString());
    }

    @Test
    void getAllMuseumsShouldReturnEmptyList() {
        when(museumRepository.findAll()).thenReturn(List.of());

        List<MuseumResponse> result = museumService.getAllMuseums();

        assertTrue(result.isEmpty());
        verify(museumRepository).findAll();
        verifyNoInteractions(museumMapper);
    }

    // ---------- getMuseumsPage ----------

    @Test
    void getMuseumsPageWithoutTitleShouldCallFindAll() {
        when(museumRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<MuseumResponse> result = museumService.getMuseumsPage(null, 0, 10);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        verify(museumRepository).findAll(any(PageRequest.class));
        verify(museumRepository, never()).findAllByTitleContainingIgnoreCase(anyString(), any());
    }

    @Test
    void getMuseumsPageWithEmptyTitleShouldCallFindAll() {
        when(museumRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        museumService.getMuseumsPage("   ", 0, 10);

        verify(museumRepository).findAll(any(PageRequest.class));
        verify(museumRepository, never()).findAllByTitleContainingIgnoreCase(anyString(), any());
    }

    @Test
    void getMuseumsPageWithTitleShouldCallFindByTitle() {
        UUID id = UUID.randomUUID();
        UUID countryId = UUID.randomUUID();
        MuseumEntity entity = new MuseumEntity();
        entity.setId(id);
        entity.setTitle("Эрмитаж");
        entity.setCountryId(countryId);

        when(museumRepository.findAllByTitleContainingIgnoreCase(eq("эрм"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(museumMapper.toResponse(eq(entity), anyString()))
                .thenReturn(new MuseumResponse(id.toString(), "Эрмитаж", null, null, null, countryId.toString(), "Country"));

        Page<MuseumResponse> result = museumService.getMuseumsPage("эрм", 0, 10);

        assertEquals(1, result.getContent().size());
        assertEquals("Эрмитаж", result.getContent().get(0).title());
        verify(museumRepository).findAllByTitleContainingIgnoreCase(eq("эрм"), any(Pageable.class));
        verify(museumRepository, never()).findAll(any(Pageable.class));
    }

    // ---------- getMuseumsByCountry ----------

    @Test
    void getMuseumsByCountryShouldReturnPage() {
        UUID countryId = UUID.randomUUID();
        UUID museumId = UUID.randomUUID();
        MuseumEntity entity = new MuseumEntity();
        entity.setId(museumId);
        entity.setTitle("Эрмитаж");
        entity.setCountryId(countryId);

        when(museumRepository.findAllByCountryId(eq(countryId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(museumMapper.toResponse(eq(entity), anyString()))
                .thenReturn(new MuseumResponse(museumId.toString(), "Эрмитаж", null, null, null, countryId.toString(), "Country"));

        Page<MuseumResponse> result = museumService.getMuseumsByCountry(countryId.toString(), 0, 10);

        assertEquals(1, result.getContent().size());
        assertEquals("Эрмитаж", result.getContent().get(0).title());
        verify(museumRepository).findAllByCountryId(eq(countryId), any(Pageable.class));
        verify(museumMapper).toResponse(eq(entity), anyString());
    }

    // ---------- createMuseum ----------

    @Test
    void createMuseumShouldSaveAndReturnResponse() {
        UUID countryId = UUID.randomUUID();
        UUID savedId = UUID.randomUUID();

        CreateMuseumRequest.GeoWrapper geo = new CreateMuseumRequest.GeoWrapper(
                "Санкт-Петербург",
                new CreateMuseumRequest.CountryRef(countryId.toString())
        );
        CreateMuseumRequest request = new CreateMuseumRequest(
                "Эрмитаж",
                "Описание",
                null,
                geo
        );

        MuseumEntity entityToSave = new MuseumEntity();
        entityToSave.setTitle("Эрмитаж");
        entityToSave.setCity("Санкт-Петербург");
        entityToSave.setCountryId(countryId);

        MuseumEntity savedEntity = new MuseumEntity();
        savedEntity.setId(savedId);
        savedEntity.setTitle("Эрмитаж");
        savedEntity.setCity("Санкт-Петербург");
        savedEntity.setCountryId(countryId);

        when(museumMapper.toEntity(request)).thenReturn(entityToSave);
        when(museumRepository.save(entityToSave)).thenReturn(savedEntity);
        when(museumMapper.toResponse(eq(savedEntity), anyString()))
                .thenReturn(new MuseumResponse(savedId.toString(), "Эрмитаж", "Описание", null, "Санкт-Петербург", countryId.toString(), "Country"));

        MuseumResponse result = museumService.createMuseum(request);

        assertEquals(savedId.toString(), result.id());
        assertEquals("Эрмитаж", result.title());
        assertEquals("Санкт-Петербург", result.city());
        verify(museumMapper).toEntity(request);
        verify(museumRepository).save(entityToSave);
        verify(museumMapper).toResponse(eq(savedEntity), anyString());
    }

    // ---------- updateMuseum ----------

    @Test
    void updateMuseumShouldUpdateAndReturnResponse() {
        UUID id = UUID.randomUUID();
        UUID countryId = UUID.randomUUID();
        UUID newCountryId = UUID.randomUUID();

        MuseumEntity existing = new MuseumEntity();
        existing.setId(id);
        existing.setTitle("Старое название");
        existing.setCity("Старый город");
        existing.setCountryId(countryId);

        UpdateMuseumRequest.GeoWrapper geo = new UpdateMuseumRequest.GeoWrapper(
                "Новый город",
                new UpdateMuseumRequest.CountryRef(newCountryId.toString())
        );
        UpdateMuseumRequest request = new UpdateMuseumRequest(
                "Новое название",
                "Новое описание",
                null,
                geo
        );

        MuseumEntity updated = new MuseumEntity();
        updated.setId(id);
        updated.setTitle("Новое название");
        updated.setCity("Новый город");
        updated.setCountryId(newCountryId);

        when(museumRepository.findById(id)).thenReturn(Optional.of(existing));
        doNothing().when(museumMapper).updateEntity(existing, request);
        when(museumRepository.save(existing)).thenReturn(updated);
        when(museumMapper.toResponse(eq(updated), anyString()))
                .thenReturn(new MuseumResponse(id.toString(), "Новое название", "Новое описание", null, "Новый город", newCountryId.toString(), "Country"));

        MuseumResponse result = museumService.updateMuseum(id.toString(), request);

        assertEquals("Новое название", result.title());
        assertEquals("Новый город", result.city());
        verify(museumRepository).findById(id);
        verify(museumMapper).updateEntity(existing, request);
        verify(museumRepository).save(existing);
        verify(museumMapper).toResponse(eq(updated), anyString());
    }

    @Test
    void updateMuseumShouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        UpdateMuseumRequest request = new UpdateMuseumRequest("Title", null, null, null);

        when(museumRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> museumService.updateMuseum(id.toString(), request));
        verify(museumRepository).findById(id);
        verify(museumRepository, never()).save(any());
        verifyNoInteractions(museumMapper);
    }

    // ---------- deleteMuseum ----------

    @Test
    void deleteMuseumShouldDeleteWhenExists() {
        UUID id = UUID.randomUUID();
        when(museumRepository.existsById(id)).thenReturn(true);
        doNothing().when(museumRepository).deleteById(id);

        museumService.deleteMuseum(id.toString());

        verify(museumRepository).existsById(id);
        verify(museumRepository).deleteById(id);
    }

    @Test
    void deleteMuseumShouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(museumRepository.existsById(id)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> museumService.deleteMuseum(id.toString()));
        verify(museumRepository).existsById(id);
        verify(museumRepository, never()).deleteById(any());
    }
}