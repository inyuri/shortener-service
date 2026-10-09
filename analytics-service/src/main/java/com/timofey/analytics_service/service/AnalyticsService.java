package com.timofey.analytics_service.service;

import com.timofey.analytics_service.dto.AnalyticsResponse;
import com.timofey.analytics_service.entity.ClickEvent;
import com.timofey.analytics_service.event.LinkClickedEvent;
import com.timofey.analytics_service.repository.ClickEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final ClickEventRepository clickEventRepository;

    @Value("${kafka.topics.link-clicks-dlt}")
    private String dltTopic;

    public void process(LinkClickedEvent event) {
        String correlationId = event.correlationId();

        if (correlationId == null || correlationId.isBlank()) {
            MDC.put("correlationId", correlationId);
        }

        try {
            ClickEvent clickEvent = new ClickEvent(
                    event.shortCode(),
                    event.originalUrl(),
                    event.clickedAt(),
                    event.userAgent(),
                    event.correlationId()
            );

            clickEventRepository.save(clickEvent);

            log.info("Click event saved for shortCode={}", event.shortCode());
        }
        finally {
            MDC.clear();
        }
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
