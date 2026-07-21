package com.fraus.spring.universityapi.group.domain;

import com.fraus.spring.universityapi.group.domain.db.GroupEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Integer> {

    @Query(value = """
            select g
            from GroupEntity g
            left join fetch g.specialty s
            left join fetch s.faculty f
            where (:specialtyId is null or s.id = :specialtyId) and
                  (:facultyId is null or f.id = :facultyId) and
                  (:course is null or g.course = :course)
            """,
            countQuery = """
            select count(g)
            from GroupEntity g
            left join g.specialty s
            left join s.faculty f
            where (:specialtyId is null or s.id = :specialtyId) and
                  (:facultyId is null or f.id = :facultyId) and
                  (:course is null or g.course = :course)
            """)
    Page<GroupEntity> findGroupsByFilter(
            Pageable pageable,
            @Param("specialtyId") Short specialtyId,
            @Param("facultyId") Short facultyId,
            @Param("course") Short course
    );
}