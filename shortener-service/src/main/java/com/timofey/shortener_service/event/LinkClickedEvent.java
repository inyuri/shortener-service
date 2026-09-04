package com.timofey.shortener_service.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record LinkClickedEvent(
        String shortCode,
        String originalUrl,
        LocalDateTime clickedAt,
        String userAgent,
        UUID correlationUUID
) {
}