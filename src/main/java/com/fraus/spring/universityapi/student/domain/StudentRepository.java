package com.fraus.spring.universityapi.student.domain;

import com.fraus.spring.universityapi.student.domain.db.StudentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<StudentEntity, Long> {

    @Query(value = """
            select s
            from StudentEntity s
            left join fetch s.user u
            left join fetch s.group g
            where (cast(:groupId as Integer) is null or g.id = :groupId) and
                  (cast(:lastName as string) is null or lower(u.lastName) like lower(concat(cast(:lastName as string), '%'))) and
                  (cast(:firstName as string) is null or lower(u.firstName) like lower(concat(cast(:firstName as string), '%'))) and
                  (cast(:patronymic as string) is null or lower(u.patronymic) like lower(concat(cast(:patronymic as string), '%')))
            """, countQuery = """
            select count(s)
            from StudentEntity s
            left join s.group g
            left join s.user u
            where (cast(:groupId as Integer) is null or g.id = :groupId) and
                  (cast(:lastName as string) is null or lower(u.lastName) like lower(concat(cast(:lastName as string), '%'))) and
                  (cast(:firstName as string) is null or lower(u.firstName) like lower(concat(cast(:firstName as string), '%'))) and
                  (cast(:patronymic as string) is null or lower(u.patronymic) like lower(concat(cast(:patronymic as string), '%')))
            """)
    Page<StudentEntity> findStudentsByFilter(
            Pageable pageable,
            @Param("lastName") String lastName,
            @Param("firstName") String firstName,
            @Param("patronymic") String patronymic,
            @Param("groupId") Integer groupId
    );
}