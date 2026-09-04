package com.timofey.analytics_service.service;

import com.timofey.analytics_service.dto.AnalyticsResponse;
import com.timofey.analytics_service.entity.ClickEvent;
import com.timofey.analytics_service.event.LinkClickedEvent;
import com.timofey.analytics_service.repository.ClickEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ClickEventRepository clickEventRepository;

    @Value("${kafka.topics.link-clicks-dlt}")
    private String dltTopic;

    public void process(LinkClickedEvent event) {
        ClickEvent clickEvent = new ClickEvent(
                event.shortCode(),
                event.originalUrl(),
                event.clickedAt(),
                event.userAgent(),
                event.correlationUUID()
        );

        clickEventRepository.save(clickEvent);
    }

    public AnalyticsResponse getAnalytics(String shortCode) {
        LocalDateTime startOfDay = LocalDateTime.now()
                .toLocalDate()
                .atStartOfDay();


        int totalClicks = clickEventRepository
                .countByShortCode(shortCode);


        int todayClicks = clickEventRepository
                .countTodayClicks(
                        shortCode,
                        startOfDay
                );

        return new AnalyticsResponse(
                shortCode,
                totalClicks,
                todayClicks
        );
    }
}
