package com.fraus.spring.universityapi.department.domain;

import com.fraus.spring.universityapi.department.web.dto.DepartmentRequest;
import com.fraus.spring.universityapi.department.web.dto.DepartmentResponse;
import com.fraus.spring.universityapi.department.web.mapper.DepartmentMapper;
import com.fraus.spring.universityapi.faculty.domain.FacultyRepository;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentMapper departmentMapper;

    @Transactional
    public DepartmentResponse createDepartment(
            DepartmentRequest request
    ) {
        var foundedFaculty = facultyRepository.findById(request.facultyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                                "Факультет не найден",
                                "Faculty not found: facultyId=" + request.facultyId()
                ));
        var departmentToSave = departmentMapper.toEntity(request);
        departmentToSave.setFaculty(foundedFaculty);
        var savedDepartment = departmentRepository.save(departmentToSave);
        return departmentMapper.toResponse(savedDepartment);
    }

    public List<DepartmentResponse> getDepartmentsByFilter(
            Short facultyId
    ) {
        var foundedDepartments = departmentRepository.findDepartmentsByFilter(facultyId);
        return departmentMapper.toResponseList(foundedDepartments);
    }

    public DepartmentResponse getDepartmentById(Short id) {
        return departmentRepository.findById(id)
                .map(departmentMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Кафедра не найдена",
                        "Department not found: id=" + id
                ));
    }

    @Transactional
    public DepartmentResponse updateDepartmentById(
            Short id,
            DepartmentRequest request
    ) {
        var foundedDepartment = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "Кафедра не найдена",
                                "Department not found: id=" + id
                        ));
        var foundedFaculty = facultyRepository.findById(request.facultyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                                "Факультет не найден",
                                "Faculty not found: facultyId=" + request.facultyId()
                        ));
        foundedDepartment.setFaculty(foundedFaculty);
        foundedDepartment.setName(request.name());
        return departmentMapper.toResponse(foundedDepartment);
    }

    @Transactional
    public void deleteDepartmentById(Short id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Кафедра не найдена",
                    "Department not found: id=" + id
            );
        }
        departmentRepository.deleteById(id);
    }
}
