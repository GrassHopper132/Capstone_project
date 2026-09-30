package org.peopleshores.capstone_project.Controller;


import com.museum.entity.Artifact;
import com.museum.service.ArtifactService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/artifacts")
@CrossOrigin(origins = "http://localhost:5173") // Enables React frontend to talk to this API
public class ArtifactController {

    private final ArtifactService artifactService;

    public ArtifactController(ArtifactService artifactService) {
        this.artifactService = artifactService;
    }

    // GET /api/artifacts?search=vase&page=0&size=10
    @GetMapping
    public ResponseEntity<Page<Artifact>> getArtifacts(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(artifactService.getAllArtifacts(search, PageRequest.of(page, size)));
    }

    // GET /api/artifacts/5
    @GetMapping("/{id}")
    public ResponseEntity<Artifact> getArtifact(@PathVariable Integer id) {
        return ResponseEntity.ok(artifactService.getArtifactById(id));
    }

    // POST /api/artifacts?locationId=2
    @PostMapping
    public ResponseEntity<Artifact> createArtifact(
            @RequestBody Artifact artifact,
            @RequestParam Integer locationId) {

        Artifact savedArtifact = artifactService.createArtifact(artifact, locationId);
        return new ResponseEntity<>(savedArtifact, HttpStatus.CREATED);
    }

    // DELETE /api/artifacts/5
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArtifact(@PathVariable Integer id) {
        artifactService.deleteArtifact(id);
        return ResponseEntity.noContent().build();
    }
}
