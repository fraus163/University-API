package com.fraus.spring.universityapi.department.domain.db;

import com.fraus.spring.universityapi.faculty.domain.db.FacultyEntity;
import com.fraus.spring.universityapi.position.domain.db.DepartmentPositionEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments")
@NoArgsConstructor
@Getter
@Setter
public class DepartmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Short id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private FacultyEntity faculty;

    @Column(name = "name", unique = true, nullable = false, length = 60)
    private String name;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<DepartmentPositionEntity> departmentPositions = new ArrayList<>();

    public void addDepartmentPosition(DepartmentPositionEntity position) {
        this.departmentPositions.add(position);
        position.setDepartment(this);
    }

    public void removeDepartmentPosition(DepartmentPositionEntity position) {
        this.departmentPositions.remove(position);
        position.setDepartment(null);
    }
}
