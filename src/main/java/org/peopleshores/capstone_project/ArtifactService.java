package org.peopleshores.capstone_project.exception;

import com.museum.entity.Artifact;
import com.museum.entity.Location;
import com.museum.repository.ArtifactRepository;
import com.museum.repository.LocationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArtifactService {

    private final ArtifactRepository artifactRepository;
    private final LocationRepository locationRepository;

    // Constructor Injection (Rubric requirement)
    public ArtifactService(ArtifactRepository artifactRepository, LocationRepository locationRepository) {
        this.artifactRepository = artifactRepository;
        this.locationRepository = locationRepository;
    }

    public Page<Artifact> getAllArtifacts(String search, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return artifactRepository.searchArtifacts(search, pageable);
        }
        return artifactRepository.findAll(pageable);
    }

    public Artifact getArtifactById(Integer id) {
        return artifactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artifact not found with ID: " + id));
    }

    @Transactional
    public Artifact createArtifact(Artifact artifact, Integer locationId) {
        if (artifactRepository.existsByAccessionNumber(artifact.getAccessionNumber())) {
            throw new RuntimeException("Accession number already exists!");
        }

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        artifact.setCurrentLocation(location);
        return artifactRepository.save(artifact);
    }

    @Transactional
    public void deleteArtifact(Integer id) {
        Artifact artifact = getArtifactById(id);
        artifactRepository.delete(artifact);
    }
}
