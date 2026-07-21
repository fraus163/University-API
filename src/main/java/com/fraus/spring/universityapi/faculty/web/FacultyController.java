package com.fraus.spring.universityapi.faculty.web;

import com.fraus.spring.universityapi.faculty.domain.FacultyService;
import com.fraus.spring.universityapi.faculty.web.dto.FacultyRequest;
import com.fraus.spring.universityapi.faculty.web.dto.FacultyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/faculties")
public class FacultyController {

    private final FacultyService facultyService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FacultyResponse> createFaculty(
            @RequestBody @Valid FacultyRequest request,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        log.info("Controller createFaculty is called: request={}", request);
        FacultyResponse createdFaculty = facultyService.createFaculty(request);

        URI location = uriComponentsBuilder
                .path("/api/v1/faculties/{id}")
                .buildAndExpand(createdFaculty.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(createdFaculty);
    }

    @GetMapping
    public List<FacultyResponse> getAllFaculties() {
        log.info("Controller getAllFaculties is called");
        return facultyService.getAllFaculties();
    }

    @GetMapping("/{id}")
    public FacultyResponse getFacultyById(
            @PathVariable Short id
    ) {
        log.info("Controller getFacultyById is called: id={}", id);
        return facultyService.getFacultyById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public FacultyResponse updateFacultyById(
            @PathVariable Short id,
            @RequestBody @Valid FacultyRequest request
    ) {
        log.info("Controller updateFacultyById is called: id={}", id);
        return facultyService.updateFacultyById(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFacultyById(
            @PathVariable Short id
    ) {
        log.info("Controller deleteFacultyById is called: id={}", id);
        facultyService.deleteFacultyById(id);
    }
}
