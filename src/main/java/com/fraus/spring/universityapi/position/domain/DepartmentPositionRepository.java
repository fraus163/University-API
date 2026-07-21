package com.fraus.spring.universityapi.position.domain;

import com.fraus.spring.universityapi.position.domain.db.DepartmentPositionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentPositionRepository extends JpaRepository<DepartmentPositionEntity, Integer> {

    @Query(value = """
            select dp
            from DepartmentPositionEntity dp
            left join fetch dp.position
            left join fetch dp.department
            where (:positionId is null or dp.position.id = :positionId) and
                  (:departmentId is null or dp.department.id = :departmentId)
            """,
            countQuery = """
            select count(dp)
            from DepartmentPositionEntity dp
            where (:positionId is null or dp.position.id = :positionId) and
                  (:departmentId is null or dp.department.id = :departmentId)
            """)
    Page<DepartmentPositionEntity> findDepartmentPositionsByFilter(
            Pageable pageable,
            @Param("positionId") Short positionId,
            @Param("departmentId") Short departmentId
    );
}
