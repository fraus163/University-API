package com.fraus.spring.universityapi.position.web;

import com.fraus.spring.universityapi.position.domain.DepartmentPositionService;
import com.fraus.spring.universityapi.position.web.dto.DepartmentPositionRequest;
import com.fraus.spring.universityapi.position.web.dto.DepartmentPositionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/department_positions")
@RequiredArgsConstructor
@Slf4j
public class DepartmentPositionController {

    private final DepartmentPositionService departmentPositionService;

    @PostMapping
    public ResponseEntity<DepartmentPositionResponse> createDepartmentPosition(
            @RequestBody @Valid DepartmentPositionRequest request,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        log.info("Controller createDepartmentPositions is called: request={}", request);
        DepartmentPositionResponse createdDepartmentPosition = departmentPositionService
                .createDepartmentPositions(request);

        URI location = uriComponentsBuilder
                .path("/api/v1/department_positions/{id}")
                .buildAndExpand(createdDepartmentPosition.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(createdDepartmentPosition);
    }

    @GetMapping
    public Page<DepartmentPositionResponse> getDepartmentPositionByFilter(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @RequestParam(required = false) Short positionId,
            @RequestParam(required = false) Short departmentId
    ) {
        log.info("Controller getDepartmentPositionByFilter is called: positionId={}, departmentId={}",
                positionId, departmentId);
        return departmentPositionService.getDepartmentPositionByFilter(
                        pageable,
                        positionId,
                        departmentId
                );
    }

    @GetMapping("/{id}")
    public DepartmentPositionResponse getDepartmentPositionById(
            @PathVariable Integer id
    ) {
        log.info("Controller getDepartmentPositionById is called");
        return departmentPositionService.getDepartmentPositionById(id);
    }

    @PutMapping("/{id}")
    public DepartmentPositionResponse updateDepartmentPositionById(
            @PathVariable Integer id,
            @RequestBody @Valid DepartmentPositionRequest request
    ) {
        log.info("Controller updateDepartmentPositionById is called: id={}", id);
        return departmentPositionService.updateDepartmentPositionById(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDepartmentPositionById(
            @PathVariable Integer id
    ) {
        log.info("Controller deleteDepartmentPositionById is called: id={}", id);
        departmentPositionService.deleteDepartmentPositionById(id);
    }
}
