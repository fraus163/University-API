package com.fraus.spring.universityapi.subject.domain.db;

import com.fraus.spring.universityapi.group.domain.db.GroupEntity;
import com.fraus.spring.universityapi.scoresheet.domain.db.ScoreSheetEntity;
import com.fraus.spring.universityapi.teacher.domain.db.TeacherEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "group_subjects")
@NoArgsConstructor
@Getter
@Setter
public class GroupSubjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private SubjectEntity subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private TeacherEntity teacher;

    @Column(name = "term", nullable = false)
    private Short term;

    @Column(name = "hours", nullable = false)
    private Short hours;

    @Column(name = "type_of_control", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private ControlType typeOfControl;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<ScoreSheetEntity> scoreSheets = new ArrayList<>();

    public void addScoreSheet(ScoreSheetEntity scoreSheet) {
        this.scoreSheets.add(scoreSheet);
        scoreSheet.setSubject(this);
    }

    public void removeScoreSheet(ScoreSheetEntity scoreSheet) {
        this.scoreSheets.remove(scoreSheet);
        scoreSheet.setSubject(null);
    }
}
