package com.fraus.spring.universityapi.position.web.mapper;

import com.fraus.spring.universityapi.department.web.mapper.DepartmentMapper;
import com.fraus.spring.universityapi.position.domain.db.DepartmentPositionEntity;
import com.fraus.spring.universityapi.position.web.dto.DepartmentPositionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DepartmentPositionMapper {

    private final PositionMapper positionMapper;
    private final DepartmentMapper departmentMapper;

    public DepartmentPositionResponse toResponse(DepartmentPositionEntity entity) {
        if (entity == null) {
            return null;
        }
        return new DepartmentPositionResponse(
                entity.getId(),
                positionMapper.toResponse(entity.getPosition()),
                departmentMapper.toResponse(entity.getDepartment())
        );
    }

    public List<DepartmentPositionResponse> toResponseList(
            List<DepartmentPositionEntity> entities
    ) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }

    public Page<DepartmentPositionResponse> toResponsePage(
            Page<DepartmentPositionEntity> entitiesPage
    ) {
        if (entitiesPage == null) {
            return Page.empty();
        }
        return entitiesPage.map(this::toResponse);
    }
}
