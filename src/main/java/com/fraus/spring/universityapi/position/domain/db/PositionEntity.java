package com.fraus.spring.universityapi.position.domain.db;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "positions")
@NoArgsConstructor
@Getter
@Setter
public class PositionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Short id;

    @Column(name = "name", unique = true, nullable = false, length = 60)
    private String name;

    @OneToMany(mappedBy = "position", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<DepartmentPositionEntity> departmentPositions = new ArrayList<>();

    public void addDepartmentPosition(DepartmentPositionEntity position) {
        this.departmentPositions.add(position);
        position.setPosition(this);
    }

    public void removeDepartmentPosition(DepartmentPositionEntity position) {
        this.departmentPositions.remove(position);
        position.setPosition(null);
    }
}
