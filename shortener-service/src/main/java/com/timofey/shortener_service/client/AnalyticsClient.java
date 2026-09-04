package com.timofey.shortener_service.client;

import com.timofey.shortener_service.dto.AnalyticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class AnalyticsClient {

    private final WebClient analyticsWebClient;

    public AnalyticsResponse getAnalytics(String shortCode) {
        return analyticsWebClient
                .get()
                .uri("/api/analytics/{shortCode}", shortCode)
                .retrieve()
                .bodyToMono(AnalyticsResponse.class)
                .onErrorReturn(
                        new AnalyticsResponse(
                                shortCode,
                                0,
                                0
                        )
                )
                .block();
    }
}