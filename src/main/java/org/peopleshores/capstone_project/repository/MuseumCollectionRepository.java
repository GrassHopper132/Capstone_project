package org.peopleshores.capstone_project.repository;

import org.peopleshores.capstone_project.entity.MuseumCollection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MuseumCollectionRepository extends JpaRepository<MuseumCollection, Long> {

    Optional<MuseumCollection> findByName(String name);

    boolean existsByName(String name);

    Page<MuseumCollection> findByCuratorId(Long curatorId, Pageable pageable);

    /** Collection name plus its artifact count, for the collections list screen. */
    @Query("""
           SELECT c.id, c.name, COUNT(a.id)
           FROM MuseumCollection c
           LEFT JOIN c.artifacts a
           GROUP BY c.id, c.name
           ORDER BY c.name ASC
           """)
    List<Object[]> findAllWithArtifactCounts();

    @Query("SELECT COUNT(a) FROM Artifact a WHERE a.collection.id = :collectionId")
    long countArtifacts(@Param("collectionId") Long collectionId);
}
