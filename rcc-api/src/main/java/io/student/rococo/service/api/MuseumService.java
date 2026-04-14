package io.student.rococo.service.api;

import io.student.rococo.model.MuseumJson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MuseumService {
  MuseumJson findById(String id);

  Page<MuseumJson> all(String title, Pageable pageable);

  MuseumJson create(MuseumJson museum);

  MuseumJson update(MuseumJson museum);
}
