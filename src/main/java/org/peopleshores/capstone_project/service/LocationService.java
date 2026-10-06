package org.peopleshores.capstone_project.service;

import org.peopleshores.capstone_project.dto.LocationResponse;
import org.peopleshores.capstone_project.repository.LocationRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public List<LocationResponse> findAll() {
        return locationRepository.findAll(Sort.by("building", "room", "caseCode")).stream()
                .map(l -> new LocationResponse(
                        l.getId(), l.getBuilding(), l.getRoom(), l.getCaseCode(),
                        l.getLabel(), l.isClimateControlled()))
                .toList();
    }
}