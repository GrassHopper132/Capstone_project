package org.peopleshores.capstone_project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;

/** An inspection record. Append-only: never updated after creation. */
@Entity
@Table(name = "condition_reports")
public class ConditionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artifact_id", nullable = false)
    private Artifact artifact;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inspector_id", nullable = false)
    private User inspector;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConditionGrade grade;

    @Size(max = 1000)
    @Column(length = 1000)
    private String notes;

    @NotNull
    @PastOrPresent
    @Column(name = "inspected_on", nullable = false)
    private LocalDate inspectedOn;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public ConditionReport() {
    }

    public ConditionReport(User inspector, ConditionGrade grade, String notes, LocalDate inspectedOn) {
        this.inspector = inspector;
        this.grade = grade;
        this.notes = notes;
        this.inspectedOn = inspectedOn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

    public User getInspector() {
        return inspector;
    }

    public void setInspector(User inspector) {
        this.inspector = inspector;
    }

    public ConditionGrade getGrade() {
        return grade;
    }

    public void setGrade(ConditionGrade grade) {
        this.grade = grade;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDate getInspectedOn() {
        return inspectedOn;
    }

    public void setInspectedOn(LocalDate inspectedOn) {
        this.inspectedOn = inspectedOn;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}