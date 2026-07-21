package com.fraus.spring.universityapi.position.web.mapper;

import com.fraus.spring.universityapi.position.domain.db.PositionEntity;
import com.fraus.spring.universityapi.position.web.dto.PositionRequest;
import com.fraus.spring.universityapi.position.web.dto.PositionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface PositionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "departmentPositions", ignore = true)
    PositionEntity toEntity(PositionRequest request);

    PositionResponse toResponse(PositionEntity entity);

    default Page<PositionResponse> toResponsePage(Page<PositionEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.map(this::toResponse);
    }
}
