package com.fraus.spring.universityapi.faculty.domain.db;

import com.fraus.spring.universityapi.department.domain.db.DepartmentEntity;
import com.fraus.spring.universityapi.group.domain.db.GroupEntity;
import com.fraus.spring.universityapi.specialty.domain.db.SpecialtyEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "faculties")
@NoArgsConstructor
@Setter
@Getter
public class FacultyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(lombok.AccessLevel.NONE)
    private Short id;

    @Column(name = "number", unique = true, nullable = false)
    private Short number;

    @Column(name = "name", unique = true, nullable = false, length = 60)
    private String name;

    @OneToMany(mappedBy = "faculty", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(lombok.AccessLevel.NONE)
    private List<DepartmentEntity> departments = new ArrayList<>();

    @OneToMany(mappedBy = "faculty", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(lombok.AccessLevel.NONE)
    private List<SpecialtyEntity> specialties = new ArrayList<>();

    public void addDepartment(DepartmentEntity department) {
        this.departments.add(department);
        department.setFaculty(this);
    }

    public void removeDepartment(DepartmentEntity department) {
        this.departments.remove(department);
        department.setFaculty(null);
    }

    public void addSpecialty(SpecialtyEntity specialty) {
        this.specialties.add(specialty);
        specialty.setFaculty(this);
    }

    public void removeSpecialty(SpecialtyEntity specialty) {
        this.specialties.remove(specialty);
        specialty.setFaculty(null);
    }
}
