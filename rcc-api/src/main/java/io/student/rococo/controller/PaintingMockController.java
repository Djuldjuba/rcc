package io.student.rococo.controller;

import io.student.rococo.model.PaintingJson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/painting")
public class PaintingMockController extends MockController<PaintingJson> {

  @Override
  protected String getMockResourcePath() {
    return "mock/painting";
  }

  @Override
  protected Class<PaintingJson> getItemClass() {
    return PaintingJson.class;
  }

  @Override
  protected String getItemId(PaintingJson item) {
    return item.id().toString();
  }

  @Override
  protected String getResourceNotFoundMessage(String id) {
    return String.format("Картина не найдена по id: %s", id);
  }

  @GetMapping()
  public Page<PaintingJson> getAll(@RequestParam(required = false) String title,
                                   @PageableDefault Pageable pageable) {
    if (title != null && !title.isEmpty()) {
      return getFilteredPage(
          painting -> painting.title().toLowerCase().contains(title.toLowerCase()),
          pageable
      );
    }
    return getFilteredPage(painting -> true, pageable);
  }

  @GetMapping("/{id}")
  public PaintingJson findPaintingById(@PathVariable("id") String id) {
    return findById(id);
  }

  @GetMapping("/author/{id}")
  public Page<PaintingJson> findPaintingByAuthorId(@PathVariable("id") String id,
                                                   @PageableDefault Pageable pageable) {
    return getFilteredPage(
        painting -> painting.artist().id().toString().equals(id),
        pageable
    );
  }

  @PatchMapping()
  public PaintingJson updatePainting(@RequestBody PaintingJson painting) {
    return painting;
  }

  @PostMapping()
  public PaintingJson addPainting(@RequestBody PaintingJson painting) {
    return painting;
  }
}
