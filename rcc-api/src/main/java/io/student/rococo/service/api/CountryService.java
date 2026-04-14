package io.student.rococo.service.api;

import io.student.rococo.model.CountryJson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CountryService {
  Page<CountryJson> all(String name, Pageable pageable);

  CountryJson findCountryByName(String name);

  CountryJson findCountryById(String id);
}
