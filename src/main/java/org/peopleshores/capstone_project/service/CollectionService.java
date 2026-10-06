package org.peopleshores.capstone_project.service;

import org.peopleshores.capstone_project.dto.CollectionResponse;
import org.peopleshores.capstone_project.exception.ResourceNotFoundException;
import org.peopleshores.capstone_project.repository.MuseumCollectionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read-only reference data. Mapping happens inside the transaction so the lazy
 * curator association resolves without an open-in-view session.
 */
@Service
@Transactional(readOnly = true)
public class CollectionService {

    private final MuseumCollectionRepository collectionRepository;

    public CollectionService(MuseumCollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    public List<CollectionResponse> findAll() {
        return collectionRepository.findAll(Sort.by("name")).stream()
                .map(c -> new CollectionResponse(
                        c.getId(),
                        c.getName(),
                        c.getDescription(),
                        c.getCurator() == null ? null : c.getCurator().getFullName(),
                        collectionRepository.countArtifacts(c.getId())))
                .toList();
    }

    public CollectionResponse findById(Long id) {
        return collectionRepository.findById(id)
                .map(c -> new CollectionResponse(
                        c.getId(),
                        c.getName(),
                        c.getDescription(),
                        c.getCurator() == null ? null : c.getCurator().getFullName(),
                        collectionRepository.countArtifacts(c.getId())))
                .orElseThrow(() -> ResourceNotFoundException.of("Collection", id));
    }
}