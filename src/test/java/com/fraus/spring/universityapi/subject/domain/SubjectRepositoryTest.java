package com.fraus.spring.universityapi.subject.domain;

import com.fraus.spring.universityapi.subject.domain.db.SubjectEntity;
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
@DisplayName("DataJpaTest для SubjectRepository")
class SubjectRepositoryTest {

    @Autowired
    private SubjectRepository subjectRepository;

    @BeforeEach
    void setUp() {
        createAndSaveSubject("Высшая математика", "Базовый курс математики");
        createAndSaveSubject("Вычислительная техника", "Основы аппаратного обеспечения");
        createAndSaveSubject("Физика", "Общий курс физики");
    }

    private void createAndSaveSubject(String name, String description) {
        SubjectEntity subject = new SubjectEntity();
        subject.setName(name);
        subject.setDescription(description);
        subjectRepository.save(subject);
    }

    @Nested
    @DisplayName("Поиск предметов по фильтру (findSubjectsByFilter)")
    class FilterTests {

        @Test
        @DisplayName("Должен вернуть все предметы, если фильтр name равен null")
        void shouldReturnAllSubjectsWhenNameIsNull() {
            Page<SubjectEntity> result = subjectRepository.findSubjectsByFilter(
                    PageRequest.of(0, 10), null
            );

            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("Должен отфильтровать предметы по префиксу названия без учета регистра")
        void shouldFilterByNamePrefixCaseInsensitive() {
            Page<SubjectEntity> result = subjectRepository.findSubjectsByFilter(
                    PageRequest.of(0, 10), "вы"
            );

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(SubjectEntity::getName)
                    .containsExactlyInAnyOrder("Высшая математика", "Вычислительная техника");
        }

        @Test
        @DisplayName("Должен вернуть пустую страницу, если совпадений по названию нет")
        void shouldReturnEmptyPageWhenNoMatches() {
            Page<SubjectEntity> result = subjectRepository.findSubjectsByFilter(
                    PageRequest.of(0, 10), "История"
            );

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }
}