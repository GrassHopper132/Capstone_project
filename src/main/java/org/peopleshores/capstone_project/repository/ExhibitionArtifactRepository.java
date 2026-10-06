package org.peopleshores.capstone_project.repository;

import org.peopleshores.capstone_project.entity.ExhibitionArtifact;
import org.peopleshores.capstone_project.entity.ExhibitionArtifactId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExhibitionArtifactRepository
        extends JpaRepository<ExhibitionArtifact, ExhibitionArtifactId> {

    List<ExhibitionArtifact> findByExhibitionIdOrderByDisplayOrderAsc(Long exhibitionId);

    List<ExhibitionArtifact> findByArtifactId(Long artifactId);

    Optional<ExhibitionArtifact> findByExhibitionIdAndArtifactId(Long exhibitionId, Long artifactId);

    void deleteByExhibitionIdAndArtifactId(Long exhibitionId, Long artifactId);

    /** Next free slot when attaching an artifact without an explicit order. */
    @Query("""
           SELECT COALESCE(MAX(e.displayOrder), 0) + 1
           FROM ExhibitionArtifact e
           WHERE e.exhibition.id = :exhibitionId
           """)
    int nextDisplayOrder(@Param("exhibitionId") Long exhibitionId);
}
