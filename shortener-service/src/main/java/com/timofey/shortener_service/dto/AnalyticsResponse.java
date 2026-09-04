package com.timofey.shortener_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class AnalyticsResponse {
    private String shortCode;

    private int totalClicks;

    private int todayClicks;
}
