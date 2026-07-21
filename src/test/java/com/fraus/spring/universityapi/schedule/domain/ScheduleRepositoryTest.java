package com.fraus.spring.universityapi.schedule.domain;

import com.fraus.spring.universityapi.auth.domain.UserRepository;
import com.fraus.spring.universityapi.auth.domain.db.UserEntity;
import com.fraus.spring.universityapi.auth.domain.db.UserRole;
import com.fraus.spring.universityapi.faculty.domain.FacultyRepository;
import com.fraus.spring.universityapi.faculty.domain.db.FacultyEntity;
import com.fraus.spring.universityapi.group.domain.GroupRepository;
import com.fraus.spring.universityapi.group.domain.db.GroupEntity;
import com.fraus.spring.universityapi.schedule.domain.db.ScheduleEntity;
import com.fraus.spring.universityapi.specialty.domain.SpecialtyRepository;
import com.fraus.spring.universityapi.specialty.domain.db.DegreeType;
import com.fraus.spring.universityapi.specialty.domain.db.SpecialtyEntity;
import com.fraus.spring.universityapi.subject.domain.SubjectRepository;
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

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("DataJpaTest для ScheduleRepository")
class ScheduleRepositoryTest {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    private TeacherEntity savedTeacher;
    private SubjectEntity savedSubject;
    private GroupEntity savedGroup;

    @BeforeEach
    void setUp() {
        UserEntity user = new UserEntity();
        user.setEmail("teacher@university.com");
        user.setPassword("password123");
        user.setLastName("Петров");
        user.setFirstName("Пётр");
        user.setPatronymic("Петрович");
        user.setRole(UserRole.TEACHER);
        user.setPhoneNumber("+79290000000");
        userRepository.save(user);

        TeacherEntity teacher = new TeacherEntity();
        teacher.setExperience((short) 5);
        user.setTeacher(teacher);
        savedTeacher = teacherRepository.save(teacher);

        SubjectEntity subject = new SubjectEntity();
        subject.setName("Высшая математика");
        subject.setDescription("Базовый курс высшей математики");
        savedSubject = subjectRepository.save(subject);

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
        savedGroup = groupRepository.save(group);
    }

    private ScheduleEntity saveSchedule(LocalDate date, LocalTime from, LocalTime to, String audience) {
        ScheduleEntity schedule = new ScheduleEntity();
        schedule.setDate(date);
        schedule.setTimeFrom(from);
        schedule.setTimeTo(to);
        schedule.setAudience(audience);
        schedule.setTeacher(savedTeacher);
        schedule.setSubject(savedSubject);
        schedule.addGroup(savedGroup);
        return scheduleRepository.save(schedule);
    }

    @Nested
    @DisplayName("Проверка коллизий аудитории (hasAudienceCollision)")
    class AudienceCollision {

        @Test
        @DisplayName("Должен вернуть true, если время занятий пересекается в одной аудитории")
        void shouldReturnTrueWhenAudienceIsOccupied() {
            LocalDate date = LocalDate.now().plusDays(1);
            saveSchedule(date, LocalTime.of(9, 0), LocalTime.of(10, 30), "301-1");

            boolean collision = scheduleRepository.hasAudienceCollision(
                    "301-1", date, LocalTime.of(9, 30), LocalTime.of(11, 0), null
            );

            assertThat(collision).isTrue();
        }

        @Test
        @DisplayName("Должен вернуть false, если время не пересекается (занятие идет следом)")
        void shouldReturnFalseWhenNoTimeOverlap() {
            LocalDate date = LocalDate.now().plusDays(1);
            saveSchedule(date, LocalTime.of(9, 0), LocalTime.of(10, 30), "301-1");

            boolean collision = scheduleRepository.hasAudienceCollision(
                    "301-1", date, LocalTime.of(10, 30), LocalTime.of(12, 0), null
            );

            assertThat(collision).isFalse();
        }

        @Test
        @DisplayName("Должен исключать текущую запись при редактировании (excludeId)")
        void shouldIgnoreExcludedScheduleId() {
            LocalDate date = LocalDate.now().plusDays(1);
            ScheduleEntity existing = saveSchedule(date, LocalTime.of(9, 0), LocalTime.of(10, 30), "301-1");

            boolean collision = scheduleRepository.hasAudienceCollision(
                    "301-1", date, LocalTime.of(9, 0), LocalTime.of(10, 30), existing.getId()
            );

            assertThat(collision).isFalse();
        }
    }

    @Nested
    @DisplayName("Поиск расписания с фильтрацией (findSchedulesByFilter)")
    class ScheduleFilter {

        @Test
        @DisplayName("Должен найти расписание по ID преподавателя, предмету, дате и группе")
        void shouldFilterByAllParameters() {
            LocalDate date = LocalDate.now().plusDays(1);
            saveSchedule(date, LocalTime.of(9, 0), LocalTime.of(10, 30), "301-1");

            Page<ScheduleEntity> result = scheduleRepository.findSchedulesByFilter(
                    PageRequest.of(0, 10),
                    savedTeacher.getId(),
                    savedSubject.getId(),
                    date,
                    "301-1",
                    savedGroup.getId()
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getAudience()).isEqualTo("301-1");
        }

        @Test
        @DisplayName("Должен вернуть все записи, если все параметры фильтрации null")
        void shouldReturnAllSchedulesWhenFiltersAreNull() {
            LocalDate date = LocalDate.now().plusDays(1);
            saveSchedule(date, LocalTime.of(9, 0), LocalTime.of(10, 30), "301-1");
            saveSchedule(date, LocalTime.of(10, 45), LocalTime.of(12, 15), "302-1");

            Page<ScheduleEntity> result = scheduleRepository.findSchedulesByFilter(
                    PageRequest.of(0, 10), null, null, null, null, null
            );

            assertThat(result.getContent()).hasSize(2);
        }
    }
}