package com.fraus.spring.universityapi.student.domain;

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
import com.fraus.spring.universityapi.student.domain.db.StudentEntity;
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
@DisplayName("DataJpaTest для StudentRepository")
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    private GroupEntity groupPo11;
    private GroupEntity groupPo21;

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

        groupPo11 = new GroupEntity();
        groupPo11.setName("ПО-11");
        groupPo11.setCourse((short) 1);
        groupPo11.setSpecialty(specialty);
        groupRepository.save(groupPo11);

        groupPo21 = new GroupEntity();
        groupPo21.setName("ПО-21");
        groupPo21.setCourse((short) 2);
        groupPo21.setSpecialty(specialty);
        groupRepository.save(groupPo21);

        createAndSaveStudent("Иванов", "Иван", "Иванович", "ivanov@test.com", "+79991112233", groupPo11);
        createAndSaveStudent("Иванченко", "Петр", "Сергеевич", "ivanchenko@test.com", "+79992223344", groupPo11);
        createAndSaveStudent("Сидоров", "Алексей", "Игоревич", "sidorov@test.com", "+79993334455", groupPo21);
    }

    private void createAndSaveStudent(String lastName, String firstName, String patronymic, String email, String phone, GroupEntity group) {
        UserEntity user = new UserEntity();
        user.setLastName(lastName);
        user.setFirstName(firstName);
        user.setPatronymic(patronymic);
        user.setEmail(email);
        user.setPassword("password123");
        user.setRole(UserRole.STUDENT);
        user.setPhoneNumber(phone);
        userRepository.save(user);

        StudentEntity student = new StudentEntity();
        student.setGroup(group);
        user.setStudent(student);
        studentRepository.save(student);
    }

    @Nested
    @DisplayName("Поиск студентов по фильтру (findStudentsByFilter)")
    class FilterTests {

        @Test
        @DisplayName("Должен вернуть всех студентов, если все фильтры null")
        void shouldReturnAllStudentsWhenFiltersAreNull() {
            Page<StudentEntity> result = studentRepository.findStudentsByFilter(
                    PageRequest.of(0, 10), null, null, null, null
            );

            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("Должен фильтровать по группе")
        void shouldFilterByGroup() {
            Page<StudentEntity> result = studentRepository.findStudentsByFilter(
                    PageRequest.of(0, 10), null, null, null, groupPo11.getId()
            );

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .allMatch(s -> s.getGroup().getId().equals(groupPo11.getId()));
        }

        @Test
        @DisplayName("Должен фильтровать по префиксу фамилии (без учета регистра)")
        void shouldFilterByLastNamePrefixCaseInsensitive() {
            Page<StudentEntity> result = studentRepository.findStudentsByFilter(
                    PageRequest.of(0, 10), "иван", null, null, null
            );

            assertThat(result.getContent()).hasSize(2); // Иванов, Иванченко
        }

        @Test
        @DisplayName("Должен корректно комбинировать ФИО и группу")
        void shouldFilterByAllParametersCombined() {
            Page<StudentEntity> result = studentRepository.findStudentsByFilter(
                    PageRequest.of(0, 10), "Иванов", "Иван", "Иванович", groupPo11.getId()
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getUser().getLastName()).isEqualTo("Иванов");
        }
    }
}