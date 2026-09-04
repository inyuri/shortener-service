package com.timofey.shortener_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CreateLinkRequest {

    @NotBlank
    @URL
    private String originalUrl;

    private LocalDateTime expiresAt;

}
