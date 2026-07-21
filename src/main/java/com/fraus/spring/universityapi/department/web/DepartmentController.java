package com.fraus.spring.universityapi.department.web;

import com.fraus.spring.universityapi.department.domain.DepartmentService;
import com.fraus.spring.universityapi.department.web.dto.DepartmentRequest;
import com.fraus.spring.universityapi.department.web.dto.DepartmentResponse;
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

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
@Slf4j
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponse> createDepartment(
            @RequestBody @Valid DepartmentRequest request,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        log.info("Controller createDepartment is called: request={}", request);
        DepartmentResponse createdDepartment = departmentService.createDepartment(request);

        URI location = uriComponentsBuilder
                .path("/api/v1/departments/{id}")
                .buildAndExpand(createdDepartment.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(createdDepartment);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public List<DepartmentResponse> getDepartmentsByFilter(
            @RequestParam(required = false) Short facultyId
    ) {
        log.info("Controller getDepartmentsByFilter is called: facultyId={}", facultyId);
        return departmentService.getDepartmentsByFilter(facultyId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public DepartmentResponse getDepartmentById(
            @PathVariable Short id
    ) {
        log.info("Controller getDepartmentById is called: id={}", id);
        return departmentService.getDepartmentById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DepartmentResponse updateDepartmentById(
            @PathVariable Short id,
            @RequestBody @Valid DepartmentRequest request
    ) {
        log.info("Controller updateDepartmentById is called: id={}", id);
        return departmentService.updateDepartmentById(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDepartmentById(
            @PathVariable Short id
    ) {
        log.info("Controller deleteDepartmentById is called: id={}", id);
        departmentService.deleteDepartmentById(id);
    }
}
