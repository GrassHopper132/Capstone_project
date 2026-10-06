package org.peopleshores.capstone_project.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The two rules the Exhibition entity enforces on its own, independently of any
 * service: a run must move forward in time, and "is it open" is a question about
 * a specific date rather than a stored flag.
 */
@DisplayName("Exhibition")
class ExhibitionTest {

    private Exhibition run(LocalDate start, LocalDate end) {
        Exhibition e = new Exhibition();
        e.setTitle("Clay and Bronze");
        e.setGallery("Gallery 1");
        e.setStartDate(start);
        e.setEndDate(end);
        return e;
    }

    @Test
    @DisplayName("accepts a run that moves forward in time")
    void forwardRangeIsValid() {
        assertThat(run(LocalDate.of(2026, 9, 1), LocalDate.of(2027, 1, 31)).isDateRangeValid()).isTrue();
    }

    @Test
    @DisplayName("rejects a run that ends before it opens")
    void backwardsRangeIsInvalid() {
        assertThat(run(LocalDate.of(2027, 1, 31), LocalDate.of(2026, 9, 1)).isDateRangeValid()).isFalse();
    }

    @Test
    @DisplayName("rejects a run that opens and closes on the same day")
    void zeroLengthRangeIsInvalid() {
        LocalDate day = LocalDate.of(2026, 9, 1);
        assertThat(run(day, day).isDateRangeValid()).isFalse();
    }

    @Test
    @DisplayName("treats a half-filled form as not yet invalid")
    void partialRangeIsNotRejected() {
        Exhibition e = new Exhibition();
        e.setStartDate(LocalDate.of(2026, 9, 1));

        assertThat(e.isDateRangeValid()).isTrue();
    }

    @Test
    @DisplayName("is open on its first and last day, and on the days between")
    void isOpenThroughoutTheRun() {
        Exhibition e = run(LocalDate.of(2026, 9, 1), LocalDate.of(2027, 1, 31));

        assertThat(e.isActiveOn(LocalDate.of(2026, 9, 1))).isTrue();
        assertThat(e.isActiveOn(LocalDate.of(2026, 11, 15))).isTrue();
        assertThat(e.isActiveOn(LocalDate.of(2027, 1, 31))).isTrue();
    }

    @Test
    @DisplayName("is closed the day before it opens and the day after it ends")
    void isClosedOutsideTheRun() {
        Exhibition e = run(LocalDate.of(2026, 9, 1), LocalDate.of(2027, 1, 31));

        assertThat(e.isActiveOn(LocalDate.of(2026, 8, 31))).isFalse();
        assertThat(e.isActiveOn(LocalDate.of(2027, 2, 1))).isFalse();
    }

    @Test
    @DisplayName("keeps objects in the order they were hung")
    void addArtifactRecordsDisplayOrder() {
        Exhibition e = run(LocalDate.of(2026, 9, 1), LocalDate.of(2027, 1, 31));

        Artifact first = new Artifact();
        first.setAccessionNumber("1994.22.7");
        Artifact second = new Artifact();
        second.setAccessionNumber("1994.22.8");

        e.addArtifact(first, 1);
        e.addArtifact(second, 2);

        assertThat(e.getEntries()).hasSize(2);
        assertThat(e.getEntries().get(0).getDisplayOrder()).isEqualTo(1);
        assertThat(e.getEntries().get(1).getArtifact().getAccessionNumber()).isEqualTo("1994.22.8");
    }
}