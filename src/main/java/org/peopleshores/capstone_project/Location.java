package org.peopleshores.capstone_project.exception;

import jakarta.persistence.*;
import java.util.List;
import jakarta.persistence.Entity;

@Entity
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer locationId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String zoneType;

    @Column(nullable = false)
    private Boolean climateControlled = true;

    // One Location can hold multiple Artifacts
    @OneToMany(mappedBy = "currentLocation")
    private List<Artifact> artifacts;

    // Getters and Setters omitted
}
