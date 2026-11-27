package io.student.rcc.museum.data.repository;

import io.student.rcc.museum.data.entity.MuseumEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface MuseumRepository extends JpaRepository<MuseumEntity, UUID> {

    @Query("SELECT m FROM MuseumEntity m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<MuseumEntity> findAllByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<MuseumEntity> findAllByCountryId(UUID countryId, Pageable pageable);
}