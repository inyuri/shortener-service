package com.timofey.analytics_service.event;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.LocalDateTime;
import java.util.UUID;

public record LinkClickedEvent(
        String shortCode,
        String originalUrl,
        LocalDateTime clickedAt,
        String userAgent,
        @JsonAlias("correlationUUID") String correlationId,
        UUID eventId
) {
}
