package com.fraus.spring.universityapi.scoresheet.domain.db;

import com.fraus.spring.universityapi.student.domain.db.StudentEntity;
import com.fraus.spring.universityapi.subject.domain.db.GroupSubjectEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "score_sheets",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_student_group_subject",
                columnNames = {"student_id", "group_subject_id"}
        )
)
@NoArgsConstructor
@Getter
@Setter
public class ScoreSheetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_subject_id", nullable = false)
    private GroupSubjectEntity subject;

    @Column(name = "assessment", length = 20)
    @Enumerated(EnumType.STRING)
    private AssessmentType assessment;
}
