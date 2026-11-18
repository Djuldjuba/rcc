package io.student.rcc.gateway.controller;

import io.student.rcc.gateway.model.PaintingJson;
import io.student.rcc.gateway.service.PaintingClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/painting")
public class PaintingController {

    private final PaintingClient paintingClient;

    public PaintingController(PaintingClient paintingClient) {
        this.paintingClient = paintingClient;
    }

    @GetMapping
    public Map<String, Object> getPaintingsWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        return paintingClient.getPaintingsPage(page, size);
    }

    @GetMapping("/{id}")
    public PaintingJson getPaintingById(@PathVariable String id) {
        return paintingClient.getPaintingById(id);
    }

    @GetMapping("/author/{artistId}")
    public Map<String, Object> getPaintingsByArtist(
            @PathVariable String artistId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        return paintingClient.getPaintingsByArtist(artistId, page, size);
    }

    @GetMapping("/museum/{museumId}")
    public Map<String, Object> getPaintingsByMuseum(
            @PathVariable String museumId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        return paintingClient.getPaintingsByMuseum(museumId, page, size);
    }

    @PostMapping
    public PaintingJson createPainting(@RequestBody PaintingJson painting) {
        return paintingClient.addPainting(painting);
    }

    @PutMapping("/{id}")
    public PaintingJson updatePainting(@PathVariable String id,
                                       @RequestBody PaintingJson painting) {
        return paintingClient.updatePainting(id, painting);
    }

    @DeleteMapping("/{id}")
    public void deletePainting(@PathVariable String id) {
        paintingClient.deletePainting(id);
    }

    @PatchMapping
    public PaintingJson patchPainting(@RequestBody PaintingJson painting) {
        if (painting.id() == null || painting.id().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "id обязателен для PATCH /api/painting");
        }
        return paintingClient.patchPainting(painting.id(), painting);
    }
}