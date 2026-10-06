package org.peopleshores.capstone_project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peopleshores.capstone_project.dto.ExhibitionArtifactRequest;
import org.peopleshores.capstone_project.dto.ExhibitionRequest;
import org.peopleshores.capstone_project.entity.Artifact;
import org.peopleshores.capstone_project.entity.ArtifactStatus;
import org.peopleshores.capstone_project.entity.Exhibition;
import org.peopleshores.capstone_project.entity.ExhibitionArtifact;
import org.peopleshores.capstone_project.exception.BusinessRuleException;
import org.peopleshores.capstone_project.exception.DuplicateResourceException;
import org.peopleshores.capstone_project.exception.ResourceNotFoundException;
import org.peopleshores.capstone_project.repository.ArtifactRepository;
import org.peopleshores.capstone_project.repository.ExhibitionArtifactRepository;
import org.peopleshores.capstone_project.repository.ExhibitionRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the seasonal rotation rules: an object joining a show that is open
 * today goes on display, leaving one returns it to storage unless some other
 * open show still needs it, and a show cannot be deleted while the public can
 * walk into it.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExhibitionService")
class ExhibitionServiceTest {

    @Mock private ExhibitionRepository exhibitionRepository;
    @Mock private ExhibitionArtifactRepository entryRepository;
    @Mock private ArtifactRepository artifactRepository;

    @InjectMocks private ExhibitionService service;

    private Exhibition openShow;
    private Exhibition closedShow;

    @BeforeEach
    void setUp() {
        openShow = new Exhibition();
        openShow.setId(1L);
        openShow.setTitle("Clay and Bronze");
        openShow.setGallery("Gallery 1");
        openShow.setStartDate(LocalDate.now().minusDays(30));
        openShow.setEndDate(LocalDate.now().plusDays(60));

        closedShow = new Exhibition();
        closedShow.setId(2L);
        closedShow.setTitle("Thread and Dye");
        closedShow.setGallery("Gallery 2");
        closedShow.setStartDate(LocalDate.now().minusDays(400));
        closedShow.setEndDate(LocalDate.now().minusDays(300));
    }

    private Artifact artifact(long id, String accession, ArtifactStatus status) {
        Artifact a = org.mockito.Mockito.mock(Artifact.class);
        when(a.getId()).thenReturn(id);
        org.mockito.Mockito.lenient().when(a.getAccessionNumber()).thenReturn(accession);
        org.mockito.Mockito.lenient().when(a.getStatus()).thenReturn(status);
        return a;
    }

    // --- scheduling -------------------------------------------------------

    @Test
    @DisplayName("refuses a run that ends before it opens")
    void createRejectsBackwardsDateRange() {
        ExhibitionRequest request = new ExhibitionRequest(
                "Winter Light", "Gallery 3",
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 11, 1));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("endDate");

        verify(exhibitionRepository, never()).save(any());
    }

    @Test
    @DisplayName("refuses a run that opens and closes on the same day")
    void createRejectsZeroLengthRun() {
        LocalDate day = LocalDate.of(2026, 12, 1);
        ExhibitionRequest request = new ExhibitionRequest("One Day Only", "Gallery 3", day, day);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("saves a valid run")
    void createSavesValidExhibition() {
        ExhibitionRequest request = new ExhibitionRequest(
                "Winter Light", "Gallery 3",
                LocalDate.of(2026, 11, 1), LocalDate.of(2027, 2, 1));

        when(exhibitionRepository.save(any(Exhibition.class))).thenAnswer(i -> i.getArgument(0));

        Exhibition saved = service.create(request);

        assertThat(saved.getTitle()).isEqualTo("Winter Light");
        assertThat(saved.getGallery()).isEqualTo("Gallery 3");
        assertThat(saved.getEndDate()).isEqualTo(LocalDate.of(2027, 2, 1));
    }

    @Test
    @DisplayName("applies the same date rule on update")
    void updateRejectsBackwardsDateRange() {
        ExhibitionRequest request = new ExhibitionRequest(
                "Winter Light", "Gallery 3",
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 11, 1));

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(BusinessRuleException.class);

        verify(exhibitionRepository, never()).findById(any());
    }

    @Test
    @DisplayName("reports a missing exhibition rather than returning null")
    void getDetailThrowsWhenAbsent() {
        when(exhibitionRepository.findDetailById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDetail(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("will not delete a show the public can walk into today")
    void deleteBlocksRunningShow() {
        when(exhibitionRepository.findById(1L)).thenReturn(Optional.of(openShow));

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("currently running");

        verify(exhibitionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deletes a show that has already closed")
    void deleteRemovesClosedShow() {
        when(exhibitionRepository.findById(2L)).thenReturn(Optional.of(closedShow));

        service.delete(2L);

        verify(exhibitionRepository).delete(closedShow);
    }

    // --- adding objects ---------------------------------------------------

    @Test
    @DisplayName("refuses to list the same object twice in one show")
    void addArtifactRejectsDuplicate() {
        Artifact a = artifact(5L, "1994.22.7", ArtifactStatus.STORED);
        when(exhibitionRepository.findById(1L)).thenReturn(Optional.of(openShow));
        when(artifactRepository.findById(5L)).thenReturn(Optional.of(a));
        ExhibitionArtifact existing = new ExhibitionArtifact(openShow, a, 1);
        when(entryRepository.findByExhibitionIdAndArtifactId(1L, 5L))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.addArtifact(1L, new ExhibitionArtifactRequest(5L, null)))
                .isInstanceOf(DuplicateResourceException.class);

        verify(entryRepository, never()).save(any());
    }

    @Test
    @DisplayName("will not exhibit an object that is in the workshop")
    void addArtifactRejectsObjectInRestoration() {
        Artifact a = artifact(6L, "1987.4.19", ArtifactStatus.IN_RESTORATION);
        when(exhibitionRepository.findById(1L)).thenReturn(Optional.of(openShow));
        when(artifactRepository.findById(6L)).thenReturn(Optional.of(a));
        when(entryRepository.findByExhibitionIdAndArtifactId(1L, 6L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addArtifact(1L, new ExhibitionArtifactRequest(6L, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("conservation");

        verify(entryRepository, never()).save(any());
    }

    @Test
    @DisplayName("will not exhibit an object that has left the collection")
    void addArtifactRejectsDeaccessionedObject() {
        Artifact a = artifact(7L, "1955.1.1", ArtifactStatus.DEACCESSIONED);
        when(exhibitionRepository.findById(1L)).thenReturn(Optional.of(openShow));
        when(artifactRepository.findById(7L)).thenReturn(Optional.of(a));
        when(entryRepository.findByExhibitionIdAndArtifactId(1L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addArtifact(1L, new ExhibitionArtifactRequest(7L, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("deaccessioned");
    }

    @Test
    @DisplayName("puts an object on display when the show is already open")
    void addArtifactToOpenShowPutsObjectOnDisplay() {
        Artifact a = artifact(8L, "1994.22.8", ArtifactStatus.STORED);
        when(exhibitionRepository.findById(1L)).thenReturn(Optional.of(openShow));
        when(artifactRepository.findById(8L)).thenReturn(Optional.of(a));
        when(entryRepository.findByExhibitionIdAndArtifactId(1L, 8L)).thenReturn(Optional.empty());
        when(exhibitionRepository.findDetailById(1L)).thenReturn(Optional.of(openShow));

        service.addArtifact(1L, new ExhibitionArtifactRequest(8L, 1));

        verify(a).setStatus(ArtifactStatus.ON_DISPLAY);
        verify(artifactRepository).save(a);
    }

    @Test
    @DisplayName("leaves an object in storage when the show has not opened yet")
    void addArtifactToFutureShowLeavesObjectStored() {
        Exhibition future = new Exhibition();
        future.setId(3L);
        future.setTitle("Lines on Paper");
        future.setStartDate(LocalDate.now().plusDays(30));
        future.setEndDate(LocalDate.now().plusDays(120));

        Artifact a = artifact(9L, "1968.9.44", ArtifactStatus.STORED);
        when(exhibitionRepository.findById(3L)).thenReturn(Optional.of(future));
        when(artifactRepository.findById(9L)).thenReturn(Optional.of(a));
        when(entryRepository.findByExhibitionIdAndArtifactId(3L, 9L)).thenReturn(Optional.empty());
        when(exhibitionRepository.findDetailById(3L)).thenReturn(Optional.of(future));

        service.addArtifact(3L, new ExhibitionArtifactRequest(9L, 1));

        verify(a, never()).setStatus(any());
        verify(artifactRepository, never()).save(any(Artifact.class));
    }

    @Test
    @DisplayName("appends to the running order when no position is given")
    void addArtifactWithoutOrderAppendsToEnd() {
        Artifact a = artifact(10L, "1976.31.2", ArtifactStatus.STORED);
        when(exhibitionRepository.findById(1L)).thenReturn(Optional.of(openShow));
        when(artifactRepository.findById(10L)).thenReturn(Optional.of(a));
        when(entryRepository.findByExhibitionIdAndArtifactId(1L, 10L)).thenReturn(Optional.empty());
        when(entryRepository.nextDisplayOrder(1L)).thenReturn(7);
        when(exhibitionRepository.findDetailById(1L)).thenReturn(Optional.of(openShow));

        service.addArtifact(1L, new ExhibitionArtifactRequest(10L, null));

        ArgumentCaptor<ExhibitionArtifact> saved = ArgumentCaptor.forClass(ExhibitionArtifact.class);
        verify(entryRepository).save(saved.capture());
        assertThat(saved.getValue().getDisplayOrder()).isEqualTo(7);
    }

    // --- removing objects -------------------------------------------------

    @Test
    @DisplayName("returns an object to storage when no other open show needs it")
    void removeArtifactReturnsObjectToStorage() {
        Artifact a = artifact(11L, "2001.10.1", ArtifactStatus.ON_DISPLAY);
        ExhibitionArtifact entry = new ExhibitionArtifact(openShow, a, 1);

        when(entryRepository.findByExhibitionIdAndArtifactId(1L, 11L)).thenReturn(Optional.of(entry));
        when(artifactRepository.isOnActiveExhibition(org.mockito.ArgumentMatchers.eq(11L), any(LocalDate.class)))
                .thenReturn(false);
        when(exhibitionRepository.findDetailById(1L)).thenReturn(Optional.of(openShow));

        service.removeArtifact(1L, 11L);

        verify(entryRepository).delete(entry);
        verify(a).setStatus(ArtifactStatus.STORED);
    }

    @Test
    @DisplayName("keeps an object on display when a second open show still has it")
    void removeArtifactKeepsObjectOnDisplayForOtherShow() {
        Artifact a = artifact(12L, "2001.10.2", ArtifactStatus.ON_DISPLAY);
        ExhibitionArtifact entry = new ExhibitionArtifact(openShow, a, 1);

        when(entryRepository.findByExhibitionIdAndArtifactId(1L, 12L)).thenReturn(Optional.of(entry));
        when(artifactRepository.isOnActiveExhibition(org.mockito.ArgumentMatchers.eq(12L), any(LocalDate.class)))
                .thenReturn(true);
        when(exhibitionRepository.findDetailById(1L)).thenReturn(Optional.of(openShow));

        service.removeArtifact(1L, 12L);

        verify(entryRepository).delete(entry);
        verify(a, never()).setStatus(ArtifactStatus.STORED);
    }

    @Test
    @DisplayName("reports an object that was never in the show")
    void removeArtifactThrowsWhenNotInShow() {
        when(entryRepository.findByExhibitionIdAndArtifactId(1L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeArtifact(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(entryRepository, never()).delete(any(ExhibitionArtifact.class));
    }
}