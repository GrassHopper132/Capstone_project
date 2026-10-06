package org.peopleshores.capstone_project.repository;

import org.peopleshores.capstone_project.entity.Exhibition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExhibitionRepository extends JpaRepository<Exhibition, Long> {

    Page<Exhibition> findByEndDateGreaterThanEqual(LocalDate date, Pageable pageable);

    List<Exhibition> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate start, LocalDate end);

    /** Exhibition with its artifact entries loaded, in display order. */
    @Query("""
           SELECT DISTINCT e FROM Exhibition e
           LEFT JOIN FETCH e.entries en
           LEFT JOIN FETCH en.artifact
           WHERE e.id = :id
           """)
    Optional<Exhibition> findDetailById(@Param("id") Long id);
}
