package io.student.rcc.userdata.kafka;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record UserUpdatedEvent(
        String userId,
        String username,
        String eventType,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime eventDate
) {}