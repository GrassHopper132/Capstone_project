package org.peopleshores.capstone_project.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Attaches an artifact to an exhibition. displayOrder is optional; when it is
 * omitted the service appends the artifact to the end of the running order.
 */
public record ExhibitionArtifactRequest(

        @NotNull Long artifactId,

        @Positive Integer displayOrder
) {
}