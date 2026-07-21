package com.fraus.spring.universityapi.faculty.domain;

import com.fraus.spring.universityapi.faculty.domain.db.FacultyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FacultyRepository extends JpaRepository<FacultyEntity, Short> {
}
