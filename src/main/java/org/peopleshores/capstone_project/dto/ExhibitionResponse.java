package org.peopleshores.capstone_project.dto;

import org.peopleshores.capstone_project.entity.Exhibition;

import java.time.LocalDate;

/**
 * Summary view of an exhibition, used in list responses.
 *
 * phase is derived from the date range rather than stored, so a show moves from
 * UPCOMING to CURRENT to PAST on its own as the calendar advances. That is what
 * makes the rotation seasonal without anyone having to flip a flag.
 */
public record ExhibitionResponse(
        Long id,
        String title,
        String gallery,
        LocalDate startDate,
        LocalDate endDate,
        String phase
) {

    public static ExhibitionResponse from(Exhibition e) {
        return new ExhibitionResponse(
                e.getId(),
                e.getTitle(),
                e.getGallery(),
                e.getStartDate(),
                e.getEndDate(),
                phaseOf(e, LocalDate.now())
        );
    }

    public static String phaseOf(Exhibition e, LocalDate on) {
        if (e.getStartDate() == null || e.getEndDate() == null) {
            return "UNKNOWN";
        }
        if (on.isBefore(e.getStartDate())) {
            return "UPCOMING";
        }
        if (on.isAfter(e.getEndDate())) {
            return "PAST";
        }
        return "CURRENT";
    }
}