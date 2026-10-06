package org.peopleshores.capstone_project.controller;

import org.peopleshores.capstone_project.dto.ExhibitionDetailResponse;
import org.peopleshores.capstone_project.dto.ExhibitionResponse;
import org.peopleshores.capstone_project.service.ExhibitionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * What a registered visitor gets beyond the anonymous pages: the full
 * programme, past and future, rather than only what is hanging today.
 *
 * This is the reason a visitor account exists at all. Without it, signing up
 * would grant nothing the anonymous endpoints already give away, and a
 * registration form that buys the user nothing is just friction.
 */
@RestController
@RequestMapping("/api/v1/visitor")
public class VisitorController {

    private final ExhibitionService exhibitionService;

    public VisitorController(ExhibitionService exhibitionService) {
        this.exhibitionService = exhibitionService;
    }

    /** Every exhibition on record: closed, open and announced. */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/exhibitions")
    public Page<ExhibitionResponse> archive(
            @PageableDefault(size = 50, sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return exhibitionService.list(pageable).map(ExhibitionResponse::from);
    }

    /** One exhibition from the archive, open or not, with the objects in it. */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/exhibitions/{id}")
    public ExhibitionDetailResponse one(@PathVariable Long id) {
        return ExhibitionDetailResponse.from(exhibitionService.getDetail(id));
    }
}