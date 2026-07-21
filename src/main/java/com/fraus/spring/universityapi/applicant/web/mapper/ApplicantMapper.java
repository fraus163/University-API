package com.fraus.spring.universityapi.applicant.web.mapper;

import com.fraus.spring.universityapi.applicant.domain.db.ApplicantEntity;
import com.fraus.spring.universityapi.applicant.web.dto.ApplicantRequest;
import com.fraus.spring.universityapi.applicant.web.dto.ApplicantResponse;
import com.fraus.spring.universityapi.specialty.web.mapper.SpecialtyMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {SpecialtyMapper.class})
public interface ApplicantMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "specialty", ignore = true)
    ApplicantEntity toEntity(ApplicantRequest request);

    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "patronymic", source = "user.patronymic")
    @Mapping(target = "phoneNumber", source = "user.phoneNumber")
    @Mapping(target = "email", source = "user.email")
    ApplicantResponse toResponse(ApplicantEntity entity);

    List<ApplicantResponse> toResponseList(List<ApplicantEntity> entities);

    default Page<ApplicantResponse> toResponsePage(Page<ApplicantEntity> entities) {
        if (entities == null) {
            return Page.empty();
        }
        return entities.map(this::toResponse);
    }
}
