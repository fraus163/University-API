package com.fraus.spring.universityapi.scoresheet.domain;

import com.fraus.spring.universityapi.scoresheet.domain.db.AssessmentType;
import com.fraus.spring.universityapi.scoresheet.domain.db.ScoreSheetEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreSheetRepository extends JpaRepository<ScoreSheetEntity, Long> {

    @Query("""
            select case when count(ss) > 0 then true else false end
            from ScoreSheetEntity ss
            join ss.subject gs
            where ss.student.id = :studentId and gs.subject.id = :subjectId
            """)
    boolean existsByStudentIdAndSubjectId(@Param("studentId") Long studentId, @Param("subjectId") Integer subjectId);

    @Query(value = """
            select ss
            from ScoreSheetEntity ss
            left join fetch ss.student st
            left join fetch ss.subject gs
            left join fetch gs.subject su
            where (cast(:studentId as Long) is null or st.id = :studentId) and
                  (cast(:subjectId as Integer) is null or su.id = :subjectId) and
                  (cast(:assessment as String) is null or ss.assessment = :assessment) and
                  (cast(:term as Short) is null or gs.term = :term)
            """, countQuery = """
            select count(ss)
            from ScoreSheetEntity ss
            left join ss.student st
            left join ss.subject gs
            left join gs.subject su
            where (cast(:studentId as Long) is null or st.id = :studentId) and
                  (cast(:subjectId as Integer) is null or su.id = :subjectId) and
                  (cast(:assessment as String) is null or ss.assessment = :assessment) and
                  (cast(:term as Short) is null or gs.term = :term)
            """)
    Page<ScoreSheetEntity> findScoreSheetsByFilter(
            Pageable pageable,
            @Param("studentId") Long studentId,
            @Param("subjectId") Integer subjectId,
            @Param("assessment") AssessmentType assessment,
            @Param("term") Short term
    );
}