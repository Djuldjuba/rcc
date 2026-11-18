package io.student.rcc.museum.controller;

import io.student.rcc.museum.domain.CreateMuseumRequest;
import io.student.rcc.museum.domain.MuseumResponse;
import io.student.rcc.museum.domain.UpdateMuseumRequest;
import io.student.rcc.museum.service.MuseumService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/museum")
public class MuseumController {

    private final MuseumService museumService;

    public MuseumController(MuseumService museumService) {
        this.museumService = museumService;
    }

    @GetMapping
    public ResponseEntity<List<MuseumResponse>> getAllMuseums() {
        return ResponseEntity.ok(museumService.getAllMuseums());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<MuseumResponse>> getMuseumsPage(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(museumService.getMuseumsPage(title, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MuseumResponse> getMuseumById(@PathVariable String id) {
        return ResponseEntity.ok(museumService.getMuseumById(id));
    }

    @GetMapping("/country/{countryId}")
    public ResponseEntity<Page<MuseumResponse>> getMuseumsByCountry(
            @PathVariable String countryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(museumService.getMuseumsByCountry(countryId, page, size));
    }

    @PostMapping
    public ResponseEntity<MuseumResponse> createMuseum(@Valid @RequestBody CreateMuseumRequest request) {
        MuseumResponse response = museumService.createMuseum(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MuseumResponse> updateMuseum(
            @PathVariable String id,
            @Valid @RequestBody UpdateMuseumRequest request) {
        MuseumResponse response = museumService.updateMuseum(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMuseum(@PathVariable String id) {
        museumService.deleteMuseum(id);
        return ResponseEntity.noContent().build();
    }
}