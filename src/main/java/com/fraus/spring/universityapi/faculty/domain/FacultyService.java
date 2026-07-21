package com.fraus.spring.universityapi.faculty.domain;

import com.fraus.spring.universityapi.faculty.domain.db.FacultyEntity;
import com.fraus.spring.universityapi.faculty.web.dto.FacultyRequest;
import com.fraus.spring.universityapi.faculty.web.dto.FacultyResponse;
import com.fraus.spring.universityapi.faculty.web.mapper.FacultyMapper;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final FacultyMapper facultyMapper;

    @Transactional
    public FacultyResponse createFaculty(FacultyRequest request) {
        FacultyEntity facultyEntity = facultyMapper.toEntity(request);
        FacultyEntity savedFaculty = facultyRepository.save(facultyEntity);
        return facultyMapper.toResponse(savedFaculty);
    }

    public List<FacultyResponse> getAllFaculties() {
        List<FacultyEntity> faculties = facultyRepository.findAll();
        return facultyMapper.toResponseList(faculties);
    }

    public FacultyResponse getFacultyById(Short id) {
        return facultyRepository.findById(id)
                .map(facultyMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Факультет не найден", "Faculty not found: id=" + id));
    }

    @Transactional
    public FacultyResponse updateFacultyById(Short id, FacultyRequest request) {
        FacultyEntity faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Факультет не найден", "Faculty not found: id=" + id));
        faculty.setNumber(request.number());
        faculty.setName(request.name());
        return facultyMapper.toResponse(faculty);
    }

    @Transactional
    public void deleteFacultyById(Short id) {
        if (!facultyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Факультет не найден", "Faculty not found: id=" + id);
        }
        facultyRepository.deleteById(id);
    }
}
