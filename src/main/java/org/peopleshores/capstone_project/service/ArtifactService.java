package org.peopleshores.capstone_project.service;

import org.peopleshores.capstone_project.dto.ArtifactRequest;
import org.peopleshores.capstone_project.entity.Artifact;
import org.peopleshores.capstone_project.entity.ArtifactStatus;
import org.peopleshores.capstone_project.entity.Location;
import org.peopleshores.capstone_project.entity.MuseumCollection;
import org.peopleshores.capstone_project.exception.BusinessRuleException;
import org.peopleshores.capstone_project.exception.DuplicateResourceException;
import org.peopleshores.capstone_project.exception.ResourceNotFoundException;
import org.peopleshores.capstone_project.repository.ArtifactRepository;
import org.peopleshores.capstone_project.repository.ArtifactSpecifications;
import org.peopleshores.capstone_project.repository.LocationRepository;
import org.peopleshores.capstone_project.repository.MuseumCollectionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.peopleshores.capstone_project.repository.ArtifactSpecifications;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/** Business rules for artifacts. Controllers hold no logic of their own. */
@Service
@Transactional(readOnly = true)
public class ArtifactService {

    private final ArtifactRepository artifactRepository;
    private final MuseumCollectionRepository collectionRepository;
    private final LocationRepository locationRepository;

    /** Constructor injection, per the rubric. */
    public ArtifactService(ArtifactRepository artifactRepository,
                           MuseumCollectionRepository collectionRepository,
                           LocationRepository locationRepository) {
        this.artifactRepository = artifactRepository;
        this.collectionRepository = collectionRepository;
        this.locationRepository = locationRepository;
    }

    public Page<Artifact> search(ArtifactStatus status,
                                 String material,
                                 Long collectionId,
                                 String search,
                                 Pageable pageable) {
        String term = (search == null || search.isBlank()) ? null : search.trim();
        String mat = (material == null || material.isBlank()) ? null : material.trim();

        Specification<Artifact> spec = ArtifactSpecifications.hasStatus(status)
                .and(ArtifactSpecifications.hasMaterial(mat))
                .and(ArtifactSpecifications.inCollection(collectionId))
                .and(ArtifactSpecifications.matches(term));

        return artifactRepository.findAll(spec, pageable);
    }

    public Artifact getById(Long id) {
        return artifactRepository.findDetailById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Artifact", id));
    }

    /** FR-5: accession numbers are unique and assigned once. */
    @Transactional
    public Artifact create(ArtifactRequest request) {
        if (artifactRepository.existsByAccessionNumber(request.accessionNumber())) {
            throw new DuplicateResourceException(
                    "Accession number " + request.accessionNumber() + " already exists");
        }

        Artifact artifact = new Artifact();
        artifact.setAccessionNumber(request.accessionNumber());
        applyEditableFields(artifact, request);
        artifact.setStatus(ArtifactStatus.STORED);

        return artifactRepository.save(artifact);
    }

    /** FR-5 again: the accession number is immutable, so it is ignored here. */
    @Transactional
    public Artifact update(Long id, ArtifactRequest request) {
        Artifact artifact = artifactRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Artifact", id));
        applyEditableFields(artifact, request);
        return artifactRepository.save(artifact);
    }

    /** FR-11: an artifact on a live exhibition cannot be removed. */
    @Transactional
    public void delete(Long id) {
        Artifact artifact = artifactRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Artifact", id));

        if (artifactRepository.isOnActiveExhibition(id, LocalDate.now())) {
            throw new BusinessRuleException(
                    "Artifact " + artifact.getAccessionNumber()
                            + " is part of an active exhibition and cannot be deleted");
        }

        artifactRepository.delete(artifact);
    }

    @Transactional
    public Artifact changeStatus(Long id, ArtifactStatus status) {
        Artifact artifact = artifactRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Artifact", id));
        artifact.setStatus(status);
        return artifactRepository.save(artifact);
    }

    private void applyEditableFields(Artifact artifact, ArtifactRequest request) {
        MuseumCollection collection = collectionRepository.findById(request.collectionId())
                .orElseThrow(() -> ResourceNotFoundException.of("Collection", request.collectionId()));

        Location location = locationRepository.findById(request.locationId())
                .orElseThrow(() -> ResourceNotFoundException.of("Location", request.locationId()));

        artifact.setTitle(request.title());
        artifact.setOriginCulture(request.originCulture());
        artifact.setDatePeriod(request.datePeriod());
        artifact.setMaterial(request.material());
        artifact.setDescription(request.description());
        artifact.setAcquiredOn(request.acquiredOn());
        artifact.setImageUrl(request.imageUrl());
        artifact.setCollection(collection);
        artifact.setLocation(location);
    }
}