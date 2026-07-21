package com.fraus.spring.universityapi.group.web;

import com.fraus.spring.universityapi.group.domain.GroupService;
import com.fraus.spring.universityapi.group.web.dto.GroupRequest;
import com.fraus.spring.universityapi.group.web.dto.GroupResponse;
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
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Slf4j
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GroupResponse> createGroup(
            @RequestBody @Valid GroupRequest request,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        log.info("Controller createGroup is called: request={}", request);
        GroupResponse createdGroup = groupService.createGroup(request);

        URI location = uriComponentsBuilder
                .path("/api/v1/groups/{id}")
                .buildAndExpand(createdGroup.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(createdGroup);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public GroupResponse getGroupById(
            @PathVariable Integer id
    ) {
        log.info("Controller getGroupById is called: id={}", id);
        return groupService.getGroupById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public Page<GroupResponse> getGroupsByFilter(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @RequestParam(required = false) Short specialtyId,
            @RequestParam(required = false) Short facultyId,
            @RequestParam(required = false) Short course
    ) {
        log.info("Controller getGroupsByFilter is called");
        return groupService.getGroupsByFilter(
                pageable,
                specialtyId,
                facultyId,
                course
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public GroupResponse updateGroupById(
            @PathVariable Integer id,
            @RequestBody @Valid GroupRequest request
    ) {
        log.info("Controller updateGroupById is called: id={}", id);
        return groupService.updateGroupById(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroupById(
            @PathVariable Integer id
    ) {
        log.info("Controller deleteGroupById is called: id={}", id);
        groupService.deleteGroupById(id);
    }
}
