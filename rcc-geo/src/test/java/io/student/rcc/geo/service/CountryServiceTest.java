package io.student.rcc.geo.service;

import io.student.rcc.geo.entity.CountryEntity;
import io.student.rcc.geo.exception.CountryNotFoundException;
import io.student.rcc.geo.model.CountryJson;
import io.student.rcc.geo.repository.CountryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CountryServiceTest {

    @Test
    void allWithoutNameShouldCallFindAll(@Mock CountryRepository countryRepository) {
        CountryEntity entity = new CountryEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Россия");

        when(countryRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        CountryService service = new CountryService(countryRepository);
        var result = service.all(null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Россия", result.getContent().get(0).name());
        verify(countryRepository).findAll(any(Pageable.class));
    }

    @Test
    void allWithNameShouldSearch(@Mock CountryRepository countryRepository) {
        CountryEntity entity = new CountryEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Россия");

        when(countryRepository.findAllByNameContainsIgnoreCase(eq("росс"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        CountryService service = new CountryService(countryRepository);
        var result = service.all("росс", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        verify(countryRepository).findAllByNameContainsIgnoreCase(eq("росс"), any(Pageable.class));
    }

    @Test
    void findByIdShouldThrowWhenNotFound(@Mock CountryRepository countryRepository) {
        UUID id = UUID.randomUUID();
        when(countryRepository.findById(id)).thenReturn(Optional.empty());

        CountryService service = new CountryService(countryRepository);
        assertThrows(CountryNotFoundException.class,
                () -> service.findCountryById(id.toString()));
    }

    @Test
    void findByNameShouldReturn(@Mock CountryRepository countryRepository) {
        CountryEntity entity = new CountryEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Япония");
        when(countryRepository.findByName("Япония")).thenReturn(Optional.of(entity));

        CountryService service = new CountryService(countryRepository);
        CountryJson result = service.findCountryByName("Япония");

        assertEquals("Япония", result.name());
    }
}