package org.peopleshores.capstone_project.controller;

import org.peopleshores.capstone_project.dto.LocationResponse;
import org.peopleshores.capstone_project.service.LocationService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@CrossOrigin(origins = "http://localhost:5173")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public List<LocationResponse> list() {
        return locationService.findAll();
    }
}