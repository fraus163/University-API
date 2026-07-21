package com.fraus.spring.universityapi.student.domain.db;

import com.fraus.spring.universityapi.auth.domain.db.UserEntity;
import com.fraus.spring.universityapi.group.domain.db.GroupEntity;
import com.fraus.spring.universityapi.scoresheet.domain.db.ScoreSheetEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students")
@NoArgsConstructor
@Getter
@Setter
public class StudentEntity {

    @Id
    @Column(name = "user_id")
    @Setter(AccessLevel.NONE)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<ScoreSheetEntity> scoreSheets = new ArrayList<>();

    public void addScoreSheet(ScoreSheetEntity scoreSheet) {
        this.scoreSheets.add(scoreSheet);
        scoreSheet.setStudent(this);
    }

    public void removeScoreSheet(ScoreSheetEntity scoreSheet) {
        this.scoreSheets.remove(scoreSheet);
        scoreSheet.setStudent(null);
    }
}
