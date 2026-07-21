package com.fraus.spring.universityapi.subject.domain;

import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.group.domain.GroupRepository;
import com.fraus.spring.universityapi.teacher.domain.TeacherRepository;
import com.fraus.spring.universityapi.subject.web.dto.GroupSubjectRequest;
import com.fraus.spring.universityapi.subject.web.dto.GroupSubjectResponse;
import com.fraus.spring.universityapi.subject.web.mapper.GroupSubjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupSubjectService {

    private final GroupSubjectRepository groupSubjectRepository;
    private final GroupSubjectMapper groupSubjectMapper;
    private final GroupRepository groupRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;

    @Transactional
    public GroupSubjectResponse createGroupSubject(GroupSubjectRequest request) {
        var groupSubjectToSave = groupSubjectMapper.toEntity(request);

        var foundedGroup = groupRepository.findById(request.groupId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Группа не найдена",
                        "Group not found: groupId=" + request.groupId()
                ));

        var foundedSubject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Дисциплина не найдена",
                        "Subject not found: subjectId=" + request.subjectId()
                ));

        var foundedTeacher = teacherRepository.findById(request.teacherId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Преподаватель не найден",
                        "Teacher not found: teacherId=" + request.teacherId()
                ));

        foundedGroup.addGroupSubject(groupSubjectToSave);
        foundedSubject.addGroupSubject(groupSubjectToSave);
        foundedTeacher.addGroupSubject(groupSubjectToSave);

        var createdGroupSubject = groupSubjectRepository.save(groupSubjectToSave);
        return groupSubjectMapper.toResponse(createdGroupSubject);
    }

    public GroupSubjectResponse getGroupSubjectById(Integer id) {
        return groupSubjectRepository.findById(id)
                .map(groupSubjectMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Дисциплина группы не найдена",
                        "Group subject not found: groupSubjectId=" + id
                ));
    }

    public Page<GroupSubjectResponse> getGroupSubjectsByFilter(
            Pageable pageable,
            Integer groupId,
            Integer subjectId,
            Long teacherId,
            Short term
    ) {
        var foundedGroupSubjects = groupSubjectRepository.findGroupSubjectsByFilter(
                pageable,
                groupId,
                subjectId,
                teacherId,
                term
        );
        return groupSubjectMapper.toResponsePage(foundedGroupSubjects);
    }

    @Transactional
    public GroupSubjectResponse updateGroupSubjectById(Integer id, GroupSubjectRequest request) {
        var foundedGroupSubject = groupSubjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Дисциплина группы не найдена",
                        "Group subject not found: groupSubjectId=" + id
                ));

        if (!foundedGroupSubject.getGroup().getId().equals(request.groupId())) {
            var newGroup = groupRepository.findById(request.groupId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Группа не найдена",
                            "Group not found: groupId=" + request.groupId()
                    ));
            foundedGroupSubject.getGroup().getGroupSubjects().remove(foundedGroupSubject);
            newGroup.addGroupSubject(foundedGroupSubject);
        }

        if (!foundedGroupSubject.getSubject().getId().equals(request.subjectId())) {
            var newSubject = subjectRepository.findById(request.subjectId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Дисциплина не найдена",
                            "Subject not found: subjectId=" + request.subjectId()
                    ));
            foundedGroupSubject.getSubject().getGroupSubjects().remove(foundedGroupSubject);
            newSubject.addGroupSubject(foundedGroupSubject);
        }

        if (foundedGroupSubject.getTeacher() == null || !foundedGroupSubject.getTeacher().getId().equals(request.teacherId())) {
            var newTeacher = teacherRepository.findById(request.teacherId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Преподаватель не найден",
                            "Teacher not found: teacherId=" + request.teacherId()
                    ));

            if (foundedGroupSubject.getTeacher() != null) {
                foundedGroupSubject.getTeacher().removeGroupSubject(foundedGroupSubject);
            }
            newTeacher.addGroupSubject(foundedGroupSubject);
        }

        foundedGroupSubject.setTerm(request.term());
        foundedGroupSubject.setHours(request.hours());
        foundedGroupSubject.setTypeOfControl(request.typeOfControl());

        return groupSubjectMapper.toResponse(foundedGroupSubject);
    }

    @Transactional
    public void deleteGroupSubjectById(Integer id) {
        if (!groupSubjectRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Дисциплина группы не найдена",
                    "Group subject not found: groupSubjectId=" + id
            );
        }
        groupSubjectRepository.deleteById(id);
    }
}