package com.fraus.spring.universityapi.group.domain;

import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.group.web.dto.GroupRequest;
import com.fraus.spring.universityapi.group.web.dto.GroupResponse;
import com.fraus.spring.universityapi.group.web.mapper.GroupMapper;
import com.fraus.spring.universityapi.specialty.domain.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {

    private final GroupRepository groupRepository;
    private final SpecialtyRepository specialtyRepository;
    private final GroupMapper groupMapper;

    @Transactional
    public GroupResponse createGroup(
            GroupRequest request
    ) {
        var groupToSave = groupMapper.toEntity(request);
        var foundedSpecialty = specialtyRepository
                .findById(request.specialtyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Специальность не найдена",
                        "Specialty not found: id=" + request.specialtyId()
                ));
        foundedSpecialty.addGroup(groupToSave);
        var createdGroup = groupRepository.save(groupToSave);
        return groupMapper.toResponse(createdGroup);
    }

    public GroupResponse getGroupById(
            Integer id
    ) {
        return groupRepository.findById(id)
                .map(groupMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Группа не найдена",
                        "Group not found: id=" + id
                ));
    }

    public Page<GroupResponse> getGroupsByFilter(
            Pageable pageable,
            Short specialtyId,
            Short facultyId,
            Short course
    ) {
        var foundedGroups = groupRepository.findGroupsByFilter(
                pageable,
                specialtyId,
                facultyId,
                course
        );
        return groupMapper.toResponsePage(foundedGroups);
    }

    @Transactional
    public GroupResponse updateGroupById(
            Integer id,
            GroupRequest request
    ) {
        var foundedGroup = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Группа не найдена",
                        "Group not found: id=" + id
                ));
        var foundedSpecialty = specialtyRepository
                .findById(request.specialtyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Специальность не найдена",
                        "Specialty not found: id=" + request.specialtyId()
                ));
        if (foundedGroup.getSpecialty() != null) {
            foundedGroup.getSpecialty().removeGroup(foundedGroup);
        }
        foundedSpecialty.addGroup(foundedGroup);
        foundedGroup.setName(request.name());
        foundedGroup.setCourse(request.course());
        return groupMapper.toResponse(foundedGroup);
    }

    @Transactional
    public void deleteGroupById(Integer id) {
        if (!groupRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Группа не найдена",
                    "Group not found: id=" + id
            );
        }
        groupRepository.deleteById(id);
    }
}
