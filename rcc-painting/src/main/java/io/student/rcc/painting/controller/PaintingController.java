package io.student.rcc.painting.controller;

import io.student.rcc.painting.domain.CreatePaintingRequest;
import io.student.rcc.painting.domain.PaintingResponse;
import io.student.rcc.painting.domain.UpdatePaintingRequest;
import io.student.rcc.painting.service.PaintingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/painting")
public class PaintingController {

    private final PaintingService paintingService;

    public PaintingController(PaintingService paintingService) {
        this.paintingService = paintingService;
    }

    @GetMapping
    public ResponseEntity<List<PaintingResponse>> getAllPaintings() {
        return ResponseEntity.ok(paintingService.getAllPaintings());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<PaintingResponse>> getPaintingsPage(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(paintingService.getPaintingsPage(title, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaintingResponse> getPaintingById(@PathVariable String id) {
        return ResponseEntity.ok(paintingService.getPaintingById(id));
    }

    @GetMapping("/artist/{artistId}")
    public ResponseEntity<Page<PaintingResponse>> getPaintingsByArtist(
            @PathVariable String artistId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(paintingService.getPaintingsByArtist(artistId, page, size));
    }

    @GetMapping("/museum/{museumId}")
    public ResponseEntity<Page<PaintingResponse>> getPaintingsByMuseum(
            @PathVariable String museumId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(paintingService.getPaintingsByMuseum(museumId, page, size));
    }

    @PostMapping
    public ResponseEntity<PaintingResponse> createPainting(@Valid @RequestBody CreatePaintingRequest request) {
        PaintingResponse response = paintingService.addPainting(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaintingResponse> updatePainting(
            @PathVariable String id,
            @Valid @RequestBody UpdatePaintingRequest request) {
        PaintingResponse response = paintingService.updatePainting(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePainting(@PathVariable String id) {
        paintingService.deletePainting(id);
        return ResponseEntity.noContent().build();
    }
}