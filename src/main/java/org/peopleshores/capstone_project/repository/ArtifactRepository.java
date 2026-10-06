package org.peopleshores.capstone_project.repository;

import org.peopleshores.capstone_project.entity.Artifact;
import org.peopleshores.capstone_project.entity.ArtifactStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArtifactRepository
        extends JpaRepository<Artifact, Long>, JpaSpecificationExecutor<Artifact> {

    /**
     * Loads collection and location alongside each artifact. Without this the
     * DTO mapping would trigger a lazy load per row (N+1), and with
     * open-in-view disabled it would fail outright.
     */
    @Override
    @EntityGraph(attributePaths = {"collection", "location"})
    Page<Artifact> findAll(Specification<Artifact> spec, Pageable pageable);

    Optional<Artifact> findByAccessionNumber(String accessionNumber);

    boolean existsByAccessionNumber(String accessionNumber);

    @EntityGraph(attributePaths = {"collection", "location"})
    Page<Artifact> findByStatus(ArtifactStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"collection", "location"})
    Page<Artifact> findByCollectionId(Long collectionId, Pageable pageable);

    /** Detail screen: one query instead of three round trips. */
    @Query("""
           SELECT a FROM Artifact a
           JOIN FETCH a.collection
           JOIN FETCH a.location
           WHERE a.id = :id
           """)
    Optional<Artifact> findDetailById(@Param("id") Long id);

    /** Artifacts with no inspection since the given date. Drives the backlog view. */
    @Query("""
           SELECT a FROM Artifact a
           WHERE a.status <> org.peopleshores.capstone_project.entity.ArtifactStatus.DEACCESSIONED
             AND NOT EXISTS (
                   SELECT 1 FROM ConditionReport r
                   WHERE r.artifact = a AND r.inspectedOn >= :since)
           ORDER BY a.accessionNumber ASC
           """)
    List<Artifact> findOverdueForInspection(@Param("since") LocalDate since);

    /** Guards FR-11: refuse to delete an artifact on a live exhibition. */
    @Query("""
           SELECT COUNT(e) > 0 FROM ExhibitionArtifact e
           WHERE e.artifact.id = :artifactId
             AND e.exhibition.endDate >= :today
           """)
    boolean isOnActiveExhibition(@Param("artifactId") Long artifactId,
                                 @Param("today") LocalDate today);

    long countByStatus(ArtifactStatus status);
}