package com.fraus.spring.universityapi.auth.domain.db;

import com.fraus.spring.universityapi.applicant.domain.db.ApplicantEntity;
import com.fraus.spring.universityapi.student.domain.db.StudentEntity;
import com.fraus.spring.universityapi.teacher.domain.db.TeacherEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
@Setter
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 30)
    private String email;

    @Column(name = "password", nullable = false, length = 120)
    private String password;

    @Column(name = "role", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "last_name", nullable = false, length = 30)
    private String lastName;

    @Column(name = "first_name", nullable = false, length = 30)
    private String firstName;

    @Column(name = "patronymic", length = 30)
    private String patronymic;

    @Column(name = "phone_number", nullable = false, unique = true, length = 12)
    private String phoneNumber;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private TeacherEntity teacher;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private ApplicantEntity applicant;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private StudentEntity student;

    public void setTeacher(TeacherEntity teacher) {
        if (teacher == null) {
            if (this.teacher != null) {
                this.teacher.setUser(null);
            }
        } else {
            teacher.setUser(this);
        }
        this.teacher = teacher;
    }

    public void setApplicant(ApplicantEntity applicant) {
        if (applicant == null) {
            if (this.applicant != null) {
                this.applicant.setUser(null);
            }
        } else {
            applicant.setUser(this);
        }
        this.applicant = applicant;
    }

    public void setStudent(StudentEntity student) {
        if (student == null) {
            if (this.student != null) {
                this.student.setUser(null);
            }
        } else {
            student.setUser(this);
        }
        this.student = student;
    }
}