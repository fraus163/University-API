package com.fraus.spring.universityapi.position.domain.db;

import com.fraus.spring.universityapi.department.domain.db.DepartmentEntity;
import com.fraus.spring.universityapi.teacher.domain.db.TeacherEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "department_positions")
@NoArgsConstructor
@Getter
@Setter
public class DepartmentPositionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private PositionEntity position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private DepartmentEntity department;

    @ManyToMany(mappedBy = "positions")
    @Setter(AccessLevel.NONE)
    private List<TeacherEntity> teachers = new ArrayList<>();

    public void addTeacher(TeacherEntity teacher) {
        this.teachers.add(teacher);
        if (!teacher.getPositions().contains(this)) {
            teacher.getPositions().add(this);
        }
    }

    public void removeTeacher(TeacherEntity teacher) {
        this.teachers.remove(teacher);
        if (teacher.getPositions().contains(this)) {
            teacher.getPositions().remove(this);
        }
    }
}
