package com.timofey.analytics_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "click_events")
@Getter
@Setter
@NoArgsConstructor
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_code", nullable = false)
    private String shortCode;

    @Column(name = "original_url", nullable = false)
    private String originalUrl;

    @Column(name = "clicked_at", nullable = false)
    private LocalDateTime clickedAt;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "correlation_uuid", nullable = false)
    private UUID correlationUUID;

    public ClickEvent(
            String shortCode,
            String originalUrl,
            LocalDateTime clickedAt,
            String userAgent,
            UUID correlationUUID
    ) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.clickedAt = clickedAt;
        this.userAgent = userAgent;
        this.correlationUUID = correlationUUID;
    }
}