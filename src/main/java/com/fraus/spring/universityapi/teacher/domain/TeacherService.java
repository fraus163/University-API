package com.fraus.spring.universityapi.teacher.domain;

import com.fraus.spring.universityapi.auth.domain.UserRepository;
import com.fraus.spring.universityapi.auth.domain.db.UserRole;
import com.fraus.spring.universityapi.globalexception.customexception.InvalidValueException;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.position.domain.DepartmentPositionRepository;
import com.fraus.spring.universityapi.teacher.domain.db.TeacherEntity;
import com.fraus.spring.universityapi.teacher.web.dto.TeacherCreateRequest;
import com.fraus.spring.universityapi.teacher.web.dto.TeacherResponse;
import com.fraus.spring.universityapi.teacher.web.dto.TeacherUpdateRequest;
import com.fraus.spring.universityapi.teacher.web.mapper.TeacherMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final DepartmentPositionRepository departmentPositionRepository;
    private final UserRepository userRepository;
    private final TeacherMapper teacherMapper;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public TeacherResponse createTeacher(TeacherCreateRequest request) {
        var foundedUser = userRepository
                .findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Пользователь не найден",
                        "User not found: id=" + request.userId()
                ));

        if (foundedUser.getTeacher() != null) {
            throw new InvalidValueException(
                    "Пользователь уже является преподавателем",
                    "User with id=" + request.userId() + " is already a teacher"
            );
        }

        var teacherToSave = teacherMapper.toEntity(request);

        foundedUser.setRole(UserRole.TEACHER);
        foundedUser.setTeacher(teacherToSave);
        teacherToSave.setUser(foundedUser);

        findAndAddDepartmentPositions(teacherToSave, request.positionIds());
        var createdTeacher = teacherRepository.save(teacherToSave);
        return teacherMapper.toResponse(createdTeacher);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public TeacherResponse getTeacherById(Long id) {
        return teacherRepository
                .findById(id)
                .map(teacherMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Преподаватель не найден",
                        "Teacher not found: id=" + id
                ));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public Page<TeacherResponse> getTeachersByFilter(
            Pageable pageable,
            String lastName,
            String firstName,
            String patronymic,
            Short positionId,
            Short departmentId,
            Short facultyId
    ) {
        var foundedTeachers = teacherRepository.findTeachersByFilter(
                pageable,
                lastName,
                firstName,
                patronymic,
                positionId,
                departmentId,
                facultyId
        );
        return teacherMapper.toResponsePage(foundedTeachers);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public TeacherResponse updateTeacherById(Long id, TeacherUpdateRequest request) {
        var foundedTeacher = teacherRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Преподаватель не найден",
                        "Teacher not found: id=" + id
                ));

        foundedTeacher.removeAllPositions();
        findAndAddDepartmentPositions(foundedTeacher, request.positionIds());
        foundedTeacher.setAcademicDegree(request.academicDegree());
        foundedTeacher.setAcademicRank(request.academicRank());
        foundedTeacher.setExperience(request.experience());
        return teacherMapper.toResponse(foundedTeacher);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteTeacherById(Long id) {
        TeacherEntity foundedTeacher = teacherRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Преподаватель не найден",
                        "Teacher not found: id=" + id
                ));

        foundedTeacher.removeAllPositions();
        teacherRepository.delete(foundedTeacher);
    }

    private void findAndAddDepartmentPositions(
            TeacherEntity teacher,
            List<Integer> positionIds
    ) {
        for (var positionId : positionIds) {
            var foundedDepartmentPosition = departmentPositionRepository
                    .findById(positionId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Должность кафедры не найдена",
                            "Department position not found: departmentPositionId=" + positionId
                    ));
            teacher.addPosition(foundedDepartmentPosition);
        }
    }
}