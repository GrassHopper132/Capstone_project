package org.peopleshores.capstone_project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Incoming payload for creating or updating an exhibition. */
public record ExhibitionRequest(

        @NotBlank @Size(max = 200) String title,

        @Size(max = 120) String gallery,

        @NotNull LocalDate startDate,

        @NotNull LocalDate endDate
) {
}