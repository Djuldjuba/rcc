package io.student.rcc.gateway.controller;

import io.student.rcc.gateway.model.ArtistJson;
import io.student.rcc.gateway.service.ArtistClient;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/artist")
public class ArtistController {

    private final ArtistClient artistClient;

    public ArtistController(ArtistClient artistClient) {
        this.artistClient = artistClient;
    }

    @GetMapping
    public Map<String, Object> getArtistsWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "18") int size,
            @RequestParam(required = false) String name) {
        return artistClient.getArtistsPage(name, page, size);
    }

    @GetMapping("/all")
    public List<ArtistJson> getAllArtists() {
        return artistClient.getAllArtists();
    }

    @GetMapping("/{id}")
    public ArtistJson getArtistById(@PathVariable String id) {
        return artistClient.getArtistById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArtistJson createArtist(@Valid @RequestBody ArtistJson artist) {
        return artistClient.createArtist(artist);
    }

    @PutMapping("/{id}")
    public ArtistJson updateArtist(
            @PathVariable String id,
            @Valid @RequestBody ArtistJson artist) {
        return artistClient.updateArtist(id, artist);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteArtist(@PathVariable String id) {
        artistClient.deleteArtist(id);
    }

    @PatchMapping
    public ArtistJson patchArtist(@RequestBody ArtistJson artist) {
        if (artist.id() == null || artist.id().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "id обязателен для PATCH /api/artist");
        }
        return artistClient.patchArtist(artist.id(), artist);
    }
}