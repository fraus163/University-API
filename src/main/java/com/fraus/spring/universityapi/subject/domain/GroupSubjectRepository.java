package com.fraus.spring.universityapi.subject.domain;

import com.fraus.spring.universityapi.subject.domain.db.GroupSubjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupSubjectRepository extends JpaRepository<GroupSubjectEntity, Integer> {

    @Query(value = """
            select gs
            from GroupSubjectEntity gs
            left join fetch gs.group g
            left join fetch gs.subject s
            left join fetch gs.teacher t
            where (cast(:groupId as Integer) is null or g.id = :groupId) and
                  (cast(:subjectId as Integer) is null or s.id = :subjectId) and
                  (cast(:teacherId as Long) is null or t.id = :teacherId) and
                  (cast(:term as Short) is null or gs.term = :term)
            """, countQuery = """
            select count(gs)
            from GroupSubjectEntity gs
            left join gs.group g
            left join gs.subject s
            left join gs.teacher t
            where (cast(:groupId as Integer) is null or g.id = :groupId) and
                  (cast(:subjectId as Integer) is null or s.id = :subjectId) and
                  (cast(:teacherId as Long) is null or t.id = :teacherId) and
                  (cast(:term as Short) is null or gs.term = :term)
            """)
    Page<GroupSubjectEntity> findGroupSubjectsByFilter(
            Pageable pageable,
            @Param("groupId") Integer groupId,
            @Param("subjectId") Integer subjectId,
            @Param("teacherId") Long teacherId,
            @Param("term") Short term
    );

    boolean existsByGroup_IdAndSubject_IdAndTeacher_Id(Integer groupId, Integer subjectId, Long teacherId);
}