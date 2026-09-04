package com.timofey.shortener_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LinkResponse {

    private String shortCode;

    private String originalUrl;

    private int clicks;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

}
