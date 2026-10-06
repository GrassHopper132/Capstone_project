package org.peopleshores.capstone_project.dto;

/** Reference data for the location picker. */
public record LocationResponse(
        Long id,
        String building,
        String room,
        String caseCode,
        String label,
        boolean climateControlled
) {
}