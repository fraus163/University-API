package com.fraus.spring.universityapi.scoresheet.domain;

import com.fraus.spring.universityapi.globalexception.customexception.AlreadyExistsException;
import com.fraus.spring.universityapi.globalexception.customexception.InvalidValueException;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import com.fraus.spring.universityapi.scoresheet.domain.db.AssessmentType;
import com.fraus.spring.universityapi.scoresheet.domain.db.ScoreSheetEntity;
import com.fraus.spring.universityapi.scoresheet.web.dto.ScoreSheetCreateRequest;
import com.fraus.spring.universityapi.scoresheet.web.dto.ScoreSheetResponse;
import com.fraus.spring.universityapi.scoresheet.web.dto.ScoreSheetUpdateRequest;
import com.fraus.spring.universityapi.scoresheet.web.mapper.ScoreSheetMapper;
import com.fraus.spring.universityapi.student.domain.StudentRepository;
import com.fraus.spring.universityapi.student.domain.db.StudentEntity;
import com.fraus.spring.universityapi.subject.domain.GroupSubjectRepository;
import com.fraus.spring.universityapi.subject.domain.db.ControlType;
import com.fraus.spring.universityapi.subject.domain.db.GroupSubjectEntity;
import com.fraus.spring.universityapi.teacher.domain.db.TeacherEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тестирование ScoreSheetService")
class ScoreSheetServiceTest {

    @Mock
    private ScoreSheetRepository scoreSheetRepository;

    @Mock
    private ScoreSheetMapper scoreSheetMapper;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private GroupSubjectRepository groupSubjectRepository;

    @InjectMocks
    private ScoreSheetService scoreSheetService;

    @BeforeEach
    void setUpSecurityContext() {
        setMockAuthentication("1", "ROLE_ADMIN");
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void setMockAuthentication(String userId, String role) {
        var auth = new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority(role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private GroupSubjectEntity createTestGroupSubject(Integer id, Long teacherId, ControlType controlType) {
        GroupSubjectEntity gs = new GroupSubjectEntity();
        ReflectionTestUtils.setField(gs, "id", id);
        gs.setTypeOfControl(controlType);

        if (teacherId != null) {
            TeacherEntity teacher = new TeacherEntity();
            ReflectionTestUtils.setField(teacher, "id", teacherId);
            gs.setTeacher(teacher);
        }
        return gs;
    }

    private StudentEntity createTestStudent(Long id) {
        StudentEntity student = new StudentEntity();
        ReflectionTestUtils.setField(student, "id", id);
        return student;
    }

    private ScoreSheetEntity createTestScoreSheet(Long id, StudentEntity student, GroupSubjectEntity groupSubject, AssessmentType assessment) {
        ScoreSheetEntity scoreSheet = new ScoreSheetEntity();
        ReflectionTestUtils.setField(scoreSheet, "id", id);
        scoreSheet.setStudent(student);
        scoreSheet.setSubject(groupSubject);
        scoreSheet.setAssessment(assessment);
        return scoreSheet;
    }

    @Nested
    @DisplayName("Создание оценки в ведомости")
    class CreateScoreSheet {

        @Test
        @DisplayName("Должен успешно создать оценку, если преподает назначенный учитель")
        void shouldCreateScoreSheetSuccessfullyForAssignedTeacher() {
            Long teacherId = 5L;
            setMockAuthentication(teacherId.toString(), "ROLE_TEACHER");

            ScoreSheetCreateRequest request = new ScoreSheetCreateRequest(100L, 10, AssessmentType.EXCELLENT);
            GroupSubjectEntity groupSubject = createTestGroupSubject(10, teacherId, ControlType.EXAM);
            StudentEntity student = createTestStudent(100L);

            ScoreSheetEntity unsaved = new ScoreSheetEntity();
            ScoreSheetEntity saved = createTestScoreSheet(1L, student, groupSubject, AssessmentType.EXCELLENT);
            ScoreSheetResponse expectedResponse = new ScoreSheetResponse(1L, null, null, AssessmentType.EXCELLENT);

            when(groupSubjectRepository.findById(10)).thenReturn(Optional.of(groupSubject));
            when(scoreSheetRepository.existsByStudentIdAndSubjectId(100L, 10)).thenReturn(false);
            when(studentRepository.findById(100L)).thenReturn(Optional.of(student));
            when(scoreSheetMapper.toEntity(request)).thenReturn(unsaved);
            when(scoreSheetRepository.save(unsaved)).thenReturn(saved);
            when(scoreSheetMapper.toResponse(saved)).thenReturn(expectedResponse);

            ScoreSheetResponse actualResponse = scoreSheetService.createScoreSheet(request);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.id()).isEqualTo(1L);
            verify(scoreSheetRepository, times(1)).save(unsaved);
        }

        @Test
        @DisplayName("Должен выбросить AccessDeniedException, если преподаватель пытается выставить оценку по чужой дисциплине")
        void shouldThrowAccessDeniedForOtherTeacher() {
            Long currentTeacherId = 5L;
            Long assignedTeacherId = 99L;
            setMockAuthentication(currentTeacherId.toString(), "ROLE_TEACHER");

            ScoreSheetCreateRequest request = new ScoreSheetCreateRequest(100L, 10, AssessmentType.EXCELLENT);
            GroupSubjectEntity groupSubject = createTestGroupSubject(10, assignedTeacherId, ControlType.EXAM);

            when(groupSubjectRepository.findById(10)).thenReturn(Optional.of(groupSubject));

            assertThatThrownBy(() -> scoreSheetService.createScoreSheet(request))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("не преподаёте этой группе");

            verify(scoreSheetRepository, never()).save(any());
        }

        @Test
        @DisplayName("Должен выбросить AlreadyExistsException, если оценка студенту по предмету уже выставлена")
        void shouldThrowAlreadyExistsExceptionWhenScoreAlreadyExists() {
            ScoreSheetCreateRequest request = new ScoreSheetCreateRequest(100L, 10, AssessmentType.GOOD);
            GroupSubjectEntity groupSubject = createTestGroupSubject(10, 1L, ControlType.EXAM);

            when(groupSubjectRepository.findById(10)).thenReturn(Optional.of(groupSubject));
            when(scoreSheetRepository.existsByStudentIdAndSubjectId(100L, 10)).thenReturn(true);

            assertThatThrownBy(() -> scoreSheetService.createScoreSheet(request))
                    .isInstanceOf(AlreadyExistsException.class)
                    .hasMessage("Оценка по предмету в ведомости уже существует");
        }

        @Test
        @DisplayName("Должен выбросить InvalidValueException при попытке поставить PASSED за экзамен EXAM")
        void shouldThrowInvalidValueExceptionForPassedOnExam() {
            ScoreSheetCreateRequest request = new ScoreSheetCreateRequest(100L, 10, AssessmentType.PASSED);
            GroupSubjectEntity groupSubject = createTestGroupSubject(10, 1L, ControlType.EXAM);

            when(groupSubjectRepository.findById(10)).thenReturn(Optional.of(groupSubject));

            assertThatThrownBy(() -> scoreSheetService.createScoreSheet(request))
                    .isInstanceOf(InvalidValueException.class)
                    .hasMessage("Неверное значение оценки");
        }

        @Test
        @DisplayName("Должен выбросить InvalidValueException при попытке поставить экзаменационную оценку (EXCELLENT) за зачёт PASS")
        void shouldThrowInvalidValueExceptionForNumericOnPass() {
            ScoreSheetCreateRequest request = new ScoreSheetCreateRequest(100L, 10, AssessmentType.EXCELLENT);
            GroupSubjectEntity groupSubject = createTestGroupSubject(10, 1L, ControlType.PASS);

            when(groupSubjectRepository.findById(10)).thenReturn(Optional.of(groupSubject));

            assertThatThrownBy(() -> scoreSheetService.createScoreSheet(request))
                    .isInstanceOf(InvalidValueException.class)
                    .hasMessage("Неверное значение оценки");
        }
    }

    @Nested
    @DisplayName("Получение оценок")
    class GetScoreSheets {

        @Test
        @DisplayName("Должен разрешить студенту просмотреть свою собственную оценку")
        void shouldAllowStudentToViewOwnScore() {
            Long studentId = 100L;
            setMockAuthentication(studentId.toString(), "ROLE_STUDENT");

            StudentEntity student = createTestStudent(studentId);
            ScoreSheetEntity scoreSheet = createTestScoreSheet(1L, student, null, AssessmentType.EXCELLENT);
            ScoreSheetResponse expectedResponse = new ScoreSheetResponse(1L, null, null, AssessmentType.EXCELLENT);

            when(scoreSheetRepository.findById(1L)).thenReturn(Optional.of(scoreSheet));
            when(scoreSheetMapper.toResponse(scoreSheet)).thenReturn(expectedResponse);

            ScoreSheetResponse response = scoreSheetService.getScoreSheetById(1L);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Должен выбросить AccessDeniedException, если студент пытается просмотреть чужую оценку")
        void shouldThrowAccessDeniedWhenStudentViewsOtherScore() {
            Long currentStudentId = 100L;
            Long otherStudentId = 200L;
            setMockAuthentication(currentStudentId.toString(), "ROLE_STUDENT");

            StudentEntity otherStudent = createTestStudent(otherStudentId);
            ScoreSheetEntity scoreSheet = createTestScoreSheet(1L, otherStudent, null, AssessmentType.GOOD);

            when(scoreSheetRepository.findById(1L)).thenReturn(Optional.of(scoreSheet));

            assertThatThrownBy(() -> scoreSheetService.getScoreSheetById(1L))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Вы не можете просматривать чужие оценки");
        }

        @Test
        @DisplayName("Должен принудительно заменить studentId на ID авторизованного студента при фильтрации")
        void shouldOverrideStudentIdForStudentInFilter() {
            Long currentStudentId = 100L;
            Long requestedStudentId = 999L;
            setMockAuthentication(currentStudentId.toString(), "ROLE_STUDENT");

            Pageable pageable = PageRequest.of(0, 10);
            Page<ScoreSheetEntity> emptyPage = new PageImpl<>(List.of());

            when(scoreSheetRepository.findScoreSheetsByFilter(pageable, currentStudentId, null, null, null))
                    .thenReturn(emptyPage);
            when(scoreSheetMapper.toResponsePage(emptyPage)).thenReturn(new PageImpl<>(List.of()));

            scoreSheetService.getScoreSheetsByFilter(pageable, requestedStudentId, null, null, null);

            verify(scoreSheetRepository, times(1))
                    .findScoreSheetsByFilter(pageable, currentStudentId, null, null, null);
        }
    }

    @Nested
    @DisplayName("Обновление и удаление оценки")
    class UpdateAndDeleteScoreSheet {

        @Test
        @DisplayName("Должен выбросить AccessDeniedException при попытке обновления оценки чужой дисциплины")
        void shouldThrowAccessDeniedOnUpdateForOtherTeacher() {
            Long currentTeacherId = 5L;
            Long assignedTeacherId = 99L;
            setMockAuthentication(currentTeacherId.toString(), "ROLE_TEACHER");

            GroupSubjectEntity groupSubject = createTestGroupSubject(10, assignedTeacherId, ControlType.EXAM);
            ScoreSheetEntity scoreSheet = createTestScoreSheet(1L, createTestStudent(100L), groupSubject, AssessmentType.SATISFACTORY);

            when(scoreSheetRepository.findById(1L)).thenReturn(Optional.of(scoreSheet));

            ScoreSheetUpdateRequest updateRequest = new ScoreSheetUpdateRequest(AssessmentType.GOOD);

            assertThatThrownBy(() -> scoreSheetService.updateScoreSheetById(1L, updateRequest))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("не преподаёте этой группе");
        }

        @Test
        @DisplayName("Должен выбросить ResourceNotFoundException при попытке удаления несуществующей оценки")
        void shouldThrowNotFoundOnDelete() {
            when(scoreSheetRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> scoreSheetService.deleteScoreSheetById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Оценка по предмету не найдена");
        }
    }
}