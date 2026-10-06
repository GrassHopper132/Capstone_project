package org.peopleshores.capstone_project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/** Composite key for the exhibition_artifacts join table. */
@Embeddable
public class ExhibitionArtifactId implements Serializable {

    @Column(name = "exhibition_id")
    private Long exhibitionId;

    @Column(name = "artifact_id")
    private Long artifactId;

    public ExhibitionArtifactId() {
    }

    public ExhibitionArtifactId(Long exhibitionId, Long artifactId) {
        this.exhibitionId = exhibitionId;
        this.artifactId = artifactId;
    }

    public Long getExhibitionId() {
        return exhibitionId;
    }

    public void setExhibitionId(Long exhibitionId) {
        this.exhibitionId = exhibitionId;
    }

    public Long getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(Long artifactId) {
        this.artifactId = artifactId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ExhibitionArtifactId)) {
            return false;
        }
        ExhibitionArtifactId that = (ExhibitionArtifactId) other;
        return Objects.equals(exhibitionId, that.exhibitionId)
            && Objects.equals(artifactId, that.artifactId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exhibitionId, artifactId);
    }
}