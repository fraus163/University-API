package com.fraus.spring.universityapi.subject.domain;

import com.fraus.spring.universityapi.subject.domain.db.SubjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<SubjectEntity, Integer> {

    @Query(value = """
            select s
            from SubjectEntity s
            where (cast(:name as string) is null or lower(s.name) like lower(concat(cast(:name as string), '%')))
            """, countQuery = """
            select count(s)
            from SubjectEntity s
            where (cast(:name as string) is null or lower(s.name) like lower(concat(cast(:name as string), '%')))
            """)
    Page<SubjectEntity> findSubjectsByFilter(
            Pageable pageable,
            @Param("name") String name
    );
}