package io.student.rcc.logkafka.service;

import io.student.rcc.logkafka.data.LogEntity;
import io.student.rcc.logkafka.data.LogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LogService {

    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Transactional
    public void save(String userId, String photoId, String eventType,
                     LocalDateTime eventDate) {
        LogEntity entity = new LogEntity();
        entity.setUserId(userId);
        entity.setPhotoId(photoId);
        entity.setEventType(eventType);
        entity.setEventDate(eventDate);
        entity.setLogDate(LocalDateTime.now());
        logRepository.save(entity);
    }
}