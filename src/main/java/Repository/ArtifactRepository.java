package Repository;


import com.museum.entity.Artifact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtifactRepository extends JpaRepository<Artifact, Integer> {

    // Custom query to search by name or accession number with pagination
    @Query("SELECT a FROM Artifact a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(a.accessionNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Artifact> searchArtifacts(@Param("searchTerm") String searchTerm, Pageable pageable);

    boolean existsByAccessionNumber(String accessionNumber);
}
