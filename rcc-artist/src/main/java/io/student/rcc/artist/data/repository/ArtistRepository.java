package io.student.rcc.artist.data.repository;

import io.student.rcc.artist.data.entity.ArtistEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ArtistRepository extends JpaRepository<ArtistEntity, UUID> {

    Page<ArtistEntity> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByName(String name);
}