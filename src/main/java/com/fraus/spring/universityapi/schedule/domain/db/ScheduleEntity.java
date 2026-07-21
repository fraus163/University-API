package com.fraus.spring.universityapi.schedule.domain.db;

import com.fraus.spring.universityapi.group.domain.db.GroupEntity;
import com.fraus.spring.universityapi.subject.domain.db.SubjectEntity;
import com.fraus.spring.universityapi.teacher.domain.db.TeacherEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schedule")
@NoArgsConstructor
@Getter
@Setter
public class ScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private TeacherEntity teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private SubjectEntity subject;

    @Column(name = "time_from", nullable = false)
    private LocalTime timeFrom;

    @Column(name = "time_to", nullable = false)
    private LocalTime timeTo;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "audience", nullable = false, length = 10)
    private String audience;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "schedule_groups",
            joinColumns = @JoinColumn(name = "schedule_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "group_id", referencedColumnName = "id")
    )
    @Setter(AccessLevel.NONE)
    private List<GroupEntity> groups = new ArrayList<>();

    public void addGroup(GroupEntity group) {
        this.groups.add(group);
        if (!group.getSchedule().contains(this)) {
            group.getSchedule().add(this);
        }
    }

    public void removeGroup(GroupEntity group) {
        this.groups.remove(group);
        if (group.getSchedule().contains(this)) {
            group.getSchedule().remove(this);
        }
    }
}
