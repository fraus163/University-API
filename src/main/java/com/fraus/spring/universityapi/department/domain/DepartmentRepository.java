package com.fraus.spring.universityapi.department.domain;

import com.fraus.spring.universityapi.department.domain.db.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Short> {
    @Query("""
            SELECT d 
            FROM DepartmentEntity d 
            LEFT JOIN FETCH d.faculty 
            WHERE (:facultyId IS NULL OR d.faculty.id = :facultyId)
            """)
    List<DepartmentEntity> findDepartmentsByFilter(
            @Param("facultyId") Short facultyId
    );
}