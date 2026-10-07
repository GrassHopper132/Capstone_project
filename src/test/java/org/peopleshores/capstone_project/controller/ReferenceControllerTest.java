package org.peopleshores.capstone_project.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peopleshores.capstone_project.dto.CollectionResponse;
import org.peopleshores.capstone_project.dto.LocationResponse;
import org.peopleshores.capstone_project.service.CollectionService;
import org.peopleshores.capstone_project.service.LocationService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The reference endpoints feed the dropdowns on the accession form. They hold no
 * logic, so these tests check only that the request reaches the service and the
 * response comes back unaltered.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Reference data controllers")
class ReferenceControllerTest {

    @Mock private CollectionService collectionService;
    @Mock private LocationService locationService;

    @InjectMocks private CollectionController collectionController;

    @Test
    @DisplayName("lists the collections for the accession form")
    void listsCollections() {
        CollectionResponse c = new CollectionResponse(3L, "Mediterranean Antiquities",
                "Greek and Roman material.", "Jane Curator", 6);
        when(collectionService.findAll()).thenReturn(List.of(c));

        List<CollectionResponse> result = collectionController.list();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Mediterranean Antiquities");
    }

    @Test
    @DisplayName("returns one collection by id")
    void returnsOneCollection() {
        CollectionResponse c = new CollectionResponse(3L, "Textiles and Dress",
                "Woven and worn.", "Jane Curator", 4);
        when(collectionService.findById(3L)).thenReturn(c);

        assertThat(collectionController.getOne(3L).name()).isEqualTo("Textiles and Dress");
        verify(collectionService).findById(3L);
    }

    @Test
    @DisplayName("returns an empty list when the museum has no collections")
    void listsNoCollections() {
        when(collectionService.findAll()).thenReturn(List.of());

        assertThat(collectionController.list()).isEmpty();
    }

    @Test
    @DisplayName("lists the storage locations for the accession form")
    void listsLocations() {
        LocationController controller = new LocationController(locationService);
        LocationResponse l = new LocationResponse(12L, "West Wing", "Store 2", "B",
                "West Wing, Store 2, Case B", true);
        when(locationService.findAll()).thenReturn(List.of(l));

        List<LocationResponse> result = controller.list();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).label()).isEqualTo("West Wing, Store 2, Case B");
    }
}