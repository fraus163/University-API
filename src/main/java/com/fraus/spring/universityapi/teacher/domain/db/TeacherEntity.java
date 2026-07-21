package com.fraus.spring.universityapi.teacher.domain.db;

import com.fraus.spring.universityapi.auth.domain.db.UserEntity;
import com.fraus.spring.universityapi.position.domain.db.DepartmentPositionEntity;
import com.fraus.spring.universityapi.schedule.domain.db.ScheduleEntity;
import com.fraus.spring.universityapi.subject.domain.db.GroupSubjectEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teachers")
@NoArgsConstructor
@Getter
@Setter
public class TeacherEntity {

    @Id
    @Column(name = "user_id")
    @Setter(AccessLevel.NONE)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "experience")
    private Short experience;

    @Column(name = "academic_rank", length = 20)
    @Enumerated(EnumType.STRING)
    private AcademicRankType academicRank;

    @Column(name = "academic_degree", length = 30)
    @Enumerated(EnumType.STRING)
    private AcademicDegreeType academicDegree;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "teachers_positions",
            joinColumns = @JoinColumn(name = "teacher_id", referencedColumnName = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "department_positions_id", referencedColumnName = "id")
    )
    @Setter(AccessLevel.NONE)
    private List<DepartmentPositionEntity> positions = new ArrayList<>();

    @OneToMany(mappedBy = "teacher")
    @Setter(AccessLevel.NONE)
    private List<ScheduleEntity> schedule = new ArrayList<>();

    @OneToMany(mappedBy = "teacher")
    @Setter(AccessLevel.NONE)
    private List<GroupSubjectEntity> groupSubjects = new ArrayList<>();

    public void addPosition(DepartmentPositionEntity position) {
        this.positions.add(position);
        if (!position.getTeachers().contains(this)) {
            position.getTeachers().add(this);
        }
    }

    public void removePosition(DepartmentPositionEntity position) {
        this.positions.remove(position);
        if (position.getTeachers().contains(this)) {
            position.getTeachers().remove(this);
        }
    }

    public void removeAllPositions() {
        List<DepartmentPositionEntity> positionsCopy = new ArrayList<>(this.positions);
        for (var position : positionsCopy) {
            position.getTeachers().remove(this);
        }
        this.positions.clear();
    }

    public void addSchedule(ScheduleEntity schedule) {
        this.schedule.add(schedule);
        schedule.setTeacher(this);
    }

    public void removeSchedule(ScheduleEntity schedule) {
        this.schedule.remove(schedule);
        schedule.setTeacher(null);
    }

    public void addGroupSubject(GroupSubjectEntity groupSubject) {
        this.groupSubjects.add(groupSubject);
        groupSubject.setTeacher(this);
    }

    public void removeGroupSubject(GroupSubjectEntity groupSubject) {
        this.groupSubjects.remove(groupSubject);
        groupSubject.setTeacher(null);
    }
}
