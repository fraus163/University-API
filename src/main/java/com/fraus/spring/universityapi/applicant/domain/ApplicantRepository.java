package com.fraus.spring.universityapi.applicant.domain;

import com.fraus.spring.universityapi.applicant.domain.db.ApplicantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicantRepository extends JpaRepository<ApplicantEntity, Long> {

    @Query(value = """
            select a
            from ApplicantEntity a
            left join fetch a.user u
            left join fetch a.specialty s
            where (:specialtyId is null or s.id = :specialtyId) and
                  (:scores is null or a.scores = :scores) and
                  (:lastName is null or lower(u.lastName) like lower(concat(cast(:lastName as string), '%'))) and
                  (:firstName is null or lower(u.firstName) like lower(concat(cast(:firstName as string), '%'))) and
                  (:patronymic is null or lower(u.patronymic) like lower(concat(cast(:patronymic as string), '%')))
            """,
            countQuery = """
            select count(a)
            from ApplicantEntity a
            left join a.user u
            left join a.specialty s
            where (:specialtyId is null or s.id = :specialtyId) and
                  (:scores is null or a.scores = :scores) and
                  (:lastName is null or lower(u.lastName) like lower(concat(cast(:lastName as string), '%'))) and
                  (:firstName is null or lower(u.firstName) like lower(concat(cast(:firstName as string), '%'))) and
                  (:patronymic is null or lower(u.patronymic) like lower(concat(cast(:patronymic as string), '%')))
            """)
    Page<ApplicantEntity> findApplicantsByFilter(
            Pageable pageable,
            @Param("specialtyId") Short specialtyId,
            @Param("scores") Short scores,
            @Param("lastName") String lastName,
            @Param("firstName") String firstName,
            @Param("patronymic") String patronymic
    );
}