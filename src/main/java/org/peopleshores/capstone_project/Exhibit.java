package org.peopleshores.capstone_project.exception;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "exhibits")
public class Exhibit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer exhibitId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    // The Junction Table mapping
    @ManyToMany
    @JoinTable(
            name = "exhibit_artifacts",
            joinColumns = @JoinColumn(name = "exhibit_id"),
            inverseJoinColumns = @JoinColumn(name = "artifact_id")
    )
    private List<Artifact> featuredArtifacts;

    // Getters and Setters omitted
}
