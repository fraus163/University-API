package com.fraus.spring.universityapi.specialty.domain;

import com.fraus.spring.universityapi.faculty.domain.FacultyRepository;
import com.fraus.spring.universityapi.faculty.domain.db.FacultyEntity;
import com.fraus.spring.universityapi.specialty.domain.db.DegreeType;
import com.fraus.spring.universityapi.specialty.domain.db.SpecialtyEntity;
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
@DisplayName("DataJpaTest для SpecialtyRepository")
class SpecialtyRepositoryTest {

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    private FacultyEntity facultyIt;
    private FacultyEntity facultyArt;

    @BeforeEach
    void setUp() {
        facultyIt = new FacultyEntity();
        facultyIt.setName("Факультет ИТ");
        facultyIt.setNumber((short) 1);
        facultyRepository.save(facultyIt);

        facultyArt = new FacultyEntity();
        facultyArt.setName("Факультет Искусств");
        facultyArt.setNumber((short) 2);
        facultyRepository.save(facultyArt);

        createAndSaveSpecialty("Программная инженерия", DegreeType.BACHELOR, facultyIt);
        createAndSaveSpecialty("Прикладная информатика", DegreeType.MASTER, facultyIt);
        createAndSaveSpecialty("Графический дизайн", DegreeType.BACHELOR, facultyArt);
    }

    private void createAndSaveSpecialty(String name, DegreeType degree, FacultyEntity faculty) {
        SpecialtyEntity specialty = new SpecialtyEntity();
        specialty.setName(name);
        specialty.setDegree(degree);
        specialty.setFaculty(faculty);
        specialtyRepository.save(specialty);
    }

    @Nested
    @DisplayName("Поиск специальностей по фильтру (findSpecialtiesByFilter)")
    class FilterTests {

        @Test
        @DisplayName("Должен вернуть все специальности, если facultyId не передан (null)")
        void shouldReturnAllSpecialtiesWhenFacultyIdIsNull() {
            Page<SpecialtyEntity> result = specialtyRepository.findSpecialtiesByFilter(
                    PageRequest.of(0, 10), null
            );

            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("Должен вернуть только специальности конкретного факультета")
        void shouldReturnSpecialtiesBySpecificFaculty() {
            Page<SpecialtyEntity> result = specialtyRepository.findSpecialtiesByFilter(
                    PageRequest.of(0, 10), facultyIt.getId()
            );

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .allMatch(spec -> spec.getFaculty().getId().equals(facultyIt.getId()));
        }

        @Test
        @DisplayName("Должен вернуть пустую страницу, если факультет с таким ID не существует")
        void shouldReturnEmptyPageForNonExistingFacultyId() {
            Page<SpecialtyEntity> result = specialtyRepository.findSpecialtiesByFilter(
                    PageRequest.of(0, 10), (short) 999
            );

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }
}