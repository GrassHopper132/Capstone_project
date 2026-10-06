package org.peopleshores.capstone_project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * A curatorial collection. Named MuseumCollection rather than Collection so it
 * does not clash with java.util.Collection.
 */
@Entity
@Table(name = "collections")
public class MuseumCollection extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curator_id")
    private User curator;

    @OneToMany(mappedBy = "collection", fetch = FetchType.LAZY)
    private List<Artifact> artifacts = new ArrayList<>();

    public MuseumCollection() {
    }

    public MuseumCollection(String name, String description, User curator) {
        this.name = name;
        this.description = description;
        this.curator = curator;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getCurator() {
        return curator;
    }

    public void setCurator(User curator) {
        this.curator = curator;
    }

    public List<Artifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<Artifact> artifacts) {
        this.artifacts = artifacts;
    }
}