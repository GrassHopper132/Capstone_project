package org.peopleshores.capstone_project.controller;

import jakarta.validation.Valid;
import org.peopleshores.capstone_project.dto.ExhibitionArtifactRequest;
import org.peopleshores.capstone_project.dto.ExhibitionDetailResponse;
import org.peopleshores.capstone_project.dto.ExhibitionRequest;
import org.peopleshores.capstone_project.dto.ExhibitionResponse;
import org.peopleshores.capstone_project.service.ExhibitionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * Exhibition scheduling and the seasonal rotation of objects on display.
 *
 * Authority model:
 *   READ    - every authenticated role.
 *   WRITE   - ADMIN and CURATOR, who plan the programme.
 *   DELETE  - ADMIN only, and never while the show is open.
 */
@RestController
@RequestMapping("/api/v1/exhibitions")
public class ExhibitionController {

    private final ExhibitionService exhibitionService;

    public ExhibitionController(ExhibitionService exhibitionService) {
        this.exhibitionService = exhibitionService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','CURATOR','RESTORER')")
    @GetMapping
    public Page<ExhibitionResponse> list(
            @PageableDefault(size = 20, sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return exhibitionService.list(pageable).map(ExhibitionResponse::from);
    }

    /** What the public would see in the galleries today. */
    @PreAuthorize("hasAnyRole('ADMIN','CURATOR','RESTORER')")
    @GetMapping("/current")
    public List<ExhibitionResponse> current(
            @RequestParam(required = false) LocalDate on) {
        LocalDate date = on != null ? on : LocalDate.now();
        return exhibitionService.runningOn(date).stream()
                .map(ExhibitionResponse::from)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','CURATOR','RESTORER')")
    @GetMapping("/{id}")
    public ExhibitionDetailResponse getOne(@PathVariable Long id) {
        return ExhibitionDetailResponse.from(exhibitionService.getDetail(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CURATOR')")
    @PostMapping
    public ResponseEntity<ExhibitionResponse> create(@Valid @RequestBody ExhibitionRequest request) {
        ExhibitionResponse created = ExhibitionResponse.from(exhibitionService.create(request));
        return ResponseEntity.created(URI.create("/api/v1/exhibitions/" + created.id())).body(created);
    }

    @PreAuthorize("hasAnyRole('ADMIN','CURATOR')")
    @PutMapping("/{id}")
    public ExhibitionResponse update(@PathVariable Long id, @Valid @RequestBody ExhibitionRequest request) {
        return ExhibitionResponse.from(exhibitionService.update(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        exhibitionService.delete(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','CURATOR')")
    @PostMapping("/{id}/artifacts")
    public ExhibitionDetailResponse addArtifact(@PathVariable Long id,
                                                @Valid @RequestBody ExhibitionArtifactRequest request) {
        return ExhibitionDetailResponse.from(exhibitionService.addArtifact(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CURATOR')")
    @DeleteMapping("/{id}/artifacts/{artifactId}")
    public ExhibitionDetailResponse removeArtifact(@PathVariable Long id,
                                                   @PathVariable Long artifactId) {
        return ExhibitionDetailResponse.from(exhibitionService.removeArtifact(id, artifactId));
    }
}