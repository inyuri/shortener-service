package com.timofey.analytics_service.controller;

import com.timofey.analytics_service.consumer.DlqConsumer;
import com.timofey.analytics_service.dto.AnalyticsResponse;
import com.timofey.analytics_service.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    private final DlqConsumer dlqConsumer;

    @GetMapping("/{shortCode}")
    public ResponseEntity<AnalyticsResponse> getAnalytics(@PathVariable String shortCode) {
        AnalyticsResponse response = analyticsService.getAnalytics(shortCode);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/failed")
    public ResponseEntity<Map<String, Object>> getFailedEvents() {
        Map<String, Object> response = dlqConsumer.getFailedEvents();

        return ResponseEntity.ok(response);
    }
}
