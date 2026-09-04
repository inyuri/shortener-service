package com.timofey.analytics_service.repository;

import com.timofey.analytics_service.entity.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {
    int countByShortCode(String shortCode);

    @Query("""
        SELECT COUNT(c)
        FROM ClickEvent c
        WHERE c.shortCode = :shortCode
        AND c.clickedAt >= :startOfDay
    """)
    int countTodayClicks(
            @Param("shortCode") String shortCode,
            @Param("startOfDay") LocalDateTime startOfDay
    );
}
