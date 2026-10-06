package org.peopleshores.capstone_project.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.peopleshores.capstone_project.entity.Artifact;
import org.peopleshores.capstone_project.entity.ArtifactStatus;
import org.peopleshores.capstone_project.entity.Exhibition;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * phase is computed, not stored, so these tests pin the only thing that moves a
 * show from announced to open to closed: the calendar.
 */
@DisplayName("Exhibition response mapping")
class ExhibitionResponseTest {

    private Exhibition show(LocalDate start, LocalDate end) {
        Exhibition e = new Exhibition();
        e.setId(1L);
        e.setTitle("Clay and Bronze");
        e.setGallery("Gallery 1");
        e.setStartDate(start);
        e.setEndDate(end);
        return e;
    }

    private Artifact object(String accession, String title, String image) {
        Artifact a = new Artifact();
        a.setAccessionNumber(accession);
        a.setTitle(title);
        a.setStatus(ArtifactStatus.ON_DISPLAY);
        a.setImageUrl(image);
        return a;
    }

    @Test
    @DisplayName("a show that opens next month reads as upcoming")
    void futureShowIsUpcoming() {
        LocalDate today = LocalDate.of(2026, 10, 6);
        Exhibition e = show(LocalDate.of(2026, 11, 1), LocalDate.of(2027, 1, 31));

        assertThat(ExhibitionResponse.phaseOf(e, today)).isEqualTo("UPCOMING");
    }

    @Test
    @DisplayName("a show open today reads as current")
    void openShowIsCurrent() {
        LocalDate today = LocalDate.of(2026, 10, 6);
        Exhibition e = show(LocalDate.of(2026, 9, 1), LocalDate.of(2027, 1, 31));

        assertThat(ExhibitionResponse.phaseOf(e, today)).isEqualTo("CURRENT");
    }

    @Test
    @DisplayName("the closing day itself still counts as open")
    void lastDayIsStillCurrent() {
        LocalDate closing = LocalDate.of(2027, 1, 31);
        Exhibition e = show(LocalDate.of(2026, 9, 1), closing);

        assertThat(ExhibitionResponse.phaseOf(e, closing)).isEqualTo("CURRENT");
    }

    @Test
    @DisplayName("a show that has closed reads as past")
    void closedShowIsPast() {
        LocalDate today = LocalDate.of(2027, 3, 1);
        Exhibition e = show(LocalDate.of(2026, 9, 1), LocalDate.of(2027, 1, 31));

        assertThat(ExhibitionResponse.phaseOf(e, today)).isEqualTo("PAST");
    }

    @Test
    @DisplayName("a show with no dates reads as unknown rather than throwing")
    void undatedShowIsUnknown() {
        Exhibition e = new Exhibition();

        assertThat(ExhibitionResponse.phaseOf(e, LocalDate.of(2026, 10, 6))).isEqualTo("UNKNOWN");
    }

    @Test
    @DisplayName("summary mapping keeps title, gallery and both dates")
    void summaryMapsFields() {
        Exhibition e = show(LocalDate.of(2026, 9, 1), LocalDate.of(2027, 1, 31));

        ExhibitionResponse r = ExhibitionResponse.from(e);

        assertThat(r.id()).isEqualTo(1L);
        assertThat(r.title()).isEqualTo("Clay and Bronze");
        assertThat(r.gallery()).isEqualTo("Gallery 1");
        assertThat(r.startDate()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(r.endDate()).isEqualTo(LocalDate.of(2027, 1, 31));
    }

    @Test
    @DisplayName("detail mapping lists the objects in display order with their photographs")
    void detailMapsEntries() {
        Exhibition e = show(LocalDate.of(2026, 9, 1), LocalDate.of(2027, 1, 31));
        e.addArtifact(object("1994.22.7", "Red-figure amphora", "https://images.example.org/a.jpg"), 1);
        e.addArtifact(object("1994.22.8", "Black-glaze kylix", null), 2);

        ExhibitionDetailResponse d = ExhibitionDetailResponse.from(e);

        assertThat(d.artifacts()).hasSize(2);
        assertThat(d.artifacts().get(0).accessionNumber()).isEqualTo("1994.22.7");
        assertThat(d.artifacts().get(0).imageUrl()).isEqualTo("https://images.example.org/a.jpg");
        assertThat(d.artifacts().get(0).status()).isEqualTo("ON_DISPLAY");
        assertThat(d.artifacts().get(1).displayOrder()).isEqualTo(2);
        assertThat(d.artifacts().get(1).imageUrl()).isNull();
    }

    @Test
    @DisplayName("detail mapping copes with a show that has no objects yet")
    void detailMapsEmptyShow() {
        Exhibition e = show(LocalDate.of(2026, 9, 1), LocalDate.of(2027, 1, 31));

        assertThat(ExhibitionDetailResponse.from(e).artifacts()).isEmpty();
    }
}