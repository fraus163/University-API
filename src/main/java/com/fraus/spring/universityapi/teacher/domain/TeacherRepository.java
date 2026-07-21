package com.fraus.spring.universityapi.teacher.domain;

import com.fraus.spring.universityapi.teacher.domain.db.TeacherEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends JpaRepository<TeacherEntity, Long> {

    @Query(value = """
        select distinct t
        from TeacherEntity t
        left join fetch t.user u
        left join t.positions p
        left join p.department d
        left join p.position pos
        left join d.faculty f
        where (cast(:positionId as Short) is null or pos.id = :positionId) and
              (cast(:departmentId as Short) is null or d.id = :departmentId) and
              (cast(:facultyId as Short) is null or f.id = :facultyId) and
              (cast(:lastName as string) is null or lower(u.lastName) like lower(concat(cast(:lastName as string), '%'))) and 
              (cast(:firstName as string) is null or lower(u.firstName) like lower(concat(cast(:firstName as string), '%'))) and
              (cast(:patronymic as string) is null or lower(u.patronymic) like lower(concat(cast(:patronymic as string), '%')))
        """, countQuery = """
        select count(distinct t)
        from TeacherEntity t
        left join t.user u
        left join t.positions p
        left join p.department d
        left join p.position pos
        left join d.faculty f
        where (cast(:positionId as Short) is null or pos.id = :positionId) and
              (cast(:departmentId as Short) is null or d.id = :departmentId) and
              (cast(:facultyId as Short) is null or f.id = :facultyId) and
              (cast(:lastName as string) is null or lower(u.lastName) like lower(concat(cast(:lastName as string), '%'))) and 
              (cast(:firstName as string) is null or lower(u.firstName) like lower(concat(cast(:firstName as string), '%'))) and
              (cast(:patronymic as string) is null or lower(u.patronymic) like lower(concat(cast(:patronymic as string), '%')))
        """)
    Page<TeacherEntity> findTeachersByFilter(
            Pageable pageable,
            @Param("lastName") String lastName,
            @Param("firstName") String firstName,
            @Param("patronymic") String patronymic,
            @Param("positionId") Short positionId,
            @Param("departmentId") Short departmentId,
            @Param("facultyId") Short facultyId
    );
}