package com.fraus.spring.universityapi.subject.domain.db;

import com.fraus.spring.universityapi.schedule.domain.db.ScheduleEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subjects")
@NoArgsConstructor
@Getter
@Setter
public class SubjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true, length = 30)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<GroupSubjectEntity> groupSubjects = new ArrayList<>();

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<ScheduleEntity> schedule = new ArrayList<>();

    public void addGroupSubject(GroupSubjectEntity groupSubject) {
        this.groupSubjects.add(groupSubject);
        groupSubject.setSubject(this);
    }

    public void removeGroupSubject(GroupSubjectEntity groupSubject) {
        this.groupSubjects.remove(groupSubject);
        groupSubject.setSubject(null);
    }

    public void addSchedule(ScheduleEntity schedule) {
        this.schedule.add(schedule);
        schedule.setSubject(this);
    }

    public void removeSchedule(ScheduleEntity schedule) {
        this.schedule.remove(schedule);
        schedule.setSubject(null);
    }
}
