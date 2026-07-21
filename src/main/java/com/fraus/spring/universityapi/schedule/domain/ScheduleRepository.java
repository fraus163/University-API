package com.fraus.spring.universityapi.schedule.domain;

import com.fraus.spring.universityapi.schedule.domain.db.ScheduleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {

    @Query(value = """
            select s
            from ScheduleEntity s
            left join fetch s.teacher t
            left join fetch s.subject su
            left join s.groups g
            where (cast(:teacherId as Long) is null or t.id = :teacherId) and
                  (cast(:subjectId as Integer) is null or su.id = :subjectId) and
                  (cast(:date as date) is null or s.date = :date) and
                  (cast(:audience as string) is null or s.audience = :audience) and
                  (cast(:groupId as Integer) is null or g.id = :groupId)
            """, countQuery = """
            select count(distinct s)
            from ScheduleEntity s
            left join s.teacher t
            left join s.subject su
            left join s.groups g
            where (cast(:teacherId as Long) is null or t.id = :teacherId) and
                  (cast(:subjectId as Integer) is null or su.id = :subjectId) and
                  (cast(:date as date) is null or s.date = :date) and
                  (cast(:audience as string) is null or s.audience = :audience) and
                  (cast(:groupId as Integer) is null or g.id = :groupId)
            """)
    Page<ScheduleEntity> findSchedulesByFilter(
            Pageable pageable,
            @Param("teacherId") Long teacherId,
            @Param("subjectId") Integer subjectId,
            @Param("date") LocalDate date,
            @Param("audience") String audience,
            @Param("groupId") Integer groupId
    );

    @Query("""
            select case when count(s) > 0 then true else false end
            from ScheduleEntity s
            where s.audience = :audience
              and s.date = :date
              and s.timeFrom < :timeTo
              and s.timeTo > :timeFrom
              and (cast(:excludeId as Long) is null or s.id != :excludeId)
            """)
    boolean hasAudienceCollision(
            @Param("audience") String audience,
            @Param("date") LocalDate date,
            @Param("timeFrom") LocalTime timeFrom,
            @Param("timeTo") LocalTime timeTo,
            @Param("excludeId") Long excludeId
    );
}