package io.student.rococo.controller;

import io.student.rococo.model.ArtistJson;
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
@RequestMapping("/api/artist")
public class ArtistMockController extends MockController<ArtistJson> {

  @Override
  protected String getMockResourcePath() {
    return "mock/artist";
  }

  @Override
  protected Class<ArtistJson> getItemClass() {
    return ArtistJson.class;
  }

  @Override
  protected String getItemId(ArtistJson item) {
    return item.id().toString();
  }

  @Override
  protected String getResourceNotFoundMessage(String id) {
    return String.format("Художник не найден по id: %s", id);
  }

  @GetMapping()
  public Page<ArtistJson> getAll(@RequestParam(required = false) String name,
                                 @PageableDefault Pageable pageable) {
    if (name != null && !name.isEmpty()) {
      return getFilteredPage(
          artist -> artist.name().toLowerCase().contains(name.toLowerCase()),
          pageable
      );
    }
    return getFilteredPage(artist -> true, pageable);
  }

  @GetMapping("/{id}")
  public ArtistJson findArtistById(@PathVariable("id") String id) {
    return findById(id);
  }

  @PatchMapping()
  public ArtistJson updateArtist(@RequestBody ArtistJson artist) {
    return artist;
  }

  @PostMapping()
  public ArtistJson addArtist(@RequestBody ArtistJson artist) {
    return artist;
  }

}
