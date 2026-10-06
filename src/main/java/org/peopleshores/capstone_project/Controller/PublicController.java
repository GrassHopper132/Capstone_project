package org.peopleshores.capstone_project.controller;

import org.peopleshores.capstone_project.dto.ExhibitionDetailResponse;
import org.peopleshores.capstone_project.dto.ExhibitionResponse;
import org.peopleshores.capstone_project.service.ExhibitionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Unauthenticated endpoints for the visitor-facing pages.
 *
 * Deliberately narrow: only exhibitions open to the public today, and only the
 * fields a visitor would see on a wall label. Nothing here exposes locations,
 * condition grades, acquisition records or anything else a thief would find
 * useful, which is why this does not simply reuse the staff endpoints without
 * a token.
 */
@RestController
@RequestMapping("/api/v1/public")
public class PublicController {

    private final ExhibitionService exhibitionService;

    public PublicController(ExhibitionService exhibitionService) {
        this.exhibitionService = exhibitionService;
    }

    /** What is on show in the galleries today. */
    @GetMapping("/exhibitions")
    public List<ExhibitionResponse> current() {
        return exhibitionService.runningOn(LocalDate.now()).stream()
                .map(ExhibitionResponse::from)
                .toList();
    }

    /** One open exhibition, with the objects in it. */
    @GetMapping("/exhibitions/{id}")
    public ExhibitionDetailResponse one(@PathVariable Long id) {
        return ExhibitionDetailResponse.from(exhibitionService.getPublicDetail(id));
    }
}