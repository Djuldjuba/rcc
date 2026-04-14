package io.student.rococo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.student.rococo.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class MockController<T> {

  protected final List<T> items = new ArrayList<>();
  protected final ObjectMapper objectMapper = new ObjectMapper();

  protected abstract String getMockResourcePath();

  protected abstract Class<T> getItemClass();

  protected abstract String getItemId(T item);

  protected abstract String getResourceNotFoundMessage(String id);

  @PostConstruct
  public void loadMockData() throws IOException {
    Resource[] resources = new PathMatchingResourcePatternResolver()
        .getResources(String.format("classpath:%s/*.json", getMockResourcePath()));
    for (Resource resource : resources) {
      T item = objectMapper.readValue(resource.getInputStream(), getItemClass());
      items.add(item);
    }
  }

  protected Page<T> getFilteredPage(Predicate<T> filter, Pageable pageable) {
    List<T> filtered = items.stream()
        .filter(filter)
        .collect(Collectors.toList());

    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), filtered.size());

    List<T> pageContent = start < filtered.size()
        ? filtered.subList(start, end)
        : new ArrayList<>();

    return new PageImpl<>(pageContent, pageable, filtered.size());
  }

  protected T findById(String id) {
    return items.stream()
        .filter(item -> getItemId(item).equals(id))
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException(getResourceNotFoundMessage(id)));
  }
}
