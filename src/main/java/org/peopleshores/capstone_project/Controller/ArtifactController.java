package org.peopleshores.capstone_project.controller;

import jakarta.validation.Valid;
import org.peopleshores.capstone_project.dto.ArtifactRequest;
import org.peopleshores.capstone_project.dto.ArtifactResponse;
import org.peopleshores.capstone_project.entity.ArtifactStatus;
import org.peopleshores.capstone_project.service.ArtifactService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Artifact endpoints.
 *
 * Authority model, enforced by @PreAuthorize and verified by the security tests:
 *   READ    - every authenticated role, because all staff need to find objects.
 *   WRITE   - ADMIN and CURATOR, who are responsible for the catalogue record.
 *   STATUS  - adds RESTORER, who moves objects in and out of the workshop.
 *   DELETE  - ADMIN only. Deaccession is irreversible.
 */
@RestController
@RequestMapping("/api/v1/artifacts")
public class ArtifactController {

    private final ArtifactService artifactService;

    public ArtifactController(ArtifactService artifactService) {
        this.artifactService = artifactService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','CURATOR','RESTORER')")
    @GetMapping
    public Page<ArtifactResponse> list(
            @RequestParam(required = false) ArtifactStatus status,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) Long collectionId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "accessionNumber") Pageable pageable) {

        return artifactService.search(status, material, collectionId, search, pageable)
                .map(ArtifactResponse::from);
    }

    @PreAuthorize("hasAnyRole('ADMIN','CURATOR','RESTORER')")
    @GetMapping("/{id}")
    public ArtifactResponse getOne(@PathVariable Long id) {
        return ArtifactResponse.from(artifactService.getById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CURATOR')")
    @PostMapping
    public ResponseEntity<ArtifactResponse> create(@Valid @RequestBody ArtifactRequest request) {
        ArtifactResponse created = ArtifactResponse.from(artifactService.create(request));
        return ResponseEntity.created(URI.create("/api/v1/artifacts/" + created.id())).body(created);
    }

    @PreAuthorize("hasAnyRole('ADMIN','CURATOR')")
    @PutMapping("/{id}")
    public ArtifactResponse update(@PathVariable Long id, @Valid @RequestBody ArtifactRequest request) {
        return ArtifactResponse.from(artifactService.update(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CURATOR','RESTORER')")
    @PatchMapping("/{id}/status")
    public ArtifactResponse changeStatus(@PathVariable Long id, @RequestParam ArtifactStatus status) {
        return ArtifactResponse.from(artifactService.changeStatus(id, status));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        artifactService.delete(id);
    }
}