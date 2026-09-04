package com.timofey.analytics_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnalyticsResponse {
    private String shortCode;

    private int totalClicks;

    private int todayClicks;
}
