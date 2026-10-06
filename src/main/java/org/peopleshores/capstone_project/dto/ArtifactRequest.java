package org.peopleshores.capstone_project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Incoming payload for creating or updating an artifact. Keeping this separate
 * from the entity stops clients from setting ids, timestamps, or relationships
 * they should not control.
 */
public record ArtifactRequest(

        @NotBlank @Size(max = 40) String accessionNumber,

        @NotBlank @Size(max = 200) String title,

        @Size(max = 120) String originCulture,

        @Size(max = 80) String datePeriod,

        @Size(max = 80) String material,

        @Size(max = 1000) String description,

        @NotNull Long collectionId,

        @NotNull Long locationId,

        @PastOrPresent LocalDate acquiredOn,

        @Size(max = 500) String imageUrl
) {
}