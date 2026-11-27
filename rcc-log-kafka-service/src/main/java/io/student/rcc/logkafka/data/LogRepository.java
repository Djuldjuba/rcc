package io.student.rcc.logkafka.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LogRepository extends JpaRepository<LogEntity, UUID> {
}