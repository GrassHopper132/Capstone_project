package org.peopleshores.capstone_project.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peopleshores.capstone_project.dto.LocationResponse;
import org.peopleshores.capstone_project.entity.Location;
import org.peopleshores.capstone_project.repository.LocationRepository;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Storage locations are read-only reference data, but the sort order is load
 * bearing: staff pick from this list by walking the building, so it has to come
 * back in building, room, case order rather than by id.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LocationService")
class LocationServiceTest {

    @Mock private LocationRepository locationRepository;
    @InjectMocks private LocationService service;

    private Location location(Long id, String building, String room, String caseCode,
                              String label, boolean climateControlled) {
        Location l = mock(Location.class);
        lenient().when(l.getId()).thenReturn(id);
        lenient().when(l.getBuilding()).thenReturn(building);
        lenient().when(l.getRoom()).thenReturn(room);
        lenient().when(l.getCaseCode()).thenReturn(caseCode);
        lenient().when(l.getLabel()).thenReturn(label);
        lenient().when(l.isClimateControlled()).thenReturn(climateControlled);
        return l;
    }

    @Test
    @DisplayName("carries every part of the address through to the client")
    void listMapsEveryField() {
        Location l = location(12L, "West Wing", "Store 2", "B", "West Wing, Store 2, Case B", true);
        when(locationRepository.findAll(any(Sort.class))).thenReturn(List.of(l));

        List<LocationResponse> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(12L);
        assertThat(result.get(0).building()).isEqualTo("West Wing");
        assertThat(result.get(0).room()).isEqualTo("Store 2");
        assertThat(result.get(0).caseCode()).isEqualTo("B");
        assertThat(result.get(0).label()).isEqualTo("West Wing, Store 2, Case B");
        assertThat(result.get(0).climateControlled()).isTrue();
    }

    @Test
    @DisplayName("records a location that is not climate controlled")
    void listReportsUncontrolledStorage() {
        Location l = location(13L, "Annexe", "Loft", "A", "Annexe, Loft, Case A", false);
        when(locationRepository.findAll(any(Sort.class))).thenReturn(List.of(l));

        assertThat(service.findAll().get(0).climateControlled()).isFalse();
    }

    @Test
    @DisplayName("sorts by building, then room, then case, so the list reads as a walk through the building")
    void listSortsByPhysicalOrder() {
        when(locationRepository.findAll(any(Sort.class))).thenReturn(List.of());

        service.findAll();

        ArgumentCaptor<Sort> sort = ArgumentCaptor.forClass(Sort.class);
        verify(locationRepository).findAll(sort.capture());
        assertThat(sort.getValue().getOrderFor("building")).isNotNull();
        assertThat(sort.getValue().getOrderFor("room")).isNotNull();
        assertThat(sort.getValue().getOrderFor("caseCode")).isNotNull();
    }

    @Test
    @DisplayName("returns an empty list rather than null when no locations exist")
    void listHandlesNoLocations() {
        when(locationRepository.findAll(any(Sort.class))).thenReturn(List.of());

        assertThat(service.findAll()).isEmpty();
    }
}