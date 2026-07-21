package com.fraus.spring.universityapi.applicant.web;

import com.fraus.spring.universityapi.applicant.domain.ApplicantService;
import com.fraus.spring.universityapi.applicant.web.dto.ApplicantRequest;
import com.fraus.spring.universityapi.applicant.web.dto.ApplicantResponse;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/applicants")
@RequiredArgsConstructor
@Slf4j
public class ApplicantController {

    private final ApplicantService applicantService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMMISSION')")
    public ApplicantResponse getApplicantById(
            @PathVariable(name = "id") Long userId
    ) {
        log.info("Controller getApplicantById is called: userId={}", userId);
        return applicantService.getApplicantById(userId);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COMMISSION')")
    public Page<ApplicantResponse> getApplicantsByFilter(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @RequestParam(required = false) Short specialtyId,
            @RequestParam(required = false) Short scores,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String patronymic
    ) {
        log.info("Controller getApplicantsByFilter is called");
        return applicantService.getApplicantsByFilter(
               pageable,
               specialtyId,
               scores,
               lastName,
               firstName,
               patronymic
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApplicantResponse updateApplicantById(
            @PathVariable(name = "id") Long userId,
            @RequestBody @Valid ApplicantRequest request
    ) {
        log.info("Controller updateApplicantById is called: userId={}", userId);
        return applicantService.updateApplicantById(userId, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteApplicantById(
            @PathVariable(name = "id") Long userId
    ) {
        log.info("Controller deleteApplicantById is called: userId={}", userId);
        applicantService.deleteApplicantById(userId);
    }
}
