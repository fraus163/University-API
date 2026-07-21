package com.fraus.spring.universityapi.specialty.web;

import com.fraus.spring.universityapi.specialty.domain.SpecialtyService;
import com.fraus.spring.universityapi.specialty.web.dto.SpecialtyRequest;
import com.fraus.spring.universityapi.specialty.web.dto.SpecialtyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/specialties")
@RequiredArgsConstructor
@Slf4j
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecialtyResponse> createSpecialty(
            @RequestBody @Valid SpecialtyRequest request,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        log.info("Controller createSpecialty is called: request={}", request);
        SpecialtyResponse createdSpecialty = specialtyService
                .createSpecialty(request);

        URI location = uriComponentsBuilder
                .path("/api/v1/specialties/{id}")
                .buildAndExpand(createdSpecialty.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(createdSpecialty);
    }

    @GetMapping("/{id}")
    public SpecialtyResponse getSpecialtyById(
            @PathVariable Short id
    ) {
        log.info("Controller getSpecialtyById is called: id={}", id);
        return specialtyService.getSpecialtyById(id);
    }

    @GetMapping
    public Page<SpecialtyResponse> getSpecialtiesByFilter(
            @PageableDefault(
                    page = 0, size = 10,
                    sort = "name",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,
            @RequestParam(required = false) Short facultyId
    ) {
        log.info("Controller getSpecialtiesByFilter is called");
        return specialtyService.getSpecialtiesByFilter(
                pageable,
                facultyId
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SpecialtyResponse updateSpecialtyById(
            @PathVariable Short id,
            @RequestBody @Valid SpecialtyRequest request
    ) {
        log.info("Controller updateSpecialtyById is called: id={}", id);
        return specialtyService.updateSpecialtyById(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSpecialtyById(
            @PathVariable Short id
    ) {
        log.info("Controller deleteSpecialtyById is called: id={}", id);
        specialtyService.deleteSpecialtyById(id);
    }
}
