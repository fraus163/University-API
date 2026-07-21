package com.fraus.spring.universityapi.position.domain;

import com.fraus.spring.universityapi.department.domain.DepartmentRepository;
import com.fraus.spring.universityapi.department.domain.db.DepartmentEntity;
import com.fraus.spring.universityapi.faculty.domain.FacultyRepository;
import com.fraus.spring.universityapi.faculty.domain.db.FacultyEntity;
import com.fraus.spring.universityapi.position.domain.db.DepartmentPositionEntity;
import com.fraus.spring.universityapi.position.domain.db.PositionEntity;
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
@DisplayName("DataJpaTest для DepartmentPositionRepository")
class DepartmentPositionRepositoryTest {

    @Autowired
    private DepartmentPositionRepository departmentPositionRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    private PositionEntity posDocent;
    private PositionEntity posProfessor;

    private DepartmentEntity deptSoftwareEng;
    private DepartmentEntity deptMath;

    @BeforeEach
    void setUp() {
        FacultyEntity faculty = new FacultyEntity();
        faculty.setName("Факультет ИТ");
        faculty.setNumber((short) 1);
        facultyRepository.save(faculty);

        deptSoftwareEng = new DepartmentEntity();
        deptSoftwareEng.setName("Кафедра ПО");
        deptSoftwareEng.setFaculty(faculty);
        departmentRepository.save(deptSoftwareEng);

        deptMath = new DepartmentEntity();
        deptMath.setName("Кафедра ВМ");
        deptMath.setFaculty(faculty);
        departmentRepository.save(deptMath);

        posDocent = new PositionEntity();
        posDocent.setName("Доцент");
        positionRepository.save(posDocent);

        posProfessor = new PositionEntity();
        posProfessor.setName("Профессор");
        positionRepository.save(posProfessor);

        createAndSaveDepartmentPosition(posDocent, deptSoftwareEng);
        createAndSaveDepartmentPosition(posProfessor, deptSoftwareEng);
        createAndSaveDepartmentPosition(posDocent, deptMath);
    }

    private void createAndSaveDepartmentPosition(PositionEntity position, DepartmentEntity department) {
        DepartmentPositionEntity dp = new DepartmentPositionEntity();
        dp.setPosition(position);
        dp.setDepartment(department);
        departmentPositionRepository.save(dp);
    }

    @Nested
    @DisplayName("Поиск должностей кафедры по фильтру (findDepartmentPositionsByFilter)")
    class FilterTests {

        @Test
        @DisplayName("Должен вернуть все записи, если фильтры не переданы (null)")
        void shouldReturnAllDepartmentPositionsWhenNoFilterProvided() {
            Page<DepartmentPositionEntity> result = departmentPositionRepository.findDepartmentPositionsByFilter(
                    PageRequest.of(0, 10), null, null
            );

            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("Должен фильтровать записи по ID должности (positionId)")
        void shouldFilterByPositionId() {
            Page<DepartmentPositionEntity> result = departmentPositionRepository.findDepartmentPositionsByFilter(
                    PageRequest.of(0, 10), posDocent.getId(), null
            );

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .allMatch(dp -> dp.getPosition().getId().equals(posDocent.getId()));
        }

        @Test
        @DisplayName("Должен фильтровать записи по ID кафедры (departmentId)")
        void shouldFilterByDepartmentId() {
            Page<DepartmentPositionEntity> result = departmentPositionRepository.findDepartmentPositionsByFilter(
                    PageRequest.of(0, 10), null, deptSoftwareEng.getId()
            );

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .allMatch(dp -> dp.getDepartment().getId().equals(deptSoftwareEng.getId()));
        }

        @Test
        @DisplayName("Должен корректно комбинировать фильтры по должности и кафедре")
        void shouldFilterByBothPositionAndDepartment() {
            Page<DepartmentPositionEntity> result = departmentPositionRepository.findDepartmentPositionsByFilter(
                    PageRequest.of(0, 10), posProfessor.getId(), deptSoftwareEng.getId()
            );

            assertThat(result.getContent()).hasSize(1);
            DepartmentPositionEntity dp = result.getContent().get(0);
            assertThat(dp.getPosition().getName()).isEqualTo("Профессор");
            assertThat(dp.getDepartment().getName()).isEqualTo("Кафедра ПО");
        }
    }
}