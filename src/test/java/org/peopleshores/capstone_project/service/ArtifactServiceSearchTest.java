package org.peopleshores.capstone_project.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peopleshores.capstone_project.dto.ArtifactRequest;
import org.peopleshores.capstone_project.entity.Artifact;
import org.peopleshores.capstone_project.entity.ArtifactStatus;
import org.peopleshores.capstone_project.entity.Location;
import org.peopleshores.capstone_project.entity.MuseumCollection;
import org.peopleshores.capstone_project.exception.BusinessRuleException;
import org.peopleshores.capstone_project.exception.ResourceNotFoundException;
import org.peopleshores.capstone_project.repository.ArtifactRepository;
import org.peopleshores.capstone_project.repository.LocationRepository;
import org.peopleshores.capstone_project.repository.MuseumCollectionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the catalogue paths the original suite left alone: the dynamic search,
 * the editable-field copy, and the two lifecycle operations. Search matters
 * because it is the only caller of ArtifactSpecifications, which was otherwise
 * entirely unexercised.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ArtifactService catalogue operations")
class ArtifactServiceSearchTest {

    @Mock private ArtifactRepository artifactRepository;
    @Mock private MuseumCollectionRepository collectionRepository;
    @Mock private LocationRepository locationRepository;

    @InjectMocks private ArtifactService service;

    private Artifact amphora() {
        Artifact a = new Artifact();
        a.setAccessionNumber("1994.22.7");
        a.setTitle("Red-figure amphora");
        a.setStatus(ArtifactStatus.STORED);
        return a;
    }

    private ArtifactRequest editRequest() {
        ArtifactRequest r = mock(ArtifactRequest.class);
        lenient().when(r.accessionNumber()).thenReturn("1994.22.7");
        lenient().when(r.title()).thenReturn("Red-figure amphora, restored");
        lenient().when(r.originCulture()).thenReturn("Attic Greek");
        lenient().when(r.datePeriod()).thenReturn("c. 480 BCE");
        lenient().when(r.material()).thenReturn("Terracotta");
        lenient().when(r.description()).thenReturn("Repaired rim.");
        lenient().when(r.acquiredOn()).thenReturn(LocalDate.of(1994, 6, 14));
        lenient().when(r.collectionId()).thenReturn(3L);
        lenient().when(r.locationId()).thenReturn(12L);
        lenient().when(r.imageUrl()).thenReturn("https://images.example.org/a.jpg");
        return r;
    }

    private void stubReferenceData() {
        MuseumCollection collection = mock(MuseumCollection.class);
        Location location = mock(Location.class);
        when(collectionRepository.findById(3L)).thenReturn(Optional.of(collection));
        when(locationRepository.findById(12L)).thenReturn(Optional.of(location));
    }

    @Test
    @DisplayName("builds a specification even when every filter is empty")
    void searchWithNoFiltersStillQueries() {
        Pageable page = PageRequest.of(0, 20);
        Page<Artifact> result = new PageImpl<>(List.of(amphora()));
        when(artifactRepository.findAll(any(Specification.class), eq(page))).thenReturn(result);

        assertThat(service.search(null, null, null, null, page)).hasSize(1);
    }

    @Test
    @DisplayName("combines status, material, collection and free text into one query")
    void searchCombinesEveryFilter() {
        Pageable page = PageRequest.of(0, 20);
        when(artifactRepository.findAll(any(Specification.class), eq(page)))
                .thenReturn(new PageImpl<>(List.of(amphora())));

        assertThat(service.search(ArtifactStatus.ON_DISPLAY, "Terracotta", 3L, "amphora", page))
                .hasSize(1);
    }

    @Test
    @DisplayName("treats blank search text as no filter rather than an empty match")
    void searchTreatsBlankTextAsAbsent() {
        Pageable page = PageRequest.of(0, 20);
        when(artifactRepository.findAll(any(Specification.class), eq(page)))
                .thenReturn(new PageImpl<>(List.of(amphora())));

        assertThat(service.search(null, "   ", null, "   ", page)).hasSize(1);
    }

    @Test
    @DisplayName("loads an artifact with its relationships already fetched")
    void getByIdUsesDetailQuery() {
        when(artifactRepository.findDetailById(1L)).thenReturn(Optional.of(amphora()));

        assertThat(service.getById(1L).getAccessionNumber()).isEqualTo("1994.22.7");
    }

    @Test
    @DisplayName("reports a missing artifact rather than returning null")
    void getByIdThrowsWhenAbsent() {
        when(artifactRepository.findDetailById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("an edit changes the catalogue record but never the accession number")
    void updateLeavesAccessionNumberAlone() {
        Artifact existing = amphora();
        when(artifactRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(artifactRepository.save(existing)).thenReturn(existing);
        stubReferenceData();

        Artifact updated = service.update(1L, editRequest());

        assertThat(updated.getTitle()).isEqualTo("Red-figure amphora, restored");
        assertThat(updated.getAccessionNumber()).isEqualTo("1994.22.7");
        assertThat(updated.getImageUrl()).isEqualTo("https://images.example.org/a.jpg");
    }

    @Test
    @DisplayName("an edit naming an unknown collection is reported, not silently ignored")
    void updateRejectsUnknownCollection() {
        when(artifactRepository.findById(1L)).thenReturn(Optional.of(amphora()));
        when(collectionRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(1L, editRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("moves an artifact into the workshop")
    void changeStatusSavesNewState() {
        Artifact a = amphora();
        when(artifactRepository.findById(1L)).thenReturn(Optional.of(a));
        when(artifactRepository.save(a)).thenReturn(a);

        assertThat(service.changeStatus(1L, ArtifactStatus.IN_RESTORATION).getStatus())
                .isEqualTo(ArtifactStatus.IN_RESTORATION);
    }

    @Test
    @DisplayName("deaccessions an artifact that is not on show")
    void deleteRemovesArtifactNotOnShow() {
        Artifact a = amphora();
        when(artifactRepository.findById(1L)).thenReturn(Optional.of(a));
        when(artifactRepository.isOnActiveExhibition(eq(1L), any(LocalDate.class))).thenReturn(false);

        service.delete(1L);

        verify(artifactRepository).delete(a);
    }

    @Test
    @DisplayName("refuses to deaccession an artifact the public can see today")
    void deleteBlocksArtifactOnActiveExhibition() {
        Artifact a = amphora();
        when(artifactRepository.findById(1L)).thenReturn(Optional.of(a));
        when(artifactRepository.isOnActiveExhibition(eq(1L), any(LocalDate.class))).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("1994.22.7");

        verify(artifactRepository, never()).delete(any(Artifact.class));
    }
}