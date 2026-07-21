package com.fraus.spring.universityapi.specialty.domain;

import com.fraus.spring.universityapi.faculty.domain.FacultyRepository;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.specialty.web.dto.SpecialtyRequest;
import com.fraus.spring.universityapi.specialty.web.dto.SpecialtyResponse;
import com.fraus.spring.universityapi.specialty.web.mapper.SpecialtyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final FacultyRepository facultyRepository;
    private final SpecialtyMapper specialtyMapper;

    @Transactional
    public SpecialtyResponse createSpecialty(
            SpecialtyRequest request
    ) {
        var specialtyToSave = specialtyMapper.toEntity(request);
        var foundedFaculty = facultyRepository
                .findById(request.facultyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Факультет не найден",
                        "Faculty not found: id=" + request.facultyId()
                ));
        foundedFaculty.addSpecialty(specialtyToSave);
        var createdSpecialty = specialtyRepository.save(specialtyToSave);
        return specialtyMapper.toResponse(createdSpecialty);
    }

    public SpecialtyResponse getSpecialtyById(
            Short id
    ) {
        return specialtyRepository
                .findById(id)
                .map(specialtyMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Специальность не найдена",
                        "Specialty not found: id=" + id
                ));
    }

    public Page<SpecialtyResponse> getSpecialtiesByFilter(
            Pageable pageable,
            Short facultyId
    ) {
        var foundedSpecialties = specialtyRepository
                .findSpecialtiesByFilter(pageable, facultyId);
        return specialtyMapper.toResponsePage(foundedSpecialties);
    }

    @Transactional
    public SpecialtyResponse updateSpecialtyById(
            Short id,
            SpecialtyRequest request
    ) {
        var foundedSpecialty = specialtyRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Специальность не найдена",
                        "Specialty not found: id=" + id
                ));
        var foundedFaculty = facultyRepository
                .findById(request.facultyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Факультет не найден",
                        "Faculty not found: id=" + request.facultyId()
                ));
        if (foundedSpecialty.getFaculty() != null) {
            foundedSpecialty.getFaculty().removeSpecialty(foundedSpecialty);
        }
        foundedFaculty.addSpecialty(foundedSpecialty);
        foundedSpecialty.setName(request.name());
        foundedSpecialty.setDegree(request.degree());
        return specialtyMapper.toResponse(foundedSpecialty);
    }

    @Transactional
    public void deleteSpecialtyById(Short id) {
        if (!specialtyRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Специальность не найдена",
                    "Specialty not found: id=" + id
            );
        }
        specialtyRepository.deleteById(id);
    }
}
