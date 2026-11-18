package io.student.rcc.gateway.controller;

import io.student.rcc.gateway.model.MuseumJson;
import io.student.rcc.gateway.service.MuseumClient;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/museum")
public class MuseumController {

    private final MuseumClient museumClient;

    public MuseumController(MuseumClient museumClient) {
        this.museumClient = museumClient;
    }

    @GetMapping
    public Map<String, Object> getMuseumsWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String title) {
        if (title != null && !title.trim().isEmpty()) {
            return museumClient.getMuseumsPage(title, page, size);
        }
        return museumClient.getMuseumsPage(page, size);
    }

    @GetMapping("/all")
    public List<MuseumJson> getAllMuseums() {
        return museumClient.getAllMuseums();
    }

    @GetMapping("/{id}")
    public MuseumJson getMuseumById(@PathVariable String id) {
        return museumClient.getMuseumById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MuseumJson createMuseum(@Valid @RequestBody MuseumJson museum) {
        return museumClient.createMuseum(museum);
    }

    @PutMapping("/{id}")
    public MuseumJson updateMuseum(
            @PathVariable String id,
            @Valid @RequestBody MuseumJson museum) {
        return museumClient.updateMuseum(id, museum);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMuseum(@PathVariable String id) {
        museumClient.deleteMuseum(id);
    }

    @PatchMapping
    public MuseumJson patchMuseum(@RequestBody MuseumJson museum) {
        if (museum.id() == null || museum.id().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "id обязателен для PATCH /api/museum");
        }
        return museumClient.patchMuseum(museum.id(), museum);
    }
}