package org.peopleshores.capstone_project.dto;

import org.peopleshores.capstone_project.entity.Exhibition;
import org.peopleshores.capstone_project.entity.ExhibitionArtifact;

import java.time.LocalDate;
import java.util.List;

/**
 * Full view of one exhibition, including the objects on show in display order.
 * Build this only from ExhibitionRepository.findDetailById, which fetch-joins
 * the entries; the lazy collection would otherwise fail outside a transaction.
 */
public record ExhibitionDetailResponse(
        Long id,
        String title,
        String gallery,
        LocalDate startDate,
        LocalDate endDate,
        String phase,
        List<Entry> artifacts
) {

    public record Entry(
            Long artifactId,
            String accessionNumber,
            String title,
            String status,
            Integer displayOrder,
            String imageUrl
    ) {
        public static Entry from(ExhibitionArtifact ea) {
            return new Entry(
                    ea.getArtifact().getId(),
                    ea.getArtifact().getAccessionNumber(),
                    ea.getArtifact().getTitle(),
                    ea.getArtifact().getStatus() == null ? null : ea.getArtifact().getStatus().name(),
                    ea.getDisplayOrder(),
                    ea.getArtifact().getImageUrl()
            );
        }
    }

    public static ExhibitionDetailResponse from(Exhibition e) {
        List<Entry> entries = e.getEntries().stream()
                .map(Entry::from)
                .toList();

        return new ExhibitionDetailResponse(
                e.getId(),
                e.getTitle(),
                e.getGallery(),
                e.getStartDate(),
                e.getEndDate(),
                ExhibitionResponse.phaseOf(e, LocalDate.now()),
                entries
        );
    }
}