package com.fraus.spring.universityapi.specialty.domain;

import com.fraus.spring.universityapi.specialty.domain.db.SpecialtyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecialtyRepository extends JpaRepository<SpecialtyEntity, Short> {

    @Query(value = """
            select s
            from SpecialtyEntity s
            left join fetch s.faculty f
            where (cast(:facultyId as Short) is null or f.id = :facultyId)
            """, countQuery = """
            select count(s)
            from SpecialtyEntity s
            left join s.faculty f
            where (cast(:facultyId as Short) is null or f.id = :facultyId)
            """)
    Page<SpecialtyEntity> findSpecialtiesByFilter(
            Pageable pageable,
            @Param("facultyId") Short facultyId
    );
}