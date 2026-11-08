package com.timofey.analytics_service;

import com.timofey.analytics_service.consumer.ClickEventConsumer;
import com.timofey.analytics_service.event.LinkClickedEvent;
import com.timofey.analytics_service.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ClickEventConsumerTest {

    private final AnalyticsService analyticsService = mock(AnalyticsService.class);
    private final ClickEventConsumer consumer = new ClickEventConsumer(analyticsService, JsonMapper.builder().build());

    @ParameterizedTest
    @ValueSource(strings = {"correlationUUID", "correlationId"})
    void consumesClickWithCorrelationId(String correlationField) {
        UUID eventId = UUID.randomUUID();
        byte[] payload = """
                {"eventId":"%s","shortCode":"abc12345","originalUrl":"https://example.org",
                 "clickedAt":"2026-11-08T10:30:00","userAgent":"test-agent","%s":"trace-123"}
                """.formatted(eventId, correlationField).getBytes(StandardCharsets.UTF_8);

        consumer.consume(payload);

        verify(analyticsService).process(new LinkClickedEvent(
                "abc12345", "https://example.org", LocalDateTime.of(2026, 11, 8, 10, 30),
                "test-agent", "trace-123", eventId
        ));
    }

    @Test
    void rejectsMalformedJsonBeforeProcessing() {
        byte[] payload = "not JSON".getBytes(StandardCharsets.UTF_8);

        assertThrows(JacksonException.class, () -> consumer.consume(payload));

        verifyNoInteractions(analyticsService);
    }
}
