package com.fraus.spring.universityapi.position.domain;

import com.fraus.spring.universityapi.department.domain.DepartmentRepository;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.position.domain.db.DepartmentPositionEntity;
import com.fraus.spring.universityapi.position.web.dto.DepartmentPositionRequest;
import com.fraus.spring.universityapi.position.web.dto.DepartmentPositionResponse;
import com.fraus.spring.universityapi.position.web.mapper.DepartmentPositionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentPositionService {

    private final DepartmentPositionRepository departmentPositionRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;

    private final DepartmentPositionMapper departmentPositionMapper;

    @Transactional
    public DepartmentPositionResponse createDepartmentPositions(
            DepartmentPositionRequest request
    ) {
        var foundedPosition = positionRepository
                .findById(request.positionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Должность не найдена",
                        "Position not found: positionId=" + request.positionId()
                ));
        var foundedDepartment = departmentRepository
                .findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Кафедра не найдена",
                        "Department not found: departmentId=" + request.departmentId()
                ));
        var departmentPositionToSave = new DepartmentPositionEntity();
        foundedPosition.addDepartmentPosition(departmentPositionToSave);
        foundedDepartment.addDepartmentPosition(departmentPositionToSave);
        var createdDepartmentPosition = departmentPositionRepository.save(departmentPositionToSave);
        return departmentPositionMapper.toResponse(createdDepartmentPosition);
    }

    public Page<DepartmentPositionResponse> getDepartmentPositionByFilter(
            Pageable pageable,
            Short positionId,
            Short departmentId
    ) {
        var foundedDepartmentPositions = departmentPositionRepository
                .findDepartmentPositionsByFilter(
                        pageable,
                        positionId,
                        departmentId
                );
        return departmentPositionMapper
                .toResponsePage(foundedDepartmentPositions);
    }

    public DepartmentPositionResponse getDepartmentPositionById(
            Integer id
    ) {
        return departmentPositionRepository.findById(id)
                .map(departmentPositionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "Должность кафедры не найдена",
                        "Department position not found: id=" + id
                ));
    }

    @Transactional
    public DepartmentPositionResponse updateDepartmentPositionById(
            Integer id,
            DepartmentPositionRequest request
    ) {
        var foundedDepartmentPosition = departmentPositionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Должность кафедры не найдена",
                        "Department position not found: id=" + id
                ));
        var foundedPosition = positionRepository.findById(request.positionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Должность не найдена",
                        "Position not found: positionId=" + request.positionId()
                ));
        var foundedDepartment = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Кафедра не найдена",
                        "Department not found: departmentId=" + request.departmentId()
                ));
        if (foundedDepartmentPosition.getPosition() != null) {
            foundedDepartmentPosition.getPosition().removeDepartmentPosition(foundedDepartmentPosition);
        }
        if (foundedDepartmentPosition.getDepartment() != null) {
            foundedDepartmentPosition.getDepartment().removeDepartmentPosition(foundedDepartmentPosition);
        }
        foundedPosition.addDepartmentPosition(foundedDepartmentPosition);
        foundedDepartment.addDepartmentPosition(foundedDepartmentPosition);
        return departmentPositionMapper.toResponse(foundedDepartmentPosition);
    }

    @Transactional
    public void deleteDepartmentPositionById(Integer id) {
        if (!departmentPositionRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Должность кафедры не найдена",
                    "Department position not found: id=" + id
            );
        }
        departmentPositionRepository.deleteById(id);
    }
}
