package org.peopleshores.capstone_project.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peopleshores.capstone_project.dto.ArtifactRequest;
import org.peopleshores.capstone_project.dto.ArtifactResponse;
import org.peopleshores.capstone_project.entity.Artifact;
import org.peopleshores.capstone_project.entity.ArtifactStatus;
import org.peopleshores.capstone_project.service.ArtifactService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The controller holds no logic of its own, so these tests check the two things
 * it is actually responsible for: handing the request to the service unchanged,
 * and turning entities into response DTOs rather than leaking them.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ArtifactController")
class ArtifactControllerTest {

    @Mock private ArtifactService artifactService;
    @InjectMocks private ArtifactController controller;

    private Artifact artifact(String accession, String title) {
        Artifact a = new Artifact();
        a.setAccessionNumber(accession);
        a.setTitle(title);
        a.setStatus(ArtifactStatus.STORED);
        return a;
    }

    @Test
    @DisplayName("passes every filter through to the service untouched")
    void listForwardsFilters() {
        Pageable page = PageRequest.of(0, 20);
        when(artifactService.search(ArtifactStatus.ON_DISPLAY, "Terracotta", 3L, "amphora", page))
                .thenReturn(new PageImpl<>(List.of(artifact("1994.22.7", "Red-figure amphora"))));

        Page<ArtifactResponse> result =
                controller.list(ArtifactStatus.ON_DISPLAY, "Terracotta", 3L, "amphora", page);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).accessionNumber()).isEqualTo("1994.22.7");
    }

    @Test
    @DisplayName("returns an empty page rather than null when nothing matches")
    void listHandlesNoMatches() {
        Pageable page = PageRequest.of(0, 20);
        when(artifactService.search(any(), any(), any(), any(), eq(page)))
                .thenReturn(new PageImpl<>(List.of()));

        assertThat(controller.list(null, null, null, null, page).getContent()).isEmpty();
    }

    @Test
    @DisplayName("returns a single artifact as a response DTO, not the entity")
    void getOneMapsToDto() {
        when(artifactService.getById(1L)).thenReturn(artifact("1994.22.7", "Red-figure amphora"));

        ArtifactResponse r = controller.getOne(1L);

        assertThat(r.title()).isEqualTo("Red-figure amphora");
    }

    @Test
    @DisplayName("answers a successful accession with 201 and a Location header")
    void createReturns201WithLocation() {
        ArtifactRequest request = mock(ArtifactRequest.class);
        when(artifactService.create(request)).thenReturn(artifact("2026.1.1", "New accession"));

        ResponseEntity<ArtifactResponse> response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();
    }

    @Test
    @DisplayName("hands an update to the service with the path id")
    void updateDelegatesWithPathId() {
        ArtifactRequest request = mock(ArtifactRequest.class);
        when(artifactService.update(4L, request)).thenReturn(artifact("1994.22.7", "Edited title"));

        assertThat(controller.update(4L, request).title()).isEqualTo("Edited title");
    }

    @Test
    @DisplayName("moves an artifact between lifecycle states")
    void changeStatusDelegates() {
        Artifact moved = artifact("1994.22.7", "Red-figure amphora");
        moved.setStatus(ArtifactStatus.IN_RESTORATION);
        when(artifactService.changeStatus(4L, ArtifactStatus.IN_RESTORATION)).thenReturn(moved);

        assertThat(controller.changeStatus(4L, ArtifactStatus.IN_RESTORATION).status())
                .isEqualTo("IN_RESTORATION");
    }

    @Test
    @DisplayName("deaccession returns no body")
    void deleteDelegates() {
        controller.delete(9L);

        verify(artifactService).delete(9L);
    }
}