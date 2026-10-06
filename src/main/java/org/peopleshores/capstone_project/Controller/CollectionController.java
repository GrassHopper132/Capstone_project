package org.peopleshores.capstone_project.controller;

import org.peopleshores.capstone_project.dto.CollectionResponse;
import org.peopleshores.capstone_project.service.CollectionService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/collections")
@CrossOrigin(origins = "http://localhost:5173")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping
    public List<CollectionResponse> list() {
        return collectionService.findAll();
    }

    @GetMapping("/{id}")
    public CollectionResponse getOne(@PathVariable Long id) {
        return collectionService.findById(id);
    }
}