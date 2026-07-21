package com.fraus.spring.universityapi.position.domain;

import com.fraus.spring.universityapi.position.domain.db.PositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionRepository extends JpaRepository<PositionEntity, Short> {
}
