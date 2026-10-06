package org.peopleshores.capstone_project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

/**
 * The many-to-many join between Exhibition and Artifact, modelled as its own
 * entity because it carries display_order.
 */
@Entity
@Table(name = "exhibition_artifacts")
public class ExhibitionArtifact {

    @EmbeddedId
    private ExhibitionArtifactId id = new ExhibitionArtifactId();

    @MapsId("exhibitionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exhibition_id", nullable = false)
    private Exhibition exhibition;

    @MapsId("artifactId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artifact_id", nullable = false)
    private Artifact artifact;

    @Positive
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public ExhibitionArtifact() {
    }

    public ExhibitionArtifact(Exhibition exhibition, Artifact artifact, Integer displayOrder) {
        this.exhibition = exhibition;
        this.artifact = artifact;
        this.displayOrder = displayOrder;
        this.id = new ExhibitionArtifactId(exhibition.getId(), artifact.getId());
    }

    public ExhibitionArtifactId getId() {
        return id;
    }

    public void setId(ExhibitionArtifactId id) {
        this.id = id;
    }

    public Exhibition getExhibition() {
        return exhibition;
    }

    public void setExhibition(Exhibition exhibition) {
        this.exhibition = exhibition;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}