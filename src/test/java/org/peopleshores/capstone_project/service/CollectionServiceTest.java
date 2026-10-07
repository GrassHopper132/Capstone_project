package org.peopleshores.capstone_project.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peopleshores.capstone_project.dto.CollectionResponse;
import org.peopleshores.capstone_project.entity.MuseumCollection;
import org.peopleshores.capstone_project.entity.User;
import org.peopleshores.capstone_project.exception.ResourceNotFoundException;
import org.peopleshores.capstone_project.repository.MuseumCollectionRepository;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Mapping happens inside the service transaction because the curator is a lazy
 * association and open-in-view is disabled. These tests pin the two things that
 * would break first: the curator being absent, and the sort order the dropdowns
 * in the interface depend on.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CollectionService")
class CollectionServiceTest {

    @Mock private MuseumCollectionRepository collectionRepository;
    @InjectMocks private CollectionService service;

    private User curator(String fullName) {
        User u = mock(User.class);
        lenient().when(u.getFullName()).thenReturn(fullName);
        return u;
    }

    private MuseumCollection collection(Long id, String name, String description, User curator) {
        MuseumCollection c = mock(MuseumCollection.class);
        lenient().when(c.getId()).thenReturn(id);
        lenient().when(c.getName()).thenReturn(name);
        lenient().when(c.getDescription()).thenReturn(description);
        lenient().when(c.getCurator()).thenReturn(curator);
        return c;
    }

    @Test
    @DisplayName("flattens the curator to a name the interface can print")
    void listFlattensCurator() {
        User jane = curator("Jane Curator");
        MuseumCollection c = collection(3L, "Mediterranean Antiquities", "Greek and Roman material.", jane);
        when(collectionRepository.findAll(any(Sort.class))).thenReturn(List.of(c));

        List<CollectionResponse> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(3L);
        assertThat(result.get(0).name()).isEqualTo("Mediterranean Antiquities");
        assertThat(result.get(0).description()).isEqualTo("Greek and Roman material.");
        assertThat(result.get(0).curatorName()).isEqualTo("Jane Curator");
    }

    @Test
    @DisplayName("survives a collection with no curator assigned")
    void listToleratesMissingCurator() {
        MuseumCollection c = collection(4L, "Unassigned", "Awaiting a curator.", null);
        when(collectionRepository.findAll(any(Sort.class))).thenReturn(List.of(c));

        assertThat(service.findAll().get(0).curatorName()).isNull();
    }

    @Test
    @DisplayName("sorts by name, because the interface renders this straight into a dropdown")
    void listSortsByName() {
        when(collectionRepository.findAll(any(Sort.class))).thenReturn(List.of());

        service.findAll();

        ArgumentCaptor<Sort> sort = ArgumentCaptor.forClass(Sort.class);
        verify(collectionRepository).findAll(sort.capture());
        assertThat(sort.getValue().getOrderFor("name")).isNotNull();
    }

    @Test
    @DisplayName("returns an empty list rather than null when there are no collections")
    void listHandlesEmptyCatalogue() {
        when(collectionRepository.findAll(any(Sort.class))).thenReturn(List.of());

        assertThat(service.findAll()).isEmpty();
    }

    @Test
    @DisplayName("returns a single collection by id")
    void findByIdReturnsCollection() {
        User jane = curator("Jane Curator");
        MuseumCollection c = collection(3L, "Mediterranean Antiquities", "Greek and Roman material.", jane);
        when(collectionRepository.findById(3L)).thenReturn(Optional.of(c));

        CollectionResponse r = service.findById(3L);

        assertThat(r.name()).isEqualTo("Mediterranean Antiquities");
        assertThat(r.curatorName()).isEqualTo("Jane Curator");
    }

    @Test
    @DisplayName("reports a missing collection rather than returning null")
    void findByIdThrowsWhenAbsent() {
        when(collectionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}