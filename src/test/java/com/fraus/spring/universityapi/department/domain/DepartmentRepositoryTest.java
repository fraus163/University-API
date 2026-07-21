package com.fraus.spring.universityapi.department.domain;

import com.fraus.spring.universityapi.department.domain.db.DepartmentEntity;
import com.fraus.spring.universityapi.faculty.domain.FacultyRepository;
import com.fraus.spring.universityapi.faculty.domain.db.FacultyEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("DataJpaTest для DepartmentRepository")
class DepartmentRepositoryTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    private FacultyEntity facultyIt;
    private FacultyEntity facultyMath;

    @BeforeEach
    void setUp() {
        facultyIt = new FacultyEntity();
        facultyIt.setName("Факультет ИТ");
        facultyIt.setNumber((short) 1);
        facultyRepository.save(facultyIt);

        facultyMath = new FacultyEntity();
        facultyMath.setName("Математический факультет");
        facultyMath.setNumber((short) 2);
        facultyRepository.save(facultyMath);

        createAndSaveDepartment("Кафедра Программного Обеспечения", facultyIt);
        createAndSaveDepartment("Кафедра Искусственного Интеллекта", facultyIt);
        createAndSaveDepartment("Кафедра Высшей Математики", facultyMath);
    }

    private void createAndSaveDepartment(String name, FacultyEntity faculty) {
        DepartmentEntity department = new DepartmentEntity();
        department.setName(name);
        department.setFaculty(faculty);
        departmentRepository.save(department);
    }

    @Nested
    @DisplayName("Поиск кафедр по фильтру (findDepartmentsByFilter)")
    class FilterTests {

        @Test
        @DisplayName("Должен вернуть все кафедры, если facultyId не передан (null)")
        void shouldReturnAllDepartmentsWhenFacultyIdIsNull() {
            List<DepartmentEntity> result = departmentRepository.findDepartmentsByFilter(null);

            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("Должен вернуть только кафедры конкретного факультета")
        void shouldReturnDepartmentsBySpecificFaculty() {
            List<DepartmentEntity> result = departmentRepository.findDepartmentsByFilter(facultyIt.getId());

            assertThat(result).hasSize(2);
            assertThat(result)
                    .allMatch(dept -> dept.getFaculty().getId().equals(facultyIt.getId()));
        }

        @Test
        @DisplayName("Должен вернуть пустой список, если факультет с таким ID не существует")
        void shouldReturnEmptyListForNonExistingFacultyId() {
            List<DepartmentEntity> result = departmentRepository.findDepartmentsByFilter((short) 999);

            assertThat(result).isEmpty();
        }
    }
}