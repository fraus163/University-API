package com.fraus.spring.universityapi.scoresheet.web;

import com.fraus.spring.universityapi.scoresheet.domain.ScoreSheetService;
import com.fraus.spring.universityapi.scoresheet.domain.db.AssessmentType;
import com.fraus.spring.universityapi.scoresheet.web.dto.ScoreSheetCreateRequest;
import com.fraus.spring.universityapi.scoresheet.web.dto.ScoreSheetResponse;
import com.fraus.spring.universityapi.scoresheet.web.dto.ScoreSheetUpdateRequest;
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
@RequestMapping("/api/v1/score_sheets")
@RequiredArgsConstructor
@Slf4j
public class ScoreSheetController {

    private final ScoreSheetService scoreSheetService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ScoreSheetResponse> createScoreSheet(
            @RequestBody @Valid ScoreSheetCreateRequest request,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        log.info("Controller createScoreSheet is called: request={}", request);
        ScoreSheetResponse createdScoreSheet = scoreSheetService
                .createScoreSheet(request);

        URI location = uriComponentsBuilder
                .path("/api/v1/score_sheets/{id}")
                .buildAndExpand(createdScoreSheet.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(createdScoreSheet);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ScoreSheetResponse getScoreSheetById(
            @PathVariable Long id
    ) {
        log.info("Controller getScoreSheetById is called: scoreSheetId={}", id);
        return scoreSheetService.getScoreSheetById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public Page<ScoreSheetResponse> getScoreSheetsByFilter(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) AssessmentType assessment,
            @RequestParam(required = false) Short term
    ) {
        log.info("Controller getScoreSheetsByFilter is called");
        return scoreSheetService.getScoreSheetsByFilter(
                pageable,
                studentId,
                subjectId,
                assessment,
                term
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ScoreSheetResponse updateScoreSheetById(
            @PathVariable Long id,
            @RequestBody @Valid ScoreSheetUpdateRequest request
    ) {
        log.info("Controller updateScoreSheetById is called: scoreSheetId={}", id);
        return scoreSheetService.updateScoreSheetById(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteScoreSheetById(
            @PathVariable Long id
    ) {
        log.info("Controller deleteScoreSheetById is called: scoreSheetId={}", id);
        scoreSheetService.deleteScoreSheetById(id);
    }
}