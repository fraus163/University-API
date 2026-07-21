package com.fraus.spring.universityapi.subject.domain;

import com.fraus.spring.universityapi.auth.domain.UserRepository;
import com.fraus.spring.universityapi.auth.domain.db.UserEntity;
import com.fraus.spring.universityapi.auth.domain.db.UserRole;
import com.fraus.spring.universityapi.faculty.domain.FacultyRepository;
import com.fraus.spring.universityapi.faculty.domain.db.FacultyEntity;
import com.fraus.spring.universityapi.group.domain.GroupRepository;
import com.fraus.spring.universityapi.group.domain.db.GroupEntity;
import com.fraus.spring.universityapi.specialty.domain.SpecialtyRepository;
import com.fraus.spring.universityapi.specialty.domain.db.DegreeType;
import com.fraus.spring.universityapi.specialty.domain.db.SpecialtyEntity;
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
@DisplayName("DataJpaTest для GroupSubjectRepository")
class GroupSubjectRepositoryTest {

    @Autowired
    private GroupSubjectRepository groupSubjectRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    private GroupEntity savedGroupPo11;
    private SubjectEntity savedSubjectMath;
    private TeacherEntity savedTeacher;

    @BeforeEach
    void setUp() {
        FacultyEntity faculty = new FacultyEntity();
        faculty.setName("ИТ Факультет");
        faculty.setNumber((short) 1);
        facultyRepository.save(faculty);

        SpecialtyEntity specialty = new SpecialtyEntity();
        specialty.setName("Программная инженерия");
        specialty.setDegree(DegreeType.BACHELOR);
        specialty.setFaculty(faculty);
        specialtyRepository.save(specialty);

        savedGroupPo11 = new GroupEntity();
        savedGroupPo11.setName("ПО-11");
        savedGroupPo11.setCourse((short) 1);
        savedGroupPo11.setSpecialty(specialty);
        groupRepository.save(savedGroupPo11);

        UserEntity user = new UserEntity();
        user.setEmail("teacher@university.com");
        user.setPassword("password123");
        user.setLastName("Петров");
        user.setFirstName("Пётр");
        user.setPatronymic("Петрович");
        user.setRole(UserRole.TEACHER);
        user.setPhoneNumber("+79290001122");
        userRepository.save(user);

        TeacherEntity teacher = new TeacherEntity();
        teacher.setExperience((short) 5);
        user.setTeacher(teacher);
        savedTeacher = teacherRepository.save(teacher);

        savedSubjectMath = new SubjectEntity();
        savedSubjectMath.setName("Высшая математика");
        savedSubjectMath.setDescription("Курс высшей математики");
        subjectRepository.save(savedSubjectMath);

        createAndSaveGroupSubject(savedGroupPo11, savedSubjectMath, savedTeacher, (short) 1, (short) 72, ControlType.EXAM);
    }

    private void createAndSaveGroupSubject(GroupEntity group, SubjectEntity subject, TeacherEntity teacher, Short term, Short hours, ControlType controlType) {
        GroupSubjectEntity gs = new GroupSubjectEntity();
        gs.setGroup(group);
        gs.setSubject(subject);
        gs.setTeacher(teacher);
        gs.setTerm(term);
        gs.setHours(hours);
        gs.setTypeOfControl(controlType);
        groupSubjectRepository.save(gs);
    }

    @Nested
    @DisplayName("Проверка существования (existsByGroup_IdAndSubject_IdAndTeacher_Id)")
    class ExistsTests {

        @Test
        @DisplayName("Должен вернуть true, если связка группа + предмет + преподаватель существует")
        void shouldReturnTrueWhenGroupSubjectExists() {
            boolean exists = groupSubjectRepository.existsByGroup_IdAndSubject_IdAndTeacher_Id(
                    savedGroupPo11.getId(), savedSubjectMath.getId(), savedTeacher.getId()
            );

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Должен вернуть false, если связка не существует")
        void shouldReturnFalseWhenGroupSubjectDoesNotExist() {
            boolean exists = groupSubjectRepository.existsByGroup_IdAndSubject_IdAndTeacher_Id(
                    savedGroupPo11.getId(), 999, savedTeacher.getId()
            );

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Поиск связок предметов с фильтрацией (findGroupSubjectsByFilter)")
    class FilterTests {

        @Test
        @DisplayName("Должен вернуть все записи, если фильтры null")
        void shouldReturnAllGroupSubjectsWhenFiltersAreNull() {
            Page<GroupSubjectEntity> result = groupSubjectRepository.findGroupSubjectsByFilter(
                    PageRequest.of(0, 10), null, null, null, null
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Должен отфильтровать по группе, предмету, преподавателю и семестру")
        void shouldFilterByAllParameters() {
            Page<GroupSubjectEntity> result = groupSubjectRepository.findGroupSubjectsByFilter(
                    PageRequest.of(0, 10),
                    savedGroupPo11.getId(),
                    savedSubjectMath.getId(),
                    savedTeacher.getId(),
                    (short) 1
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getSubject().getName()).isEqualTo("Высшая математика");
        }
    }
}