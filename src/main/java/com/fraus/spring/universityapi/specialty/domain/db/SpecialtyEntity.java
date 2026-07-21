package com.fraus.spring.universityapi.specialty.domain.db;

import com.fraus.spring.universityapi.applicant.domain.db.ApplicantEntity;
import com.fraus.spring.universityapi.faculty.domain.db.FacultyEntity;
import com.fraus.spring.universityapi.group.domain.db.GroupEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "specialties")
@NoArgsConstructor
@Getter
@Setter
public class SpecialtyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Short id;

    @Column(name = "name", nullable = false, length = 30, unique = true)
    private String name;

    @Column(name = "degree", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DegreeType degree;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private FacultyEntity faculty;

    @OneToMany(mappedBy = "specialty", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<GroupEntity> groups = new ArrayList<>();

    @OneToMany(mappedBy = "specialty")
    @Setter(AccessLevel.NONE)
    private List<ApplicantEntity> applicants = new ArrayList<>();

    public void addGroup(GroupEntity group) {
        this.groups.add(group);
        group.setSpecialty(this);
    }

    public void removeGroup(GroupEntity group) {
        this.groups.remove(group);
        group.setSpecialty(null);
    }

    public void addApplicant(ApplicantEntity applicant) {
        this.applicants.add(applicant);
        applicant.setSpecialty(this);
    }

    public void removeApplicant(ApplicantEntity applicant) {
        this.applicants.remove(applicant);
        applicant.setSpecialty(null);
    }
}
