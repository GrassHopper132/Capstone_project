package org.peopleshores.capstone_project.repository;

import org.peopleshores.capstone_project.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByBuildingAndRoomAndCaseCode(String building, String room, String caseCode);

    List<Location> findByClimateControlledTrue();

    List<Location> findByBuildingOrderByRoomAscCaseCodeAsc(String building);
}
