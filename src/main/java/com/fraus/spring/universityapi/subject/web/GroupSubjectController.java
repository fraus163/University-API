package com.fraus.spring.universityapi.subject.web;

import com.fraus.spring.universityapi.subject.domain.GroupSubjectService;
import com.fraus.spring.universityapi.subject.web.dto.GroupSubjectRequest;
import com.fraus.spring.universityapi.subject.web.dto.GroupSubjectResponse;
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
@RequestMapping("/api/v1/group-subjects")
@Slf4j
@RequiredArgsConstructor
public class GroupSubjectController {

    private final GroupSubjectService groupSubjectService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GroupSubjectResponse> createGroupSubject(
            @RequestBody @Valid GroupSubjectRequest request,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        log.info("Controller createGroupSubject is called: request={}", request);
        GroupSubjectResponse createdGroupSubject = groupSubjectService
                .createGroupSubject(request);

        URI location = uriComponentsBuilder
                .path("/api/v1/group-subjects/{id}")
                .buildAndExpand(createdGroupSubject.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(createdGroupSubject);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public GroupSubjectResponse getGroupSubjectById(
            @PathVariable Integer id
    ) {
        log.info("Controller getGroupSubjectById is called: groupSubjectId={}", id);
        return groupSubjectService.getGroupSubjectById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public Page<GroupSubjectResponse> getGroupSubjectsByFilter(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @RequestParam(required = false) Integer groupId,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Short term

    ) {
        log.info("Controller getGroupSubjectsByFilter is called");
        return groupSubjectService.getGroupSubjectsByFilter(
               pageable,
               groupId,
               subjectId,
               teacherId,
               term
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public GroupSubjectResponse updateGroupSubjectById(
          @PathVariable Integer id,
          @RequestBody @Valid GroupSubjectRequest request
    ) {
        log.info("Controller updateGroupSubjectById is called: groupSubjectId={}", id);
        return groupSubjectService.updateGroupSubjectById(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroupSubjectById(
            @PathVariable Integer id
    ) {
        log.info("Controller deleteGroupSubjectById is called: groupSubjectId={}", id);
        groupSubjectService.deleteGroupSubjectById(id);
    }
}
