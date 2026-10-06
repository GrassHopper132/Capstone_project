package org.peopleshores.capstone_project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exhibitions")
public class Exhibition extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    @Size(max = 120)
    @Column(length = 120)
    private String gallery;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "exhibition", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<ExhibitionArtifact> entries = new ArrayList<>();

    public Exhibition() {
    }

    @AssertTrue(message = "endDate must be after startDate")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || endDate.isAfter(startDate);
    }

    public boolean isActiveOn(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public void addArtifact(Artifact artifact, int displayOrder) {
        entries.add(new ExhibitionArtifact(this, artifact, displayOrder));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGallery() {
        return gallery;
    }

    public void setGallery(String gallery) {
        this.gallery = gallery;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<ExhibitionArtifact> getEntries() {
        return entries;
    }

    public void setEntries(List<ExhibitionArtifact> entries) {
        this.entries = entries;
    }
}