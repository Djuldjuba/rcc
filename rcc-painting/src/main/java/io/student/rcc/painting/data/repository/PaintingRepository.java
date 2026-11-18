package io.student.rcc.painting.data.repository;

import io.student.rcc.painting.data.entity.PaintingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaintingRepository extends JpaRepository<PaintingEntity, UUID> {

    Page<PaintingEntity> findAllByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<PaintingEntity> findAllByArtistId(UUID artistId, Pageable pageable);

    Page<PaintingEntity> findAllByMuseumId(UUID museumId, Pageable pageable);
}