package org.peopleshores.capstone_project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Entity
@Table(name = "restoration_jobs")
public class RestorationJob extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artifact_id", nullable = false)
    private Artifact artifact;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restorer_id", nullable = false)
    private User restorer;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status = JobStatus.OPEN;

    @NotNull
    @Column(name = "opened_on", nullable = false)
    private LocalDate openedOn;

    @Column(name = "closed_on")
    private LocalDate closedOn;

    @Size(max = 1000)
    @Column(length = 1000)
    private String summary;

    public RestorationJob() {
    }

    public RestorationJob(User restorer, LocalDate openedOn, String summary) {
        this.restorer = restorer;
        this.openedOn = openedOn;
        this.summary = summary;
    }

    /** Mirrors ck_restoration_jobs_dates so the violation surfaces before the insert. */
    @AssertTrue(message = "closedOn must not be earlier than openedOn")
    public boolean isDateOrderValid() {
        return closedOn == null || openedOn == null || !closedOn.isBefore(openedOn);
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

    public User getRestorer() {
        return restorer;
    }

    public void setRestorer(User restorer) {
        this.restorer = restorer;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public LocalDate getOpenedOn() {
        return openedOn;
    }

    public void setOpenedOn(LocalDate openedOn) {
        this.openedOn = openedOn;
    }

    public LocalDate getClosedOn() {
        return closedOn;
    }

    public void setClosedOn(LocalDate closedOn) {
        this.closedOn = closedOn;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}