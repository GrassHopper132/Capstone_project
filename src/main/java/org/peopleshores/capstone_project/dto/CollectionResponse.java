package org.peopleshores.capstone_project.dto;

/** Reference data for the collection picker and the collections screen. */
public record CollectionResponse(
        Long id,
        String name,
        String description,
        String curatorName,
        long artifactCount
) {
}