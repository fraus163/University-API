package com.fraus.spring.universityapi.subject.web;

import com.fraus.spring.universityapi.subject.domain.SubjectService;
import com.fraus.spring.universityapi.subject.web.dto.SubjectRequest;
import com.fraus.spring.universityapi.subject.web.dto.SubjectResponse;
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

@RestController
@RequestMapping("/api/v1/subjects")
@RequiredArgsConstructor
@Slf4j
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubjectResponse> createSubject(
            @RequestBody @Valid SubjectRequest request,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        log.info("Controller createSubject is called: request={}", request);
        SubjectResponse createdSubject = subjectService.createSubject(request);

        URI location = uriComponentsBuilder
                .path("/api/v1/subjects/{id}")
                .buildAndExpand(createdSubject.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(createdSubject);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public SubjectResponse getSubjectById(
            @PathVariable Integer id
    ) {
        log.info("Controller getSubjectById is called: subjectId={}", id);
        return subjectService.getSubjectById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public Page<SubjectResponse> getSubjectsByFilter(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @RequestParam(required = false) String name
    ) {
        log.info("Controller getSubjectsByFilter is called");
        return subjectService.getSubjectsByFilter(
                pageable,
                name
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SubjectResponse updateSubjectById(
            @PathVariable Integer id,
            @RequestBody @Valid SubjectRequest request
    ) {
        log.info("Controller updateSubjectById is called: subjectId={}", id);
        return subjectService.updateSubjectById(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubjectById(
            @PathVariable Integer id
    ) {
        log.info("Controller deleteSubjectById is called: subjectId={}", id);
        subjectService.deleteSubjectById(id);
    }
}
