package org.peopleshores.capstone_project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "artifacts")
public class Artifact extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Immutable once assigned. Enforced in the service layer. */
    @NotBlank
    @Size(max = 40)
    @Column(name = "accession_number", nullable = false, unique = true, updatable = false, length = 40)
    private String accessionNumber;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    @Size(max = 120)
    @Column(name = "origin_culture", length = 120)
    private String originCulture;

    @Size(max = 80)
    @Column(name = "date_period", length = 80)
    private String datePeriod;

    @Size(max = 80)
    @Column(length = 80)
    private String material;

    @Size(max = 1000)
    @Column(length = 1000)
    private String description;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_id", nullable = false)
    private MuseumCollection collection;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArtifactStatus status = ArtifactStatus.STORED;

    @PastOrPresent
    @Column(name = "acquired_on")
    private LocalDate acquiredOn;

    @OneToMany(mappedBy = "artifact", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("inspectedOn DESC")
    private List<ConditionReport> conditionReports = new ArrayList<>();

    @OneToMany(mappedBy = "artifact", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RestorationJob> restorationJobs = new ArrayList<>();

    @OneToMany(mappedBy = "artifact", fetch = FetchType.LAZY)
    private List<ExhibitionArtifact> exhibitionEntries = new ArrayList<>();

    public Artifact() {
    }

    /** Keeps both sides of the association in step. */
    public void addConditionReport(ConditionReport report) {
        conditionReports.add(report);
        report.setArtifact(this);
    }

    public void addRestorationJob(RestorationJob job) {
        restorationJobs.add(job);
        job.setArtifact(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginCulture() {
        return originCulture;
    }

    public void setOriginCulture(String originCulture) {
        this.originCulture = originCulture;
    }

    public String getDatePeriod() {
        return datePeriod;
    }

    public void setDatePeriod(String datePeriod) {
        this.datePeriod = datePeriod;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MuseumCollection getCollection() {
        return collection;
    }

    public void setCollection(MuseumCollection collection) {
        this.collection = collection;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public ArtifactStatus getStatus() {
        return status;
    }

    public void setStatus(ArtifactStatus status) {
        this.status = status;
    }

    public LocalDate getAcquiredOn() {
        return acquiredOn;
    }

    public void setAcquiredOn(LocalDate acquiredOn) {
        this.acquiredOn = acquiredOn;
    }

    public List<ConditionReport> getConditionReports() {
        return conditionReports;
    }

    public void setConditionReports(List<ConditionReport> conditionReports) {
        this.conditionReports = conditionReports;
    }

    public List<RestorationJob> getRestorationJobs() {
        return restorationJobs;
    }

    public void setRestorationJobs(List<RestorationJob> restorationJobs) {
        this.restorationJobs = restorationJobs;
    }

    public List<ExhibitionArtifact> getExhibitionEntries() {
        return exhibitionEntries;
    }

    public void setExhibitionEntries(List<ExhibitionArtifact> exhibitionEntries) {
        this.exhibitionEntries = exhibitionEntries;
    }
}