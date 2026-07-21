package com.fraus.spring.universityapi.group.domain;

import com.fraus.spring.universityapi.faculty.domain.FacultyRepository;
import com.fraus.spring.universityapi.faculty.domain.db.FacultyEntity;
import com.fraus.spring.universityapi.group.domain.db.GroupEntity;
import com.fraus.spring.universityapi.specialty.domain.SpecialtyRepository;
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
@DisplayName("DataJpaTest для GroupRepository")
class GroupRepositoryTest {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    private SpecialtyEntity specialtySoftwareEng;
    private SpecialtyEntity specialtyDesign;
    private FacultyEntity facultyIt;

    @BeforeEach
    void setUp() {
        // 1. Создаем факультеты
        facultyIt = new FacultyEntity();
        facultyIt.setName("Факультет ИТ");
        facultyIt.setNumber((short) 1);
        facultyRepository.save(facultyIt);

        FacultyEntity facultyArt = new FacultyEntity();
        facultyArt.setName("Факультет Искусств");
        facultyArt.setNumber((short) 2);
        facultyRepository.save(facultyArt);

        // 2. Создаем специальности
        specialtySoftwareEng = new SpecialtyEntity();
        specialtySoftwareEng.setName("Программная инженерия");
        specialtySoftwareEng.setDegree(DegreeType.BACHELOR);
        specialtySoftwareEng.setFaculty(facultyIt);
        specialtyRepository.save(specialtySoftwareEng);

        specialtyDesign = new SpecialtyEntity();
        specialtyDesign.setName("Дизайн");
        specialtyDesign.setDegree(DegreeType.BACHELOR);
        specialtyDesign.setFaculty(facultyArt);
        specialtyRepository.save(specialtyDesign);

        // 3. Создаем группы
        createAndSaveGroup("ПО-11", (short) 1, specialtySoftwareEng);
        createAndSaveGroup("ПО-21", (short) 2, specialtySoftwareEng);
        createAndSaveGroup("Д-11", (short) 1, specialtyDesign);
    }

    private void createAndSaveGroup(String name, Short course, SpecialtyEntity specialty) {
        GroupEntity group = new GroupEntity();
        group.setName(name);
        group.setCourse(course);
        group.setSpecialty(specialty);
        groupRepository.save(group);
    }

    @Nested
    @DisplayName("Поиск групп по фильтру (findGroupsByFilter)")
    class FilterTests {

        @Test
        @DisplayName("Должен вернуть все группы, если фильтры не переданы (null)")
        void shouldReturnAllGroupsWhenNoFilterProvided() {
            Page<GroupEntity> result = groupRepository.findGroupsByFilter(
                    PageRequest.of(0, 10), null, null, null
            );

            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("Должен правильно фильтровать группы по специальности")
        void shouldFilterBySpecialty() {
            Page<GroupEntity> result = groupRepository.findGroupsByFilter(
                    PageRequest.of(0, 10), specialtySoftwareEng.getId(), null, null
            );

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .allMatch(g -> g.getSpecialty().getId().equals(specialtySoftwareEng.getId()));
        }

        @Test
        @DisplayName("Должен правильно фильтровать группы по факультету")
        void shouldFilterByFaculty() {
            Page<GroupEntity> result = groupRepository.findGroupsByFilter(
                    PageRequest.of(0, 10), null, facultyIt.getId(), null
            );

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .allMatch(g -> g.getSpecialty().getFaculty().getId().equals(facultyIt.getId()));
        }

        @Test
        @DisplayName("Должен правильно фильтровать группы по номеру курса")
        void shouldFilterByCourse() {
            Page<GroupEntity> result = groupRepository.findGroupsByFilter(
                    PageRequest.of(0, 10), null, null, (short) 1
            );

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .allMatch(g -> g.getCourse().equals((short) 1));
        }

        @Test
        @DisplayName("Должен корректно комбинировать все фильтры")
        void shouldFilterByAllParametersCombined() {
            Page<GroupEntity> result = groupRepository.findGroupsByFilter(
                    PageRequest.of(0, 10),
                    specialtySoftwareEng.getId(),
                    facultyIt.getId(),
                    (short) 2
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("ПО-21");
        }
    }
}