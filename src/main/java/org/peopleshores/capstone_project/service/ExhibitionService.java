package org.peopleshores.capstone_project.service;

import org.peopleshores.capstone_project.dto.ExhibitionArtifactRequest;
import org.peopleshores.capstone_project.dto.ExhibitionRequest;
import org.peopleshores.capstone_project.entity.Artifact;
import org.peopleshores.capstone_project.entity.ArtifactStatus;
import org.peopleshores.capstone_project.entity.Exhibition;
import org.peopleshores.capstone_project.entity.ExhibitionArtifact;
import org.peopleshores.capstone_project.exception.BusinessRuleException;
import org.peopleshores.capstone_project.exception.DuplicateResourceException;
import org.peopleshores.capstone_project.exception.ResourceNotFoundException;
import org.peopleshores.capstone_project.repository.ArtifactRepository;
import org.peopleshores.capstone_project.repository.ExhibitionArtifactRepository;
import org.peopleshores.capstone_project.repository.ExhibitionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Exhibition scheduling and the seasonal rotation of objects on display.
 *
 * An exhibition is a date range, so "what is on show this season" is a query
 * rather than a stored flag. Attaching an artifact to a show that is running
 * today moves it to ON_DISPLAY; detaching it returns it to STORED unless some
 * other live show still needs it.
 */
@Service
@Transactional(readOnly = true)
public class ExhibitionService {

    private final ExhibitionRepository exhibitionRepository;
    private final ExhibitionArtifactRepository entryRepository;
    private final ArtifactRepository artifactRepository;

    public ExhibitionService(ExhibitionRepository exhibitionRepository,
                             ExhibitionArtifactRepository entryRepository,
                             ArtifactRepository artifactRepository) {
        this.exhibitionRepository = exhibitionRepository;
        this.entryRepository = entryRepository;
        this.artifactRepository = artifactRepository;
    }

    public Page<Exhibition> list(Pageable pageable) {
        return exhibitionRepository.findAll(pageable);
    }

    /** Everything running on the given day. This is the seasonal view. */
    public List<Exhibition> runningOn(LocalDate date) {
        return exhibitionRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date);
    }

    public Exhibition getDetail(Long id) {
        return exhibitionRepository.findDetailById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Exhibition", id));
    }

    @Transactional
    public Exhibition create(ExhibitionRequest request) {
        requireValidRange(request);

        Exhibition exhibition = new Exhibition();
        exhibition.setTitle(request.title());
        exhibition.setGallery(request.gallery());
        exhibition.setStartDate(request.startDate());
        exhibition.setEndDate(request.endDate());

        return exhibitionRepository.save(exhibition);
    }

    @Transactional
    public Exhibition update(Long id, ExhibitionRequest request) {
        requireValidRange(request);

        Exhibition exhibition = exhibitionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Exhibition", id));

        exhibition.setTitle(request.title());
        exhibition.setGallery(request.gallery());
        exhibition.setStartDate(request.startDate());
        exhibition.setEndDate(request.endDate());

        return exhibitionRepository.save(exhibition);
    }

    /** A show that is open to the public today cannot be struck from the record. */
    @Transactional
    public void delete(Long id) {
        Exhibition exhibition = exhibitionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Exhibition", id));

        if (exhibition.isActiveOn(LocalDate.now())) {
            throw new BusinessRuleException(
                    "Exhibition \"" + exhibition.getTitle()
                            + "\" is currently running and cannot be deleted. End it first.");
        }

        exhibitionRepository.delete(exhibition);
    }

    /**
     * Puts an artifact into a show. If the show is running today the artifact
     * goes on display immediately, which is the whole point of the feature.
     */
    @Transactional
    public Exhibition addArtifact(Long exhibitionId, ExhibitionArtifactRequest request) {
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> ResourceNotFoundException.of("Exhibition", exhibitionId));

        Artifact artifact = artifactRepository.findById(request.artifactId())
                .orElseThrow(() -> ResourceNotFoundException.of("Artifact", request.artifactId()));

        entryRepository.findByExhibitionIdAndArtifactId(exhibitionId, artifact.getId())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Artifact " + artifact.getAccessionNumber()
                                    + " is already in this exhibition");
                });

        if (artifact.getStatus() == ArtifactStatus.IN_RESTORATION) {
            throw new BusinessRuleException(
                    "Artifact " + artifact.getAccessionNumber()
                            + " is in the conservation workshop and cannot be exhibited");
        }

        if (artifact.getStatus() == ArtifactStatus.DEACCESSIONED) {
            throw new BusinessRuleException(
                    "Artifact " + artifact.getAccessionNumber()
                            + " has been deaccessioned and is no longer part of the collection");
        }

        int order = request.displayOrder() != null
                ? request.displayOrder()
                : entryRepository.nextDisplayOrder(exhibitionId);

        entryRepository.save(new ExhibitionArtifact(exhibition, artifact, order));

        if (exhibition.isActiveOn(LocalDate.now())) {
            artifact.setStatus(ArtifactStatus.ON_DISPLAY);
            artifactRepository.save(artifact);
        }

        return getDetail(exhibitionId);
    }

    /**
     * Takes an artifact out of a show. It returns to storage only when no other
     * live exhibition still has it, so an object in two concurrent shows is not
     * marked as stored while it is still hanging on a wall.
     */
    @Transactional
    public Exhibition removeArtifact(Long exhibitionId, Long artifactId) {
        ExhibitionArtifact entry = entryRepository
                .findByExhibitionIdAndArtifactId(exhibitionId, artifactId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Artifact " + artifactId + " is not in exhibition " + exhibitionId));

        Artifact artifact = entry.getArtifact();

        entryRepository.delete(entry);
        entryRepository.flush();

        if (artifact.getStatus() == ArtifactStatus.ON_DISPLAY
                && !artifactRepository.isOnActiveExhibition(artifactId, LocalDate.now())) {
            artifact.setStatus(ArtifactStatus.STORED);
            artifactRepository.save(artifact);
        }

        return getDetail(exhibitionId);
    }

    private void requireValidRange(ExhibitionRequest request) {
        if (!request.endDate().isAfter(request.startDate())) {
            throw new BusinessRuleException("endDate must fall after startDate");
        }
    }
}