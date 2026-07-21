package com.fraus.spring.universityapi.applicant.domain;

import com.fraus.spring.universityapi.applicant.domain.db.ApplicantEntity;
import com.fraus.spring.universityapi.auth.domain.UserRepository;
import com.fraus.spring.universityapi.auth.domain.db.UserEntity;
import com.fraus.spring.universityapi.auth.domain.db.UserRole;
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
@DisplayName("DataJpaTest для ApplicantRepository")
class ApplicantRepositoryTest {

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    private SpecialtyEntity specialtySoftwareEng;
    private SpecialtyEntity specialtyDesign;

    @BeforeEach
    void setUp() {
        specialtySoftwareEng = new SpecialtyEntity();
        specialtySoftwareEng.setName("Программная инженерия");
        specialtySoftwareEng.setDegree(DegreeType.BACHELOR);
        specialtyRepository.save(specialtySoftwareEng);

        specialtyDesign = new SpecialtyEntity();
        specialtyDesign.setName("Дизайн");
        specialtyDesign.setDegree(DegreeType.BACHELOR);
        specialtyRepository.save(specialtyDesign);

        createAndSaveApplicant("Иванов", "Иван", "Иванович", (short) 320, specialtySoftwareEng);
        createAndSaveApplicant("Иванова", "Мария", "Сергеевна", (short) 280, specialtyDesign);
        createAndSaveApplicant("Петров", "Пётр", "Петрович", (short) 280, specialtySoftwareEng);
    }

    private void createAndSaveApplicant(String lastName, String firstName, String patronymic, Short scores, SpecialtyEntity specialty) {
        UserEntity user = new UserEntity();
        user.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@mail.com");
        user.setPassword("password123");
        user.setLastName(lastName);
        user.setFirstName(firstName);
        user.setPatronymic(patronymic);
        user.setRole(UserRole.APPLICANT);
        user.setPhoneNumber("+7929" + (int)(Math.random() * 8999999 + 1000000));
        userRepository.save(user);

        ApplicantEntity applicant = new ApplicantEntity();
        applicant.setScores(scores);
        applicant.setSpecialty(specialty);
        user.setApplicant(applicant);

        applicantRepository.save(applicant);
    }

    @Nested
    @DisplayName("Поиск абитуриентов с фильтрацией (findApplicantsByFilter)")
    class FilterTests {

        @Test
        @DisplayName("Должен вернуть всех абитуриентов, если фильтры не переданы")
        void shouldReturnAllApplicantsWhenNoFilterProvided() {
            Page<ApplicantEntity> result = applicantRepository.findApplicantsByFilter(
                    PageRequest.of(0, 10), null, null, null, null, null
            );

            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("Должен правильно фильтровать по специальности")
        void shouldFilterBySpecialty() {
            Page<ApplicantEntity> result = applicantRepository.findApplicantsByFilter(
                    PageRequest.of(0, 10), specialtySoftwareEng.getId(), null, null, null, null
            );

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .allMatch(a -> a.getSpecialty().getId().equals(specialtySoftwareEng.getId()));
        }

        @Test
        @DisplayName("Должен правильно фильтровать по баллам")
        void shouldFilterByScores() {
            Page<ApplicantEntity> result = applicantRepository.findApplicantsByFilter(
                    PageRequest.of(0, 10), null, (short) 280, null, null, null
            );

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .allMatch(a -> a.getScores().equals((short) 280));
        }

        @Test
        @DisplayName("Должен фильтровать по префиксу фамилии без учета регистра")
        void shouldFilterByLastNameCaseInsensitivePrefix() {
            Page<ApplicantEntity> result = applicantRepository.findApplicantsByFilter(
                    PageRequest.of(0, 10), null, null, "иван", null, null
            );

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(a -> a.getUser().getLastName())
                    .containsExactlyInAnyOrder("Иванов", "Иванова");
        }
    }
}