package org.peopleshores.capstone_project.repository;

import org.peopleshores.capstone_project.entity.ConditionGrade;
import org.peopleshores.capstone_project.entity.ConditionReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConditionReportRepository extends JpaRepository<ConditionReport, Long> {

    Page<ConditionReport> findByArtifactIdOrderByInspectedOnDesc(Long artifactId, Pageable pageable);

    List<ConditionReport> findByGrade(ConditionGrade grade);

    /** Most recent report for one artifact, shown on the detail screen. */
    @Query("""
           SELECT r FROM ConditionReport r
           WHERE r.artifact.id = :artifactId
           ORDER BY r.inspectedOn DESC, r.id DESC
           LIMIT 1
           """)
    Optional<ConditionReport> findLatestForArtifact(@Param("artifactId") Long artifactId);

    @Query("""
           SELECT r FROM ConditionReport r
           JOIN FETCH r.artifact
           WHERE r.grade IN (org.peopleshores.capstone_project.entity.ConditionGrade.POOR,
                             org.peopleshores.capstone_project.entity.ConditionGrade.CRITICAL)
           ORDER BY r.inspectedOn DESC
           """)
    List<ConditionReport> findRecentConcerns();
}
