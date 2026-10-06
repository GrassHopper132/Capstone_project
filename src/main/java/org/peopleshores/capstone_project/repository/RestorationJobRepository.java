package org.peopleshores.capstone_project.repository;

import org.peopleshores.capstone_project.entity.JobStatus;
import org.peopleshores.capstone_project.entity.RestorationJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RestorationJobRepository extends JpaRepository<RestorationJob, Long> {

    Page<RestorationJob> findByStatus(JobStatus status, Pageable pageable);

    List<RestorationJob> findByArtifactIdOrderByOpenedOnDesc(Long artifactId);

    Page<RestorationJob> findByRestorerId(Long restorerId, Pageable pageable);

    /** The restorer's work queue: anything not yet closed, oldest first. */
    @Query("""
           SELECT j FROM RestorationJob j
           JOIN FETCH j.artifact
           WHERE j.status <> org.peopleshores.capstone_project.entity.JobStatus.CLOSED
           ORDER BY j.openedOn ASC
           """)
    List<RestorationJob> findOpenQueue();

    boolean existsByArtifactIdAndStatusNot(Long artifactId, JobStatus status);

    long countByStatus(JobStatus status);
}
