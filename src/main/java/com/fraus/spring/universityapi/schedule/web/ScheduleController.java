package com.fraus.spring.universityapi.schedule.web;

import com.fraus.spring.universityapi.schedule.domain.ScheduleService;
import com.fraus.spring.universityapi.schedule.web.dto.ScheduleRequest;
import com.fraus.spring.universityapi.schedule.web.dto.ScheduleResponse;
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
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScheduleResponse> createSchedule(
            @RequestBody @Valid ScheduleRequest request,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        log.info("Controller createSchedule is called: request={}", request);
        ScheduleResponse createdSchedule = scheduleService.createSchedule(request);

        URI location = uriComponentsBuilder
                .path("/api/v1/schedule/{id}")
                .buildAndExpand(createdSchedule.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(createdSchedule);
    }

    @GetMapping("/{id}")
    public ScheduleResponse getScheduleById(
            @PathVariable Long id
    ) {
        log.info("Controller getScheduleById is called: scheduleId={}", id);
        return scheduleService.getScheduleById(id);
    }

    @GetMapping
    public Page<ScheduleResponse> getSchedulesByFilter(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String audience,
            @RequestParam(required = false) Integer groupId
    ) {
        log.info("Controller getSchedulesByFilter is called");
        return scheduleService.getSchedulesByFilter(
                pageable,
                teacherId,
                subjectId,
                date,
                audience,
                groupId
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ScheduleResponse updateScheduleById(
            @PathVariable Long id,
            @RequestBody @Valid ScheduleRequest request
    ) {
        log.info("Controller updateScheduleById is called: scheduleId={}", id);
        return scheduleService.updateScheduleById(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteScheduleById(
            @PathVariable Long id
    ) {
        log.info("Controller deleteScheduleById is called: scheduleId={}", id);
        scheduleService.deleteScheduleById(id);
    }
}
