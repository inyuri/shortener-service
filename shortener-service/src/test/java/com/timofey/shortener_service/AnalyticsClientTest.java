package com.timofey.shortener_service;

import com.timofey.shortener_service.client.AnalyticsClient;
import com.timofey.shortener_service.dto.AnalyticsResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsClientTest {

    @Mock
    private WebClient analyticsWebClient;

    @Mock
    @SuppressWarnings("rawtypes")
    private WebClient.RequestHeadersUriSpec requestSpec;

    @Mock
    @SuppressWarnings("rawtypes")
    private WebClient.RequestHeadersSpec headersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private AnalyticsClient analyticsClient;

    @Test
    void getAnalytics_whenAnalyticsServiceUnavailable_returnsDefaultResponse() {

        when(analyticsWebClient.get())
                .thenReturn(requestSpec);

        when(requestSpec.uri(
                "/api/analytics/{shortCode}",
                "abc123"
        )).thenReturn(headersSpec);

        when(headersSpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.bodyToMono(AnalyticsResponse.class))
                .thenReturn(
                        Mono.error(
                                new RuntimeException("analytics-service unavailable")
                        )
                );

        AnalyticsResponse response =
                analyticsClient.getAnalytics("abc123");

        assertEquals("abc123", response.getShortCode());
        assertEquals(0, response.getTotalClicks());
        assertEquals(0, response.getTodayClicks());
    }
}