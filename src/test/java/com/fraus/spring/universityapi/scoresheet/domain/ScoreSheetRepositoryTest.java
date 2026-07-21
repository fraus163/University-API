package com.fraus.spring.universityapi.scoresheet.domain;

import com.fraus.spring.universityapi.auth.domain.UserRepository;
import com.fraus.spring.universityapi.auth.domain.db.UserEntity;
import com.fraus.spring.universityapi.auth.domain.db.UserRole;
import com.fraus.spring.universityapi.faculty.domain.FacultyRepository;
import com.fraus.spring.universityapi.faculty.domain.db.FacultyEntity;
import com.fraus.spring.universityapi.group.domain.GroupRepository;
import com.fraus.spring.universityapi.group.domain.db.GroupEntity;
import com.fraus.spring.universityapi.scoresheet.domain.db.AssessmentType;
import com.fraus.spring.universityapi.scoresheet.domain.db.ScoreSheetEntity;
import com.fraus.spring.universityapi.specialty.domain.SpecialtyRepository;
import com.fraus.spring.universityapi.specialty.domain.db.DegreeType;
import com.fraus.spring.universityapi.specialty.domain.db.SpecialtyEntity;
import com.fraus.spring.universityapi.student.domain.StudentRepository;
import com.fraus.spring.universityapi.student.domain.db.StudentEntity;
import com.fraus.spring.universityapi.subject.domain.GroupSubjectRepository;
import com.fraus.spring.universityapi.subject.domain.SubjectRepository;
import com.fraus.spring.universityapi.subject.domain.db.ControlType;
import com.fraus.spring.universityapi.subject.domain.db.GroupSubjectEntity;
import com.fraus.spring.universityapi.subject.domain.db.SubjectEntity;
import com.fraus.spring.universityapi.teacher.domain.TeacherRepository;
import com.fraus.spring.universityapi.teacher.domain.db.TeacherEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("DataJpaTest для ScoreSheetRepository")
class ScoreSheetRepositoryTest {

    @Autowired
    private ScoreSheetRepository scoreSheetRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private GroupSubjectRepository groupSubjectRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    private StudentEntity savedStudent;
    private SubjectEntity savedSubjectMath;
    private SubjectEntity savedSubjectPhysics;

    @BeforeEach
    void setUp() {
        // 1. Преподаватель
        UserEntity teacherUser = new UserEntity();
        teacherUser.setEmail("teacher@university.com");
        teacherUser.setPassword("password123");
        teacherUser.setLastName("Иванов");
        teacherUser.setFirstName("Иван");
        teacherUser.setPatronymic("Иванович");
        teacherUser.setRole(UserRole.TEACHER);
        teacherUser.setPhoneNumber("+79290001122");
        userRepository.save(teacherUser);

        TeacherEntity teacher = new TeacherEntity();
        teacher.setExperience((short) 10);
        teacherUser.setTeacher(teacher);
        TeacherEntity savedTeacher = teacherRepository.save(teacher);

        // 2. Студент и структура (Факультет -> Специальность -> Группа)
        UserEntity studentUser = new UserEntity();
        studentUser.setEmail("student@university.com");
        studentUser.setPassword("password123");
        studentUser.setLastName("Сидоров");
        studentUser.setFirstName("Алексей");
        studentUser.setPatronymic("Игоревич");
        studentUser.setRole(UserRole.STUDENT);
        studentUser.setPhoneNumber("+79291112233");
        userRepository.save(studentUser);

        FacultyEntity faculty = new FacultyEntity();
        faculty.setName("ИТ Факультет");
        faculty.setNumber((short) 1);
        facultyRepository.save(faculty);

        SpecialtyEntity specialty = new SpecialtyEntity();
        specialty.setName("Программная инженерия");
        specialty.setDegree(DegreeType.BACHELOR);
        specialty.setFaculty(faculty);
        specialtyRepository.save(specialty);

        GroupEntity group = new GroupEntity();
        group.setName("ПО-11");
        group.setCourse((short) 1);
        group.setSpecialty(specialty);
        groupRepository.save(group);

        StudentEntity student = new StudentEntity();
        student.setGroup(group);
        studentUser.setStudent(student);
        savedStudent = studentRepository.save(student);

        // 3. Предметы
        savedSubjectMath = new SubjectEntity();
        savedSubjectMath.setName("Высшая математика");
        savedSubjectMath.setDescription("Базовый курс математики");
        subjectRepository.save(savedSubjectMath);

        savedSubjectPhysics = new SubjectEntity();
        savedSubjectPhysics.setName("Физика");
        savedSubjectPhysics.setDescription("Общий курс физики");
        subjectRepository.save(savedSubjectPhysics);

        // 4. Назначение предметов группе (с правильными enum ControlType.EXAM и ControlType.PASS)
        GroupSubjectEntity groupSubjectMath = createGroupSubject(
                group, savedSubjectMath, savedTeacher, (short) 1, (short) 72, ControlType.EXAM
        );

        GroupSubjectEntity groupSubjectPhysics = createGroupSubject(
                group, savedSubjectPhysics, savedTeacher, (short) 2, (short) 108, ControlType.PASS
        );

        // 5. Создание ведомостей (с правильными enum AssessmentType.EXCELLENT и AssessmentType.PASSED)
        createAndSaveScoreSheet(savedStudent, groupSubjectMath, AssessmentType.EXCELLENT);
        createAndSaveScoreSheet(savedStudent, groupSubjectPhysics, AssessmentType.PASSED);
    }

    private GroupSubjectEntity createGroupSubject(GroupEntity group, SubjectEntity subject, TeacherEntity teacher, Short term, Short hours, ControlType typeOfControl) {
        GroupSubjectEntity gs = new GroupSubjectEntity();
        gs.setGroup(group);
        gs.setSubject(subject);
        gs.setTeacher(teacher);
        gs.setTerm(term);
        gs.setHours(hours);
        gs.setTypeOfControl(typeOfControl);
        return groupSubjectRepository.save(gs);
    }

    private void createAndSaveScoreSheet(StudentEntity student, GroupSubjectEntity groupSubject, AssessmentType assessment) {
        ScoreSheetEntity scoreSheet = new ScoreSheetEntity();
        scoreSheet.setStudent(student);
        scoreSheet.setSubject(groupSubject);
        scoreSheet.setAssessment(assessment);
        scoreSheetRepository.save(scoreSheet);
    }

    @Nested
    @DisplayName("Проверка существования записи (existsByStudentIdAndSubjectId)")
    class ExistsCheck {

        @Test
        @DisplayName("Должен вернуть true, если ведомость существует")
        void shouldReturnTrueWhenScoreSheetExists() {
            boolean exists = scoreSheetRepository.existsByStudentIdAndSubjectId(
                    savedStudent.getId(), savedSubjectMath.getId()
            );

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Должен вернуть false, если ведомости нет")
        void shouldReturnFalseWhenScoreSheetDoesNotExist() {
            boolean exists = scoreSheetRepository.existsByStudentIdAndSubjectId(
                    savedStudent.getId(), 999
            );

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Поиск ведомостей с фильтрацией (findScoreSheetsByFilter)")
    class FilterTests {

        @Test
        @DisplayName("Должен вернуть все ведомости, если фильтры null")
        void shouldReturnAllScoreSheetsWhenFiltersAreNull() {
            Page<ScoreSheetEntity> result = scoreSheetRepository.findScoreSheetsByFilter(
                    PageRequest.of(0, 10), null, null, null, null
            );

            assertThat(result.getContent()).hasSize(2);
        }

        @Test
        @DisplayName("Должен правильно фильтровать по типу оценки (assessment)")
        void shouldFilterByAssessmentType() {
            Page<ScoreSheetEntity> result = scoreSheetRepository.findScoreSheetsByFilter(
                    PageRequest.of(0, 10), null, null, AssessmentType.EXCELLENT, null
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getAssessment()).isEqualTo(AssessmentType.EXCELLENT);
        }

        @Test
        @DisplayName("Должен правильно фильтровать по семестру (term)")
        void shouldFilterByTerm() {
            Page<ScoreSheetEntity> result = scoreSheetRepository.findScoreSheetsByFilter(
                    PageRequest.of(0, 10), null, null, null, (short) 2
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getSubject().getSubject().getName()).isEqualTo("Физика");
        }
    }
}