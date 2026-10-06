package org.peopleshores.capstone_project.dto;

import org.peopleshores.capstone_project.entity.Artifact;

import java.time.LocalDate;

/** What the API returns for an artifact. Never exposes the entity directly. */
public record ArtifactResponse(
        Long id,
        String accessionNumber,
        String title,
        String originCulture,
        String datePeriod,
        String material,
        String description,
        String status,
        LocalDate acquiredOn,
        Long collectionId,
        String collectionName,
        Long locationId,
        String locationLabel
) {

    public static ArtifactResponse from(Artifact a) {
        return new ArtifactResponse(
                a.getId(),
                a.getAccessionNumber(),
                a.getTitle(),
                a.getOriginCulture(),
                a.getDatePeriod(),
                a.getMaterial(),
                a.getDescription(),
                a.getStatus() == null ? null : a.getStatus().name(),
                a.getAcquiredOn(),
                a.getCollection() == null ? null : a.getCollection().getId(),
                a.getCollection() == null ? null : a.getCollection().getName(),
                a.getLocation() == null ? null : a.getLocation().getId(),
                a.getLocation() == null ? null : a.getLocation().getLabel()
        );
    }
}