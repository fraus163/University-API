package com.fraus.spring.universityapi.teacher.domain;

import com.fraus.spring.universityapi.auth.domain.UserRepository;
import com.fraus.spring.universityapi.auth.domain.db.UserEntity;
import com.fraus.spring.universityapi.auth.domain.db.UserRole;
import com.fraus.spring.universityapi.department.domain.DepartmentRepository;
import com.fraus.spring.universityapi.department.domain.db.DepartmentEntity;
import com.fraus.spring.universityapi.faculty.domain.FacultyRepository;
import com.fraus.spring.universityapi.faculty.domain.db.FacultyEntity;
import com.fraus.spring.universityapi.position.domain.DepartmentPositionRepository;
import com.fraus.spring.universityapi.position.domain.PositionRepository;
import com.fraus.spring.universityapi.position.domain.db.DepartmentPositionEntity;
import com.fraus.spring.universityapi.position.domain.db.PositionEntity;
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
@DisplayName("DataJpaTest для TeacherRepository")
class TeacherRepositoryTest {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private DepartmentPositionRepository departmentPositionRepository;

    private FacultyEntity facultyIt;
    private DepartmentEntity deptSoftwareEng;
    private PositionEntity posDocent;
    private TeacherEntity teacherIvanov;

    @BeforeEach
    void setUp() {
        facultyIt = new FacultyEntity();
        facultyIt.setName("ИТ Факультет");
        facultyIt.setNumber((short) 1);
        facultyRepository.save(facultyIt);

        deptSoftwareEng = new DepartmentEntity();
        deptSoftwareEng.setName("Кафедра ПО");
        deptSoftwareEng.setFaculty(facultyIt);
        departmentRepository.save(deptSoftwareEng);

        posDocent = new PositionEntity();
        posDocent.setName("Доцент");
        positionRepository.save(posDocent);

        DepartmentPositionEntity deptPos = new DepartmentPositionEntity();
        deptPos.setDepartment(deptSoftwareEng);
        deptPos.setPosition(posDocent);
        departmentPositionRepository.save(deptPos);

        teacherIvanov = createAndSaveTeacher("Иванов", "Иван", "Иванович", "ivanov@test.com", "+79001112233", (short) 10);
        teacherIvanov.getPositions().add(deptPos);
        teacherRepository.save(teacherIvanov);

        createAndSaveTeacher("Петров", "Пётр", "Петрович", "petrov@test.com", "+79002223344", (short) 5);
    }

    private TeacherEntity createAndSaveTeacher(String lastName, String firstName, String patronymic, String email, String phone, Short experience) {
        UserEntity user = new UserEntity();
        user.setLastName(lastName);
        user.setFirstName(firstName);
        user.setPatronymic(patronymic);
        user.setEmail(email);
        user.setPassword("password123");
        user.setRole(UserRole.TEACHER);
        user.setPhoneNumber(phone);
        userRepository.save(user);

        TeacherEntity teacher = new TeacherEntity();
        teacher.setExperience(experience);
        user.setTeacher(teacher);
        return teacherRepository.save(teacher);
    }

    @Nested
    @DisplayName("Поиск преподавателей по фильтру (findTeachersByFilter)")
    class FilterTests {

        @Test
        @DisplayName("Должен вернуть всех преподавателей, если фильтры null")
        void shouldReturnAllTeachersWhenFiltersAreNull() {
            Page<TeacherEntity> result = teacherRepository.findTeachersByFilter(
                    PageRequest.of(0, 10), null, null, null, null, null, null
            );

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("Должен фильтровать по факультету, кафедре и должности")
        void shouldFilterByStructureAndPosition() {
            Page<TeacherEntity> result = teacherRepository.findTeachersByFilter(
                    PageRequest.of(0, 10),
                    null, null, null,
                    posDocent.getId(),
                    deptSoftwareEng.getId(),
                    facultyIt.getId()
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getUser().getLastName()).isEqualTo("Иванов");
        }

        @Test
        @DisplayName("Должен фильтровать по префиксу фамилии без учета регистра")
        void shouldFilterByLastNamePrefix() {
            Page<TeacherEntity> result = teacherRepository.findTeachersByFilter(
                    PageRequest.of(0, 10), "иван", null, null, null, null, null
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getUser().getLastName()).isEqualTo("Иванов");
        }
    }
}