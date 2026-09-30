package org.peopleshores.capstone_project.exception;



import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "condition_reports")
public class ConditionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reportId;

    @Column(nullable = false)
    private LocalDate inspectionDate;

    @Column(nullable = false)
    private String conditionStatus;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artifact_id", nullable = false)
    private Artifact artifact;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_id", nullable = false)
    private User inspector;

    // Getters and Setters omitted
}
