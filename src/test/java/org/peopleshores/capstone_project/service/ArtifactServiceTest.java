package org.peopleshores.capstone_project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peopleshores.capstone_project.dto.ArtifactRequest;
import org.peopleshores.capstone_project.entity.Artifact;
import org.peopleshores.capstone_project.entity.ArtifactStatus;
import org.peopleshores.capstone_project.entity.Location;
import org.peopleshores.capstone_project.entity.MuseumCollection;
import org.peopleshores.capstone_project.exception.BusinessRuleException;
import org.peopleshores.capstone_project.exception.DuplicateResourceException;
import org.peopleshores.capstone_project.exception.ResourceNotFoundException;
import org.peopleshores.capstone_project.repository.ArtifactRepository;
import org.peopleshores.capstone_project.repository.LocationRepository;
import org.peopleshores.capstone_project.repository.MuseumCollectionRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArtifactService")
class ArtifactServiceTest {

    @Mock private ArtifactRepository artifactRepository;
    @Mock private MuseumCollectionRepository collectionRepository;
    @Mock private LocationRepository locationRepository;

    @InjectMocks private ArtifactService service;

    private MuseumCollection collection;
    private Location location;
    private ArtifactRequest request;

    @BeforeEach
    void setUp() {
        collection = new MuseumCollection("Mediterranean Antiquities", "Greek and Roman", null);
        collection.setId(1L);

        location = new Location("Annex", "Storage 2", "S2-R3", true);
        location.setId(12L);

        request = new ArtifactRequest(
                "1994.22.7", "Red-figure amphora", "Attic Greek", "c. 480 BCE",
                "Terracotta", "Storage jar", 1L, 12L, LocalDate.of(1994, 6, 14));
    }

    @Test
    @DisplayName("rejects a duplicate accession number")
    void createRejectsDuplicateAccessionNumber() {
        when(artifactRepository.existsByAccessionNumber("1994.22.7")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("1994.22.7");

        verify(artifactRepository, never()).save(any());
    }

    @Test
    @DisplayName("creates an artifact as STORED with collection and location resolved")
    void createSavesWithStoredStatus() {
        when(artifactRepository.existsByAccessionNumber("1994.22.7")).thenReturn(false);
        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(locationRepository.findById(12L)).thenReturn(Optional.of(location));
        when(artifactRepository.save(any(Artifact.class))).thenAnswer(i -> i.getArgument(0));

        service.create(request);

        ArgumentCaptor<Artifact> saved = ArgumentCaptor.forClass(Artifact.class);
        verify(artifactRepository).save(saved.capture());

        Artifact a = saved.getValue();
        assertThat(a.getAccessionNumber()).isEqualTo("1994.22.7");
        assertThat(a.getStatus()).isEqualTo(ArtifactStatus.STORED);
        assertThat(a.getCollection()).isSameAs(collection);
        assertThat(a.getLocation()).isSameAs(location);
    }

    @Test
    @DisplayName("fails when the referenced collection does not exist")
    void createFailsOnUnknownCollection() {
        when(artifactRepository.existsByAccessionNumber("1994.22.7")).thenReturn(false);
        when(collectionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Collection");
    }

    @Test
    @DisplayName("getById reports a missing artifact rather than returning null")
    void getByIdThrowsWhenMissing() {
        when(artifactRepository.findDetailById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("refuses to delete an artifact on an active exhibition")
    void deleteBlockedByActiveExhibition() {
        Artifact a = new Artifact();
        a.setId(5L);
        a.setAccessionNumber("1994.22.7");

        when(artifactRepository.findById(5L)).thenReturn(Optional.of(a));
        when(artifactRepository.isOnActiveExhibition(eq(5L), any(LocalDate.class))).thenReturn(true);

        assertThatThrownBy(() -> service.delete(5L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("active exhibition");

        verify(artifactRepository, never()).delete(any(Artifact.class));
    }

    @Test
    @DisplayName("deletes an artifact that is not on any active exhibition")
    void deleteSucceedsWhenNotExhibited() {
        Artifact a = new Artifact();
        a.setId(5L);
        a.setAccessionNumber("1994.22.7");

        when(artifactRepository.findById(5L)).thenReturn(Optional.of(a));
        when(artifactRepository.isOnActiveExhibition(eq(5L), any(LocalDate.class))).thenReturn(false);

        service.delete(5L);

        verify(artifactRepository).delete(a);
    }

    @Test
    @DisplayName("update never changes the accession number")
    void updateLeavesAccessionNumberAlone() {
        Artifact existing = new Artifact();
        existing.setId(5L);
        existing.setAccessionNumber("1987.4.19");

        when(artifactRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(locationRepository.findById(12L)).thenReturn(Optional.of(location));
        when(artifactRepository.save(any(Artifact.class))).thenAnswer(i -> i.getArgument(0));

        Artifact result = service.update(5L, request);

        assertThat(result.getAccessionNumber()).isEqualTo("1987.4.19");
        assertThat(result.getTitle()).isEqualTo("Red-figure amphora");
    }
}