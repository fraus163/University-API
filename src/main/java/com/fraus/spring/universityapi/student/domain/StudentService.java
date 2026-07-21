package com.fraus.spring.universityapi.student.domain;

import com.fraus.spring.universityapi.auth.domain.UserRepository;
import com.fraus.spring.universityapi.auth.domain.db.UserRole;
import com.fraus.spring.universityapi.globalexception.customexception.InvalidValueException;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.group.domain.GroupRepository;
import com.fraus.spring.universityapi.student.domain.db.StudentEntity;
import com.fraus.spring.universityapi.student.web.dto.StudentCreateRequest;
import com.fraus.spring.universityapi.student.web.dto.StudentResponse;
import com.fraus.spring.universityapi.student.web.dto.StudentUpdateRequest;
import com.fraus.spring.universityapi.student.web.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final StudentMapper studentMapper;
    private final GroupRepository groupRepository;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public StudentResponse createStudent(
            StudentCreateRequest request
    ) {
        var foundedUser = userRepository
                .findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Пользователь не найден",
                        "User not found: userId=" + request.userId()
                ));

        if (foundedUser.getStudent() != null) {
            throw new InvalidValueException(
                    "Пользователь уже является студентом",
                    "User with id=" + request.userId() + " is already a student"
            );
        }

        var foundedGroup = groupRepository
                .findById(request.groupId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Группа не найдена",
                        "Group not found: groupId=" + request.groupId()
                ));

        var studentToSave = studentMapper.toEntity(request);

        foundedUser.setRole(UserRole.STUDENT);
        foundedUser.setStudent(studentToSave);

        foundedGroup.addStudent(studentToSave);

        var savedStudent = studentRepository.save(studentToSave);
        return studentMapper.toResponse(savedStudent);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public StudentResponse getStudentById(
            Long id
    ) {
        return studentRepository
                .findById(id)
                .map(studentMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Студент не найден",
                        "Student not found: studentId=" + id
                ));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public Page<StudentResponse> getStudentsByFilter(
            Pageable pageable,
            String lastName,
            String firstName,
            String patronymic,
            Integer groupId
    ) {
        var foundedStudents = studentRepository.findStudentsByFilter(
                pageable,
                lastName,
                firstName,
                patronymic,
                groupId
        );
        return studentMapper.toResponsePage(foundedStudents);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public StudentResponse updateStudentById(
            Long id,
            StudentUpdateRequest request
    ) {
        var foundedStudent = studentRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Студент не найден",
                        "Student not found: studentId=" + id
                ));

        var foundedGroup = groupRepository
                .findById(request.groupId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Группа не найдена",
                        "Group not found: groupId=" + request.groupId()
                ));

        if (foundedStudent.getGroup() != null) {
            foundedStudent.getGroup().removeStudent(foundedStudent);
        }
        foundedGroup.addStudent(foundedStudent);

        return studentMapper.toResponse(foundedStudent);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteStudentById(Long id) {
        StudentEntity foundedStudent = studentRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Студент не найден",
                        "Student not found: studentId=" + id
                ));

        if (foundedStudent.getGroup() != null) {
            foundedStudent.getGroup().removeStudent(foundedStudent);
        }

        studentRepository.delete(foundedStudent);
    }
}