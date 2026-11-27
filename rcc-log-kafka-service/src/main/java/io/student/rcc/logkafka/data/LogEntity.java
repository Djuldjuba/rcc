package io.student.rcc.logkafka.data;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "log")
public class LogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "photo_id")
    private String photoId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "log_date", nullable = false)
    private LocalDateTime logDate;
}