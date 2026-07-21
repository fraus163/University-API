package com.fraus.spring.universityapi.group.domain.db;

import com.fraus.spring.universityapi.schedule.domain.db.ScheduleEntity;
import com.fraus.spring.universityapi.specialty.domain.db.SpecialtyEntity;
import com.fraus.spring.universityapi.student.domain.db.StudentEntity;
import com.fraus.spring.universityapi.subject.domain.db.GroupSubjectEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "groups")
@NoArgsConstructor
@Getter
@Setter
public class GroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Integer id;

    @Column(name = "name", unique = true, nullable = false, length = 10)
    private String name;

    @Column(name = "course", nullable = false)
    private Short course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id", nullable = false)
    private SpecialtyEntity specialty;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<GroupSubjectEntity> groupSubjects = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    @Setter(AccessLevel.NONE)
    private List<StudentEntity> students = new ArrayList<>();

    @ManyToMany(mappedBy = "groups")
    @Setter(AccessLevel.NONE)
    private List<ScheduleEntity> schedule = new ArrayList<>();

    public void addGroupSubject(GroupSubjectEntity groupSubject) {
        this.groupSubjects.add(groupSubject);
        groupSubject.setGroup(this);
    }

    public void removeGroupSubject(GroupSubjectEntity groupSubject) {
        this.groupSubjects.remove(groupSubject);
        groupSubject.setGroup(null);
    }

    public void addStudent(StudentEntity student) {
        this.students.add(student);
        student.setGroup(this);
    }

    public void removeStudent(StudentEntity student) {
        this.students.remove(student);
        student.setGroup(null);
    }

    public void addSchedule(ScheduleEntity schedule) {
        this.schedule.add(schedule);
        if (!schedule.getGroups().contains(this)) {
            schedule.getGroups().add(this);
        }
    }

    public void removeSchedule(ScheduleEntity schedule) {
        this.schedule.remove(schedule);
        if (schedule.getGroups().contains(this)) {
            schedule.getGroups().remove(this);
        }
    }
}