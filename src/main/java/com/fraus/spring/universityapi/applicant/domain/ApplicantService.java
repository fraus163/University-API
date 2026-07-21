package com.fraus.spring.universityapi.applicant.domain;

import com.fraus.spring.universityapi.applicant.web.dto.ApplicantRequest;
import com.fraus.spring.universityapi.applicant.web.dto.ApplicantResponse;
import com.fraus.spring.universityapi.applicant.web.mapper.ApplicantMapper;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.specialty.domain.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicantService {

    private final ApplicantRepository applicantRepository;
    private final ApplicantMapper applicantMapper;
    private final SpecialtyRepository specialtyRepository;

    public ApplicantResponse getApplicantById(
            Long userId
    ) {
        return applicantRepository
                .findById(userId)
                .map(applicantMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Абитуриент не найден",
                        "Applicant not found: userId=" + userId
                ));
    }

    public Page<ApplicantResponse> getApplicantsByFilter(
            Pageable pageable,
            Short specialtyId,
            Short scores,
            String lastName,
            String firstName,
            String patronymic
    ) {
        var foundedApplicants = applicantRepository.findApplicantsByFilter(
                pageable,
                specialtyId,
                scores,
                lastName,
                firstName,
                patronymic
        );
        return applicantMapper.toResponsePage(foundedApplicants);
    }

    @Transactional
    public ApplicantResponse updateApplicantById(Long userId, ApplicantRequest request) {
        var foundedApplicant = applicantRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Абитуриент не найден",
                        "Applicant not found: userId=" + userId
                ));
        var foundedSpecialty = specialtyRepository.findById(request.specialtyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Специальность не найдена",
                        "Specialty not found: specialtyId=" + request.specialtyId()
                ));
        if (foundedApplicant.getSpecialty() != null) {
            foundedApplicant.getSpecialty().removeApplicant(foundedApplicant);
        }
        foundedSpecialty.addApplicant(foundedApplicant);
        foundedApplicant.setScores(request.scores());
        return applicantMapper.toResponse(foundedApplicant);
    }

    @Transactional
    public void deleteApplicantById(Long userId) {
        if (!applicantRepository.existsById(userId)) {
            throw new ResourceNotFoundException(
                    "Абитуриент не найден",
                    "Applicant not found: userId=" + userId
            );
        }
        applicantRepository.deleteById(userId);
    }
}
