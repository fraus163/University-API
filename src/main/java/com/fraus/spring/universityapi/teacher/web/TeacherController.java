package com.fraus.spring.universityapi.teacher.web;

import com.fraus.spring.universityapi.teacher.domain.TeacherService;
import com.fraus.spring.universityapi.teacher.web.dto.TeacherCreateRequest;
import com.fraus.spring.universityapi.teacher.web.dto.TeacherResponse;
import com.fraus.spring.universityapi.teacher.web.dto.TeacherUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/teachers")
@RequiredArgsConstructor
@Slf4j
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherResponse> createTeacher(
            @RequestBody @Valid TeacherCreateRequest request,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        log.info("Controller createTeacher is called: request={}", request);
        TeacherResponse createdTeacher = teacherService.createTeacher(request);

        URI location = uriComponentsBuilder
                .path("/api/v1/teachers/{id}")
                .buildAndExpand(createdTeacher.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(createdTeacher);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public TeacherResponse getTeacherById(
            @PathVariable Long id
    ) {
        log.info("Controller getTeacherById is called: id={}", id);
        return teacherService.getTeacherById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public Page<TeacherResponse> getTeachersByFilter(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String patronymic,
            @RequestParam(required = false) Short positionId,
            @RequestParam(required = false) Short departmentId,
            @RequestParam(required = false) Short facultyId
    ) {
        log.info("Controller getTeachersByFilter is called");
        return teacherService.getTeachersByFilter(
                pageable,
                lastName,
                firstName,
                patronymic,
                positionId,
                departmentId,
                facultyId
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TeacherResponse updateTeacherById(
            @PathVariable Long id,
            @RequestBody @Valid TeacherUpdateRequest request
    ) {
        log.info("Controller updateTeacherById is called: id={}", id);
        return teacherService.updateTeacherById(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeacherById(
            @PathVariable Long id
    ) {
        log.info("Controller deleteTeacherById is called: id={}", id);
        teacherService.deleteTeacherById(id);
    }
}
