package org.peopleshores.capstone_project.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peopleshores.capstone_project.dto.ExhibitionArtifactRequest;
import org.peopleshores.capstone_project.dto.ExhibitionDetailResponse;
import org.peopleshores.capstone_project.dto.ExhibitionRequest;
import org.peopleshores.capstone_project.dto.ExhibitionResponse;
import org.peopleshores.capstone_project.entity.Exhibition;
import org.peopleshores.capstone_project.service.ExhibitionService;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Exhibition, public and visitor controllers")
class ExhibitionControllerTest {

    @Mock private ExhibitionService exhibitionService;

    @InjectMocks private ExhibitionController controller;

    private Exhibition openShow;

    @BeforeEach
    void setUp() {
        openShow = new Exhibition();
        openShow.setId(1L);
        openShow.setTitle("Clay and Bronze");
        openShow.setGallery("Gallery 1");
        openShow.setStartDate(LocalDate.now().minusDays(10));
        openShow.setEndDate(LocalDate.now().plusDays(80));
    }

    @Test
    @DisplayName("lists the programme as summaries, newest run first")
    void listMapsToSummaries() {
        Pageable page = PageRequest.of(0, 20);
        when(exhibitionService.list(page)).thenReturn(new PageImpl<>(List.of(openShow)));

        assertThat(controller.list(page).getContent().get(0).title()).isEqualTo("Clay and Bronze");
    }

    @Test
    @DisplayName("defaults the what-is-on query to today")
    void currentDefaultsToToday() {
        when(exhibitionService.runningOn(any(LocalDate.class))).thenReturn(List.of(openShow));

        List<ExhibitionResponse> shows = controller.current(null);

        assertThat(shows).hasSize(1);
        assertThat(shows.get(0).phase()).isEqualTo("CURRENT");
    }

    @Test
    @DisplayName("honours an explicit date when asked what was on then")
    void currentHonoursExplicitDate() {
        LocalDate asked = LocalDate.of(2026, 12, 25);
        when(exhibitionService.runningOn(asked)).thenReturn(List.of());

        assertThat(controller.current(asked)).isEmpty();
        verify(exhibitionService).runningOn(asked);
    }

    @Test
    @DisplayName("returns the objects on show with the exhibition")
    void getOneReturnsDetail() {
        when(exhibitionService.getDetail(1L)).thenReturn(openShow);

        ExhibitionDetailResponse d = controller.getOne(1L);

        assertThat(d.title()).isEqualTo("Clay and Bronze");
        assertThat(d.artifacts()).isEmpty();
    }

    @Test
    @DisplayName("answers a scheduled show with 201 and a Location header")
    void createReturns201() {
        ExhibitionRequest request = mock(ExhibitionRequest.class);
        when(exhibitionService.create(request)).thenReturn(openShow);

        ResponseEntity<ExhibitionResponse> response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();
    }

    @Test
    @DisplayName("hands an edit to the service with the path id")
    void updateDelegates() {
        ExhibitionRequest request = mock(ExhibitionRequest.class);
        when(exhibitionService.update(1L, request)).thenReturn(openShow);

        assertThat(controller.update(1L, request).gallery()).isEqualTo("Gallery 1");
    }

    @Test
    @DisplayName("striking a show from the record returns no body")
    void deleteDelegates() {
        controller.delete(1L);

        verify(exhibitionService).delete(1L);
    }

    @Test
    @DisplayName("adding an object returns the show's new running order")
    void addArtifactReturnsDetail() {
        ExhibitionArtifactRequest request = mock(ExhibitionArtifactRequest.class);
        when(exhibitionService.addArtifact(1L, request)).thenReturn(openShow);

        assertThat(controller.addArtifact(1L, request).id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("removing an object returns the show's new running order")
    void removeArtifactReturnsDetail() {
        when(exhibitionService.removeArtifact(1L, 5L)).thenReturn(openShow);

        assertThat(controller.removeArtifact(1L, 5L).id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("the public listing shows only what is open today")
    void publicControllerListsOpenShows() {
        PublicController publicController = new PublicController(exhibitionService);
        when(exhibitionService.runningOn(any(LocalDate.class))).thenReturn(List.of(openShow));

        assertThat(publicController.current()).hasSize(1);
    }

    @Test
    @DisplayName("the public detail route refuses a show that is not open")
    void publicControllerUsesGuardedLookup() {
        PublicController publicController = new PublicController(exhibitionService);
        when(exhibitionService.getPublicDetail(1L)).thenReturn(openShow);

        assertThat(publicController.one(1L).title()).isEqualTo("Clay and Bronze");
        verify(exhibitionService).getPublicDetail(1L);
    }

    @Test
    @DisplayName("the visitor archive uses the unguarded lookup, so closed shows are visible")
    void visitorControllerSeesTheArchive() {
        VisitorController visitorController = new VisitorController(exhibitionService);
        Pageable page = PageRequest.of(0, 50);
        when(exhibitionService.list(page)).thenReturn(new PageImpl<>(List.of(openShow)));
        when(exhibitionService.getDetail(1L)).thenReturn(openShow);

        assertThat(visitorController.archive(page).getContent()).hasSize(1);
        assertThat(visitorController.one(1L).title()).isEqualTo("Clay and Bronze");
    }
}