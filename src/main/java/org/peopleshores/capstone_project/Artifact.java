package org.peopleshores.capstone_project.exception;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.Entity;

@Entity
@Table(name = "artifacts")
public class Artifact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer artifactId;

    @Column(unique = true, nullable = false)
    private String accessionNumber;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate acquisitionDate;

    // Many Artifacts belong to One Location
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_location_id", nullable = false)
    private Location currentLocation;

    // One Artifact has Many Condition Reports
    @OneToMany(mappedBy = "artifact", cascade = CascadeType.ALL)
    private List<ConditionReport> conditionReports;

    // Many-to-Many relationship with Exhibits
    @ManyToMany(mappedBy = "featuredArtifacts")
    private List<Exhibit> exhibits;

    // Getters and Setters omitted
}
