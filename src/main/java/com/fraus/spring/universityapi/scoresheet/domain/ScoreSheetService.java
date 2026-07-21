package com.fraus.spring.universityapi.scoresheet.domain;

import com.fraus.spring.universityapi.globalexception.customexception.AlreadyExistsException;
import com.fraus.spring.universityapi.globalexception.customexception.InvalidValueException;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.scoresheet.domain.db.AssessmentType;
import com.fraus.spring.universityapi.scoresheet.web.dto.ScoreSheetCreateRequest;
import com.fraus.spring.universityapi.scoresheet.web.dto.ScoreSheetResponse;
import com.fraus.spring.universityapi.scoresheet.web.dto.ScoreSheetUpdateRequest;
import com.fraus.spring.universityapi.scoresheet.web.mapper.ScoreSheetMapper;
import com.fraus.spring.universityapi.student.domain.StudentRepository;
import com.fraus.spring.universityapi.subject.domain.GroupSubjectRepository;
import com.fraus.spring.universityapi.subject.domain.db.ControlType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScoreSheetService {

    private final ScoreSheetRepository scoreSheetRepository;
    private final ScoreSheetMapper scoreSheetMapper;
    private final StudentRepository studentRepository;
    private final GroupSubjectRepository groupSubjectRepository;

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ScoreSheetResponse createScoreSheet(
            ScoreSheetCreateRequest request
    ) {
        var foundedGroupSubject = groupSubjectRepository
                .findById(request.groupSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Дисциплина не найдена",
                        "Group subject not found: groupSubjectId=" + request.groupSubjectId()
                ));

        checkTeacherAccessForGroupSubject(foundedGroupSubject.getTeacher() != null ? foundedGroupSubject.getTeacher().getId() : null);

        if (scoreSheetRepository.existsByStudentIdAndSubjectId(
                request.studentId(),
                request.groupSubjectId()
        )) {
            throw new AlreadyExistsException(
                    "Оценка по предмету в ведомости уже существует",
                    "Subject score is already exists: studentId="
                            + request.studentId()
                            + ", groupSubjectId=" + request.groupSubjectId()
            );
        }

        checkAssessmentValue(
                foundedGroupSubject.getTypeOfControl(),
                request.assessment()
        );

        var foundedStudent = studentRepository
                .findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Студент не найден",
                        "Student not found: studentId=" + request.studentId()
                ));

        var scoreSheetToSave = scoreSheetMapper.toEntity(request);
        scoreSheetToSave.setStudent(foundedStudent);
        scoreSheetToSave.setSubject(foundedGroupSubject);
        var createdScoreSheet = scoreSheetRepository.save(scoreSheetToSave);
        return scoreSheetMapper.toResponse(createdScoreSheet);
    }

    public ScoreSheetResponse getScoreSheetById(Long id) {
        var scoreSheet = scoreSheetRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Оценка по предмету не найдена",
                                "ScoreSheet not found: scoreSheetId=" + id
                        )
                );

        if (isStudent() && !scoreSheet.getStudent().getId().equals(getCurrentUserId())) {
            throw new AccessDeniedException("Вы не можете просматривать чужие оценки");
        }

        return scoreSheetMapper.toResponse(scoreSheet);
    }

    public Page<ScoreSheetResponse> getScoreSheetsByFilter(
            Pageable pageable,
            Long studentId,
            Integer subjectId,
            AssessmentType assessment,
            Short term
    ) {
        Long targetStudentId = studentId;

        if (isStudent()) {
            targetStudentId = getCurrentUserId();
        }

        var foundedScoreSheets = scoreSheetRepository
                .findScoreSheetsByFilter(
                        pageable,
                        targetStudentId,
                        subjectId,
                        assessment,
                        term
                );
        return scoreSheetMapper.toResponsePage(foundedScoreSheets);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ScoreSheetResponse updateScoreSheetById(
            Long id,
            ScoreSheetUpdateRequest request
    ) {
        var foundedScoreSheet = scoreSheetRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Оценка по предмету не найдена",
                                "ScoreSheet not found: scoreSheetId=" + id
                        )
                );

        Long assignedTeacherId = foundedScoreSheet.getSubject().getTeacher() != null
                ? foundedScoreSheet.getSubject().getTeacher().getId()
                : null;
        checkTeacherAccessForGroupSubject(assignedTeacherId);

        checkAssessmentValue(
                foundedScoreSheet.getSubject().getTypeOfControl(),
                request.assessment()
        );
        foundedScoreSheet.setAssessment(request.assessment());
        return scoreSheetMapper.toResponse(foundedScoreSheet);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public void deleteScoreSheetById(Long id) {
        var foundedScoreSheet = scoreSheetRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Оценка по предмету не найдена",
                                "ScoreSheet not found: scoreSheetId=" + id
                        )
                );

        Long assignedTeacherId = foundedScoreSheet.getSubject().getTeacher() != null
                ? foundedScoreSheet.getSubject().getTeacher().getId()
                : null;
        checkTeacherAccessForGroupSubject(assignedTeacherId);

        scoreSheetRepository.delete(foundedScoreSheet);
    }

    private void checkTeacherAccessForGroupSubject(Long assignedTeacherId) {
        if (isAdmin()) {
            return;
        }
        Long currentUserId = getCurrentUserId();
        if (assignedTeacherId == null || !assignedTeacherId.equals(currentUserId)) {
            throw new AccessDeniedException("Вы не можете изменять оценки по дисциплине, которую не преподаёте этой группе");
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AccessDeniedException("Пользователь не авторизован");
        }
        return Long.parseLong(authentication.getName());
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isStudent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
    }

    private void checkAssessmentValue(
            ControlType controlType,
            AssessmentType assessmentType
    ) {
        switch (controlType) {
            case EXAM -> {
                if (assessmentType == AssessmentType.PASSED || assessmentType == AssessmentType.NOT_PASSED) {
                    throw new InvalidValueException(
                            "Неверное значение оценки",
                            "Invalid assessment type: assessmentType=" + assessmentType
                                    + ", type of control=" + controlType
                    );
                }
            }
            case PASS -> {
                if (assessmentType != AssessmentType.PASSED && assessmentType != AssessmentType.NOT_PASSED) {
                    throw new InvalidValueException(
                            "Неверное значение оценки",
                            "Invalid assessment type: assessmentType=" + assessmentType
                                    + ", type of control=" + controlType
                    );
                }
            }
            default -> throw new IllegalArgumentException("Invalid type of control=" + controlType);
        }
    }
}