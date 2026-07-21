package com.fraus.spring.universityapi.student.web;

import com.fraus.spring.universityapi.student.domain.StudentService;
import com.fraus.spring.universityapi.student.web.dto.StudentCreateRequest;
import com.fraus.spring.universityapi.student.web.dto.StudentResponse;
import com.fraus.spring.universityapi.student.web.dto.StudentUpdateRequest;
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
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponse> createStudent(
            @RequestBody @Valid StudentCreateRequest request,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        log.info("Controller createStudent is called: request={}", request);
        StudentResponse createdStudent = studentService.createStudent(request);

        URI location = uriComponentsBuilder
                .path("/api/v1/students/{id}")
                .buildAndExpand(createdStudent.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(createdStudent);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public StudentResponse getStudentById(
            @PathVariable Long id
    ) {
        log.info("Controller getStudentById is called: studentId={}", id);
        return studentService.getStudentById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public Page<StudentResponse> getStudentsByFilter(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String patronymic,
            @RequestParam(required = false) Integer groupId
    ) {
        log.info("Controller getStudentsByFilter is called");
        return studentService.getStudentsByFilter(
                pageable,
                lastName,
                firstName,
                patronymic,
                groupId
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public StudentResponse updateStudentById(
         @PathVariable Long id,
         @RequestBody @Valid StudentUpdateRequest request
    ) {
        log.info("Controller updateStudentById is called: studentId={}",id);
        return studentService.updateStudentById(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStudentById(
            @PathVariable Long id
    ) {
        log.info("Controller deleteStudentById is called: studentId={}", id);
        studentService.deleteStudentById(id);
    }
}
