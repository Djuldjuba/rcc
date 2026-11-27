package io.student.rcc.logkafka.kafka;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record LogEvent(
        String userId,
        String username,
        String eventType,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime eventDate
) {}