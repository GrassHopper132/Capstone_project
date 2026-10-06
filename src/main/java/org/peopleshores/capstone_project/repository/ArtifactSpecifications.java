package org.peopleshores.capstone_project.repository;

import org.peopleshores.capstone_project.entity.Artifact;
import org.peopleshores.capstone_project.entity.ArtifactStatus;
import org.springframework.data.jpa.domain.Specification;

/** Composable filters for the artifact list endpoint. */
public final class ArtifactSpecifications {

    private ArtifactSpecifications() {
    }

    public static Specification<Artifact> hasStatus(ArtifactStatus status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Artifact> hasMaterial(String material) {
        return (root, query, cb) ->
                material == null ? cb.conjunction()
                                 : cb.equal(cb.lower(root.get("material")), material.toLowerCase());
    }

    public static Specification<Artifact> inCollection(Long collectionId) {
        return (root, query, cb) ->
                collectionId == null ? cb.conjunction()
                                     : cb.equal(root.get("collection").get("id"), collectionId);
    }

    /** Matches the search term against title or accession number. */
    public static Specification<Artifact> matches(String term) {
        return (root, query, cb) -> {
            if (term == null) {
                return cb.conjunction();
            }
            String like = "%" + term.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("accessionNumber")), like));
        };
    }
}